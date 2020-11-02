package iris.vk

import iris.json.JsonItem

/**
 * @created 02.12.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface VkRetrievable {
	fun retrieve(wait: Boolean = true): List<JsonItem>
}