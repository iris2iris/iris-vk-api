@file:Suppress("unused")

package iris.vk

/**
 * @created 20.09.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface VkEventFilter {
	fun filterInvites(invites: List<VkMessage>): List<VkMessage>
	fun filterLeaves(leaves: List<VkMessage>): List<VkMessage>
	fun filterMessages(messages: List<VkMessage>): List<VkMessage>
	fun filterTitleUpdates(updaters: List<VkMessage>): List<VkMessage>
	fun filterCallbacks(callbacks: List<VkMessage>): List<VkMessage>
}

open class VkEventFilterAdapter : VkEventFilter {
	override fun filterInvites(invites: List<VkMessage>): List<VkMessage> {
		return invites
	}

	override fun filterMessages(messages: List<VkMessage>): List<VkMessage> {
		return messages
	}

	override fun filterLeaves(leaves: List<VkMessage>): List<VkMessage> {
		return leaves
	}

	override fun filterTitleUpdates(updaters: List<VkMessage>): List<VkMessage> {
		return updaters
	}

	override fun filterCallbacks(callbacks: List<VkMessage>): List<VkMessage> {
		return callbacks
	}
}