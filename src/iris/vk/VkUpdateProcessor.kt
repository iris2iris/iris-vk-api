package iris.vk

import iris.json.JsonItem

/**
 * @created 31.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface VkUpdateProcessor {
	fun processUpdates(updates: List<JsonItem>)
}