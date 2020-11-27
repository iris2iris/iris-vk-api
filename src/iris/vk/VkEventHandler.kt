package iris.vk

import iris.vk.event.*
import java.util.*

/**
 * @created 08.09.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface VkEventHandler {
	fun processMessages(messages: List<Message>)
	fun processEditedMessages(messages: List<Message>)
	fun processInvites(invites:List<ChatEvent>)
	fun processLeaves(leaves:List<ChatEvent>)
	fun processTitleUpdates(updaters:List<TitleUpdate>)
	fun processPinUpdates(updaters:List<PinUpdate>)
	fun processUnpinUpdates(updates: List<PinUpdate>)
	fun processCallbacks(callbacks: List<CallbackEvent>)
	fun processScreenshots(screenshots: List<ChatEvent>)
	fun processOthers(others: List<OtherEvent>)
}