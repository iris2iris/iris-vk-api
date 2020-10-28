package iris.vk.event

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface ChatEvent: Event {
	val id: Int
	val peerId: Int
	val fromId: Int
	val chatId: Int
	val date: Long
	val userId: Int
	val conversationMessageId: Int
}