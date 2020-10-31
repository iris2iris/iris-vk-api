package iris.vk.command

import iris.vk.event.Message

/**
 * @created 27.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface CommandExtractor {
	fun extractCommand(message: Message): String?
}