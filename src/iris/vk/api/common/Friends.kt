package iris.vk.api.common

import iris.vk.Options
import iris.vk.api.IFriends
import iris.vk.api.Requester

/**
 * @created 29.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class Friends<SingleType, ListType>(api: Requester<SingleType, ListType>) : SectionAbstract<SingleType, ListType>(api), IFriends<SingleType, ListType> {
	override fun add(userId: Int): SingleType {
		return request("friends.add", Options("user_id" to userId))
	}

	override fun getRequests(out: Int, count: Int, token: String?): SingleType {
		return request("friends.getRequests", Options("need_viewed" to 1, "count" to count, "out" to out), token)
	}

	override fun delete(id: Int, token: String?): SingleType {
		return request("friends.delete", Options("user_id" to id), token)
	}

	override fun get(amount: Int, token: String?): SingleType {
		return request("friends.get", Options("count" to amount), token)
	}

	override fun delete(userId: Int): SingleType {
		return request("friends.delete", Options("user_id" to userId))
	}
}

