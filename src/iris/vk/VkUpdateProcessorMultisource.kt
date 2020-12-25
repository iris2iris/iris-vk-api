package iris.vk

import iris.json.JsonItem

/**
 * @created 02.11.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface VkUpdateProcessorMultisource {
	fun processUpdates(updates: List<JsonItem>)
}