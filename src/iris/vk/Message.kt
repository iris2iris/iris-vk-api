package iris.vk

import iris.json.JsonItem
import kotlin.LazyThreadSafetyMode.NONE

/**
 * @created 27.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class VkMessage(val source: JsonItem, val options: Options? = null) {

	val message = source["message"]
	val text by lazy(NONE) { message["text"].asStringOrNull()?.replace("\r", "") }
	val fromId: Int by lazy(NONE) { message["from_id"].asInt() }
	val chatId: Int by lazy(NONE) { message["chat_id"].asInt() }
	val userId: Int? by lazy(NONE) { message["user_id"].asIntOrNull() }
	val peerId: Int by lazy(NONE) { message["peer_id"].asInt() }
	val attachments: List<Map<String, Any?>>? by lazy(NONE) { val res = message["attachments"]; if (res.isNull()) null else res.asTypedList() }
	val conversationMessageId: Int by lazy(NONE) { message["conversation_message_id"].asInt() }
}