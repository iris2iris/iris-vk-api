package iris.vk.command

/**
 * @created 27.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class CommandExtractorPrefixes(prefixChars: Collection<String>?, maxCommandLength: Int = 150, allowNoPrefix: Boolean = true)
	: CommandExtractorBase(CommandExtractorStrings(prefixChars, allowNoPrefix), maxCommandLength)
{

	class CommandExtractorStrings(private val prefixes: Collection<String>? = null, private val allowNoPrefix: Boolean = true) : PrefixTester {
		override fun find(text: String): Int {
			return when {
				prefixes != null -> prefixes.find { text.startsWith(it) }?.length ?: -1
				allowNoPrefix -> 0
				else -> -1
			}
		}
	}
}