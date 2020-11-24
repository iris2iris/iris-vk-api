package iris.vk

import iris.vk.event.CallbackEvent
import iris.vk.event.ChatEvent
import iris.vk.event.Message
import iris.vk.event.TitleUpdate

open class VkEventFilterAdapter : VkEventFilter {
	override fun filterInvites(invites: List<ChatEvent>): List<ChatEvent> {
		return invites
	}

	override fun filterLeaves(leaves: List<ChatEvent>): List<ChatEvent> {
		return leaves
	}

	override fun filterMessages(messages: List<Message>): List<Message> {
		return messages
	}

	override fun filterTitleUpdates(updaters: List<TitleUpdate>): List<TitleUpdate> {
		return updaters
	}

	override fun filterCallbacks(callbacks: List<CallbackEvent>): List<CallbackEvent> {
		return callbacks
	}

	override fun filterScreenshots(screenshots: List<ChatEvent>): List<ChatEvent> {
		return screenshots
	}
}