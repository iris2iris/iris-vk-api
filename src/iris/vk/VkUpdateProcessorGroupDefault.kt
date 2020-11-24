package iris.vk

import iris.json.JsonItem
import iris.vk.event.*
import iris.vk.event.group.*
import java.util.*

class VkUpdateProcessorGroupDefault(private val handler: VkEventHandler) : VkUpdateProcessor {

	override fun processUpdates(updates: List<JsonItem>) {
		var checkMessages: MutableList<Message>? = null
		var checkEditMessages: MutableList<Message>? = null
		var checkInvites: MutableList<ChatEvent>? = null
		var checkLeave: MutableList<ChatEvent>? = null
		var titleUpdaters: MutableList<TitleUpdate>? = null
		var pinUpdaters: MutableList<PinUpdate>? = null
		var checkCallbacks: MutableList<CallbackEvent>? = null
		var screenshots: LinkedList<GroupChatEvent>? = null

		for (update in updates) {
			val sourcePeerId = update["group_id"].asInt()
			val type = update["type"].asString()
			if (type == "message_new") { // это сообщение
				val obj = update["object"]
				val message = obj["message"].let { if (it.isNull()) obj else it }
				if (message["action"]["type"].isNotNull()) {
					when(message["action"]["type"].asString()) {
						"chat_invite_user" -> {
							if (checkInvites == null) checkInvites = mutableListOf()
							checkInvites!! += GroupMessage(obj, sourcePeerId)
						}
						"chat_title_update" -> {
							if (titleUpdaters == null) titleUpdaters = mutableListOf()
							titleUpdaters!! += GroupTitleUpdate(obj, sourcePeerId)
						}
						"chat_invite_user_by_link" -> {
							if (checkInvites == null) checkInvites = mutableListOf()
							checkInvites!! += GroupChatEvent(obj, sourcePeerId)
						}
						"chat_kick_user" -> {
							if (checkLeave == null) checkLeave = mutableListOf()
							checkLeave!! += GroupChatEvent(obj, sourcePeerId)
						}
						"chat_pin_message" -> {
							if (pinUpdaters == null) pinUpdaters = mutableListOf()
							pinUpdaters!! += GroupPinUpdate(obj, sourcePeerId)
						}
						"chat_screenshot" -> {
							if (screenshots == null) screenshots = mutableListOf()
							screenshots!! += GroupChatEvent(obj, sourcePeerId)
						}
						else -> {
							if (checkMessages == null) checkMessages = mutableListOf()
							checkMessages!! += GroupMessage(obj, sourcePeerId)
						}
					}
				} else {
					if (checkMessages == null) checkMessages = mutableListOf()
					checkMessages!! += GroupMessage(obj, sourcePeerId)
				}
			} else if (type == "message_reply") {
				if (checkMessages == null) checkMessages = mutableListOf()
				checkMessages!! += GroupMessageWithoutChatInfo(update["object"], sourcePeerId)
			} else if (type == "message_edit") {
				if (checkEditMessages == null) checkEditMessages = mutableListOf()
				checkEditMessages!! += GroupMessageWithoutChatInfo(update["object"], sourcePeerId)
			} else if (type == "message_event") {
				if (checkCallbacks == null) checkCallbacks = mutableListOf()
				checkCallbacks!! += GroupCallbackEvent(update["object"], sourcePeerId)
			} else {
				VkPollingUser.logger.info("Unknown type of event: $type")
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
		if (checkLeave != null)
			handler.processLeaves(checkLeave)
		if (checkCallbacks != null)
			handler.processCallbacks(checkCallbacks)
		if (screenshots != null)
			handler.processScreenshots(screenshots)
	}

	private inline fun <E>mutableListOf() = LinkedList<E>()
}