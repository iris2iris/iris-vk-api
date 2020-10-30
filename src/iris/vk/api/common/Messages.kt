package iris.vk.api.common

import iris.json.JsonEncoder
import iris.vk.Options
import iris.vk.api.IMessages
import iris.vk.api.Requester
import iris.vk.api.VkApis.chat2PeerId
import iris.vk.api.VkApis.group2PeerId
import iris.vk.api.VkApis.user2PeerId
import iris.vk.api.VkRequestData

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class Messages<SingleType, ListType>(api: Requester<SingleType, ListType>) : SectionAbstract<SingleType, ListType>(api), IMessages<SingleType, ListType> {

	override fun addChatUser(userId: Int, chatId: Int, visibleMessagesCount: Int, token: String?): SingleType {
		val options = Options("chat_id" to chatId, "user_id" to userId)
		if (visibleMessagesCount > 0) options["visible_messages_count"] = visibleMessagesCount

		return request("messages.addChatUser", options, token)
	}

	override fun allowMessagesFromGroup(groupId: Int, key: String, token: String?): SingleType {
		val options = Options("group_id" to groupId, "key" to key)

		return request("messages.addChatUser", options, token)
	}

	override fun createChat(ids: List<Long>, title: String?, name: String?, token: String?): SingleType {
		val options = Options("user_ids" to ids.joinToString {it.toString()})
		if (title != null) options["title"] = title

		return request("messages.createChat", options, token)
	}

	override fun delete(messageIds: Collection<Int>, isSpam: Boolean, deleteForAll: Boolean, groupId: Int, token: String?): SingleType {
		val options = Options("message_ids" to messageIds.joinToString(","))
		if (deleteForAll)
			options["delete_for_all"] = deleteForAll
		if (isSpam) options["spam"] = 1
		if (groupId != 0) options["group_id"] = groupId

		return request("messages.delete", options, token)
	}

	override fun deleteChatPhoto(chatId: Int, groupId: Int, token: String?): SingleType {
		val options = Options("chat_id" to chatId)
		if (groupId != 0) options["group_id"] = groupId

		return request("messages.deleteChatPhoto", options, token)
	}

	override fun deleteConversation(peerId: Int, groupId: Int, token: String?): SingleType {
		val options = Options("peer_id" to peerId)
		if (groupId != 0) options["group_id"] = groupId

		return request("messages.deleteConversation", options, token)
	}

	override fun deleteConversationChat(chatId: Int, groupId: Int, token: String?): SingleType {
		return deleteConversation(chat2PeerId(chatId), groupId, token)
	}

	override fun deleteConversationUser(id: Int, groupId: Int, token: String?): SingleType {
		return deleteConversation(user2PeerId(id), groupId, token)
	}

	override fun deleteConversationGroup(id: Int, groupId: Int, token: String?): SingleType {
		return deleteConversation(group2PeerId(id), groupId, token)
	}

	override fun denyMessagesFromGroup(groupId: Int, token: String?): SingleType {
		val options = Options("group_id" to groupId)

		return request("messages.denyMessagesFromGroup", options, token)
	}

	override fun edit(peerId: Int, message: String?, messageId: Int, options: Options?, token: String?): SingleType {
		val options = options?: Options()
		options +="peer_id" to peerId
		options += "message_id" to messageId
		if (message != null) options["message"] = message

		return request("messages.edit", options, token)
	}

	override fun editChat(chatId: Int, title: String, token: String?): SingleType {
		return request("messages.editChat", Options("chat_id" to chatId, "title" to title), token)
	}

	override fun getByConversationMessageId(peerId: Int, conversationMessageIds: Collection<Int>, extended: Boolean, fields: String?, groupId: Int, token: String?): SingleType {
		val options = Options(
				"peer_id" to peerId,
				"conversation_message_ids" to conversationMessageIds.joinToString {it.toString() }
		)
		if (extended) options["extended"] = 1
		if (groupId != 0) options["group_id"] = groupId

		return request("messages.getByConversationMessageId", options, token)
	}

	override fun getByConversationMessageId_ChatId(chatId: Int, conversationMessageIds: Collection<Int>, extended: Boolean, fields: String?, groupId: Int, token: String?): SingleType {
		return getByConversationMessageId(chat2PeerId(chatId), conversationMessageIds, extended, fields, groupId, token)
	}

	override fun getById(messageIds: List<Int>, previewLength: Int, extended: Boolean, fields: String?, groupId: Int, token: String?): SingleType {
		val options = Options("message_ids" to messageIds.joinToString { it.toString() })
		if (previewLength > 0) options["preview_length"] = previewLength
		if (extended) options["extended"] = 1
		if (groupId != 0) options["group_id"] = groupId

		return request("messages.getById", options, token)
	}


	override fun getChat(chatId: Int, fields: String?, nameCase: String?, token: String?): SingleType {
		val options = Options("message_ids" to chatId)
		if (fields != null) options["fields"] = fields
		if (nameCase != null) options["name_case"] = nameCase

		return request("messages.getChat", options, token)
	}

	override fun getChats(chatIds: Collection<Int>, fields: String?, nameCase: String?, token: String?): SingleType {
		val options = Options("message_ids" to chatIds.joinToString { it.toString() })
		if (fields != null) options["fields"] = fields
		if (nameCase != null) options["name_case"] = nameCase

		return request("messages.getChat", options, token)
	}

	override fun getChatPreview(peerId: Int, fields: String?, token: String?): SingleType {
		val options = Options("peer_id" to peerId )
		if (fields != null) options["fields"] = fields

		return request("messages.getChatPreview", options, token)
	}

	override fun getChatPreview(link: String, fields: String?, token: String?): SingleType {
		val options = Options("link" to link )
		if (fields != null) options["fields"] = fields

		return request("messages.getChatPreview", options, token)
	}

	override fun getConversationMembers(peerId: Int, fields: String?, groupId: Int, token: String?): SingleType {
		val options = Options()
		options["peer_id"] = peerId
		if (fields != null) options["fields"] = fields
		if (groupId != 0) options["group_id"] = groupId
		return request("messages.getConversationMembers", options, token)
	}

	override fun getConversations(offset: Int, count: Int, filter: String?, extended: Boolean, startMessageId: Int, fields: String?, groupId: Int, token: String?): SingleType {
		val options = Options()
		if (offset != 0) options["offset"] = offset
		if (count != 0) options["count"] = count
		if (filter != null) options["filter"] = filter
		if (extended) options["extended"] = 1
		if (startMessageId != 0) options["start_message_id"] = startMessageId
		if (fields != null) options["fields"] = fields
		if (groupId != 0) options["group_id"] = groupId

		return request("messages.getConversations", options, token)
	}

	override fun getConversationsById(peerIds: Collection<Int>, extended: Boolean, fields: String?, groupId: Int, token: String?): SingleType {
		val options = Options("peer_ids" to peerIds.joinToString {it.toString()})
		if (extended) options["extended"] = 1
		if (fields != null) options["fields"] = fields
		if (groupId != 0) options["group_id"] = groupId

		return request("messages.getConversationsById", options, token)
	}

	override fun getHistory(peerId: Int, offset: Int, count: Int, startMessageId: Int, rev: Boolean, extended: Boolean, fields: String?, groupId: Int, token: String?): SingleType {
		val options = Options("peer_id" to peerId)
		if (offset != 0) options["offset"] = offset
		if (count != 0) options["count"] = count
		if (startMessageId != 0) options["start_message_id"] = startMessageId
		if (rev) options["rev"] = rev
		if (extended) options["extended"] = 1
		if (fields != null) options["fields"] = fields
		if (groupId != 0) options["group_id"] = groupId

		return request("messages.getHistory", options, token)
	}

	override fun getHistoryAttachments(peerId: Int, startFrom: Int, count: Int, fields: String?, mediaType: String?, groupId: Int, options: Options?, token: String?): SingleType {
		val options = options ?: Options()
		options["peer_id"] = peerId
		if (startFrom != 0) options["start_from"] = startFrom
		if (count != 0) options["count"] = count
		if (fields != null) options["fields"] = fields
		if (mediaType != null) options["media_type"] = mediaType
		if (groupId != 0) options["group_id"] = groupId

		return request("messages.getHistoryAttachments", options, token)
	}

	override fun getImportantMessages(count: Int, offset: Int, startMessageId: Int, previewLength: Int, fields: String?, extended: Boolean, groupId: Int, token: String?): SingleType {
		val options = Options()
		if (count != 0) options["count"] = count
		if (offset != 0) options["offset"] = offset
		if (startMessageId != 0) options["start_message_id"] = startMessageId
		if (extended) options["extended"] = 1
		if (fields != null) options["fields"] = fields
		if (previewLength != 0) options["preview_length"] = previewLength
		if (groupId != 0) options["group_id"] = groupId

		return request("messages.getImportantMessages", options, token)
	}

	override fun getInviteLink(peerId: Int, reset: Boolean, groupId: Int, token: String?): SingleType {
		val options = Options("peer_id" to peerId)
		if (reset) options["reset"] = 1
		if (groupId != 0) options["group_id"] = groupId
		return request("messages.getInviteLink", options, token)
	}

	override fun getInviteLinkChat(chatId: Int, reset: Boolean, groupId: Int, token: String?): SingleType {
		return getInviteLink(chat2PeerId(chatId), reset, groupId, token)
	}

	override fun getLastActivity(userId: Int, token: String?): SingleType {
		val options = Options("user_id" to userId)
		return request("messages.getLastActivity", options, token)
	}

	override fun getLongPollHistory(ts: Int, pts: Int, groupId: Int, options: Options?, token: String?): SingleType {
		val options = options ?: Options()
		options["ts"] = ts
		options["pts"] = pts
		if (groupId != 0) options["group_id"] = groupId

		return request("messages.getLongPollHistory", options, token)
	}

	override fun getLongPollServer(needPts: Boolean, groupId: Int, lpVersion: Int, token: String?): SingleType {
		val options = Options()
		if (needPts) options["need_pts"] = 1
		if (groupId != 0) options["group_id"] = groupId
		if (lpVersion != 0) options["lp_version"] = lpVersion
		return request("messages.getLongPollServer", options)
	}

	override fun isMessagesFromGroupAllowed(groupId: Int, userId: Int, token: String?): SingleType {
		val options = Options()
		if (groupId != 0) options["group_id"] = groupId
		if (userId != 0) options["user_id"] = userId
		return request("messages.isMessagesFromGroupAllowed", options)
	}

	override fun joinChatByInviteLink(link: String, token: String?): SingleType {
		return request("messages.joinChatByInviteLink", Options("link" to link))
	}

	override fun markAsAnsweredConversation(peerId: Int, answered: Boolean, groupId: Int, token: String?): SingleType {
		val options = Options()
		options["peer_id"] = peerId
		options["answered"] = if (answered) "1" else "0"
		if (groupId != 0) options["group_id"] = groupId
		return request("messages.markAsAnsweredConversation", options)
	}

	override fun markAsImportant(messageIds: Collection<Int>, important: Boolean, token: String?): SingleType {
		val options = Options()
		options["message_ids"] = messageIds.joinToString { it.toString() }
		options["important"] = if (important) "1" else "0"
		return request("messages.markAsImportant", options)
	}

	override fun markAsImportantConversation(peerId: Int, important: Boolean, groupId: Int, token: String?): SingleType {
		val options = Options()
		options["peer_id"] = peerId
		options["important"] = if (important) "1" else "0"
		if (groupId != 0) options["group_id"] = groupId
		return request("messages.markAsImportantConversation", options)
	}

	override fun markAsRead(peerId: Int, token: String?): SingleType {
		return request("messages.markAsRead", Options("peer_id" to peerId), token)
	}

	override fun markAsRead(messageIds: Collection<Int>, peerId: Int, startMessageId: Int, groupId: Int, markConversationAsRead: Boolean, token: String?): SingleType {
		val options = Options()
		options["message_ids"] = messageIds.joinToString { it.toString() }
		if (peerId != 0) options["peer_id"] = peerId
		if (startMessageId != 0) options["start_message_id"] = startMessageId
		if (groupId != 0) options["group_id"] = groupId
		options["mark_conversation_as_read"] = if (markConversationAsRead) "1" else "0"
		return request("messages.markAsRead", options)
	}

	override fun pin(peerId: Int, messageId: Int, conversationMessageId: Int, token: String?): SingleType {
		val options = Options()
		options["peer_id"] = peerId
		if (messageId != 0) options["message_id"] = messageId
		if (conversationMessageId != 0) options["conversation_message_id"] = conversationMessageId
		return request("messages.pin", options)
	}

	override fun pinByCmid(peerId: Int, conversationMessageId: Int, token: String?): SingleType {
		return pin(peerId, 0, conversationMessageId, token)
	}

	override fun removeChatUser(chatId: Int, memberId: Int, token: String?): SingleType {
		return request("messages.removeChatUser", Options("chat_id" to chatId, "member_id" to memberId), token)
	}

	override fun removeChatUserList(chatId: Int, memberIds: Collection<Int>, token: String?): ListType {
		if (memberIds.isEmpty()) return emptyListType()
		val requests = memberIds.map {
			val params = Options("chat_id" to chatId, "member_id" to it)
			VkRequestData("messages.removeChatUser", params, token)
		}

		return execute(requests, token)
	}

	override fun removeChatUserData(data: List<Options>, token: String?): ListType {
		if (data.isEmpty()) return emptyListType()
		val requests = data.map {
			VkRequestData("messages.removeChatUser", it, token)
		}
		return execute(requests, token)
	}

	override fun restore(messageId: Int, groupId: Int, token: String?): SingleType {
		val options = Options("message_id" to messageId)
		if (groupId != 0) options["group_id"] = groupId
		return request("messages.restore", options, token)
	}

	override fun search(q: String, peerId: Int, offset: Int, count: Int, date: Int, previewLength: Int, extended: Boolean, fields: String?, groupId: Int, token: String?): SingleType {
		val options = Options()
		options["q"] = q
		if (peerId != 0) options["peer_id"] = peerId
		if (offset != 0) options["offset"] = offset
		if (count != 0) options["count"] = count
		if (date != 0) options["date"] = date
		if (previewLength != 0) options["preview_length"] = previewLength
		if (extended) options["extended"] = "1"
		if (fields != null) options["fields"] = fields
		if (groupId != 0) options["group_id"] = groupId
		return request("messages.search", options, token)
	}

	override fun searchConversations(q: String, count: Int, extended: Boolean, fields: String?, groupId: Int, token: String?): SingleType {
		val options = Options()
		options["q"] = q
		if (count != 0) options["count"] = count
		if (extended) options["extended"] = "1"
		if (fields != null) options["fields"] = fields
		if (groupId != 0) options["group_id"] = groupId
		return request("messages.search", options, token)
	}

	open fun send(options: Options, message: String?, attachment: String?, reply_to: Int, forward_messages: String?, dont_parse_links: Boolean, disable_mentions: Boolean, token: String?): SingleType {
		if (message != null) options["message"] = message
		if (attachment != null) options["attachment"] = attachment
		if (reply_to != 0) options["reply_to"] = reply_to
		if (forward_messages != null) options["forward_messages"] = forward_messages
		if (dont_parse_links) options["dont_parse_links"] = "1"
		if (disable_mentions) options["disable_mentions"] = "1"
		options["random_id"] = options["random_id"] ?: (0..2000000000).random()

		return request("messages.send", options, token)
	}

	override fun send(peerId: Int, message: String?, attachment: String?, replyTo: Int, forwardMessages: String?, dontParseLinks: Boolean, disableMentions: Boolean, options: Options?, token: String?): SingleType {
		val params = options ?: Options()
		params["peer_id"] = peerId
		return send(params, message, attachment, replyTo, forwardMessages, dontParseLinks, disableMentions, token)
	}

	override fun send(peerIds: Collection<Int>, message: String?, attachment: String?, replyTo: Int, forwardMessages: String?, dontParseLinks: Boolean, disableMentions: Boolean, options: Options?, token: String?): SingleType {
		val params = options ?: Options()
		params["peer_ids"] = peerIds.joinToString { it.toString() }
		return send(params, message, attachment, replyTo, forwardMessages, dontParseLinks, disableMentions, token)
	}

	override fun send(domain: String, message: String?, attachment: String?, replyTo: Int, forwardMessages: String?, dontParseLinks: Boolean, disableMentions: Boolean, options: Options?, token: String?): SingleType {
		val params = options ?: Options()
		params["domain"] = domain
		return send(params, message, attachment, replyTo, forwardMessages, dontParseLinks, disableMentions, token)
	}

	override fun sendUser(userId: Int, message: String?, attachment: String?, replyTo: Int, forwardMessages: String?, dontParseLinks: Boolean, disableMentions: Boolean, options: Options?, token: String?): SingleType {
		return send(userId, message, attachment, replyTo, forwardMessages, dontParseLinks, disableMentions, options, token)
	}

	override fun sendChat(chatId: Int, message: String?, attachment: String?, replyTo: Int, forwardMessages: String?, dontParseLinks: Boolean, disableMentions: Boolean, options: Options?, token: String?): SingleType {
		return send(chat2PeerId(chatId), message, attachment, replyTo, forwardMessages, dontParseLinks, disableMentions, options, token)
	}

	override fun setChatPhoto(file: String, token: String?): SingleType {
		val options = Options("file" to file)
		return request("messages.setChatPhoto", options, token)
	}

	override fun sendMessageEventAnswer(eventId: String, userId: Int, peerId: Int, eventData: Options?, token: String?): SingleType {
		return request("messages.sendMessageEventAnswer", Options(
				"event_id" to eventId, "user_id" to userId, "peer_id" to peerId, "event_data" to if (eventData != null) JsonEncoder.encode(eventData) else null
		), token
		)
	}

	override fun unpin(peerId: Int, groupId: Int, token: String?): SingleType {
		val options = Options("peer_id" to peerId)
		if (groupId != 0) options["group_id"] = groupId
		return request("messages.unpin", options, token)
	}

	override fun sendMulti(data: List<Options>, token: String?): ListType {
		val res = data.map {
			it.getOrPut("random_id") { (0..Integer.MAX_VALUE).random() }
			VkRequestData("messages.send", it)
		}
		return execute(res, token)
	}

	open fun setChatTitle(chatId: Int, title: String): SingleType {
		return editChat(chatId, title)
	}

	override fun getChatMembers(chatId: Int, fields: String?, token: String?): SingleType {
		return getConversationMembers(chat2PeerId(chatId), fields, token = token)
	}

	/*override fun getUpdates(lpSettings: LongPollSettings, ts: String): SingleType {
		return api.requestUrl(lpSettings.getUpdatesLink(ts), "messages.getUpdates")
	}*/
}