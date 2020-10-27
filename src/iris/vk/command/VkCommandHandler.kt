package iris.vk.command

import iris.vk.VkHandlerAdapter
import iris.vk.VkMessage

/**
 * @created 27.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

open class VkCommandHandler(
		private val commandBuilder: CommandBuilder = DefaultCommandBuilder(null),
		private val searchFirst: Boolean = true
) : VkHandlerAdapter() {

	private val map = mutableMapOf<Char, MutableList<Command>>()

	operator fun plusAssign(command: Command) {
		add(command, null)
	}

	operator fun plusAssign(command: CommandWithHash) {
		add(command, command.hashChars())
	}

	operator fun plusAssign(command: Pair<String, Command>) {
		add(command.second, command.first.toCharArray())
	}

	open fun add(command: Command, chars: CharArray?): VkCommandHandler {
		if (chars == null)
			map.getOrPut(NULL_CHAR) { mutableListOf() }.add(command)
		else
			for (char in chars)
				map.getOrPut(char) { mutableListOf() }.add(command)
		return this
	}

	companion object {
		private const val NULL_CHAR = '\u0000'
	}

	override fun processMessage(message: VkMessage) {
		val command = commandBuilder.extractCommand(message)?: return
		if (command.isEmpty()) return
		val hash = command[0]
		var hashItems = map[hash]
		if (hashItems != null) {
			for (c in hashItems)
				if (c.testAndExecute(command, message))
					if (searchFirst)
						return
		}
		hashItems = map[NULL_CHAR]
		if (hashItems != null) {
			for (c in hashItems)
				if (c.testAndExecute(command, message))
					if (searchFirst)
						return
		}
	}
}