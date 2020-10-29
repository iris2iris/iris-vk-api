package iris.vk.api

import iris.json.JsonItem

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface IUsers<SingleType, ListType> {
	fun get(users: List<String>? = null, fields: String? = null, token: String? = null): SingleType
}