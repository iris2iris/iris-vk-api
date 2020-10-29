package iris.vk.api

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface IUsers<SingleType, ListType> {
	fun get(users: List<String>? = null, fields: String? = null, token: String? = null): SingleType
}