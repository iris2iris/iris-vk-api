package iris.vk

import iris.json.JsonArray
import iris.json.JsonItem
import iris.vk.api.VkApis
import iris.vk.api.simple.VkApi
import iris.vk.event.*
import iris.vk.event.user.UserChatEvent
import iris.vk.event.user.UserMessage
import iris.vk.event.user.UserPinUpdate
import iris.vk.event.user.UserTitleUpdate
import java.util.*

class VkUpdateProcessorUserDefault(private val api: VkApi, private val eventHandler: VkHandler) : VkUpdateProcessor {

	override fun processUpdates(updates: List<JsonItem>) {
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
			val result = api.messages.getById(ids)?: return@lazy emptyMap()
			if (VkApis.isError(result))
				return@lazy emptyMap()
			val items = result["response"]["items"] as JsonArray
			items.associateBy { it["id"].asInt() }
		}

		private fun getDirect(id: Int): JsonItem? {
			val result = api.messages.getById(listOf(id))?: return null
			if (VkApis.isError(result))
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