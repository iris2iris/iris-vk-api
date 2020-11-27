package iris.vk.event

import iris.json.JsonItem

/**
 * @created 27.11.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class OtherEvent(override val source: JsonItem, override val sourcePeerId: Int) : Event