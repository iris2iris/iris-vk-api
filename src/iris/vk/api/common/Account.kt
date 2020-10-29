package iris.vk.api.common

import iris.vk.Options
import iris.vk.api.IAccount
import iris.vk.api.Requester

/**
 * @created 29.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class Account<SingleType, ListType>(api: Requester<SingleType, ListType>) : SectionAbstract<SingleType, ListType>(api), IAccount<SingleType> {
	override fun ban(id: Int): SingleType {
		return request("account.ban", Options("owner_id" to id))
	}
}