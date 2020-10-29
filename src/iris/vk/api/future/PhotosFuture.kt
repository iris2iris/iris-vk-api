package iris.vk.api.future

import iris.json.plain.IrisJsonParser
import iris.vk.Options
import iris.vk.api.VkApis.isError
import iris.vk.api.common.Photos
import iris.vk.api.future.VkApiFuture.VkFuture
import iris.vk.api.future.VkApiFuture.VkFutureList

/**
 * @created 29.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class PhotosFuture(override val api: VkApiFuture) : Photos<VkFuture, VkFutureList>(api) {

	override fun uploadWallPhoto(data: ByteArray, userId: Int?, groupId: Int?, type: String, token: String?): VkFuture {
		val future = VkFuture()
		val t = getWallUploadServer(userId, groupId, token).thenApply {uploadServerInfo ->
			if (uploadServerInfo == null || isError(uploadServerInfo)) {
				return@thenApply uploadServerInfo
			}

			val resText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("data" to data, "Content-Type" to "image/$type", "filename" to "item.$type"))).get()?.responseText
					?: return@thenApply null
			val responseImage = api.parser(resText).asMap()
			saveWallPhotoByObject(Options(responseImage), userId, groupId, token).get()
		}.whenComplete { it, err ->
			future.complete(it)
		}
		return future
	}

	override fun uploadWallPhoto(photoPath: String, userId: Int?, groupId: Int?, token: String?): VkFuture {
		val future = VkFuture()
		val t = getWallUploadServer(userId, groupId, token).thenApply {uploadServerInfo ->
			if (uploadServerInfo == null || isError(uploadServerInfo)) {
				return@thenApply uploadServerInfo
			}

			val resText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("file" to photoPath))).get()?.responseText
					?: return@thenApply null
			val responseImage = api.parser(resText).asMap()
			saveWallPhotoByObject(Options(responseImage), userId, groupId, token).get()
		}.whenComplete { it, err ->
			future.complete(it)
		}
		return future
	}

	override fun uploadAlbumPhoto(photoPath: String, albumId: Int, groupId: Int?, caption: String?, options: Options?, token: String?): VkFuture {
		val future = VkFuture()
		val t = getUploadServer(albumId, groupId, token).thenApply {uploadServerInfo ->
			if (uploadServerInfo == null || isError(uploadServerInfo)) {
				return@thenApply uploadServerInfo
			}

			val resText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("file" to photoPath))).get()?.responseText
					?: return@thenApply null
			val responseImage = api.parser(resText).asMap()
			saveByObject(Options(responseImage), albumId, groupId, caption, options, token).get()
		}.whenComplete { it, err ->
			future.complete(it)
		}
		return future
	}

	override fun uploadAlbumPhoto(data: ByteArray, albumId: Int, groupId: Int?, type: String, caption: String?, options: Options?, token: String?): VkFuture {
		val future = VkFuture()
		val t = getUploadServer(albumId, groupId, token).thenApply {uploadServerInfo ->
			if (uploadServerInfo == null || isError(uploadServerInfo)) {
				return@thenApply uploadServerInfo
			}

			val response = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("data" to data, "Content-Type" to "image/$type", "filename" to "item.$type"))).get()?.responseText
					?: return@thenApply null
			val responseImage = api.parser(response).asMap()
			saveByObject(Options(responseImage), albumId, groupId, caption, options, token).get()
		}.whenComplete { it, err ->
			future.complete(it)
		}
		return future
	}

	override fun uploadMessagePhoto(photoPath: String, peerId: Int, token: String?): VkFuture {
		val future = VkFuture()
		val t = getMessagesUploadServer(peerId, token).thenApply { uploadServerInfo ->
			if (uploadServerInfo == null || isError(uploadServerInfo))
				return@thenApply uploadServerInfo
			val responseText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("file" to photoPath))).get()
			val response = responseText?.responseText ?: return@thenApply null
			val responseImage = IrisJsonParser(response).parse().asMap()
			saveMessagesPhotoByObject(Options(responseImage), token).get()
		}.whenComplete {t, e ->
			future.complete(t)
		}
		return future
	}

	override fun uploadMessagePhoto(data: ByteArray, peerId: Int, type: String, token: String?): VkFuture {
		val future = VkFuture()
		val t = getMessagesUploadServer(peerId, token).thenApply { uploadServerInfo ->
			if (uploadServerInfo == null || isError(uploadServerInfo))
				return@thenApply uploadServerInfo
			val responseText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("data" to data, "Content-Type" to "image/$type", "filename" to "item.$type"))).get()
			val response = responseText?.responseText ?: return@thenApply null
			val responseImage = IrisJsonParser(response).parse().asMap()
			saveMessagesPhotoByObject(Options(responseImage), token).get()
		}.whenComplete {t, e ->
			future.complete(t)
		}
		return future
	}
}