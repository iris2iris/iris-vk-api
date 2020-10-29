package iris.vk.api.common

import iris.json.JsonEncoder
import iris.vk.Options
import iris.vk.api.*
import iris.vk.api.VkApis.chat2PeerId
import iris.vk.api.VkApis.group2PeerId
import iris.vk.api.VkApis.user2PeerId

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class Messages<SingleType, ListType>(api: Requester<SingleType, ListType>) : SectionAbstract<SingleType, ListType>(api), IMessages<SingleType, ListType> {

	override fun sendPm(userId: Int, message: String, options: Options?, token: String?): SingleType {
		return this.send(userId, message, options, token)
	}

	override fun send(peerId: Int, message: String, options: Options?, token: String?): SingleType {
		val params = options ?: Options()
		params["peer_id"] = peerId
		params["random_id"] = params["random_id"] ?: (0..2000000000).random()
		params["message"] = message

		return request("messages.send", params, token)
	}

	override fun getByConversationMessageId(peerId: Int, conversationMessageIds: Collection<Int>, token: String?): SingleType {
		val ids = conversationMessageIds.joinToString(",")
		val options = Options("peer_id" to peerId, "conversation_message_ids" to ids)
		return request("messages.getByConversationMessageId", options, token)
	}

	override fun getByConversationMessageId_ChatId(chatId: Int, conversationMessageIds: Collection<Int>, token: String?): SingleType {
		return getByConversationMessageId(chat2PeerId(chatId), conversationMessageIds, token)
	}

	override fun chatMessage(chatId: Int, message: String, options: Options?, token: String?): SingleType {
		return send(chat2PeerId(chatId), message, options, token)
	}

	override fun chatMessages(data: List<Options>, token: String?): ListType {
		val res = data.map { VkRequestData("messages.send", it) }
		return execute(res, token)
	}

	open fun setChatTitle(chatId: Int, title: String): SingleType {
		return editChat(chatId, title)
	}

	override fun editChat(chatId: Int, title: String, token: String?): SingleType {
		return request("messages.editChat", Options("chat_id" to chatId, "title" to title), token)
	}

	override fun messagesAddChatUser(userId: Int, chatId: Int, token: String?): SingleType {
		return request("messages.addChatUser", Options("chat_id" to chatId, "user_id" to userId), token)
	}

	override fun getConversations(amount: Int, filter: String?, token: String?): SingleType {
		return request("messages.getConversations", Options("count" to amount, "filter" to filter), token)
	}

	override fun getConversationsById(peerIds: List<Int>, fields: String?, token: String?): SingleType {
		val options = Options("peer_ids" to peerIds.joinToString(","))
		if (fields != null) {
			options["extended"] = 1
			options["fields"] = fields
		}
		return request("messages.getConversationsById", options, token)
	}

	override fun conversationDeleteChat(chatId: Int, amount: Int, token: String?): SingleType {
		return deleteConversation(chat2PeerId(chatId), amount, token)
	}

	override fun deleteConversation(peerId: Int, amount: Int, token: String?): SingleType {
		return request("messages.deleteConversation", Options("peer_id" to peerId, "count" to amount), token)
	}

	override fun conversationDeleteUser(id: Int, amount: Int, token: String?): SingleType {
		return deleteConversation(user2PeerId(id), amount, token)
	}

	override fun conversationDeleteGroup(id: Int, amount: Int, token: String?): SingleType {
		return deleteConversation(group2PeerId(id), amount, token)
	}

	override fun getHistoryChat(chatId: Int, offset: Int, count: Int, options: Options?, token: String?): SingleType {
		return getHistory(chat2PeerId(chatId), offset, count, options, token)
	}

	override fun getHistoryUser(id: Int, offset: Int, count: Int, options: Options?, token: String?): SingleType {
		return getHistory(user2PeerId(id), offset, count, options, token)
	}

	override fun getHistoryGroup(id: Int, offset: Int, count: Int, options: Options?, token: String?): SingleType {
		return getHistory(group2PeerId(id), offset, count, options, token)
	}

	override fun getHistory(peerId: Int, offset: Int, count: Int, options: Options?, token: String?): SingleType {
		val options = options?: Options()
		options["peer_id"] = peerId
		if (offset != 0)
			options["offset"] = offset
		if (count != 0)
			options["count"] = count

		return request("messages.getHistory", options, token)
	}

	override fun markAsRead(peerId: Int): SingleType {
		return request("messages.markAsRead", Options("peer_id" to peerId))
	}

	override fun removeChatUser(chatId: Int, memberId: Int, token: String?): SingleType {
		return request("messages.removeChatUser", Options("chat_id" to chatId, "member_id" to memberId), token)
	}

	override fun removeChatUserList(chatId: Int, userIds: Collection<Int>, token: String?): ListType {
		if (userIds.isEmpty()) return emptyListType()
		val requests = mutableListOf<VkRequestData>()
		for (d in userIds) {
			val params = Options("chat_id" to chatId, "member_id" to d)
			requests.add(VkRequestData("messages.removeChatUser", params, token))
		}

		return execute(requests, token)
	}

	override fun removeChatUserData(data: List<Options>, token: String?): ListType {
		if (data.isEmpty()) return emptyListType()
		val requests = mutableListOf<VkRequestData>()
		for (d in data) {
			requests.add(VkRequestData("messages.removeChatUser", d, token))
		}
		return execute(requests, token)
	}

	override fun edit(peerId: Int, message: String?, messageId: Int, options: Options?, token: String?): SingleType {
		val params = Options("peer_id" to peerId, "message_id" to messageId)
		if (options != null)
			params.putAll(options)
		if (message != null)
			params["message"] = message
		return request("messages.edit", params, token)
	}

	override fun createChat(ids: List<Long>, name: String?): SingleType {
		return request("messages.createChat", Options("user_ids" to ids.joinToString(",")))
	}

	override fun getInviteLink(chatId: Int): SingleType {
		return request("messages.getInviteLink", Options("peer_id" to chat2PeerId(chatId)))
	}

	override fun restore(messageId: Int, token: String?): SingleType {
		val options = Options("message_id" to messageId)
		return request("messages.restore", options, token)
	}

	override fun getById(messageIds: List<Int>): SingleType {
		return request("messages.getById", Options("message_ids" to messageIds.joinToString(",")))
	}

	override fun getConversationMembers(peerId: Int, fields: String?, token: String?): SingleType {
		val options = Options()
		options["peer_id"] = peerId
		if (fields != null)
			options["fields"] = fields
		return request("messages.getConversationMembers", options, token)
	}

	override fun getChatMembers(chatId: Int, fields: String?, token: String?): SingleType {
		return getConversationMembers(chat2PeerId(chatId), fields, token)
	}

	override fun pin(peerId: Int, messageId: Int): SingleType {
		return request("messages.pin", Options("peer_id" to peerId, "message_id" to messageId))
	}

	override fun search(q: String, peerId: Int?, count: Int, options: Options?, token: String?): SingleType {
		val options = options?: Options()
		options["q"] = q
		if (peerId != 0)
			options["peer_id"] = peerId
		if (count != 0)
			options["count"] = count
		return request("messages.search", options, token)
	}

	override fun delete(messageIds: Collection<Int>, deleteForAll: Boolean, isSpam: Boolean, token: String?): SingleType {
		val options = Options("message_ids" to messageIds.joinToString(","))
		if (deleteForAll)
			options["delete_for_all"] = deleteForAll
		if (isSpam)
			options["spam"] = 1

		return request("messages.delete", options, token)
	}

	override fun getLongPollServer(): SingleType {
		return request("messages.getLongPollServer", null)
	}

	override fun getUpdates(lpSettings: LongPollSettings, ts: String): SingleType {
		return api.requestUrl(lpSettings.getUpdatesLink(ts), "messages.getUpdates")
	}

	override fun sendMulti(data: Collection<Options>): ListType {
		val res = ArrayList<VkRequestData>(data.size)
		for (it in data) {
			it.getOrPut("random_id") { (0..Integer.MAX_VALUE).random() }
			res.add(VkRequestData("messages.send", it))
		}
		return execute(res)
	}

	override fun sendMessageEventAnswer(eventId: String, userId: Int, peerId: Int, eventData: Options?, token: String?): SingleType {
		return request("messages.sendMessageEventAnswer", Options(
				"event_id" to eventId, "user_id" to userId, "peer_id" to peerId, "event_data" to if (eventData != null) JsonEncoder.encode(eventData) else null
		), token
		)
	}
}