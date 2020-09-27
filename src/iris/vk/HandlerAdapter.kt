package iris.vk

/**
 * @created 26.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class VkHandlerAdapter: VkHandler {

	open fun processMessage(message: VkMessage) {}
	open fun processEditedMessage(message: VkMessage) {}

	override fun processMessages(messages: List<VkMessage>) {
		for (message in messages)
			processMessage(message)
	}

	override fun processEditedMessages(messages: List<VkMessage>) {
		for (message in messages)
			processEditedMessage(message)
	}

	override fun processInvites(invites: List<VkMessage>) {}

	override fun processTitleUpdates(updaters: List<VkMessage>) {}

	override fun processPinUpdates(updaters: List<VkMessage>) {}

	override fun processLeaves(leaves: List<VkMessage>) {}

	override fun processCallbacks(callbacks: List<VkMessage>) {}
}