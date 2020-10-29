package iris.vk.api

import iris.vk.Options

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface IMessages<SingleType, ListType> {

	fun sendPm(userId: Int, message: String, options: Options? = null, token: String? = null): SingleType

	fun send(peerId: Int, message: String, options: Options? = null, token: String? = null): SingleType

	fun getByConversationMessageId(peerId: Int, conversationMessageIds: Collection<Int>, token: String? = null): SingleType

	fun getByConversationMessageId_ChatId(chatId: Int, conversationMessageIds: Collection<Int>, token: String? = null): SingleType

	fun chatMessage(chatId: Int, message: String, options: Options? = null, token: String? = null): SingleType

	fun chatMessages(data: List<Options>, token: String? = null): ListType

	fun editChat(chatId: Int, title: String, token: String? = null): SingleType

	fun messagesAddChatUser(userId: Int, chatId: Int, token: String? = null): SingleType

	fun getConversations(amount: Int = 200, filter: String? = "all", token: String? = null): SingleType

	fun getConversationsById(peerIds: List<Int>, fields: String? = null, token: String? = null): SingleType

	fun conversationDeleteChat(chatId: Int, amount: Int = 10000, token: String? = null): SingleType

	fun deleteConversation(peerId: Int, amount: Int = 10000, token: String? = null): SingleType

	fun conversationDeleteUser(id: Int, amount: Int = 10000, token: String? = null): SingleType

	fun conversationDeleteGroup(id: Int, amount: Int = 10000, token: String? = null): SingleType

	fun getHistoryChat(chatId: Int, offset: Int = 0, count: Int = 100, options: Options? = null, token: String? = null): SingleType

	fun getHistoryUser(id: Int, offset: Int = 0, count: Int = 100, options: Options? = null, token: String? = null): SingleType

	fun getHistoryGroup(id: Int, offset: Int = 0, count: Int = 100, options: Options? = null, token: String? = null): SingleType

	fun getHistory(peerId: Int, offset: Int = 0, count: Int = 100, options: Options? = null, token: String? = null): SingleType

	fun markAsRead(peerId: Int): SingleType

	fun removeChatUser(chatId: Int, userId: Int, token: String? = null): SingleType

	fun removeChatUserList(chatId: Int, userIds: Collection<Int>, token: String? = null): ListType

	fun removeChatUserData(data: List<Options>, token: String? = null): ListType

	fun edit(peerId: Int, message: String?, messageId: Int, options: Options? = null, token: String? = null): SingleType

	fun createChat(ids: List<Long>, name: String? = null): SingleType

	fun getInviteLink(chatId: Int): SingleType

	fun restore(messageId: Int, token: String? = null): SingleType

	fun getById(messageIds: List<Int>): SingleType

	fun getConversationMembers(peerId: Int, fields: String? = null, token: String? = null): SingleType

	fun getChatMembers(chatId: Int, fields: String? = null, token: String? = null): SingleType

	fun pin(peerId: Int, messageId: Int): SingleType

	fun search(q: String, peerId: Int? = 0, count: Int = 10, options: Options?, token: String?): SingleType

	fun delete(messageIds: Collection<Int>, deleteForAll: Boolean = false, isSpam: Boolean = false, token: String? = null): SingleType

	fun getLongPollServer(): SingleType

	fun getUpdates(lpSettings: LongPollSettings, ts: String): SingleType

	fun sendMulti(data: Collection<Options>): ListType

	fun sendMessageEventAnswer(eventId: String, userId: Int, peerId: Int, eventData: Options?, token: String?): SingleType
}