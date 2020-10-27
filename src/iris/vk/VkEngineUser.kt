package iris.vk

import iris.json.JsonArray
import iris.json.JsonEntry
import iris.json.JsonItem
import iris.json.JsonObject
import iris.json.flow.JsonFlowParser
import iris.json.plain.IrisJsonArray
import iris.json.plain.IrisJsonObject
import iris.json.proxy.JsonProxyString
import iris.json.proxy.JsonProxyValue
import iris.vk.VkApi.LongPollSettings
import java.util.*
import java.util.logging.Logger
import kotlin.collections.ArrayList

/**
 * @created 08.09.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

// TODO: Проверить
open class VkEngineUser(val vkApi: VkApi, val eventHandler: VkHandler) {

	constructor(token: String, eventHandler: VkHandler) : this(VkApi(token), eventHandler)

	var workStatus = true
	companion object {
		val logger = Logger.getLogger("iris.vk")!!
	}

	fun run() {

		var longPoll = this.getLongPollServer()
		if (longPoll == null) {
			logger.warning("FAIL AUTH")
			return
		}
		if (longPoll["response"].isNull()) {
			logger.warning("No start response!")
			return
		}

		logger.fine("Server received. Starting listening")

		var lastTs = longPoll["response"]["ts"].asString()
		val accessMode = (2 + 8).toString()
		var longPollSettings = getLongPollSettings(longPoll["response"]["server"].asString(), longPoll["response"]["key"].asString(), accessMode)
		loop@ while (this.workStatus)  {
			val updates = getUpdates(longPollSettings, lastTs)

			if (updates == null) {
				longPoll = getLongPollServer()

				if (longPoll == null) {
					logger.warning("FAIL AUTH")
					return
				}

				if (longPoll["response"].isNull()) {
					logger.warning("NO RESPONSE")
					return
				}
				val response = longPoll["response"]
				longPollSettings = getLongPollSettings(response["server"].asString(), response["key"].asString(), accessMode)
				lastTs = response["ts"].asString()
				continue
			}

			if (updates["updates"].isNull()) {
				if (!updates["failed"].isNull()) {
					when (updates["failed"].asInt()) {
						2 -> {
							// истёк срок ссылки
							logger.info("Long poll connection expired. Rebuilding")
							longPoll = getLongPollServer()

							if (longPoll == null) {
								logger.warning("FAIL AUTH")
								return
							}

							if (longPoll["response"].isNull()) {
								logger.warning("NO RESPONSE")
								return
							}

							val response = longPoll["response"]
							longPollSettings = getLongPollSettings(response["server"].asString(), response["key"].asString(), accessMode)
							lastTs = longPoll["response"]["ts"].asString()
							continue@loop
						} 1 -> { // обновляем TS
							lastTs = longPoll!!["response"]["ts"].asString()
							continue@loop
						} 3 -> { // обновляем TS
							logger.info { updates["error"].asString() + ". Try to rebuild" }
							longPoll = getLongPollServer()

							if (longPoll == null) {
								logger.warning("FAIL AUTH")
								return
							}

							if (longPoll["response"].isNull()) {
								logger.warning("NO RESPONSE")
								return
							}

							val response = longPoll["response"]
							longPollSettings = getLongPollSettings(response["server"].asString(), response["key"].asString(), accessMode)
							lastTs = response["ts"].asString()
							continue@loop
						} else -> {
							logger.warning("Как мы здесь оказались???")
							return
						}
					}

				} else if (!updates["error"].isNull()) {
					if (updates["error"]["error_msg"].asString() == "User authorization failed: access_token has expired."
						|| updates["error"]["error_code"].asInt() == VK_BOT_ERROR_WRONG_TOKEN
					) {
						logger.warning("Нет токена?")
						return
					} else {
						return
					}
				} else {
					logger.warning("YOU ARE HERE. SEEMS SOMETHING WRONG")
					return
				}
			}
			lastTs = updates["ts"].asString()
			processUpdates(updates["updates"] as JsonArray)
		}
	}

	protected open fun getLongPollSettings(server: String, key: String, accessMode: String): LongPollSettings {
		return LongPollSettings("https://$server", key, accessMode)
	}

	protected open fun getLongPollServer(): JsonItem? {
		return vkApi.messages.getLongPollServer()
	}

	protected open fun getUpdates(lpSettings: LongPollSettings, ts: String): JsonItem? {
		return vkApi.messages.getUpdates(lpSettings, ts)
	}

	fun stop() {
		this.workStatus = false
	}

	open fun processUpdates(updates: JsonArray) {
		val checkMessages = mutableListOf<VkMessage>()
		val checkInvites = mutableListOf<VkMessage>()
		val titleUpdaters = mutableListOf<VkMessage>()
		val pinUpdaters = mutableListOf<VkMessage>()
		val checkLeave = mutableListOf<VkMessage>()

		for (update in updates) {
			if (!update.isArray()) continue
			if (update[0].asLong() == 4L) { // это сообщение
				if (update[7]["source_act"].isNull()) {
					when (update[7]["source_act"].asStringOrNull()) {
						"chat_invite_user" -> checkInvites += VkMessage(
							IrisJsonObject(
								"user_id" to update[7]["source_mid"],
								"chat_id" to JsonProxyValue(VkApi.peer2ChatId(update[3].asInt())),
								"from_id" to update[7]["from"]
							))
						"chat_title_update" -> titleUpdaters += VkMessage(
							IrisJsonObject(
								"user_id" to update[7]["source_mid"],
								"chat_id" to JsonProxyValue(VkApi.peer2ChatId(update[3].asInt())),
								"from_id" to update[7]["from"]
							))
						"chat_invite_user_by_link" -> checkInvites += VkMessage(
							IrisJsonObject(
								"user_id" to update[7]["from"],
								"chat_id" to JsonProxyValue(VkApi.peer2ChatId(update[3].asInt())),
								"from_id" to update[7]["from"]
							))
						"chat_kick_user" -> checkLeave += VkMessage(
							IrisJsonObject(
									"user_id" to update[7]["source_mid"],
									"chat_id" to JsonProxyValue(VkApi.peer2ChatId(update[3].asInt())),
									"from_id" to update[7]["from"]
							))
						else -> checkMessages += VkMessage(update)
					}
				} else {
					checkMessages += VkMessage(update)
				}
			}
		}
		if (checkMessages.isNotEmpty())
			processMessages(this.convertMessages(checkMessages))
		if (checkInvites.isNotEmpty())
			this.processInvites(checkInvites)
		if (titleUpdaters.isNotEmpty())
			this.processTitleUpdates(titleUpdaters)
		if (pinUpdaters.isNotEmpty())
			this.processPinUpdates(pinUpdaters)
		if (checkLeave.isNotEmpty())
			this.processLeaves(checkLeave)
	}

	private inline fun <E>mutableListOf() = LinkedList<E>()

	fun processMessages(messages: List<VkMessage>) {
		this.eventHandler.processMessages(messages)
	}

	fun processEditMessages(messages: List<VkMessage>) {
		this.eventHandler.processEditedMessages(messages)
	}

	fun processInvites(invites: List<VkMessage>) {
		this.eventHandler.processInvites(invites)
	}

	fun processTitleUpdates(updaters: List<VkMessage>) {
		this.eventHandler.processTitleUpdates(updaters)
	}

	fun processPinUpdates(updaters: List<VkMessage>) {
		this.eventHandler.processPinUpdates(updaters)
	}

	fun processLeaves(users: List<VkMessage>) {
		this.eventHandler.processLeaves(users)
	}

	private fun convertMessages(messages: List<VkMessage>): List<VkMessage> {
		val ret = ArrayList<VkMessage>(messages.size)
		val messageIds = mutableListOf<Int>()
		var needExtend = false

		for (m in messages) {
			val m = m.source
			messageIds.add(m[1].asInt())
			if (m[7]["fwd"].isNotNull() || m[7]["attach1_type"].isNotNull() && m[7]["attach1_type"].asString() == "wall") {
				needExtend = true
				break
			}
		}

		if (needExtend) {
			val req = this.vkApi.messages.getById(messageIds)
			if (req != null) {
				val res = req["response"]["items"] as JsonArray
				for (r in res) {
					ret += VkMessage(r)
				}
			}
		} else {

			for (m in messages) {
				val m = m.source
				val peerIdObj = m[3]
				val peerId = peerIdObj.asInt()
				val chatId = VkApi.peer2ChatId(peerId)
				val fromId = if (chatId > 0) {
					JsonProxyValue(m[7]["from"].asString().toInt())
				} else {
					peerIdObj
				}

				val t = IrisJsonObject(mutableListOf(
						"id" to m[1],
						"chat_id" to JsonProxyValue(chatId),
						"peer_id" to peerIdObj,
						"from_id" to fromId,
						"text" to m[6],
						"date" to m[4]
				))

				if (m[7].isNotNull()) {

					val m7 = m[7] as JsonObject

					val attachmentsFull = m7["attachments"]
					val attachments = (if (attachmentsFull.isNull()) {
						val attachments = LinkedList<MutableMap<String, JsonItem>>()
						for (el in m7) {
							val key = el.first
							if (!key.startsWith("attach")) continue
							val addValue = el.second
							val data = key.split("_")
							val num = data[0].substring("attach".length).toInt() - 1
							if (num + 1 > attachments.size) {
								extendCapacity(attachments as MutableList<Any?>, num + 1)
								attachments[num] = mutableMapOf()
							}
							val addKey = if (data.size > 1) data[1] else "id"
							attachments[num][addKey] = addValue
						}
						IrisJsonArray(attachments.map { IrisJsonObject(it.toList()) })
					} else {
						JsonFlowParser.start(attachmentsFull.asString()) as JsonArray
					}).getList() as Collection<JsonObject>

					if (attachments.isNotEmpty()) {
						val resAttachments = ArrayList<JsonItem>(attachments.size)
						for (a in attachments) {
							val entries = (a.getEntries() as MutableCollection<JsonEntry>)
							var type: String? = null
							if (!entries.removeIf { if (it.first == "type") {type = it.second.asString(); true} else false }) continue
							if (type != null) {
								resAttachments.add(IrisJsonObject(listOf("type" to JsonProxyString(type), type!! to a)))
							}
						}
						if (resAttachments.size > 0)
							t["attachments"] = IrisJsonArray(resAttachments)
					}
				}
				ret += VkMessage(IrisJsonObject("message" to t))
			}
		}
		return ret
	}

	private fun extendCapacity(array: MutableList<Any?>, newCapacity: Int) {
		if (newCapacity > array.size) {
			for (i in array.size until newCapacity) {
				array.add(null)
			}
		}
	}
}


