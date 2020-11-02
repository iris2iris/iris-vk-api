package iris.vk.event.group

import iris.json.JsonItem
import iris.vk.event.CallbackEvent

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class GroupCallbackEvent(override val source: JsonItem, override val sourcePeerId: Int) : CallbackEvent {
	override val eventId: String by lazy { source["event_id"].asString() }
	override val userId: Int by lazy { source["user_id"].asInt() }
	override val payload: String by lazy { source["payload"].asString() }
	override val peerId: Int by lazy { source["peer_id"].asInt() }
	override val conversationMessageId: Int by lazy { source["conversation_message_id"].asInt() }
}