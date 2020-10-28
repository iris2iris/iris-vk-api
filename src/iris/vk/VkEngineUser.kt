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

open class VkEngineUser(protected val vkApi: VkApi, protected val eventHandler: VkHandler) {

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
		var checkMessages: LinkedList<UserMessage>? = null
		var checkInvites: LinkedList<UserChatEvent>? = null
		var checkLeave: LinkedList<UserChatEvent>? = null
		var titleUpdaters: LinkedList<UserTitleUpdate>? = null
		val pinUpdaters: LinkedList<UserPinUpdate>? = null
		var apiSource: ApiSource? = null

		for (update in updates) {
			if (!update.isArray()) continue
			update as JsonArray
			if (update[0].asLong() == 4L) { // это сообщение
				val sourceAct = update[7]["source_act"].asStringOrNull()
				if (apiSource == null) apiSource = ApiSource()
				apiSource += update[1]
				if (sourceAct != null) {
					when (sourceAct) {
						"chat_invite_user" -> {
							if (checkInvites == null) checkInvites = mutableListOf()
							checkInvites!! += UserChatEvent(apiSource, update)
						}
						"chat_title_update" -> {
							if (titleUpdaters == null) titleUpdaters = mutableListOf()
							titleUpdaters!! += UserTitleUpdate(apiSource, update)
						}
						"chat_invite_user_by_link" -> {
							if (checkInvites == null) checkInvites = mutableListOf()
							checkInvites!! += UserChatEvent(apiSource, update)
						}
						"chat_kick_user" -> {
							if (checkLeave == null) checkLeave = mutableListOf()
							checkLeave!! += UserChatEvent(apiSource, update)
						}
						else -> {
							if (checkMessages == null) checkMessages = mutableListOf()
							checkMessages!! += UserMessage(apiSource, update)
						}
					}
				} else {
					if (checkMessages == null) checkMessages = mutableListOf()
					checkMessages!! += UserMessage(apiSource, update)
				}
			}
		}
		if (checkMessages != null)
			processMessages(checkMessages)
		if (checkInvites != null)
			processInvites(checkInvites)
		if (titleUpdaters != null)
			processTitleUpdates(titleUpdaters)
		if (pinUpdaters != null)
			processPinUpdates(pinUpdaters)
		if (checkLeave != null)
			processLeaves(checkLeave)
	}

	private inner class ApiSource : UserChatEvent.ApiSource {

		val messages = mutableListOf<JsonItem>()

		private val map: Map<Int, JsonItem> by lazy(LazyThreadSafetyMode.NONE) {
			val ids = messages.map { it.asInt() }
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

		override fun getFullEvent(messageId: Int): JsonItem? {
			return map[messageId] ?: getDirect(messageId)
		}

		operator fun plusAssign(item: JsonItem) {
			messages += item
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
}


