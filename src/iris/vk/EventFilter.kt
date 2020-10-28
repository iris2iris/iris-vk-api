@file:Suppress("unused")

package iris.vk

import iris.vk.event.*

/**
 * @created 20.09.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface VkEventFilter {
	fun filterInvites(invites: List<ChatEvent>): List<ChatEvent>
	fun filterLeaves(leaves: List<ChatEvent>): List<ChatEvent>
	fun filterMessages(messages: List<Message>): List<Message>
	fun filterTitleUpdates(updaters: List<TitleUpdate>): List<TitleUpdate>
	fun filterCallbacks(callbacks: List<CallbackEvent>): List<CallbackEvent>
}

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
}