package iris.vk.api.simple

import iris.json.JsonItem
import iris.vk.Options
import iris.vk.api.VkApis
import iris.vk.api.common.Docs

/**
 * @created 29.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class DocsSimple(override val api: VkApi) : Docs<JsonItem?, List<JsonItem?>>(api) {
	override fun upload(filePath: String, peerId: Int, type: String?, title: String?, tags: String?, token: String?): JsonItem? {
		val uploadServerInfo = getMessagesUploadServer(peerId, type, token)
		if (uploadServerInfo == null || VkApis.isError(uploadServerInfo)) {
			return uploadServerInfo
		}
		val resText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("file" to Options("file" to filePath)))?.responseText
				?: return null
		val responseFile = api.parser(resText)
		return save(responseFile["file"].asString(), title, tags, token)
	}

	override fun upload(data: ByteArray, peerId: Int, type: String?, title: String?, tags: String?, token: String?): JsonItem? {
		val uploadServerInfo = getMessagesUploadServer(peerId, type, token)
		if (uploadServerInfo == null || VkApis.isError(uploadServerInfo)) {
			return uploadServerInfo
		}
		val resText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("file" to Options("data" to data)))?.responseText
				?: return null
		val responseFile = api.parser(resText)
		return save(responseFile["file"].asString(), title, tags, token)

	}

	override fun uploadWall(filePath: String, groupId: Int, title: String?, tags: String?, token: String?): JsonItem? {
		val uploadServerInfo = getWallUploadServer(groupId, token)
		if (uploadServerInfo == null || VkApis.isError(uploadServerInfo)) {
			return uploadServerInfo
		}
		val resText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("file" to Options("file" to filePath)))?.responseText
				?: return null
		val responseFile = api.parser(resText)
		return save(responseFile["file"].asString(), title, tags, token)
	}

	override fun uploadWall(data: ByteArray, groupId: Int, title: String?, tags: String?, token: String?): JsonItem? {
		val uploadServerInfo = getWallUploadServer(groupId, token)
		if (uploadServerInfo == null || VkApis.isError(uploadServerInfo)) {
			return uploadServerInfo
		}
		val resText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("file" to Options("data" to data)))?.responseText
				?: return null
		val responseFile = api.parser(resText)
		return save(responseFile["file"].asString(), title, tags, token)
	}
}