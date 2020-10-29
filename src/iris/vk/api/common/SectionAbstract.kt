package iris.vk.api.common

import iris.vk.Options
import iris.vk.api.Requester
import iris.vk.api.VkRequestData

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
abstract class SectionAbstract<SingleType, ListType>(protected open val api: Requester<SingleType, ListType>) {

	fun emptyListType(): ListType {
		return api.emptyOfListType()
	}

	internal inline fun request(method: String, options: Options?, token: String? = null): SingleType {
		return api.request(method, options, token)
	}

	internal inline fun execute(data: List<VkRequestData>, token: String? = null): ListType {
		return api.execute(data, token)
	}
}