package iris.vk

import iris.json.JsonArray
import iris.json.JsonItem
import iris.json.JsonObject
import iris.json.plain.IrisJsonParser
import iris.json.proxy.JsonProxyObject
import iris.vk.VkApi.LongPollSettings
import java.util.*
import java.util.logging.Logger

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

		var lastTs = longPoll["response"]["ts"].obj().toString()
		val accessMode = (2 + 8).toString()
		var longPollSettings = LongPollSettings("https://" + longPoll["response"]["server"].asString(), longPoll["response"]["key"].asString(), accessMode)
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
				longPollSettings = LongPollSettings(response["server"].asString(), response["key"].asString(), accessMode)
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
							longPollSettings = LongPollSettings(response["server"].asString(), response["key"].asString(), accessMode)
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
							longPollSettings = LongPollSettings(response["server"].asString(), response["key"].asString(), accessMode)
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
			lastTs = updates["ts"].obj().toString()
			processUpdates(updates["updates"] as JsonArray)
		}
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
							JsonProxyObject(
								"user_id" to update[7]["source_mid"].asInt(),
								"chat_id" to VkApi.peer2ChatId(update[3].asInt()),
								"from_id" to update[7]["from"].asInt()
							)
						)
						"chat_title_update" -> titleUpdaters += VkMessage(
							JsonProxyObject(
								"user_id" to update[7]["source_mid"].asInt(),
								"chat_id" to VkApi.peer2ChatId(update[3].asInt()),
								"from_id" to update[7]["from"].asInt()
							)
						)
						"chat_invite_user_by_link" -> checkInvites += VkMessage(
							JsonProxyObject(
								"user_id" to update[7]["from"].asInt(),
								"chat_id" to VkApi.peer2ChatId(update[3].asInt()),
								"from_id" to update[7]["from"].asInt()
							)
						)
						"chat_kick_user" -> checkLeave += VkMessage(
							JsonProxyObject(
								"user_id" to update[7]["source_mid"].asInt(),
								"chat_id" to VkApi.peer2ChatId(update[3].asInt()),
								"from_id" to update[7]["from"].asInt()
							)
						)
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
		val ret = mutableListOf<VkMessage>()
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
				val res = req["response"]["items"].asList()
				for (r in res) {
					val d = JsonProxyObject(r as Map<String, Any?>)
					ret += VkMessage(d)
				}
			}
		} else {

			for (m in messages) {
				val m = m.source
				val peerId = m[3].asInt()
				val chatId = VkApi.peer2ChatId(peerId)
				val fromId: Int
				if (chatId > 0) {
					fromId = m[7]["from"].asString().toInt()
				} else {
					fromId = peerId
				}
				val t = mutableMapOf<String, Any?>(
					"id" to m[1].asInt(),
					"chat_id" to chatId,
					"peer_id" to peerId,
					"from_id" to fromId,
					"text" to m[6].asString(),
					"command" to "",
					"isToBot" to false,
					"date" to m[4].asLong()
				)

				if (m[7].isNotNull()) {

					val m7 = m[7] as JsonObject

					val attachmentsFull = m7["attachments"]
					val attachments = if (attachmentsFull.isNull()) {
						val attachments = mutableListOf<MutableMap<String, Any?>?>()
						for (el in m7) {
							val key = el.first
							val addValue = el.second
							if (!key.startsWith("attach")) continue
							val data = key.split("_")
							val num = data[0].substring("attach".length).toInt() - 1
							if (num + 1 > attachments.size) {
								extendCapacity(attachments as MutableList<Any?>, num + 1)
								attachments[num] = mutableMapOf<String, Any?>()
							}
							val addKey = if (data.size > 1) data[1] else "id"
							attachments[num]!![addKey] = addValue.obj()
						}
						attachments
					} else {
						IrisJsonParser(attachmentsFull.asString()).parse().asList() as List<MutableMap<String, Any?>>
					}

					if (attachments.isNotEmpty()) {
						val resAttachments = ArrayList<Map<String, Any?>>(attachments.size)
						for (a in attachments) {
							if (a == null) continue
							val type: String? = a.remove("type") as String?
							if (type != null) {
								resAttachments.add(mapOf("type" to type, type to a))
							}
						}
						if (resAttachments.size > 0)
							t["attachments"] = resAttachments
					}
				}
				ret += VkMessage(JsonProxyObject("message" to t))
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


