package iris.vk.api.common

import iris.vk.Options
import iris.vk.api.IUtils
import iris.vk.api.Requester

/**
 * @created 29.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class Utils<SingleType, ListType>(api: Requester<SingleType, ListType>) : SectionAbstract<SingleType, ListType>(api), IUtils<SingleType> {

	override fun checkLink(url: String): SingleType {
		return request("utils.checkLink", Options("url" to url))
	}

	override fun getServerTime(token: String?): SingleType {
		return request("utils.getServerTime", null, token)
	}

	override fun getShortLink(url: String, isPrivate: Boolean, token: String?): SingleType {
		val options = Options("url" to url, "private" to if (isPrivate) "1" else "0")
		return request("utils.getShortLink", options, token)
	}
}