@file:Suppress("NAME_SHADOWING", "unused")

package iris.vk

import iris.vk.event.*

/**
 * @created 20.09.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class VkEventFilterHandler(private val filters: Array<VkEventFilter>, private val handler: VkEventHandler) : VkEventHandler {

	override fun processMessages(messages: List<Message>) {
		var messages = messages
		for (i in filters) {
			messages = i.filterMessages(messages)
			if (messages.isEmpty())
				return
		}
		if (messages.isNotEmpty())
			handler.processMessages(messages)
	}

	override fun processInvites(invites: List<ChatEvent>) {
		var invites = invites
		for (i in filters) {
			invites = i.filterInvites(invites)
			if (invites.isEmpty())
				return
		}
		if (invites.isNotEmpty())
			handler.processInvites(invites)
	}

	override fun processTitleUpdates(updaters: List<TitleUpdate>) {
		var updaters = updaters
		for (i in filters) {
			updaters = i.filterTitleUpdates(updaters)
			if (updaters.isEmpty())
				return
		}
		if (updaters.isNotEmpty())
			handler.processTitleUpdates(updaters)
	}

	override fun processPinUpdates(updaters: List<PinUpdate>) {
		var updaters = updaters
		for (i in filters) {
			updaters = i.filterPinUpdates(updaters)
			if (updaters.isEmpty())
				return
		}
		handler.processPinUpdates(updaters)
	}

	override fun processUnpinUpdates(updates: List<PinUpdate>) {
		var updaters = updates
		for (i in filters) {
			updaters = i.filterUnpinUpdates(updaters)
			if (updaters.isEmpty())
				return
		}
		handler.processUnpinUpdates(updaters)
	}

	override fun processLeaves(leaves: List<ChatEvent>) {
		var leaves = leaves
		for (i in filters) {
			leaves = i.filterLeaves(leaves)
			if (leaves.isEmpty())
				return
		}
		if (leaves.isNotEmpty())
			handler.processLeaves(leaves)
	}

	override fun processEditedMessages(messages: List<Message>) {
		handler.processEditedMessages(messages)
	}

	override fun processCallbacks(callbacks: List<CallbackEvent>) {
		var updaters = callbacks
		for (i in filters) {
			updaters = i.filterCallbacks(updaters)
			if (updaters.isEmpty())
				return
		}
		if (updaters.isNotEmpty())
			handler.processCallbacks(updaters)
	}

	override fun processScreenshots(screenshots: List<ChatEvent>) {
		var updaters = screenshots
		for (i in filters) {
			updaters = i.filterScreenshots(updaters)
			if (updaters.isEmpty())
				return
		}
		if (updaters.isNotEmpty())
			handler.processScreenshots(updaters)
	}

	override fun processOthers(others: List<OtherEvent>) {
		var items = others
		for (i in filters) {
			items = i.filterOthers(items)
			if (items.isEmpty())
				return
		}
		if (items.isNotEmpty())
			handler.processOthers(items)
	}
}