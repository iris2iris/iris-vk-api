package iris.vk.api.simple

import iris.json.JsonItem
import iris.vk.Options
import iris.vk.api.VkApis.isError
import iris.vk.api.common.Photos

/**
 * @created 29.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class PhotosSimple(override val api: VkApi) : Photos<JsonItem?, List<JsonItem?>>(api) {

	override fun uploadWallPhoto(data: ByteArray, userId: Int?, groupId: Int?, type: String, token: String?): JsonItem? {
		val uploadServerInfo = getWallUploadServer(userId, groupId, token)
		if (uploadServerInfo == null || isError(uploadServerInfo)) {
			return uploadServerInfo
		}

		val resText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("data" to data, "Content-Type" to "image/$type", "filename" to "item.$type")))?.responseText
				?: return null
		val responseImage = api.parser(resText).asMap()
		return saveWallPhotoByObject(Options(responseImage), userId, groupId, token)
	}

	override fun uploadWallPhoto(photoPath: String, userId: Int?, groupId: Int?, token: String?): JsonItem? {
		val uploadServerInfo = getWallUploadServer(userId, groupId, token)
		if (uploadServerInfo == null || isError(uploadServerInfo)) {
			return uploadServerInfo
		}

		val resText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("file" to photoPath)))?.responseText
				?: return null
		val responseImage = api.parser(resText).asMap()
		return saveWallPhotoByObject(Options(responseImage), userId, groupId, token)
	}

	override fun uploadAlbumPhoto(photoPath: String, albumId: Int, groupId: Int?, caption: String?, options: Options?, token: String?): JsonItem? {
		val uploadServerInfo = getUploadServer(albumId, groupId, token)
		if (uploadServerInfo == null || isError(uploadServerInfo)) {
			return uploadServerInfo
		}

		val resText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("file" to photoPath)))?.responseText
				?: return null
		val responseImage = api.parser(resText).asMap()
		return saveByObject(Options(responseImage), albumId, groupId, caption, options, token)

	}

	override fun uploadAlbumPhoto(data: ByteArray, albumId: Int, groupId: Int?, type: String, caption: String?, options: Options?, token: String?): JsonItem? {

		val uploadServerInfo = getUploadServer(albumId, groupId, token)
		if (uploadServerInfo == null || isError(uploadServerInfo)) {
			return uploadServerInfo
		}

		val resText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("data" to data, "Content-Type" to "image/$type", "filename" to "item.$type")))?.responseText
				?: return null
		val responseImage = api.parser(resText).asMap()
		return saveByObject(Options(responseImage), albumId, groupId, caption, options, token)
	}

	override fun uploadMessagePhoto(photoPath: String, peerId: Int, token: String?): JsonItem? {
		val uploadServerInfo = getMessagesUploadServer(peerId, token)
		if (uploadServerInfo == null || isError(uploadServerInfo)) {
			return uploadServerInfo
		}

		val resText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("file" to photoPath)))?.responseText
				?: return null
		val responseImage = api.parser(resText).asMap()
		return saveMessagesPhotoByObject(Options(responseImage), token)
	}

	override fun uploadMessagePhoto(data: ByteArray, peerId: Int, type: String, token: String?): JsonItem? {
		val uploadServerInfo = getMessagesUploadServer(peerId, token)
		if (uploadServerInfo == null || isError(uploadServerInfo)) {
			return uploadServerInfo
		}

		val resText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("photo" to Options("data" to data, "Content-Type" to "image/$type", "filename" to "item.$type")))?.responseText
				?: return null
		val responseImage = api.parser(resText).asMap()
		return saveMessagesPhotoByObject(Options(responseImage), token)
	}
}