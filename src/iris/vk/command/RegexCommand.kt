package iris.vk.command

import iris.vk.event.Message

/**
 * @created 27.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class RegexCommand(private val commandTemplate: Regex, private val runCommand: (message: Message, params: List<String>) -> Unit): Command {

	override fun testAndExecute(command: String, message: Message): Boolean {
		val matcher = commandTemplate.matchEntire(command)?: return false
		runCommand(message, matcher.groupValues)
		return false
	}
}