package iris.vk.event.group

import iris.json.JsonItem
import iris.vk.event.TitleUpdate

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class GroupTitleUpdate(source: JsonItem) : GroupChatEvent(source), TitleUpdate {

}