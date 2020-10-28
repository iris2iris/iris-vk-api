package iris.vk.event.group

import iris.json.JsonArray
import iris.json.JsonItem
import iris.json.JsonObject
import iris.vk.event.Message
import kotlin.LazyThreadSafetyMode.NONE

/**
 * @created 27.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class GroupMessage(source: JsonItem) : GroupChatEvent(source), Message {

	override val text by lazy(NONE) { message["text"].asStringOrNull()?.replace("\r", "") }
	override val attachments: List<JsonItem>? by lazy(NONE) { val res = message["attachments"]; (if (res.isNull()) null else res as JsonArray)?.getList() }
	override val forwardedMessages: List<JsonItem>? by lazy(NONE) { (message["fwd_messages"] as? JsonArray)?.getList() }
	override val replyMessage: JsonObject? by lazy(NONE) { message["reply_message"] as? JsonObject }

}