package iris.vk.command

import iris.vk.event.Message

/**
 * @created 27.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class CommandMatcherRegex(private val commandPattern: Regex, private val runCommand: CommandRegex, private val charHash: CharArray? = null): CommandMatcherWithHash {

	constructor(commandPattern: String, runCommand: (message: Message, params: List<String>) -> Unit) : this(Regex(commandPattern), runCommand)

	constructor(commandPattern: Regex, runCommand: (message: Message, params: List<String>) -> Unit) : this(commandPattern, object : CommandRegex {

		override fun run(message: Message) {
			run(message, emptyList())
		}

		override fun run(message: Message, groupValues: List<String>) {
			runCommand(message,groupValues)
		}
	})

	override fun testAndExecute(command: String, message: Message): Boolean {
		val matcher = commandPattern.matchEntire(command)?: return false
		runCommand.run(message, matcher.groupValues)
		return false
	}

	override fun hashChars(): CharArray? {
		charHash?.run { return this }

		return commandPattern.pattern.firstOrNull()?.let {
			if (it.isLetterOrDigit())
				charArrayOf(it)
			else
				null
		}
	}

	interface CommandRegex : Command {
		fun run(message: Message, groupValues: List<String>)
	}
}