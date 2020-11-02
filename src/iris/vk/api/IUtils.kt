package iris.vk.api

/**
 * @created 29.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface IUtils<SingleType> {

	fun checkLink(url: String): SingleType

	fun getServerTime(token: String? = null): SingleType

	fun getShortLink(url: String, isPrivate: Boolean = false, token: String? = null): SingleType
}