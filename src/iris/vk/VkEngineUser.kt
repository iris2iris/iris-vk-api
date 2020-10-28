package iris.vk

import iris.json.JsonArray
import iris.json.JsonItem
import iris.vk.VkApi.LongPollSettings
import iris.vk.event.*
import iris.vk.event.user.UserChatEvent
import iris.vk.event.user.UserMessage
import iris.vk.event.user.UserPinUpdate
import iris.vk.event.user.UserTitleUpdate
import java.util.*
import java.util.logging.Logger

/**
 * @created 08.09.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

open class VkEngineUser(val vkApi: VkApi, private val eventHandler: VkHandler) {

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
					if (updates["error"]["error_code"].asInt() == VK_BOT_ERROR_WRONG_TOKEN
						|| updates["error"]["error_msg"].asString() == "User authorization failed: access_token has expired."
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
		var checkMessages: LinkedList<JsonItem>? = null
		var checkInvites: LinkedList<UserChatEvent>? = null
		var checkLeave: LinkedList<UserChatEvent>? = null
		var titleUpdaters: LinkedList<UserTitleUpdate>? = null
		val pinUpdaters: LinkedList<UserPinUpdate>? = null

		for (update in updates) {
			if (!update.isArray()) continue
			update as JsonArray
			if (update[0].asLong() == 4L) { // это сообщение
				if (update[7]["source_act"].isNull()) {
					when (update[7]["source_act"].asStringOrNull()) {
						"chat_invite_user" -> {
							if (checkInvites == null) checkInvites = mutableListOf()
							checkInvites!! += UserChatEvent(update)
						}
						"chat_title_update" -> {
							if (titleUpdaters == null) titleUpdaters = mutableListOf()
							titleUpdaters!! += UserTitleUpdate(update)
						}
						"chat_invite_user_by_link" -> {
							if (checkInvites == null) checkInvites = mutableListOf()
							checkInvites!! += UserChatEvent(update)
						}
						"chat_kick_user" -> {
							if (checkLeave == null) checkLeave = mutableListOf()
							checkLeave!! += UserChatEvent(update)
						}
						else -> {
							if (checkMessages == null) checkMessages = mutableListOf()
							checkMessages.add(update)
						}
					}
				} else {
					if (checkMessages == null) checkMessages = mutableListOf()
					checkMessages.add(update)
				}
			}
		}
		if (checkMessages != null) {
			val source = ApiSource(checkMessages)
			processMessages(checkMessages.map { UserMessage(source, it) })
		}
		if (checkInvites != null)
			this.processInvites(checkInvites)
		if (titleUpdaters != null)
			this.processTitleUpdates(titleUpdaters)
		if (pinUpdaters != null)
			this.processPinUpdates(pinUpdaters)
		if (checkLeave != null)
			this.processLeaves(checkLeave)
	}

	private inner class ApiSource(messages: List<JsonItem>) : UserMessage.ApiSource {

		private val map: Map<Int, JsonItem> by lazy(LazyThreadSafetyMode.NONE) {
			val ids = messages.map { it[1].asInt() }
			val result = vkApi.messages.getById(ids)?: return@lazy emptyMap()
			if (VkApi.isError(result))
				return@lazy emptyMap()
			val items = result["response"]["items"] as JsonArray
			items.associateBy { it["id"].asInt() }
		}

		private fun getDirect(id: Int): JsonItem? {
			val result = vkApi.messages.getById(listOf(id))?: return null
			if (VkApi.isError(result))
				return null
			val items = result["response"]["items"] as JsonArray
			return items.firstOrNull()
		}

		override fun getFullMessage(messageId: Int): JsonItem? {
			return map[messageId] ?: getDirect(messageId)
		}
	}

	private inline fun <E>mutableListOf() = LinkedList<E>()

	fun processMessages(messages: List<Message>) {
		this.eventHandler.processMessages(messages)
	}

	fun processEditMessages(messages: List<Message>) {
		this.eventHandler.processEditedMessages(messages)
	}

	fun processInvites(invites: List<ChatEvent>) {
		this.eventHandler.processInvites(invites)
	}

	fun processTitleUpdates(updaters: List<TitleUpdate>) {
		this.eventHandler.processTitleUpdates(updaters)
	}

	fun processPinUpdates(updaters: List<PinUpdate>) {
		this.eventHandler.processPinUpdates(updaters)
	}

	fun processLeaves(users: List<ChatEvent>) {
		this.eventHandler.processLeaves(users)
	}

	fun processCallbacks(callbacks: List<CallbackEvent>) {
		this.eventHandler.processCallbacks(callbacks)
	}

	/*private fun convertMessages(messages: List<JsonArray>): List<UserMessage> {
		val ret = ArrayList<UserMessage>(messages.size)
		val messageIds = mutableListOf<Int>()
		var needExtend = false

		for (m in messages) {
			messageIds.add(m[1].asInt())
			if (m[7]["fwd"].isNotNull() || m[7]["attach1_type"].asStringOrNull() == "wall") {
				needExtend = true
				break
			}
		}

		if (needExtend) {
			val req = this.vkApi.messages.getById(messageIds)
			if (req != null) {
				val res = req["response"]["items"] as JsonArray
				for (r in res) {
					ret += UserMessage(IrisJsonObject("message" to r))
				}
			}
		} else {

			for (m in messages) {
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
				ret += UserMessage(IrisJsonObject("message" to t))
			}
		}
		return ret
	}*/

	/*private fun extendCapacity(array: MutableList<Any?>, newCapacity: Int) {
		if (newCapacity > array.size) {
			for (i in array.size until newCapacity) {
				array.add(null)
			}
		}
	}*/
}


