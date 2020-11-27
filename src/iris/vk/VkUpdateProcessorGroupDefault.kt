package iris.vk

import iris.json.JsonItem
import iris.vk.event.*
import iris.vk.event.group.*
import java.util.*

class VkUpdateProcessorGroupDefault(
		private val handler: VkEventHandler,
		private val eventProducerFactory: VkEventProducerFactory = GroupEventProducerDefault(),

) : VkUpdateProcessor {

	open class GroupEventProducerDefault : VkEventProducer, VkEventProducerFactory {

		override fun producer(): VkEventProducer {
			return this
		}

		override fun message(obj: JsonItem, sourcePeerId: Int): Message {
			return GroupMessage(obj, sourcePeerId)
		}

		override fun messageWithoutChatInfo(obj: JsonItem, sourcePeerId: Int): Message {
			return GroupMessageWithoutChatInfo(obj["object"], sourcePeerId)
		}

		override fun invite(obj: JsonItem, sourcePeerId: Int): ChatEvent {
			return GroupChatEvent(obj, sourcePeerId)
		}

		override fun leave(obj: JsonItem, sourcePeerId: Int): ChatEvent {
			return GroupChatEvent(obj, sourcePeerId)
		}

		override fun titleUpdate(obj: JsonItem, sourcePeerId: Int): TitleUpdate {
			return GroupTitleUpdate(obj, sourcePeerId)
		}

		override fun pin(obj: JsonItem, sourcePeerId: Int): PinUpdate {
			return GroupPinUpdate(obj, sourcePeerId)
		}

		override fun unpin(obj: JsonItem, sourcePeerId: Int): PinUpdate {
			return GroupPinUpdate(obj, sourcePeerId)
		}

		override fun screenshot(obj: JsonItem, sourcePeerId: Int): ChatEvent {
			return GroupChatEvent(obj, sourcePeerId)
		}

		override fun callback(obj: JsonItem, sourcePeerId: Int): CallbackEvent {
			return GroupCallbackEvent(obj, sourcePeerId)
		}

		override fun otherEvent(obj: JsonItem, sourcePeerId: Int): OtherEvent {
			return OtherEvent(obj, sourcePeerId)
		}
	}

	override fun processUpdates(updates: List<JsonItem>) {
		var checkMessages: MutableList<Message>? = null
		var checkEditMessages: MutableList<Message>? = null
		var checkInvites: MutableList<ChatEvent>? = null
		var checkLeave: MutableList<ChatEvent>? = null
		var titleUpdaters: MutableList<TitleUpdate>? = null
		var pinUpdaters: MutableList<PinUpdate>? = null
		var unpinUpdaters: MutableList<PinUpdate>? = null
		var checkCallbacks: MutableList<CallbackEvent>? = null
		var screenshots: LinkedList<ChatEvent>? = null
		var others: LinkedList<OtherEvent>? = null

		val producer = eventProducerFactory.producer()

		for (update in updates) {
			val sourcePeerId = update["group_id"].asInt()
			when (val type = update["type"].asString()) {
				"message_new" -> { // это сообщение
					val obj = update["object"]
					val message = obj["message"].let { if (it.isNull()) obj else it }
					if (message["action"]["type"].isNotNull()) {
						when(val subType = message["action"]["type"].asString()) {
							"chat_invite_user" -> {
								if (checkInvites == null) checkInvites = mutableListOf()
								checkInvites!! += producer.invite(obj, sourcePeerId)
							}
							"chat_title_update" -> {
								if (titleUpdaters == null) titleUpdaters = mutableListOf()
								titleUpdaters!! += producer.titleUpdate(obj, sourcePeerId)
							}
							"chat_invite_user_by_link" -> {
								if (checkInvites == null) checkInvites = mutableListOf()
								checkInvites!! += producer.invite(obj, sourcePeerId)
							}
							"chat_kick_user" -> {
								if (checkLeave == null) checkLeave = mutableListOf()
								checkLeave!! += producer.leave(obj, sourcePeerId)
							}
							"chat_pin_message" -> {
								if (pinUpdaters == null) pinUpdaters = mutableListOf()
								pinUpdaters!! += producer.pin(obj, sourcePeerId)
							}

							"chat_unpin_message" -> {
								if (unpinUpdaters == null) unpinUpdaters = mutableListOf()
								unpinUpdaters!! += producer.unpin(obj, sourcePeerId)
							}
							"chat_screenshot" -> {
								if (screenshots == null) screenshots = mutableListOf()
								screenshots!! += producer.screenshot(obj, sourcePeerId)
							}
							else -> {
								VkPollingUser.logger.info("Unknown message_new type : $subType")
								if (others == null) others = mutableListOf()
								others!! += producer.otherEvent(obj, sourcePeerId)
								/*if (checkMessages == null) checkMessages = mutableListOf()
								checkMessages!! += producer.message(obj, sourcePeerId)*/
							}
						}
					} else {
						if (checkMessages == null) checkMessages = mutableListOf()
						checkMessages!! += producer.message(obj, sourcePeerId)
					}
				}
				"message_reply" -> {
					if (checkMessages == null) checkMessages = mutableListOf()
					checkMessages!! += producer.messageWithoutChatInfo(update, sourcePeerId)
				}
				"message_edit" -> {
					if (checkEditMessages == null) checkEditMessages = mutableListOf()
					checkEditMessages!! += producer.messageWithoutChatInfo(update, sourcePeerId)
				}
				"message_event" -> {
					if (checkCallbacks == null) checkCallbacks = mutableListOf()
					checkCallbacks!! += producer.callback(update, sourcePeerId)
				}
				else -> {
					VkPollingUser.logger.info("Unknown type of event: $type")
					if (others == null) others = mutableListOf()
					others!! += producer.otherEvent(update["object"], sourcePeerId)
				}
			}
		}
		if (checkMessages != null)
			handler.processMessages(checkMessages)
		if (checkEditMessages != null)
			handler.processEditedMessages(checkEditMessages)
		if (checkInvites != null)
			handler.processInvites(checkInvites)
		if (titleUpdaters != null)
			handler.processTitleUpdates(titleUpdaters)
		if (pinUpdaters != null)
			handler.processPinUpdates(pinUpdaters)
		if (unpinUpdaters != null)
			handler.processUnpinUpdates(unpinUpdaters)
		if (checkLeave != null)
			handler.processLeaves(checkLeave)
		if (checkCallbacks != null)
			handler.processCallbacks(checkCallbacks)
		if (screenshots != null)
			handler.processScreenshots(screenshots)
		if (others != null)
			handler.processOthers(others)
	}

	private inline fun <E>mutableListOf() = LinkedList<E>()
}