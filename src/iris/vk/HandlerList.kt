package iris.vk

import iris.vk.event.*

/**
 * @created 08.02.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class VkHandlerList(val list: List<VkHandler>) : VkHandler {

	operator fun plusAssign(handler: VkHandler) {
		add(handler)
	}

	fun add(handler: VkHandler) {
		(list as MutableList<VkHandler>).add(handler)
	}

	override fun processMessages(messages: List<Message>) {
		for (l in list) {
			l.processMessages(messages)
		}
	}

	override fun processInvites(invites: List<ChatEvent>) {
		for (l in list)
			l.processInvites(invites)
	}

	override fun processTitleUpdates(updaters: List<TitleUpdate>) {
		for (l in list)
			l.processTitleUpdates(updaters)
	}

	override fun processPinUpdates(updaters: List<PinUpdate>) {
		for (l in list)
			l.processPinUpdates(updaters)
	}

	override fun processLeaves(leaves: List<ChatEvent>) {
		for (l in list)
			l.processLeaves(leaves)
	}

	override fun processEditedMessages(messages: List<Message>) {
		for (l in list)
			l.processEditedMessages(messages)
	}

	override fun processCallbacks(callbacks: List<CallbackEvent>) {
		for (l in list)
			l.processCallbacks(callbacks)
	}
}