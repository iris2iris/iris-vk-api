@file:Suppress("NAME_SHADOWING", "unused")

package iris.vk

/**
 * @created 20.09.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class VkFilterHandler(private val filters: Array<VkEventFilter>, private val handler: VkHandler) : VkHandler {
	override fun processMessages(messages: List<VkMessage>) {
		var messages = messages
		for (i in filters) {
			messages = i.filterMessages(messages)
			if (messages.isEmpty())
				return
		}
		if (messages.isNotEmpty())
			handler.processMessages(messages)
	}

	override fun processInvites(invites: List<VkMessage>) {
		var invites = invites
		for (i in filters) {
			invites = i.filterInvites(invites)
			if (invites.isEmpty())
				return
		}
		if (invites.isNotEmpty())
			handler.processInvites(invites)
	}

	override fun processTitleUpdates(updaters: List<VkMessage>) {
		var updaters = updaters
		for (i in filters) {
			updaters = i.filterTitleUpdates(updaters)
			if (updaters.isEmpty())
				return
		}
		if (updaters.isNotEmpty())
			handler.processTitleUpdates(updaters)
	}

	override fun processPinUpdates(updaters: List<VkMessage>) {
		handler.processPinUpdates(updaters)
	}

	override fun processLeaves(leaves: List<VkMessage>) {
		var leaves = leaves
		for (i in filters) {
			leaves = i.filterLeaves(leaves)
			if (leaves.isEmpty())
				return
		}
		if (leaves.isNotEmpty())
			handler.processLeaves(leaves)
	}

	override fun processEditedMessages(messages: List<VkMessage>) {
		handler.processEditedMessages(messages)
	}

	override fun processCallbacks(callbacks: List<VkMessage>) {
		var updaters = callbacks
		for (i in filters) {
			updaters = i.filterCallbacks(updaters)
			if (updaters.isEmpty())
				return
		}
		if (updaters.isNotEmpty())
			handler.processCallbacks(updaters)
	}
}