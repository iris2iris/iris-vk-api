package iris.vk.multibot

import iris.json.JsonItem

/**
 * @created 02.12.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface VkMultibotRetrieveEngine {
	fun retrieve(wait: Boolean = true): Array<JsonItem>
	fun start()
}