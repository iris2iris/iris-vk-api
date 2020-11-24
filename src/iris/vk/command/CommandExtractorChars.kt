package iris.vk.command

import iris.vk.event.Message

/**
 * @created 27.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class CommandExtractorChars(prefixChars: String?, maxCommandLength: Int = 150, allowNoPrefix: Boolean = false)
	: CommandExtractorAbstract(CommandExtractorChar(prefixChars, allowNoPrefix), maxCommandLength)
{

	class CommandExtractorChar(private val prefixChars: String? = null, private val allowNoPrefix: Boolean = false) : PrefixTester {
		override fun find(text: String): Int {
			return if (prefixChars != null) {
				if (!prefixChars.contains(text.first())) -1 else 1
			} else
				if (allowNoPrefix) 0 else -1
		}
	}
}