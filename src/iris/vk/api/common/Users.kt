package iris.vk.api.common

import iris.vk.Options
import iris.vk.api.IUsers
import iris.vk.api.Requester

/**
 * @created 29.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class Users<SingleType, ListType>(api: Requester<SingleType, ListType>) : SectionAbstract<SingleType, ListType>(api), IUsers<SingleType, ListType> {
	override fun get(users: List<String>?, fields: String?, token: String?): SingleType {
		val options = Options()
		if (users != null && users.isNotEmpty())
			options["user_ids"] = users.joinToString(",")
		if (fields != null)
			options["fields"] = fields
		return request("users.get", options, token)
	}
}