package iris.vk.event.group

import iris.json.JsonItem
import iris.vk.event.TitleUpdate
import kotlin.LazyThreadSafetyMode.NONE

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class GroupTitleUpdate(source: JsonItem, sourcePeerId: Int) : GroupChatEvent(source, sourcePeerId), TitleUpdate {
	override val text: String by lazy(NONE) { message["action"]["text"].asStringOrNull()?: "" }
}