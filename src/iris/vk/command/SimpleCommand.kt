package iris.vk.command

import iris.vk.VkMessage

/**
 * @created 27.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class SimpleCommand(private val commandTemplate: String, private val runCommand: (message: VkMessage) -> Unit) : CommandWithHash {
	override fun testAndExecute(command: String, message: VkMessage): Boolean {
		if (commandTemplate != command) return false
		runCommand(message)
		return true
	}

	override fun hashChars() = commandTemplate.firstOrNull()?.let { charArrayOf(it) }
}