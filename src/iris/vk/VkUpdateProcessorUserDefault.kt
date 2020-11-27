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

class VkUpdateProcessorUserDefault(private val eventHandler: VkEventHandler, private val eventProducer: VkEventProducerFactory) : VkUpdateProcessor {

	constructor(api: VkApi, eventHandler: VkEventHandler) : this(eventHandler, UserEventProducerDefault(api))

	private class UserEventProducerDefault(private val api: VkApi) : VkEventProducerFactory {
		override fun producer(): VkEventProducer = SubUserEventProducer(api)
	}

	private class SubUserEventProducer(api: VkApi) : VkEventProducer {

		private val apiSource = ApiSource(api)

		private class ApiSource(private val api: VkApi) : UserChatEvent.ApiSource {

			val messages = mutableListOf<JsonItem>()

			private val map: Map<Int, JsonItem> by lazy(LazyThreadSafetyMode.NONE) {
				val ids = messages.map { it[1].asInt() }
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

		override fun message(obj: JsonItem, sourcePeerId: Int): Message {
			apiSource += obj[1]
			return UserMessage(apiSource, obj, sourcePeerId)
		}

		override fun messageWithoutChatInfo(obj: JsonItem, sourcePeerId: Int): Message {
			apiSource += obj[1]
			return UserMessage(apiSource, obj, sourcePeerId)
		}

		override fun invite(obj: JsonItem, sourcePeerId: Int): ChatEvent {
			apiSource += obj
			return UserChatEvent(apiSource, obj, sourcePeerId)
		}

		override fun leave(obj: JsonItem, sourcePeerId: Int): ChatEvent {
			apiSource += obj
			return UserChatEvent(apiSource, obj, sourcePeerId)
		}

		override fun titleUpdate(obj: JsonItem, sourcePeerId: Int): TitleUpdate {
			apiSource += obj
			return UserTitleUpdate(apiSource, obj, sourcePeerId)
		}

		override fun pin(obj: JsonItem, sourcePeerId: Int): PinUpdate {
			apiSource += obj
			return UserPinUpdate(apiSource, obj, sourcePeerId)
		}

		override fun unpin(obj: JsonItem, sourcePeerId: Int): PinUpdate {
			apiSource += obj
			return UserPinUpdate(apiSource, obj, sourcePeerId)
		}

		override fun screenshot(obj: JsonItem, sourcePeerId: Int): ChatEvent {
			apiSource += obj
			return UserChatEvent(apiSource, obj, sourcePeerId)
		}

		override fun callback(obj: JsonItem, sourcePeerId: Int): CallbackEvent {
			throw IllegalArgumentException("???")
		}

		override fun otherEvent(obj: JsonItem, sourcePeerId: Int): OtherEvent {
			return OtherEvent(obj, sourcePeerId)
		}
	}

	override fun processUpdates(updates: List<JsonItem>) {
		val sourcePeerId = 0
		var messages: MutableList<Message>? = null
		//var editMessages: LinkedList<Message>? = null
		var invites: MutableList<ChatEvent>? = null
		var leaves: MutableList<ChatEvent>? = null
		var titleUpdaters: MutableList<TitleUpdate>? = null
		var pinUpdaters: MutableList<PinUpdate>? = null
		var unpinUpdaters: MutableList<PinUpdate>? = null
		var screenshots: MutableList<ChatEvent>? = null
		var others: MutableList<OtherEvent>? = null
		val producer = eventProducer.producer()
		for (update in updates) {
			if (!update.isArray()) continue
			update as JsonArray
			if (update[0].asLong() == 4L) { // это сообщение
				val sourceAct = update[7]["source_act"].asStringOrNull()
				if (sourceAct != null) {
					when (sourceAct) {
						"chat_invite_user" -> {
							if (invites == null) invites = mutableListOf()
							invites!! += producer.invite(update, sourcePeerId)
						}
						"chat_title_update" -> {
							if (titleUpdaters == null) titleUpdaters = mutableListOf()
							titleUpdaters!! += producer.titleUpdate(update, sourcePeerId)
						}
						"chat_invite_user_by_link" -> {
							if (invites == null) invites = mutableListOf()
							invites!! += producer.invite(update, sourcePeerId)
						}
						"chat_kick_user" -> {
							if (leaves == null) leaves = mutableListOf()
							leaves!! += producer.leave(update, sourcePeerId)
						}
						"chat_pin_message" -> {
							if (pinUpdaters == null) pinUpdaters = mutableListOf()
							pinUpdaters!! += producer.pin(update, sourcePeerId)
						}

						"chat_unpin_message" -> {
							if (unpinUpdaters == null) unpinUpdaters = mutableListOf()
							unpinUpdaters!! += producer.unpin(update, sourcePeerId)
						}

						"chat_screenshot" -> {
							if (screenshots == null) screenshots = mutableListOf()
							screenshots!! += producer.screenshot(update, sourcePeerId)
						}
						else -> {
							if (others == null) others = mutableListOf()
							others!! += producer.otherEvent(update, sourcePeerId)
						}
					}
				} else {
					if (messages == null) messages = mutableListOf()
					messages!! += producer.message(update, sourcePeerId)
				}
			}
		}
		if (messages != null)
			eventHandler.processMessages(messages)
		if (invites != null)
			this.eventHandler.processInvites(invites)
		if (titleUpdaters != null)
			eventHandler.processTitleUpdates(titleUpdaters)
		if (pinUpdaters != null)
			this.eventHandler.processPinUpdates(pinUpdaters)
		if (unpinUpdaters != null)
			eventHandler.processUnpinUpdates(unpinUpdaters)
		if (leaves != null)
			this.eventHandler.processLeaves(leaves)
		if (screenshots != null)
			eventHandler.processScreenshots(screenshots)
	}

	private inline fun <E>mutableListOf() = LinkedList<E>()

	private fun processEditMessages(messages: List<Message>) {
		this.eventHandler.processEditedMessages(messages)
	}
}