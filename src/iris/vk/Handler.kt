package iris.vk

/**
 * @created 08.09.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface VkHandler {
	fun processMessages(messages: List<VkMessage>)
	fun processEditedMessages(messages: List<VkMessage>)
	fun processInvites(invites:List<VkMessage>)
	fun processTitleUpdates(updaters:List<VkMessage>)
	fun processPinUpdates(updaters:List<VkMessage>)
	fun processLeaves(leaves:List<VkMessage>)
	fun processCallbacks(callbacks: List<VkMessage>)
}