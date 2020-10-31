package iris.vk

import iris.vk.event.*

/**
 * @created 26.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class VkEventHandlerAdapter: VkEventHandler {

	open fun processMessage(message: Message) {}
	open fun processEditedMessage(message: Message) {}

	override fun processMessages(messages: List<Message>) {
		for (message in messages)
			processMessage(message)
	}

	override fun processEditedMessages(messages: List<Message>) {
		for (message in messages)
			processEditedMessage(message)
	}

	override fun processInvites(invites: List<ChatEvent>) {}

	override fun processTitleUpdates(updaters: List<TitleUpdate>) {}

	override fun processPinUpdates(updaters: List<PinUpdate>) {}

	override fun processLeaves(leaves: List<ChatEvent>) {}

	override fun processCallbacks(callbacks: List<CallbackEvent>) {}
}