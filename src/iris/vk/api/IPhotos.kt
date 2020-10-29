package iris.vk.api

import iris.json.JsonItem
import iris.vk.Options

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface IPhotos<SingleType, ListType> {

	fun saveMessagesPhotoByObject(responseImage: Options, token: String? = null): SingleType

	fun getMessagesUploadServer(peerId: Int, token: String? = null): SingleType

	fun getUploadServer(albumId: Int, groupId: Int? = null, token: String? = null): SingleType

	fun saveByObject(responseImage: Options, albumId: Int, groupId: Int? = null, caption: String? = null, options: Options? = null, token: String? = null): SingleType

	fun getWallUploadServer(userId: Int? = null, groupId: Int? = null, token: String? = null): SingleType

	fun saveWallPhotoByObject(responseImage: Options, userId: Int? = null, groupId: Int? = null, token: String? = null): SingleType

	fun copy(ownerId: Int, photoId: Int, accessKey: Int? = null, token: String? = null): SingleType


	// Не VK API методы

	fun uploadWallPhoto(photoPath: String, userId: Int? = null, groupId: Int? = null, token: String? = null): SingleType

	fun uploadWallPhoto(data: ByteArray, userId: Int? = null, groupId: Int? = null, type: String = "jpg", token: String? = null): SingleType

	fun uploadAlbumPhoto(photoPath: String, albumId: Int, groupId: Int? = null, caption: String? = null, options: Options? = null, token: String? = null): SingleType

	fun uploadAlbumPhoto(data: ByteArray, albumId: Int, groupId: Int? = null, type: String = "jpg", caption: String? = null, options: Options? = null, token: String? = null): SingleType

	fun uploadMessagePhoto(photoPath: String, peerId: Int = 0, token: String? = null): SingleType

	fun uploadMessagePhoto(data: ByteArray, peerId: Int = 0, type: String = "jpg", token: String? = null): SingleType
}