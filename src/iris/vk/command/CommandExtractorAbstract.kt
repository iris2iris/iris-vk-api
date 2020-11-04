package iris.vk.command

import iris.vk.event.Message

/**
 * @created 04.11.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
abstract class CommandExtractorAbstract(private val prefixTester: PrefixTester, private val maxCommandLength: Int = 150) : CommandExtractor {
	interface PrefixTester {
		fun find(text: String): Int
	}

	override fun extractCommand(message: Message): String? {
		val text = message.text ?: return null
		if (text.isEmpty()) return null

		val offset = prefixTester.find(text)
		if (offset == -1) return null


		val ind = text.indexOf('\n')
		return when {
			ind == -1 -> if (text.length <= maxCommandLength) correctText(text, offset) else null
			ind <= maxCommandLength -> text.substring(offset, ind)
			else -> null
		}?.trim()?.toLowerCase()
	}

	private inline fun correctText(text: String, startIndex: Int): String {
		return if (startIndex == 0) text else text.substring(startIndex)
	}
}