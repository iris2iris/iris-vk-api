package iris.vk.event

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface CallbackEvent : Event {
	val eventId: String
	val userId: Int
	val payload: String
	val peerId: Int
	val conversationMessageId: Int
}