package iris.vk.event.group

import iris.json.JsonItem
import iris.vk.event.Message

/**
 * @created 01.11.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class GroupMessageWithoutChatInfo(source: JsonItem) : GroupMessage(source), Message {
	override val message: JsonItem by lazy(LazyThreadSafetyMode.NONE) { this.source }
}