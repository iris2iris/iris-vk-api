package iris.vk.event.user

import iris.json.JsonItem
import iris.vk.event.PinUpdate

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class UserPinUpdate(fullItemSource: ApiSource, source: JsonItem, sourcePeerId: Int) : UserChatEvent(fullItemSource, source, sourcePeerId), PinUpdate {

}