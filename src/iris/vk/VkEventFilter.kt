@file:Suppress("unused")

package iris.vk

import iris.vk.event.CallbackEvent
import iris.vk.event.ChatEvent
import iris.vk.event.Message
import iris.vk.event.TitleUpdate

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

