@file:Suppress("unused", "MemberVisibilityCanBePrivate", "UNCHECKED_CAST", "FunctionName")

package iris.vk

import iris.json.JsonEncoder
import iris.json.JsonItem
import iris.json.flow.JsonFlowParser
import iris.json.plain.IrisJsonObject
import iris.json.proxy.JsonProxyObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.max

/**
 * @created 06.09.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

const val VK_API_VERSION = "5.124"

const val VK_BOT_ERROR_UNKNOWN = 1
const val VK_BOT_ERROR_APP_IS_OFF = 2
const val VK_BOT_ERROR_UNKNOWN_METHOD = 3
const val VK_BOT_ERROR_WRONG_TOKEN = 4
const val VK_BOT_ERROR_AUTH_FAILED = 5
const val VK_BOT_ERROR_TOO_MANY_REQUESTS = 6
const val VK_BOT_ERROR_NO_RIGHTS_FOR_ACTION = 7
const val VK_BOT_ERROR_WRONG_REQUEST = 8
const val VK_BOT_ERROR_ONE_TYPE_ACTIONS = 9
const val VK_BOT_ERROR_INTERNAL = 10
const val VK_BOT_ERROR_TEST_MODE_APP_MUST_BE_OFF = 11
const val VK_BOT_ERROR_CAPTCHA = 14
const val VK_BOT_ERROR_ACCESS_DENIED = 15
const val VK_BOT_ERROR_HTTPS_REQUIRED = 16
const val VK_BOT_ERROR_VALIDATION_REQUIRED = 17
const val VK_BOT_ERROR_PAGE_DELETED = 18
const val VK_BOT_ERROR_ACTION_DENIED_FOR_STANDALONE = 20
const val VK_BOT_ERROR_ACTION_ALLOWED_ONLY_FOR_STANDALONE = 21
const val VK_BOT_ERROR_METHOD_IS_OFF = 23
const val VK_BOT_ERROR_USER_CONFIRMATION_REQUIRED = 24
const val VK_BOT_ERROR_GROUP_TOKEN_IS_INVALID = 27
const val VK_BOT_ERROR_APP_TOKEN_IS_INVALID = 28
const val VK_BOT_ERROR_DATA_REQUEST_LIMIT = 29
const val VK_BOT_ERROR_PROFILE_IS_PRIVATE = 30
const val VK_BOT_ERROR_ONE_OF_PARAMETERS_IS_WRONG = 100
const val VK_BOT_ERROR_WRONG_APP_API = 101
const val VK_BOT_ERROR_WRONG_USER_ID = 113
const val VK_BOT_ERROR_WRONG_TIMESTAMP = 150
const val VK_BOT_ERROR_USER_NOT_FOUND = 177
const val VK_BOT_ERROR_ALBUM_ACCESS_DENIED = 200
const val VK_BOT_ERROR_AUDIO_ACCESS_DENIED = 201
const val VK_BOT_ERROR_GROUP_ACCESS_DENIED = 203
const val VK_BOT_ERROR_ALBUM_IS_FULL = 300
const val VK_BOT_ERROR_ACTION_IS_DENIED = 500
const val VK_BOT_ERROR_NO_RIGHTS_FOR_ADV_CABINET = 600
const val VK_BOT_ERROR_IN_ADV_CABINET = 603

const val VK_BOT_ERROR_CANT_SEND_TO_USER_IN_BLACKLIST = 900
const val VK_BOT_ERROR_CANT_SEND_WITHOUT_PERMISSION = 901
const val VK_BOT_ERROR_CANT_SEND_TO_USER_PRIVACY_SETTINGS = 902
const val VK_BOT_ERROR_KEYBOARD_FORMAT_IS_INVALID = 911
const val VK_BOT_ERROR_THIS_IS_CHATBOT_FEATURE = 912
const val VK_BOT_ERROR_TOO_MANY_FORWARDED_MESSAGES = 913
const val VK_BOT_ERROR_MESSAGE_IS_TOO_LONG = 914
const val VK_BOT_ERROR_NO_ACCESS_TO_THIS_CHAT = 917
const val VK_BOT_ERROR_CANT_FORWARD_SELECTED_MESSAGES = 921
const val VK_BOT_ERROR_CANT_DELETE_FOR_ALL_USERS = 924
const val VK_BOT_ERROR_USER_NOT_FOUND_IN_CHAT = 935
const val VK_BOT_ERROR_CONTACT_NOT_FOUND = 936

open class VkApi(val token: String, val version: String = VK_API_VERSION, private val connection: VkApiConnection = VkApiConnectionHttpClient()) {

	val messages = Messages()
	val groups = Groups()
	val users = Users()
	val photos = Photos()
	val docs = Docs()

	open inner class Messages {
		open fun sendPm(userId: Int, message: String, options: Options? = null, token: String? = null): JsonItem? {
			return this.send(userId, message, options, token)
		}

		open fun send(peerId: Int, message: String, options: Options? = null, token: String? = null): JsonItem? {

			val params = options ?: Options()
			params["peer_id"] = peerId
			params["random_id"] = (0..2000000000).random()
			params["message"] = message

			return request("messages.send", params, token)
		}

		open fun getByConversationMessageId(peerId: Int, conversationMessageIds: List<Int>, token: String? = null): JsonItem? {
			val ids = conversationMessageIds.joinToString(",")
			val options = Options("peer_id" to peerId, "conversation_message_ids" to ids)
			return request("messages.getByConversationMessageId", options, token)
		}

		open fun getByConversationMessageId_ChatId(chatId: Int, conversationMessageIds: List<Int>, token: String? = null): JsonItem? {
			return getByConversationMessageId(chat2PeerId(chatId), conversationMessageIds, token)
		}

		open fun chatMessage(chatId: Int, message: String, options: Options? = null, token: String? = null): JsonItem? {
			return send(chat2PeerId(chatId), message, options, token)
		}

		open fun chatMessages(data: List<Options>, token: String? = null): List<JsonItem> {
			val res = mutableListOf<VkRequestData>()
			for (it in data) {
				res.add(VkRequestData("messages.send", it))
			}
			return execute(res, token)
		}

		open fun editChat(chatId: Int, title: String, token: String? = null, version: String? = null): JsonItem? {
			return request("messages.editChat", Options("chat_id" to chatId, "title" to title), token, version)
		}

		open fun messagesAddChatUser(userId: Int, chatId: Int, token: String? = null): JsonItem? {
			return request("messages.addChatUser", Options("chat_id" to chatId, "user_id" to userId), token)
		}


		open fun getConversations(amount: Int = 200, filter: String? = "all", token: String? = null): List<JsonItem>? {
			val res = getConversations2(amount, filter, token)?: return null
			return res["response"]["items"] as List<JsonItem>
		}

		open fun getConversations2(amount: Int = 200, filter: String? = "all", token: String? = null): JsonItem? {
			return request("messages.getConversations", Options("count" to amount, "filter" to filter), token)
		}

		open fun getConversationsById(peerIds: List<Int>, fields: String? = null, token: String? = null): JsonItem? {
			val res = this.getConversationsById2(peerIds, fields, token)?: return null
			return res["response"]
		}

		open fun getConversationsById2(peerIds: List<Int>, fields: String? = null, token: String? = null): JsonItem? {
			val options = Options("peer_ids" to peerIds.joinToString(","))
			if (fields != null) {
				options["extended"] = 1
				options["fields"] = fields
			}
			return request("messages.getConversationsById", options, token)
		}

		open fun conversationDeleteChat(chatId: Int, amount: Int = 10000, token: String? = null): JsonItem? {
			return deleteConversation(chat2PeerId(chatId), amount, token)
		}

		open fun deleteConversation(peerId: Int, amount: Int = 10000, token: String? = null): JsonItem? {
			return request("messages.deleteConversation", Options("peer_id" to peerId, "count" to amount), token)
		}

		open fun conversationDeleteUser(id: Int, amount: Int = 10000, token: String? = null): JsonItem? {
			return deleteConversation(user2PeerId(id), amount, token)
		}

		open fun conversationDeleteGroup(id: Int, amount: Int = 10000, token: String? = null): JsonItem? {
			return deleteConversation(group2PeerId(id), amount, token)
		}


		open fun getHistoryChat(chatId: Int, count: Int = 20, token: String? = null): JsonItem? {
			val res = getHistory(chat2PeerId(chatId), count, token)?: return null
			return res["response"]
		}

		open fun getHistoryUser(id: Int, count: Int = 20, token: String? = null): JsonItem? {
			val res = getHistory(user2PeerId(id), count, token)?: return null
			return res["response"]
		}

		open fun getHistoryGroup(id: Int, count: Int = 20, token: String? = null): JsonItem? {
			val res = getHistory(group2PeerId(id), count, token)?: return null
			return res["response"]
		}

		open fun getHistory(peerId: Int, count: Int = 20, token: String? = null): JsonItem? {
			return request("messages.getHistory", Options("peer_id" to peerId, "count" to count), token)
		}

		open fun markAsRead(peerId: Int): JsonItem? {
			return request("messages.markAsRead", Options("peer_id" to peerId))
		}

		open fun removeChatUser(chatId: Int, userId: Int, token: String? = null): JsonItem? {
			return request("messages.removeChatUser", Options("chat_id" to chatId, "member_id" to userId), token)
		}

		open fun removeChatUserList(chatId: Int, userIds: List<Int>, token: String? = null): List<JsonItem>? {
			if (userIds.isEmpty()) return emptyList()
			val requests = mutableListOf<VkRequestData>()
			for (d in userIds) {
				val params = Options("chat_id" to chatId, "member_id" to d)
				requests.add(VkRequestData("messages.removeChatUser", params, token))
			}

			return execute(requests, token)
		}

		open fun removeChatUserData(data: List<Options>, token: String? = null): List<JsonItem> {
			if (data.isEmpty()) return emptyList()
			val requests = mutableListOf<VkRequestData>()
			for (d in data) {
				requests.add(VkRequestData("messages.removeChatUser", d, token))
			}

			return execute(requests, token)
		}

		open fun edit(peerId: Int, message: String?, messageId: Int, options: Options? = null, token: String? = null): JsonItem? {
			val params = Options("peer_id" to peerId, "message_id" to messageId)
			if (options != null)
				params.putAll(options)
			if (message != null)
				params["message"] = message
			return request("messages.edit", params, token)
		}

		open fun createChat(ids: List<Long>, name: String? = null): Int {
			val res = request("messages.createChat", "user_ids=" + ids.joinToString(","))?: return 0
			if (res["response"].isNull()) return 0
			val id = res["response"].asInt()
			if (name != null)
				editChat(id, name)
			return id
		}

		open fun getInviteLink(chatId: Int): String? {
			return (request("messages.getInviteLink", "peer_id=" + chat2PeerId(chatId))?: return null)["response"]["link"].asStringOrNull()
		}

		open fun restore(messageId: Int, token: String? = null): JsonItem? {
			val options = Options("message_id" to messageId)
			return request("messages.restore", options, token)
		}

		open fun getById(messageIds: List<Int>): JsonItem? {
			return request("messages.getById", "message_ids=" + messageIds.joinToString(","))
		}

		protected fun generateMessagesRemoveChatUserData(chatId: Int, userId: Int, token: String? = null): Options {
			return Options("method" to "messages.removeChatUser", "params" to Options("chat_id" to chatId, "member_id" to userId), "token" to token)
		}

		protected fun generateMessagesSendData(peerId: Int, message: String, options: Options? = null, token: String? = null): Options {
			val params = Options("message" to message, "random_id" to (1..2000000000).random(), "peer_id" to peerId)
			if (options != null)
				params.putAll(options)
			return Options("method" to "messages.send", "params" to params, "token" to token)
		}

		open fun getConversationMembers(peerId: Int, fields: String? = null, token: String? = null): JsonItem? {
			return request("messages.getConversationMembers", "peer_id=" + peerId + (if (fields != null) "&fields=" + encode(fields) else ""), token)
		}

		open fun getChatMembers(chatId: Int, fields: String? = null, token: String? = null): JsonItem? {
			return request("messages.getConversationMembers", "peer_id=" + chat2PeerId(chatId) + (if (fields != null) "&fields=" + encode(fields) else ""), token)
		}

		open fun pin(peerId: Int, messageId: Int): JsonItem? {
			return request("messages.pin", "peer_id=$peerId&message_id=$messageId")
		}

		open fun search(q: String, peerId: Int? = 0, count: Int = 10): JsonItem? {
			return request("messages.search", "q=" + encode(q) + (if (peerId != 0) "&peer_id=$peerId" else "") + (if (count != 0) "&count=$count" else ""))
		}

		open fun messagesDelete(messageIds: List<Int>, deleteForAll: Boolean = false, isSpam: Boolean = false, token: String? = null): JsonItem? {
			val options = Options("message_ids" to messageIds.joinToString(","))
			if (deleteForAll)
				options["delete_for_all"] = deleteForAll
			if (isSpam)
				options["spam"] = 1

			return request("messages.delete", options, token)
		}

		open fun getLongPollServer(): JsonItem? {
			return request("messages.getLongPollServer", null as String?)
		}

		open fun getUpdates(lpSettings: LongPollSettings, ts: String): JsonItem? {
			val response = connection.request(lpSettings.getUpdatesLink(ts))
			if (response == null || response.code != 200)
				return null
			return parser(response.responseText)
		}

		open fun sendMulti(data: Collection<Options>): List<JsonItem> {
			val res = mutableListOf<VkRequestData>()
			for (it in data) {
				res.add(VkRequestData("messages.send", it))
			}
			return execute(res, token)
		}
	}

	open inner class Friends {
		open fun add(userId: Int): JsonItem? {
			return request("friends.add", Options("user_id" to userId))
		}

		open fun getRequests(out: Int = 0, count: Int = 100, token: String? = null): JsonItem? {
			return request("friends.getRequests", Options("need_viewed" to 1, "count" to count, "out" to out), token)
		}

		open fun delete(id: Int, token: String? = null): JsonItem? {
			return request("friends.delete", Options("user_id" to id), token)
		}

		open fun get(amount: Int = 1000, token: String? = null): JsonItem? {
			return request("friends.get", Options("count" to amount), token)
		}

		open fun delete(userId: Int): JsonItem? {
			return request("friends.delete", "user_id=$userId")
		}
	}

	open inner class Groups {
		open fun leave(groupId: Int): JsonItem? {
			return request("groups.leave", Options("group_id" to groupId))
		}

		open fun get(userId: Int? = null, extended: Boolean = false, filter: String? = null, fields: String? = null, offset: Int = 0, count: Int = 0, token: String? = null): JsonItem? {
			val params = Options()
			if (userId != null)
				params["user_id"] = userId
			if (extended)
				params["extended"] = extended
			if (filter != null)
				params["filter"] = filter
			if (fields != null)
				params["fields"] = fields
			if (offset != 0)
				params["offset"] = offset
			if (count != 0)
				params["count"] = count
			return request("groups.get", params, token)
		}

		open fun getById(ids: List<String>, fields: String? = null, token: String? = null): JsonItem? {
			return request("groups.getById", Options("group_ids" to ids.joinToString(","), "fields" to fields), token)
		}

		open fun getLongPollSettings(groupId: Int, token: String? = null): JsonItem? {
			return request("groups.getLongPollSettings", Options("group_id" to groupId), token)
		}

		open fun setLongPollSettings(groupId: Int, options: Options?, token: String? = null): JsonItem? {
			val options = options?: Options()
			options["group_id"] = groupId
			return request("groups.setLongPollSettings", options, token)
		}

		open fun getLongPollServer(groupId: Int = 0): JsonItem? {
			val options = Options()
			if (groupId != 0)
				options["group_id"] = groupId
			return request("groups.getLongPollServer", options)
		}

		open fun getUpdates(lpSettings: LongPollSettings, ts: String): JsonItem? {
			val response = connection.request(lpSettings.getUpdatesLink(ts))
			if (response == null || response.code != 200)
				return null
			return parser(response.responseText)
		}

		open fun getBanned(groupId: Int, token: String? = null): JsonItem? {
			return request("groups.getBanned", Options("group_id" to groupId), token)
		}

		open fun addCallbackServer(groupId: Int, url: String, title: String, secret: String): JsonItem? {
			return request("groups.addCallbackServer", Options("group_id" to groupId, "url" to url, "title" to title, "secret_key" to secret))
		}

		open fun deleteCallbackServer(groupId: Int, serverId: Int): JsonItem? {
			return request("groups.deleteCallbackServer", Options("group_id" to groupId, "server_id" to serverId))
		}

		open fun getCallbackConfirmationCode(groupId: Int): JsonItem? {
			return request("groups.getCallbackConfirmationCode", Options("group_id" to groupId))
		}

		open fun getCallbackSettings(groupId: Int): JsonItem? {
			return request("groups.getCallbackSettings", Options("group_id" to groupId))
		}

		open fun getMembers(groupId: Int, filter: String? = null, offset: String? = null, count: String? = null, token: String? = null): JsonItem? {
			val options = Options("group_id" to groupId)
			if (filter != null)
				options["filter"] = filter
			if (offset != null)
				options["offset"] = offset
			if (count != null)
				options["count"] = count

			return request("groups.getMembers", options, token)
		}

		open fun setCallbackSettings(groupId: Int, serverId: Int, options: Options? = null): JsonItem? {
			val params = Options("group_id" to groupId, "server_id" to serverId)
			if (options != null)
				params.putAll(options)
			return request("groups.setCallbackSettings", params)

		}

		open fun getCallbackServers(groupId: Int, serverIds: List<Int>? = null): JsonItem? {
			val options = Options("group_id" to groupId)
			if (serverIds != null)
				options["server_ids"] = serverIds.joinToString(",")
			return request("groups.getCallbackServers", options)
		}

		open fun isMember(idUsers: List<Int>, groupId: Int): JsonItem? {
			return request("groups.isMember", Options("user_ids" to idUsers.joinToString(","), "group_id" to groupId))
		}

		open fun isMember(idUser: Int, groupId: Int): JsonItem? {
			return request("groups.isMember", Options("user_id" to idUser, "group_id" to groupId))
		}
	}

	open inner class Wall {

		open fun get(ownerId: Int, offset: Int = 0, count: Int = 100): JsonItem? {
			return request("wall.get", Options("count" to count, "filter" to "all", "owner_id" to ownerId, "offset" to offset))
		}

		open fun delete(id: Int): JsonItem? {
			return request("wall.delete", Options("post_id" to id))
		}

		open fun deleteComment(ownerId: Int, commentId: Int, token: String? = null): JsonItem? {
			return request("wall.deleteComment", "owner_id=$ownerId&comment_id=$commentId", token)
		}

		open fun reportComment(ownerId: Int, commentId: Int, reason: Int = 0, token: String? = null): JsonItem? {
			return request("wall.reportComment", "owner_id=$ownerId&comment_id=$commentId&reason=$reason", token)
		}

		open fun post(ownerId: Int, message: String?, fromGroup: Boolean = false, options: Options? = null): JsonItem? {
			val params = options?: Options()
			params["owner_id"] = ownerId
			params["from_group"] = if (fromGroup) "1" else "0"
			if (message != null)
				params["message"] = message

			return request("wall.post", params)
		}

		open fun getComments(ownerId: Int, postId: Int, offset: Int = 0, count: Int = 100): JsonItem? {
			return request("wall.getComments", "owner_id=$ownerId&post_id=$postId&offset=$offset&count=$count")
		}

		open fun createComment(ownerId: Int, postId: Int, text: String?, options: Options? = null, token: String? = null): JsonItem? {
			val params = options?: Options()
			params["owner_id"] = ownerId
			params["post_id"] = postId
			if (text != null)
				params["text"] = text
			return request("wall.createComment", params, token)
		}

		open fun getReposts(ownerId: Int, postId: Int, offset: Int = 0, count: Int = 10, token: String? = null): JsonItem? {
			val options = Options("owner_id" to ownerId, "post_id" to postId)
			if (offset != 0)
				options["offset"] = offset
			if (count != 0)
				options["count"] = count
			return request("wall.getReposts", options, token)
		}
	}

	open inner class Users {
		open fun get(users: List<String>? = null, fields: String? = null, token: String? = null): JsonItem? {
			val options = Options()
			if (users != null && users.isNotEmpty())
				options["user_ids"] = users.joinToString(",")
			if (fields != null)
				options["fields"] = fields
			return request("users.get", options, token)
		}
	}

	open inner class Account {
		open fun ban(id: Int): JsonItem? {
			return request("account.ban", "owner_id=$id")
		}
	}

	open inner class Photos {

		open fun uploadMessagePhoto(photoPath: String, token: String? = null): JsonItem? {
			val uploadServerInfo = this.getMessagesUploadServer(token)
			if (uploadServerInfo == null || isError(uploadServerInfo)) {
				return uploadServerInfo
			}
			val response = connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("file" to photoPath)), null)?.responseText
				?: return null
			val responseImage = Options(parser(response).asMap())
			return saveMessagesPhotoByObject(responseImage, token)
		}

		open fun uploadMessagePhoto(data: ByteArray, type: String = "png", token: String? = null): JsonItem? {
			val uploadServerInfo = this.getMessagesUploadServer(token)
			if (uploadServerInfo == null || isError(uploadServerInfo)) {
				return uploadServerInfo
			}
			val response = connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("data" to data, "Content-Type" to "image/$type", "filename" to "item.$type")))?.responseText
				?: return null
			val responseImage = parser(response).asMap()
			return saveMessagesPhotoByObject(Options(responseImage), token)
		}

		open fun saveMessagesPhotoByObject(responseImage: Options, token: String? = null): JsonItem? {
			return request("photos.saveMessagesPhoto", "photo=" + responseImage["photo"] + "&server=" + responseImage["server"] + "&hash=" + responseImage["hash"], token)
		}

		open fun getMessagesUploadServer(token: String? = null): JsonItem? {
			return request("photos.getMessagesUploadServer", null as String?, token)
		}

		open fun uploadWallPhoto(photoPath: String, userId: Int? = null, groupId: Int? = null, token: String? = null): JsonItem? {
			val uploadServerInfo = this.getWallUploadServer(userId, groupId, token)
			if (uploadServerInfo == null || isError(uploadServerInfo)) {
				return uploadServerInfo
			}

			val response = connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("file" to photoPath)), null)?.responseText
				?: return null
			val responseImage = parser(response).asMap()
			return saveWallPhotoByObject(Options(responseImage), userId, groupId, token)
		}

		open fun uploadAlbumPhoto(photoPath: String, albumId: Int, groupId: Int? = null, caption: String? = null, options: Options? = null, token: String? = null): JsonItem? {
			val uploadServerInfo = getUploadServer(albumId, groupId, token) ?: return null
			if (isError(uploadServerInfo)) {
				return uploadServerInfo
			}

			val resText =
				connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("file" to photoPath)), null)?.responseText ?: return null
			val responseImage = parser(resText).asMap()
			return this.saveByObject(Options(responseImage), albumId, groupId, caption, options, token)
		}

		open fun getUploadServer(albumId: Int, groupId: Int? = null, token: String? = null): JsonItem? {
			val params = Options("album_id" to albumId)
			if (groupId != null)
				params["group_id"] = groupId
			return request("photos.getUploadServer", params, token)
		}

		open fun saveByObject(responseImage: Options, albumId: Int, groupId: Int? = null, caption: String? = null, options: Options? = null, token: String? = null): JsonItem? {
			val params = options ?: Options()
			params["album_id"] = albumId
			if (groupId != null)
				params["group_id"] = groupId
			if (caption != null)
				params["caption"] = caption
			params["photos_list"] = responseImage["photos_list"]
			params["server"] = responseImage["server"]
			params["hash"] = responseImage["hash"]

			return request("photos.save", params, token)
		}

		open fun getWallUploadServer(userId: Int? = null, groupId: Int? = null, token: String? = null): JsonItem? {
			val params = Options()
			if (userId != null)
				params["user_id"] = userId
			if (groupId != null)
				params["group_id"] = groupId
			return request("photos.getWallUploadServer", params, token)
		}

		open fun saveWallPhotoByObject(responseImage: Options, userId: Int? = null, groupId: Int? = null, token: String? = null): JsonItem? {
			return request(
				"photos.saveWallPhoto",
				"user_id=" + userId + "&group_id=" + groupId + "&photo=" + responseImage["photo"] + "&server=" + responseImage["server"] + "&hash=" + responseImage["hash"],
				token
			)
		}

		open fun copy(ownerId: Int, photoId: Int, accessKey: Int? = null, token: String? = null): JsonItem? {
			val params = Options("owner_id" to ownerId, "photo_id" to photoId)
			if (accessKey != null)
				params["access_key"] = accessKey
			return request("photos.copy", params, token)
		}

	}

	open inner class Docs {
		open fun upload(filePath: String, peerId: Int, type: String? = null, title: String? = null, tags: String? = null, token: String? = null): JsonItem? {

			val uploadServerInfo = this.getMessagesUploadServer(peerId, type, token) ?: return null
			if (isError(uploadServerInfo)) {
				return uploadServerInfo
			}

			val responseText =
				connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("file" to Options("file" to filePath)), null)?.responseText ?: return null
			val responseFile = parser(responseText).asMap()
			return save(responseFile["file"] as String, title, tags, token)
		}

		open fun getMessagesUploadServer(peerId: Int, type: String? = null, token: String? = null): JsonItem? {
			return request("docs.getMessagesUploadServer", "peer_id=" + peerId + (if (type != null) "&type=" + encode(type) else ""), token)
		}

		private fun save(file: String, title: String? = null, tags: String? = null, token: String? = null): JsonItem? {
			return request("docs.save", "file=" + encode(file), token)
		}

		open fun uploadWall(filePath: String, groupId: Int, title: String? = null, tags: String? = null, token: String? = null): JsonItem? {
			val uploadServerInfo = getWallUploadServer(groupId, token) ?: return null
			if (isError(uploadServerInfo)) {
				return uploadServerInfo
			}


			val responseText =
				connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("file" to Options("file" to filePath)), null)?.responseText ?: return null
			val responseFile = parser(responseText).asMap()
			return this.save(responseFile["file"] as String, title, tags, token)
		}

		open fun getWallUploadServer(groupId: Int, token: String? = null): JsonItem? {
			return request("docs.getWallUploadServer", "group_id=$groupId", token)
		}


		open fun add(ownerId: Int, docId: Int, accessKey: String? = null, token: String? = null): JsonItem? {
			val params = Options("owner_id" to ownerId, "doc_id" to docId)
			if (accessKey != null)
				params["access_key"] = accessKey
			return request("docs.add", params, token)
		}
	}

	//////////////////////////////////////

	private fun encode(o: String): String? {
		return URLEncoder.encode(o, StandardCharsets.UTF_8)
	}

	private fun encodeOptions(obj: Options): String? {
		val sb = StringBuilder()
		for (o in obj.entries) {
			sb.append(encode(o.key)).append('=')
				.append(encode(o.value.toString())).append("&")
		}
		return sb.toString()
	}

	open fun request(req: VkRequestData): JsonItem? {
		return request(req.method, req.options, req.token, req.version)
	}

	open fun request(method: String, options: Options?, token: String? = null, version: String? = null): JsonItem? {
		val optionsRes =
			(if (options != null)
				encodeOptions(options)
			else
				null)
		return request(method, optionsRes, token, version)
	}

	@Suppress("NAME_SHADOWING")
	open fun request(method: String, options: String?, token: String? = null, version: String? = null): JsonItem? {
		val token = token ?: this.token
		val version = version ?: this.version

		val sb = StringBuilder()
		if (options != null)
			sb.append(options)

		sb.append("&access_token=").append(token).append("&v=").append(version)
		val res = connection.request("https://api.vk.com/method/$method", sb.toString())?.responseText ?: return null
		return parser(res)
	}

	private fun parser(res: String): JsonItem {
		return JsonFlowParser.start(res)
	}

	open fun utilsGetShortLink(url: String, isPrivate: Boolean = false, token: String? = null): JsonItem? {
		val options = Options("url" to url, "private" to if (isPrivate) 1 else 0)
		return request("utils.getShortLink", options, token)
	}


	open fun execute(data: List<VkRequestData>, token: String? = null): List<JsonItem> {
		val codes = generateExecuteCode(data, token?: this.token, version)
		val response = mutableListOf<JsonItem>()
		for (i in codes) {
			val res = request(i)?: continue
			val data = prepareExecuteResponses(res)
			response.addAll(data)
		}
		return response
	}

	open class LongPollSettings(server: String, key: String, mode: String, wait: Int = 10) {

		private var link = "$server?act=a_check&key=$key&wait=$wait&mode=$mode"

		open fun getUpdatesLink(ts: String): String {
			return "$link&ts=$ts"
		}

		companion object {
			fun build(data: Options): LongPollSettings {
				return LongPollSettings(data.getString("server"), data.getString("key"), data.getString("mode"), data.getIntOrNull("wait")?: 10)
			}
		}

	}

	companion object {
		fun chat2PeerId(chatId: Int): Int {
			return 2000000000 + chatId
		}

		fun peer2ChatId(peerId: Int): Int {
			return max(peerId - 2000000000, 0)
		}

		fun isChat(peerId: Int): Boolean {
			return peerId >= 2000000000
		}

		fun isGroup(peerId: Int): Boolean {
			return peerId < 0
		}

		fun isUser(peerId: Int): Boolean {
			return peerId in 1..2000000000
		}

		fun group2PeerId(groupId: Int): Int {
			return -groupId
		}

		fun peer2GroupId(peerId: Int): Int {
			return -peerId
		}

		fun user2PeerId(id: Int): Int {
			return id
		}

		fun peerId2User(id: Int): Int {
			return id
		}

		fun isError(obj: JsonItem?): Boolean {
			return obj != null && !obj["error"].isNull()
		}

		fun errorString(obj: JsonItem?): String? {
			if (obj == null) return null
			val error = obj["error"].asMap()
			return "${error["error_msg"]} (${error["error_code"]})"
		}

		fun errorString(obj: Options?): String? {
			if (obj == null) return null
			val error = obj["error"] as Options? ?: return null
			return "${error["error_msg"]} (${error["error_code"]})"
		}

		fun prepareExecuteResponses(data: JsonItem): List<JsonItem> {
			if (data["response"].isNull()) return emptyList()
			var numError = 0
			val result = mutableListOf<JsonItem>()
			val executeErrors = if (data["execute_errors"].isNotNull()) data["execute_errors"] as List<JsonItem> else emptyList()
			for (i in data["response"].iterable()) {
				if (i.isPrimitive() && i.asBooleanOrNull() == false) {
					val errorInfo = executeErrors[numError]
					result.add(IrisJsonObject("error" to errorInfo))
					numError++
				} else if (i.isArray()) {
					val items = i.asList()
					result.add(IrisJsonObject("response" to JsonProxyObject("count" to items.size, "items" to items)))
				} else {
					result.add(IrisJsonObject("response" to i))
				}
			}
			return result
		}

		fun generateExecuteCode(data: List<VkRequestData>, token: String, version: String): List<VkRequestData> {
			val sb = StringBuilder()
			val res = mutableListOf<VkRequestData>()

			for (i in data.indices) {
				val item = data[i]
				sb.append("API.").append(item.method).append('('); JsonEncoder.encode(item.options, sb); sb.append("),")
				if (i != 0 && i % 24 == 0) {
					val str = "return [" + sb.substring(0, sb.length-1) + "];"
					res.add(VkRequestData("execute", Options("code" to str), token, version))
				}
			}

			if (sb.isNotEmpty()) {
				val str = "return [" + sb.substring(0, sb.length-1) + "];"
				res.add(VkRequestData("execute", Options("code" to str), token, version))
			}
			return res
		}
	}
}

class VkRequestData(val method: String, val options: Options? = null, val token: String? = null, val version: String? = null)