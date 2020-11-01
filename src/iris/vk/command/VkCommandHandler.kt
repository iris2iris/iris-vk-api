package iris.vk.command

import iris.vk.VkEventHandlerAdapter
import iris.vk.VkEventHandlerTrigger
import iris.vk.event.Message

/**
 * @created 27.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

open class VkCommandHandler(
	private val commandBuilder: CommandExtractor = CommandExtractorDefault(null),
	private val searchFirst: Boolean = true
) : VkEventHandlerAdapter(), VkEventHandlerTrigger.TriggerMessage {

	constructor(commandBuilder: CommandExtractor = CommandExtractorDefault(null),
				searchFirst: Boolean = true, commands: Iterable<CommandMatcherWithHash>) : this(commandBuilder, searchFirst) {
		addAllWithHash(commands)
	}

	private val map = mutableMapOf<Char, MutableList<CommandMatcher>>()

	operator fun plusAssign(command: CommandMatcher) {
		add(command, null)
	}

	operator fun plusAssign(command: CommandMatcherWithHash) {
		add(command, command.hashChars())
	}

	operator fun plusAssign(command: Pair<String, CommandMatcher>) {
		add(command.second, command.first.toCharArray())
	}

	open fun add(command: CommandMatcher): VkCommandHandler {
		return add(command, null)
	}

	open fun add(command: CommandMatcherWithHash): VkCommandHandler {
		return add(command, command.hashChars())
	}

	open fun add(command: CommandMatcher, chars: CharArray?): VkCommandHandler {
		if (chars == null)
			map.getOrPut(NULL_CHAR) { mutableListOf() }.add(command)
		else
			for (char in chars)
				map.getOrPut(char) { mutableListOf() }.add(command)
		return this
	}

	fun addAll(commands: Iterable<Pair<CommandMatcher, CharArray?>>): VkCommandHandler {
		for (it in commands) add(it.first, it.second)
		return this
	}

	fun addAllWithHash(commands: Iterable<CommandMatcherWithHash>): VkCommandHandler {
		for (it in commands) add(it, it.hashChars())
		return this
	}

	operator fun plusAssign(commands: Iterable<Pair<CommandMatcher, CharArray?>>) {
		addAll(commands)
	}

	companion object {
		private const val NULL_CHAR = '\u0000'
	}

	override fun processMessage(message: Message) {
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

	fun addCommands(vararg commands: CommandMatcher): VkCommandHandler {
		for (it in commands)
			when (it) {
				is CommandMatcherWithHash -> add(it)
				else -> add(it)
			}
		return this
	}

	fun addCommands(vararg commands: CommandMatcherWithHash): VkCommandHandler {
		for (it in commands)
			add(it)
		return this
	}

	override fun process(messages: List<Message>) = processMessages(messages)
}