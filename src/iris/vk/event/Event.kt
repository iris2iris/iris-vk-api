package iris.vk.event

import iris.json.JsonItem
import iris.vk.Options

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface Event {
	val source: JsonItem
}