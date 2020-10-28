package iris.vk.event.user

import iris.json.JsonItem
import iris.vk.VkApi
import iris.vk.event.ChatEvent

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class UserChatEvent(override val source: JsonItem) : ChatEvent {

	override val id: Int by lazy(LazyThreadSafetyMode.NONE) { source[1].asInt() }

	override val peerId: Int by lazy(LazyThreadSafetyMode.NONE) { source[3].asInt() }

	override val fromId: Int by lazy(LazyThreadSafetyMode.NONE) {
		if (chatId > 0) {
			source[7]["from"].asInt()
		} else
			peerId
	}

	override val userId: Int by lazy(LazyThreadSafetyMode.NONE) { source[7]["source_mid"].asIntOrNull()?: 0 }
	override val chatId: Int by lazy(LazyThreadSafetyMode.NONE) { VkApi.peer2ChatId(peerId) }

	override val date: Long by lazy(LazyThreadSafetyMode.NONE) { source[4].asLong() }

	override val conversationMessageId: Int by lazy(LazyThreadSafetyMode.NONE) { 0
		// TODO: Понять, а как? А где? Откуда брать?
	}


}