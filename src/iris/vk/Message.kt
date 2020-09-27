package iris.vk

import iris.json.JsonItem

/**
 * @created 27.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class VkMessage(val source: JsonItem, val options: Options? = null) {
	val text by lazy { source["message"]["text"].asStringOrNull()?.replace("\r", "") }
}