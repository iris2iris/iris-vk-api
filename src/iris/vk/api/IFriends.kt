package iris.vk.api

import iris.json.JsonItem

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface IFriends<SingleType, ListType> {
	fun add(userId: Int): SingleType

	fun getRequests(out: Int = 0, count: Int = 100, token: String? = null): SingleType

	fun delete(id: Int, token: String? = null): SingleType

	fun get(amount: Int = 1000, token: String? = null): SingleType

	fun delete(userId: Int): SingleType
}