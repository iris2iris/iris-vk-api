package iris.vk.command

import iris.vk.VkMessage

/**
 * @created 27.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface CommandBuilder {
	fun extractCommand(message: VkMessage): String?
}