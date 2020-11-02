package iris.vk.event.group

import iris.json.JsonItem
import iris.vk.api.VkApis
import iris.vk.event.ChatEvent
import kotlin.LazyThreadSafetyMode.NONE

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class GroupChatEvent(source: JsonItem, override val sourcePeerId: Int) : ChatEvent {
	override val source: JsonItem = source
	open val message by lazy(NONE) { source["message"] }
	override val id: Int by lazy(NONE) { message["id"].asInt() }
	override val fromId: Int by lazy(NONE) { message["from_id"].asInt() }
	override val chatId: Int by lazy(NONE) { VkApis.peer2ChatId(peerId) }
	override val userId: Int by lazy(NONE) { message["action"]["member_id"].asInt() }
	override val peerId: Int by lazy(NONE) { message["peer_id"].asInt() }
	override val conversationMessageId: Int by lazy(NONE) { message["conversation_message_id"].asInt() }
	override val date: Long by lazy(NONE) { message["date"].asLong() }
}