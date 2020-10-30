package iris.vk.api

import iris.vk.Options

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface IMessages<SingleType, ListType> {

	fun addChatUser(userId: Int, chatId: Int, visibleMessagesCount: Int, token: String? = null): SingleType

	fun allowMessagesFromGroup(groupId: Int, key: String, token: String? = null): SingleType

	fun createChat(ids: List<Long>, title: String? = null, name: String? = null, token: String? = null): SingleType

	fun delete(messageIds: Collection<Int>, isSpam: Boolean = false, deleteForAll: Boolean = false, groupId: Int = 0, token: String? = null): SingleType

	fun deleteChatPhoto(chatId: Int, groupId: Int = 0, token: String? = null): SingleType

	fun deleteConversation(peerId: Int, groupId: Int = 0, token: String? = null): SingleType

	fun deleteConversationChat(chatId: Int, groupId: Int = 0, token: String? = null): SingleType

	fun deleteConversationUser(id: Int, groupId: Int = 0, token: String? = null): SingleType

	fun deleteConversationGroup(id: Int, groupId: Int = 0, token: String? = null): SingleType

	fun denyMessagesFromGroup(groupId: Int, token: String? = null): SingleType

	fun sendMulti(data: List<Options>, token: String? = null): ListType

	fun edit(peerId: Int, message: String?, messageId: Int, options: Options? = null, token: String? = null): SingleType

	fun editChat(chatId: Int, title: String, token: String? = null): SingleType

	fun getByConversationMessageId(peerId: Int, conversationMessageIds: Collection<Int>, extended: Boolean = false, fields: String? = null, groupId: Int = 0, token: String? = null): SingleType

	fun getByConversationMessageId_ChatId(chatId: Int, conversationMessageIds: Collection<Int>, extended: Boolean = false, fields: String? = null, groupId: Int = 0, token: String? = null): SingleType

	fun getById(messageIds: List<Int>, previewLength: Int = 0, extended: Boolean = false, fields: String? = null, groupId: Int = 0, token: String? = null): SingleType

	fun getChat(chatId: Int, fields: String? = null, nameCase: String? = null, token: String? = null): SingleType

	fun getChats(chatIds: Collection<Int>, fields: String? = null, nameCase: String? = null, token: String? = null): SingleType

	fun getChatPreview(peerId: Int, fields: String? = null, token: String? = null): SingleType

	fun getChatPreview(link: String, fields: String? = null, token: String? = null): SingleType

	fun getConversationMembers(peerId: Int, fields: String? = null, groupId: Int = 0, token: String? = null): SingleType

	fun getConversations(offset: Int = 0, count: Int = 0, filter: String? = null, extended: Boolean = false, startMessageId: Int = 0, fields: String? = null, groupId: Int = 0, token: String? = null): SingleType

	fun getConversationsById(peerIds: Collection<Int>, extended: Boolean = false, fields: String? = null, groupId: Int = 0, token: String? = null): SingleType

	fun getHistory(peerId: Int, offset: Int = 0, count: Int = 100, startMessageId: Int = 0, rev: Boolean = false, extended: Boolean = false, fields: String? = null, groupId: Int = 0, token: String? = null): SingleType

	fun getHistoryAttachments(peerId: Int, startFrom: Int = 0, count: Int = 0, fields: String? = null, mediaType: String? = null, groupId: Int = 0, options: Options? = null, token: String? = null): SingleType

	fun getImportantMessages(count: Int = 0, offset: Int = 0, startMessageId: Int = 0, previewLength: Int = 0, fields: String? = null, extended: Boolean = false, groupId: Int = 0, token: String? = null): SingleType

	fun getInviteLink(peerId: Int, reset: Boolean, groupId: Int = 0, token: String? = null): SingleType

	fun getInviteLinkChat(chatId: Int, reset: Boolean, groupId: Int = 0, token: String? = null): SingleType

	fun getLastActivity(userId: Int, token: String? = null): SingleType

	fun getLongPollHistory(ts: Int, pts: Int, groupId: Int = 0, options: Options? = null, token: String? = null): SingleType

	fun getLongPollServer(needPts: Boolean = false, groupId: Int = 0, lpVersion: Int = 0, token: String? = null): SingleType

	fun isMessagesFromGroupAllowed(groupId: Int, userId: Int = 0, token: String? = null): SingleType

	fun joinChatByInviteLink(link: String, token: String? = null): SingleType

	fun markAsAnsweredConversation(peerId: Int, answered: Boolean = true, groupId: Int = 0, token: String? = null): SingleType

	fun markAsImportant(messageIds: Collection<Int>, important: Boolean = true, token: String? = null): SingleType

	fun markAsImportantConversation(peerId: Int, important: Boolean = true, groupId: Int = 0, token: String? = null): SingleType

	fun markAsRead(peerId: Int, token: String? = null): SingleType

	fun markAsRead(messageIds: Collection<Int>, peerId: Int = 0, startMessageId: Int = 0, groupId: Int = 0, markConversationAsRead: Boolean = true, token: String? = null): SingleType

	fun pin(peerId: Int, messageId: Int, conversationMessageId: Int = 0, token: String? = null): SingleType

	fun pinByCmid(peerId: Int, conversationMessageId: Int, token: String? = null): SingleType

	fun removeChatUser(chatId: Int, memberId: Int, token: String? = null): SingleType

	fun removeChatUserList(chatId: Int, memberIds: Collection<Int>, token: String? = null): ListType

	fun removeChatUserData(data: List<Options>, token: String? = null): ListType

	fun restore(messageId: Int, groupId: Int = 0, token: String? = null): SingleType

	fun search(q: String, peerId: Int = 0, offset: Int = 0, count: Int = 0, date: Int = 0, previewLength: Int = 0, extended: Boolean = false, fields: String? = null, groupId: Int = 0, token: String? = null): SingleType

	fun searchConversations(q: String, count: Int = 0, extended: Boolean = false, fields: String? = null, groupId: Int = 0, token: String? = null): SingleType

	fun send(peerId: Int, message: String? = null, attachment: String? = null, replyTo: Int = 0, forwardMessages: String? = null, dontParseLinks: Boolean = false, disableMentions: Boolean = false, options: Options? = null, token: String? = null): SingleType

	fun send(peerIds: Collection<Int>, message: String? = null, attachment: String? = null, replyTo: Int = 0, forwardMessages: String? = null, dontParseLinks: Boolean = false, disableMentions: Boolean = false, options: Options? = null, token: String? = null): SingleType

	fun send(domain: String, message: String? = null, attachment: String? = null, replyTo: Int = 0, forwardMessages: String? = null, dontParseLinks: Boolean = false, disableMentions: Boolean = false, options: Options? = null, token: String? = null): SingleType

	fun sendUser(userId: Int, message: String? = null, attachment: String? = null, replyTo: Int = 0, forwardMessages: String? = null, dontParseLinks: Boolean = false, disableMentions: Boolean = false, options: Options? = null, token: String? = null): SingleType

	fun sendChat(chatId: Int, message: String? = null, attachment: String? = null, replyTo: Int = 0, forwardMessages: String? = null, dontParseLinks: Boolean = false, disableMentions: Boolean = false, options: Options? = null, token: String? = null): SingleType

	fun setChatPhoto(file: String, token: String? = null): SingleType

	fun sendMessageEventAnswer(eventId: String, userId: Int, peerId: Int, eventData: Options?, token: String?): SingleType

	fun unpin(peerId: Int, groupId: Int = 0, token: String? = null): SingleType






	fun getChatMembers(chatId: Int, fields: String? = null, token: String? = null): SingleType

	//fun getUpdates(lpSettings: LongPollSettings, ts: String): SingleType


}