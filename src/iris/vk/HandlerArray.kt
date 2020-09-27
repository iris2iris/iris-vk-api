package iris.vk

/**
 * @created 22.03.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class VkHandlerArray(val list: Array<VkHandler>) : VkHandler {

	override fun processMessages(messages: List<VkMessage>) {
		for (l in list) {
			l.processMessages(messages)
		}
	}

	override fun processInvites(invites: List<VkMessage>) {
		for (l in list)
			l.processInvites(invites)
	}

	override fun processTitleUpdates(updaters: List<VkMessage>) {
		for (l in list)
			l.processTitleUpdates(updaters)
	}

	override fun processPinUpdates(updaters: List<VkMessage>) {
		for (l in list)
			l.processPinUpdates(updaters)
	}

	override fun processLeaves(leaves: List<VkMessage>) {
		for (l in list)
			l.processLeaves(leaves)
	}

	override fun processEditedMessages(messages: List<VkMessage>) {
		for (l in list)
			l.processEditedMessages(messages)
	}

	override fun processCallbacks(callbacks: List<VkMessage>) {
		for (l in list)
			l.processCallbacks(callbacks)
	}
}