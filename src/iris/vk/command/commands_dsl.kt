package iris.vk.command

import iris.vk.event.Message

/**
 * @created 31.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
fun commands(initializer: DslCommandBuilder.() -> Unit): List<CommandMatcherWithHash> {
	return DslCommandBuilder().apply(initializer).build()
}

open class DslCommandBuilder {

	private val commands = mutableListOf<CommandMatcherWithHash>()

	infix fun String.runs(command: (message: Message) -> Unit) {
		commands += CommandMatcherSimple(this, command)
	}

	infix fun String.runs(command: Command) {
		commands += CommandMatcherSimple(this, command)
	}

	infix fun Collection<String>.runs(command: Command) {
		this.forEach {
			this@DslCommandBuilder.commands += CommandMatcherSimple(it, command)
		}
	}

	fun list(vararg commands: String): List<String> {
		return listOf(*commands)
	}

	infix fun Collection<String>.runs(command: (message: Message) -> Unit) {
		this.forEach {
			this@DslCommandBuilder.commands += CommandMatcherSimple(it, command)
		}
	}

	infix fun RegexBuilder.runs(command: (message: Message, params: List<String>) -> Unit) {
		commands += CommandMatcherRegex(Regex(this.pattern), command)
	}

	infix fun RegexBuilder.runs(command: CommandMatcherRegex.CommandRegex) {
		commands += CommandMatcherRegex(Regex(this.pattern), command, this.hashChars)
	}

	fun regex(template: String, hashChars: String? = null)  = regex(template, hashChars?.toCharArray())

	fun regex(template: String, hashChars: CharArray?): RegexBuilder {
		return RegexBuilder(template, hashChars)
	}

	class RegexBuilder(val pattern: String, val hashChars: CharArray?)

	fun build(): List<CommandMatcherWithHash> {
		return commands
	}
}