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
	fun filterPinUpdates(updaters: List<PinUpdate>): List<PinUpdate>
	fun filterUnpinUpdates(updaters: List<PinUpdate>): List<PinUpdate>
	fun filterCallbacks(callbacks: List<CallbackEvent>): List<CallbackEvent>
	fun filterScreenshots(screenshots: List<ChatEvent>): List<ChatEvent>
	fun filterOthers(others: List<OtherEvent>): List<OtherEvent>
}

