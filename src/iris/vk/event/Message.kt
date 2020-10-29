package iris.vk.event

import iris.json.JsonItem
import iris.json.JsonObject

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface Message: ChatEvent {

	val text: String?
	val attachments: List<JsonItem>?
	val forwardedMessages: List<JsonItem>?
	val replyMessage: JsonObject?
}