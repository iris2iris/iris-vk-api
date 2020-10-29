package iris.vk.api

/**
 * @created 29.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
object Method2UrlCache {
	private val map = HashMap<String, String>()

	fun getUrl(method: String): String {
		return map.getOrPut(method) { "https://api.vk.com/method/$method" }
	}
}