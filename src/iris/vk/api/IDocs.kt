package iris.vk.api

import iris.json.JsonItem

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface IDocs<SingleType, ListType> {

	fun getMessagesUploadServer(peerId: Int, type: String? = null, token: String? = null): SingleType

	fun save(file: String, title: String? = null, tags: String? = null, token: String? = null): SingleType

	fun getWallUploadServer(groupId: Int, token: String? = null): SingleType

	fun add(ownerId: Int, docId: Int, accessKey: String? = null, token: String? = null): SingleType


	// Не VK API методы

	fun upload(filePath: String, peerId: Int, type: String? = null, title: String? = null, tags: String? = null, token: String? = null): SingleType

	fun upload(data: ByteArray, peerId: Int, type: String? = null, title: String? = null, tags: String? = null, token: String? = null): SingleType

	fun uploadWall(filePath: String, groupId: Int, title: String? = null, tags: String? = null, token: String? = null): SingleType

	fun uploadWall(data: ByteArray, groupId: Int, title: String? = null, tags: String? = null, token: String? = null): SingleType
}