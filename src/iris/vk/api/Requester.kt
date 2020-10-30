package iris.vk.api

import iris.vk.Options

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface Requester<SingleType, ListType> {
	fun request(method: String, options: Options?, token: String? = null): SingleType
	fun execute(data: List<VkRequestData>, token: String? = null): ListType
	fun emptyOfListType(): ListType
}