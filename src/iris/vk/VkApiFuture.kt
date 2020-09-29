@file:Suppress("unused")

package iris.vk

import iris.json.JsonEncoder
import iris.json.JsonItem
import iris.json.flow.JsonFlowParser
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.math.max

/**
 * @created 25.10.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class VkApiFuture(val token: String, val version: String = VK_API_VERSION, connection: VkApiConnectionFuture? = null) {

	val connect = connection ?: VkApiConnectionFutureHttpClient(VkApiConnectionFutureHttpClient.newClient())

	open val messages = Messages()
	open val groups = Groups()
	open val users = Users()
	open val photos = Photos()
	open val docs = Docs()
	open val utils = Utils()
	open val board = Board()

	open inner class Messages {

		open fun sendPm(userId: Int, message: String, options: Options? = null, token: String? = null): VkFuture {
			return this.send(userId, message, options, token)
		}

		open fun send(peerId: Int, message: String, options: Options? = null, token: String? = null): VkFuture {
			val params = options ?: Options()
			params["peer_id"] = peerId
			params["random_id"] = (0..2000000000).random()
			params["message"] = message

			return request("messages.send", params, token)
		}

		open fun getByConversationMessageId(peerId: Int, conversationMessageIds: Collection<Int>, token: String? = null): VkFuture {
			val ids = conversationMessageIds.joinToString(",")
			val options = Options("peer_id" to peerId, "conversation_message_ids" to ids)
			return request("messages.getByConversationMessageId", options, token)
		}

		open fun getByConversationMessageId_ChatId(chatId: Int, conversationMessageIds: Collection<Int>, token: String? = null): VkFuture {
			return getByConversationMessageId(chat2PeerId(chatId), conversationMessageIds, token)
		}

		open fun chatMessage(chatId: Int, message: String, options: Options? = null, token: String? = null): VkFuture {
			return send(chat2PeerId(chatId), message, options, token)
		}

		open fun chatMessages(data: List<Options>, token: String? = null): VkFutureList {
			val res = mutableListOf<VkRequestData>()
			for (it in data) {
				res.add(VkRequestData("messages.send", it))
			}
			return execute(res, token)
		}

		open fun setChatTitle(chatId: Int, title: String): VkFuture {
			return editChat(chatId, title)
		}

		open fun editChat(chatId: Int, title: String, token: String? = null, version: String? = null): VkFuture {
			return request("messages.editChat", Options("chat_id" to chatId, "title" to title), token, version)
		}

		open fun messagesAddChatUser(userId: Int, chatId: Int, token: String? = null): VkFuture {
			return request("messages.addChatUser", Options("chat_id" to chatId, "user_id" to userId), token)
		}

		open fun getConversations(amount: Int = 200, filter: String? = "all", token: String? = null): VkFuture {
			return request("messages.getConversations", Options("count" to amount, "filter" to filter), token)
		}

		open fun getConversationsById(peerIds: List<Int>, fields: String? = null, token: String? = null): VkFuture {
			val options = Options("peer_ids" to peerIds.joinToString(","))
			if (fields != null) {
				options["extended"] = 1
				options["fields"] = fields
			}
			return request("messages.getConversationsById", options, token)
		}

		open fun conversationDeleteChat(chatId: Int, amount: Int = 10000, token: String? = null): VkFuture {
			return deleteConversation(chat2PeerId(chatId), amount, token)
		}

		open fun deleteConversation(peerId: Int, amount: Int = 10000, token: String? = null): VkFuture {
			return request("messages.deleteConversation", Options("peer_id" to peerId, "count" to amount), token)
		}

		open fun conversationDeleteUser(id: Int, amount: Int = 10000, token: String? = null): VkFuture {
			return deleteConversation(user2PeerId(id), amount, token)
		}

		open fun conversationDeleteGroup(id: Int, amount: Int = 10000, token: String? = null): VkFuture {
			return deleteConversation(group2PeerId(id), amount, token)
		}


		open fun getHistoryChat(chatId: Int, offset: Int = 0, count: Int = 100, options: Options? = null, token: String? = null): VkFuture {
			return getHistory(chat2PeerId(chatId), offset, count, options, token)
		}

		open fun getHistoryUser(id: Int, offset: Int = 0, count: Int = 100, options: Options? = null, token: String? = null): VkFuture {
			return getHistory(user2PeerId(id), offset, count, options, token)
		}

		open fun getHistoryGroup(id: Int, offset: Int = 0, count: Int = 100, options: Options? = null, token: String? = null): VkFuture {
			return getHistory(group2PeerId(id), offset, count, options, token)
		}

		open fun getHistory(peerId: Int, offset: Int = 0, count: Int = 100, options: Options? = null, token: String? = null): VkFuture {
			val options = options?: Options()
			options["peer_id"] = peerId
			if (offset != 0)
				options["offset"] = offset
			if (count != 0)
				options["count"] = count


			return request("messages.getHistory", options, token)
		}

		open fun markAsRead(peerId: Int): VkFuture {
			return request("messages.markAsRead", Options("peer_id" to peerId))
		}

		open fun removeChatUser(chatId: Int, memberId: Int, token: String? = null): VkFuture {
			return request("messages.removeChatUser", Options("chat_id" to chatId, "member_id" to memberId), token)
		}

		open fun removeChatUserList(chatId: Int, userIds: Collection<Int>, token: String? = null): VkFutureList {
			if (userIds.isEmpty()) return VkFutureList(emptyList())
			val requests = mutableListOf<VkRequestData>()
			for (d in userIds) {
				val params = Options("chat_id" to chatId, "member_id" to d)
				requests.add(VkRequestData("messages.removeChatUser", params, token))
			}

			return execute(requests, token)
		}

		open fun removeChatUserData(data: List<Options>, token: String? = null): VkFutureList {
			if (data.isEmpty()) return VkFutureList(emptyList())
			val requests = mutableListOf<VkRequestData>()
			for (d in data) {
				requests.add(VkRequestData("messages.removeChatUser", d, token))
			}
			return execute(requests, token)
		}

		open fun edit(peerId: Int, message: String?, messageId: Int, options: Options? = null, token: String? = null): VkFuture {
			val params = Options("peer_id" to peerId, "message_id" to messageId)
			if (options != null)
				params.putAll(options)
			if (message != null)
				params["message"] = message
			return request("messages.edit", params, token)
		}

		open fun editByLocalId(peerId: Int, message: String?, localId: Int, options: Options? = null, token: String? = null): VkFuture {
			val params = Options("peer_id" to peerId, "conversation_message_id" to localId)
			if (options != null)
				params.putAll(options)
			if (message != null)
				params["message"] = message
			return request("messages.edit", params, token)
		}

		open fun createChat(ids: List<Long>, name: String? = null): Int {
			val res = request("messages.createChat", "user_ids=" + ids.joinToString(",")).get()?: return 0
			if (res["response"].isNull()) return 0
			val id = res["response"].asInt()
			if (name != null)
				editChat(id, name)
			return id
		}

		open fun getInviteLink(chatId: Int): String? {
			return (request("messages.getInviteLink", "peer_id=" + chat2PeerId(chatId)).get()?: return null)["response"]["link"].asStringOrNull()
		}

		open fun restore(messageId: Int, token: String? = null): VkFuture {
			val options = Options("message_id" to messageId)
			return request("messages.restore", options, token)
		}

		open fun getById(messageIds: List<Int>): VkFuture {
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

		open fun getConversationMembers(peerId: Int, fields: String? = null, token: String? = null): VkFuture {
			return request("messages.getConversationMembers", "peer_id=" + peerId + (if (fields != null) "&fields=" + encode(fields) else ""), token)
		}

		open fun getChatMembers(chatId: Int, fields: String? = null, token: String? = null): VkFuture {
			return request("messages.getConversationMembers", "peer_id=" + chat2PeerId(chatId) + (if (fields != null) "&fields=" + encode(fields) else ""), token)
		}

		open fun pin(peerId: Int, messageId: Int): VkFuture {
			return request("messages.pin", "peer_id=" + peerId + "&message_id=" + messageId)
		}

		open fun search(q: String, peerId: Int? = 0, count: Int = 10, options: Options? = null, token: String? = null): VkFuture {
			val options = options?: Options()
			options["q"] = q
			if (peerId != 0)
				options["peer_id"] = peerId
			if (count != 0)
				options["count"] = count
			return request("messages.search", options, token)
		}

		open fun delete(messageIds: List<Int>, deleteForAll: Boolean = false, isSpam: Boolean = false, token: String? = null): VkFuture {
			val options = Options("message_ids" to messageIds.joinToString(","))
			if (deleteForAll)
				options["delete_for_all"] = deleteForAll
			if (isSpam)
				options["spam"] = 1

			return request("messages.delete", options, token)
		}

		open fun getLongPollServer(): VkFuture {
			return request("messages.getLongPollServer", null as String?)
		}

		open fun getUpdates(lpSettings: LongPollSettings, ts: String, wait: Int = 10): JsonItem? {
			val server = lpSettings.server
			val key = lpSettings.key
			val modeRes = lpSettings.mode
			val response = connect.request("https://$server?act=a_check&key=$key&ts=$ts&wait=$wait&mode=$modeRes").get()
			if (response == null || response.code != 200)
				return null;
			return parser(response.responseText)
		}

		open fun sendMulti(data: Collection<Options>): VkFutureList {
			val res = mutableListOf<VkRequestData>()
			for (it in data) {
				it.getOrPut("random_id") { (0..Integer.MAX_VALUE).random() }
				res.add(VkRequestData("messages.send", it))
			}
			return execute(res, token)
		}

		open fun sendMessageEventAnswer(eventId: String, userId: Int, peerId: Int, eventData: Options? = null, token: String? = null): VkFuture {
			return request("messages.sendMessageEventAnswer", Options(
					"event_id" to eventId
					, "user_id" to userId
					, "peer_id" to peerId
					, "event_data" to if (eventData != null) JsonEncoder.encode(eventData) else null
				), token
			)
		}
	}

	open inner class Friends {
		open fun add(userId: Int): VkFuture {
			return request("friends.add", Options("user_id" to userId))
		}

		open fun getRequests(out: Int = 0, count: Int = 100, token: String? = null): VkFuture {
			return request("friends.getRequests", Options("need_viewed" to 1, "count" to count, "out" to out), token)
		}

		open fun delete(id: Int, token: String? = null): VkFuture {
			return request("friends.delete", Options("user_id" to id), token)
		}

		open fun get(amount: Int = 1000, token: String? = null): Any? {
			return request("friends.get", Options("count" to amount), token)
		}

		open fun delete(userId: Int): VkFuture {
			return request("friends.delete", "user_id=" + userId)
		}
	}

	open inner class Groups {
		open fun leave(groupId: Int): VkFuture {
			return request("groups.leave", Options("group_id" to groupId))
		}

		open fun get(userId: Int? = null, extended: Boolean = false, filter: String? = null, fields: String? = null, offset: Int = 0, count: Int = 0, token: String? = null): VkFuture {
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

		open fun getById(ids: List<String>, fields: String? = null, token: String? = null): VkFuture {
			return request("groups.getById", Options("group_ids" to ids.joinToString(","), "fields" to fields), token)
		}

		open fun getLongPollSettings(groupId: Int, token: String? = null): VkFuture {
			return request("groups.getLongPollSettings", Options("group_id" to groupId), token)
		}

		open fun setLongPollSettings(groupId: Int, options: Options?, token: String? = null): VkFuture {
			val options = options?: Options()
			options["group_id"] = groupId
			return request("groups.setLongPollSettings", options, token)
		}

		open fun getLongPollServer(groupId: Int): VkFuture {
			return request("groups.getLongPollServer", Options("group_id" to groupId))
		}

		open fun getUpdates(lpSettings: LongPollSettings, ts: String, wait: Int = 10): JsonItem? {
			val server = lpSettings.server
			val key = lpSettings.key
			val modeRes = lpSettings.mode
			val response = connect.request("$server?act=a_check&key=$key&ts=$ts&wait=$wait&mode=$modeRes").get()
			if (response == null || response.code != 200)
				return null
			return parser(response.responseText)
		}

		open fun getBanned(groupId: Int, token: String? = null): VkFuture {
			return request("groups.getBanned", Options("group_id" to groupId), token)
		}

		open fun addCallbackServer(groupId: Int, url: String, title: String, secret: String): VkFuture {
			return request("groups.addCallbackServer", Options("group_id" to groupId, "url" to url, "title" to title, "secret_key" to secret))
		}

		open fun deleteCallbackServer(groupId: Int, serverId: Int): VkFuture {
			return request("groups.deleteCallbackServer", Options("group_id" to groupId, "server_id" to serverId))
		}

		open fun getCallbackConfirmationCode(groupId: Int): VkFuture {
			return request("groups.getCallbackConfirmationCode", Options("group_id" to groupId))
		}

		open fun getMembers(groupId: Int, filter: String? = null, offset: String? = null, count: String? = null, token: String? = null): VkFuture {
			val options = Options("group_id" to groupId)
			if (filter != null)
				options["filter"] = filter
			if (offset != null)
				options["offset"] = offset
			if (count != null)
				options["count"] = count

			return request("groups.getMembers", options, token)
		}

		open fun setCallbackSettings(groupId: Int, serverId: Int, options: Options? = null): VkFuture {
			//val params = stdClass(options.map);
			val params = Options("group_id" to groupId, "server_id" to serverId)
			if (options != null)
				params.putAll(options)
			return request("groups.setCallbackSettings", params)

		}

		open fun getCallbackServers(groupId: Int, serverIds: List<Int>? = null, token: String? = null): VkFuture {
			val options = Options("group_id" to groupId)
			if (serverIds != null)
				options["server_ids"] = serverIds.joinToString(",")
			return request("groups.getCallbackServers", options, token)
		}

		open fun isMember(idUsers: List<Int>, groupId: Int): VkFuture {
			return request("groups.isMember", Options("user_ids" to idUsers.joinToString(","), "group_id" to groupId))
		}

		open fun isMember(idUser: Int, groupId: Int): VkFuture {
			return request("groups.isMember", Options("user_id" to idUser, "group_id" to groupId))
		}
	}

	open inner class Wall {

		open fun get(ownerId: Int, offset: Int = 0, count: Int = 100): VkFuture {
			return request("wall.get", Options("count" to count, "filter" to "all", "owner_id" to ownerId, "offset" to offset))
		}

		open fun delete(id: Int): VkFuture {
			return request("wall.delete", Options("post_id" to id))
		}

		open fun deleteComment(ownerId: Int, commentId: Int, token: String? = null): VkFuture {
			return request("wall.deleteComment", "owner_id=$ownerId&comment_id=$commentId", token)
		}

		open fun reportComment(ownerId: Int, commentId: Int, reason: Int = 0, token: String? = null): VkFuture {
			return request("wall.reportComment", "owner_id=$ownerId&comment_id=$commentId&reason=$reason", token)
		}

		open fun post(ownerId: Int, message: String?, fromGroup: Boolean = false, options: Options? = null): VkFuture {
			val params = options?: Options()
			params["owner_id"] = ownerId
			params["from_group"] = if (fromGroup) "1" else "0"
			if (message != null)
				params["message"] = message

			return request("wall.post", params)
		}

		open fun getComments(ownerId: Int, postId: Int, offset: Int = 0, count: Int = 100): VkFuture {
			return request("wall.getComments", "owner_id=$ownerId&post_id=$postId&offset=$offset&count=$count")
		}

		open fun createComment(ownerId: Int, postId: Int, text: String?, options: Options? = null, token: String? = null): VkFuture {
			val params = Options()
			if (options != null)
				params.putAll(options)
			params["owner_id"] = ownerId
			params["post_id"] =postId
			if (text != null)
				params["text"] = text
			return request("wall.createComment", params, token)
		}

		open fun getReposts(ownerId: Int, postId: Int, offset: Int = 0, count: Int = 10, token: String? = null): VkFuture {
			val options = Options("owner_id" to ownerId, "post_id" to postId)
			if (offset != 0)
				options["offset"] = offset
			if (count != 0)
				options["count"] = count
			return request("wall.getReposts", options, token)
		}
	}

	open inner class Users {
		open fun get(users: List<String>? = null, fields: String? = null, token: String? = null): VkFuture {
			val options = Options()
			if (users != null && users.isNotEmpty())
				options["user_ids"] = users.joinToString(",")
			if (fields != null)
				options["fields"] = fields
			return request("users.get", options, token)
		}
	}

	open inner class Account {
		open fun ban(id: Int): VkFuture {
			return request("account.ban", "owner_id=" + id)
		}
	}

	open inner class Photos {

		open fun uploadMessagePhoto(photoPath: String, peerId: Int = 0, token: String? = null): JsonItem? {
			val uploadServerInfoF = this.getMessagesUploadServer(peerId, token)
			val uploadServerInfo = uploadServerInfoF.get()
			if (uploadServerInfo == null || isError(uploadServerInfo)) {
				return uploadServerInfo
			}
			val response = connect.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("file" to photoPath))).get()?.responseText
				?: return null
			val responseImage = parser(response).asMap()
			val res = saveMessagesPhotoByObject(Options(responseImage), token)
			return res.get()
		}

		open fun uploadMessagePhoto(data: ByteArray, peerId: Int = 0, type: String = "png", token: String? = null): JsonItem? {
			val uploadServerInfoF = this.getMessagesUploadServer(peerId, token)
			val uploadServerInfo = uploadServerInfoF.get()
			if (uploadServerInfo == null || isError(uploadServerInfo)) {
				return uploadServerInfo
			}
			val response = connect.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("data" to data, "Content-Type" to "image/" + type, "filename" to "item." + type))).get()?.responseText
				?: return null
			val responseImage = parser(response).asMap()
			return saveMessagesPhotoByObject(Options(responseImage), token).get()
		}

		open fun saveMessagesPhotoByObject(responseImage: Options, token: String? = null): VkFuture {
			return request("photos.saveMessagesPhoto", "photo=" + responseImage["photo"] + "&server=" + responseImage["server"] + "&hash=" + responseImage["hash"], token)
		}

		open fun getMessagesUploadServer(peerId: Int = 0, token: String? = null): VkFuture {
			return request("photos.getMessagesUploadServer", if (peerId != 0 ) Options("peer_id" to peerId) else null, token)
		}

		open fun uploadWallPhoto(photoPath: String, userId: Int? = null, groupId: Int? = null, token: String? = null): JsonItem? {
			val uploadServerInfoF = this.getWallUploadServer(userId, groupId, token)
			val uploadServerInfo = uploadServerInfoF.get()
			if (uploadServerInfo == null || isError(uploadServerInfo)) {
				return uploadServerInfo
			}

			val response = connect.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("file" to photoPath))).get()?.responseText
				?: return null
			val responseImage = parser(response).asMap()
			return saveWallPhotoByObject(Options(responseImage), userId, groupId, token).get()
		}

		open fun uploadAlbumPhoto(photoPath: String, albumId: Int, groupId: Int? = null, caption: String? = null, options: Options? = null, token: String? = null): JsonItem? {
			val uploadServerInfoF = getUploadServer(albumId, groupId, token)
			val uploadServerInfo = uploadServerInfoF.get()?: return null
			if (isError(uploadServerInfo)) {
				return uploadServerInfo
			}

			val resText = connect.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("file" to photoPath))).get()?.responseText
				?: return null
			val responseImage = parser(resText).asMap()
			return this.saveByObject(Options(responseImage), albumId, groupId, caption, options, token).get()
		}

		open fun uploadAlbumPhoto(data: ByteArray, albumId: Int, type: String = "jpg", groupId: Int? = null, caption: String? = null, options: Options? = null, token: String? = null): JsonItem? {
			val uploadServerInfoF = getUploadServer(albumId, groupId, token)
			val uploadServerInfo = uploadServerInfoF.get()?: return null
			if (isError(uploadServerInfo)) {
				return uploadServerInfo
			}

			val response = connect.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("data" to data, "Content-Type" to "image/" + type, "filename" to "item." + type))).get()?.responseText
				?: return null
			val responseImage = parser(response).asMap()
			return this.saveByObject(Options(responseImage), albumId, groupId, caption, options, token).get()
		}

		open fun getUploadServer(albumId: Int, groupId: Int? = null, token: String? = null): VkFuture {
			val params = Options("album_id" to albumId)
			if (groupId != null)
				params["group_id"] = groupId
			return request("photos.getUploadServer", params, token)
		}

		open fun saveByObject(responseImage: Options, albumId: Int, groupId: Int? = null, caption: String? = null, options: Options? = null, token: String? = null): VkFuture {
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

		open fun getWallUploadServer(userId: Int? = null, groupId: Int? = null, token: String? = null): VkFuture {
			val params = Options()
			if (userId != null)
				params["user_id"] = userId
			if (groupId != null)
				params["group_id"] = groupId
			return request("photos.getWallUploadServer", params, token)
		}

		open fun saveWallPhotoByObject(responseImage: Options, userId: Int? = null, groupId: Int? = null, token: String? = null): VkFuture {
			return request(
				"photos.saveWallPhoto",
				"user_id=" + userId + "&group_id=" + groupId + "&photo=" + responseImage["photo"] + "&server=" + responseImage["server"] + "&hash=" + responseImage["hash"],
				token
			)
		}

		open fun copy(ownerId: Int, photoId: Int, accessKey: Int? = null, token: String? = null): VkFuture {
			val params = Options("owner_id" to ownerId, "photo_id" to photoId)
			if (accessKey != null)
				params["access_key"] = accessKey
			return request("photos.copy", params, token)
		}

	}

	open inner class Docs {
		open fun upload(filePath: String, peerId: Int, type: String? = null, title: String? = null, tags: String? = null, token: String? = null): JsonItem? {

			val uploadServerInfoF = this.getMessagesUploadServer(peerId, type, token)
			val uploadServerInfo = uploadServerInfoF.get()?: return null
			if (isError(uploadServerInfo)) {
				return uploadServerInfo
			}


			val responseText =
				connect.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("file" to Options("file" to filePath))).get()?.responseText ?: return null
			val responseFile = parser(responseText).asMap()
			return save(responseFile["file"] as String, title, tags, token).get()
		}

		open fun getMessagesUploadServer(peerId: Int, type: String? = null, token: String? = null): VkFuture {
			return request("docs.getMessagesUploadServer", "peer_id=" + peerId + (if (type != null) "&type=" + encode(type) else ""), token)
		}

		private fun save(file: String, title: String? = null, tags: String? = null, token: String? = null): VkFuture {
			return request("docs.save", "file=" + encode(file), token)
		}

		open fun uploadWall(filePath: String, groupId: Int, title: String? = null, tags: String? = null, token: String? = null): JsonItem? {
			val uploadServerInfoF = getWallUploadServer(groupId, token)
			val uploadServerInfo = uploadServerInfoF.get() ?: return null
			if (isError(uploadServerInfo)) {
				return uploadServerInfo
			}


			val responseText =
				connect.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("file" to Options("file" to filePath))).get()?.responseText ?: return null
			val responseFile = parser(responseText).asMap()
			return this.save(responseFile["file"] as String, title, tags, token).get()
		}

		open fun getWallUploadServer(groupId: Int, token: String? = null): VkFuture {
			return request("docs.getWallUploadServer", "group_id=" + groupId, token)
		}


		open fun add(ownerId: Int, docId: Int, accessKey: String? = null, token: String? = null): VkFuture {
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

	private fun encodeOptions(obj: Options?): String? {
		val sb = StringBuilder()
		if (obj != null)
			for (o in obj.entries) {
				sb.append(encode(o.key)).append('=')
					.append(encode(o.value.toString())).append("&")
			}
		return sb.toString()
	}

	open fun request(req: VkRequestData): VkFuture {
		return request(req.method, req.options, req.token, req.version)
	}

	open fun request(method: String, options: Options?, token: String? = null, version: String? = null): VkFuture {
		val optionsRes =
			(if (options != null)
				encodeOptions(options)
			else
				null)
		return request(method, optionsRes, token, version)
	}

	open fun request(method: String, options: String?, token: String? = null, version: String? = null): VkFuture {
		val token = token ?: this.token
		val version = version ?: this.version

		val sb = StringBuilder()
		if (options != null)
			sb.append(options)

		sb.append("&access_token=").append(token).append("&v=").append(version)
		val res = connect.request("https://api.vk.com/method/$method", sb.toString()).get()?.responseText ?: return VkFuture.empty
		val ret = parser(res)
		val future = VkFuture()
		future.complete(ret)
		return future
	}

	open fun utilsGetShortLink(url: String, isPrivate: Boolean = false, token: String? = null): VkFuture {
		val options = Options("url" to url, "private" to if (isPrivate) 1 else 0)
		return request("utils.getShortLink", options, token)
	}


	open fun execute(data: List<VkRequestData>, token: String? = null): VkFutureList {
		val codes = VkApi.generateExecuteCode(data, token?: this.token, version)
		val futures = mutableListOf<VkFuture>()
		for (i in codes)
			futures += request(i)

		return VkExecuteFuture(futures)
	}

	class LongPollSettings(var server: String, var key: String, var mode: String) {
		companion object {
			fun build(data: Options): LongPollSettings {
				return LongPollSettings(data.getString("server"), data.getString("key"), data.getString("mode"))
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

		fun isError(obj: Options?): Boolean {
			return obj != null && obj["error"] != null
		}

		fun isError(obj: JsonItem?): Boolean {
			return obj != null && !obj["error"].isNull()
		}
	}

	inner class VkExecuteFuture(futures: Collection<VkFuture>) : VkFutureList(futures) {
		override fun join(): List<JsonItem?> {
			val data = super.join()
			val response = mutableListOf<JsonItem?>()
			for (res in data) {
				if (res == null) continue
				val data = VkApi.prepareExecuteResponses(res)
				response.addAll(data)
			}
			return response
		}
	}

	open inner class Utils {
		open fun checkLink(url: String): VkFuture {
			return request("utils.checkLink", Options("url" to url))
		}

		open fun checkLinkList(urls: List<String>): VkFutureList {
			val futs = mutableListOf<VkFuture>()
			for (url in urls)
				futs.add(checkLink(url))
			return VkFutureList(futs)
		}
	}

	open inner class Board {
		open fun getComments(groupId: Int, topicId: Int, startCommentId: Int, options: Options? = null, token: String? = null): VkFuture {
			val options = options?: Options()
			options["group_id"] = groupId
			options["topic_id"] = topicId
			options["start_comment_id"] = startCommentId
			if (!options.containsKey("count"))
				options["count"] = 1
			return request("board.getComments", options, token)
		}
	}

	private fun parser(res: String): JsonItem {
		return JsonFlowParser.start(res)
	}

	class VkFuture(val request: VkRequestData? = null) : CompletableFuture<JsonItem?>() {
		companion object {
			val empty = VkFuture().also { it.complete(null) }
		}
	}

	open class VkFutureList(val futures: Collection<VkFuture>) {

		open fun join(): List<JsonItem?> {
			val data = LinkedList<JsonItem?>()
			for (l in futures)
				data.add(l.get())
			return data
		}
	}
}

