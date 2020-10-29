package iris.vk.event.user

import iris.json.JsonItem
import iris.vk.api.VkApis
import iris.vk.event.ChatEvent

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class UserChatEvent(private val fullItemSource: ApiSource, override val source: JsonItem) : ChatEvent {

	interface ApiSource {
		fun getFullEvent(messageId: Int): JsonItem?
	}

	override val id: Int by lazy(LazyThreadSafetyMode.NONE) { source[1].asInt() }

	override val peerId: Int by lazy(LazyThreadSafetyMode.NONE) { source[3].asInt() }

	override val fromId: Int by lazy(LazyThreadSafetyMode.NONE) {
		if (chatId > 0) {
			source[7]["from"].asInt()
		} else
			peerId
	}

	override val userId: Int by lazy(LazyThreadSafetyMode.NONE) { source[7]["source_mid"].asIntOrNull()?: 0 }
	override val chatId: Int by lazy(LazyThreadSafetyMode.NONE) { VkApis.peer2ChatId(peerId) }

	override val date: Long by lazy(LazyThreadSafetyMode.NONE) { source[4].asLong() }

	override val conversationMessageId: Int by lazy(LazyThreadSafetyMode.NONE) { fullItem?.let { it["conversation_message_id"].asInt() }?: 0 }

	protected val fullItem: JsonItem? by lazy { fullItemSource.getFullEvent(id) }


}