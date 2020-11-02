package iris.vk.event.user

import iris.json.JsonItem
import iris.vk.event.TitleUpdate
import kotlin.LazyThreadSafetyMode.NONE

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class UserTitleUpdate(fullItemSource: ApiSource, source: JsonItem, sourcePeerId: Int) : UserChatEvent(fullItemSource, source, sourcePeerId), TitleUpdate {
	override val text: String by lazy(NONE) { source[7]["text"].asStringOrNull() ?: "" }
}