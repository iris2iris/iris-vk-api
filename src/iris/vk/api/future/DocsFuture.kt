package iris.vk.api.future

import iris.vk.Options
import iris.vk.api.VkApis
import iris.vk.api.common.Docs
import iris.vk.api.future.VkApiFuture.VkFuture
import iris.vk.api.future.VkApiFuture.VkFutureList

/**
 * @created 29.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class DocsFuture(override val api: VkApiFuture) : Docs<VkFuture, VkFutureList>(api) {
	override fun upload(filePath: String, peerId: Int, type: String?, title: String?, tags: String?, token: String?): VkFuture {
		val future = VkFuture()
		val t = getMessagesUploadServer(peerId, type, token).thenApply {uploadServerInfo ->
			if (uploadServerInfo == null || VkApis.isError(uploadServerInfo)) {
				return@thenApply uploadServerInfo
			}
			val resText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("file" to Options("file" to filePath))).get()?.responseText
					?: return@thenApply null
			val responseFile = api.parser(resText)
			save(responseFile["file"].asString(), title, tags, token).get()
		}.whenComplete { it, err ->
			future.complete(it)
		}
		return future
	}

	override fun upload(data: ByteArray, peerId: Int, type: String?, title: String?, tags: String?, token: String?): VkFuture {
		val future = VkFuture()
		val t = getMessagesUploadServer(peerId, type, token).thenApply {uploadServerInfo ->
			if (uploadServerInfo == null || VkApis.isError(uploadServerInfo)) {
				return@thenApply uploadServerInfo
			}
			val resText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("file" to Options("data" to data))).get()?.responseText
					?: return@thenApply null
			val responseFile = api.parser(resText)
			save(responseFile["file"].asString(), title, tags, token).get()
		}.whenComplete { it, err ->
			future.complete(it)
		}
		return future
	}

	override fun uploadWall(filePath: String, groupId: Int, title: String?, tags: String?, token: String?): VkFuture {
		val future = VkFuture()
		val t = getWallUploadServer(groupId, token).thenApply {uploadServerInfo ->
			if (uploadServerInfo == null || VkApis.isError(uploadServerInfo)) {
				return@thenApply uploadServerInfo
			}
			val resText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("file" to Options("file" to filePath))).get()?.responseText
					?: return@thenApply null
			val responseFile = api.parser(resText)
			save(responseFile["file"].asString(), title, tags, token).get()
		}.whenComplete { it, err ->
			future.complete(it)
		}
		return future
	}

	override fun uploadWall(data: ByteArray, groupId: Int, title: String?, tags: String?, token: String?): VkFuture {
		val future = VkFuture()
		val t = getWallUploadServer(groupId, token).thenApply {uploadServerInfo ->
			if (uploadServerInfo == null || VkApis.isError(uploadServerInfo)) {
				return@thenApply uploadServerInfo
			}
			val resText = api.connection.requestUpload(uploadServerInfo["response"]["upload_url"].asString(), mapOf("file" to Options("data" to data))).get()?.responseText
					?: return@thenApply null
			val responseFile = api.parser(resText)
			save(responseFile["file"].asString(), title, tags, token).get()
		}.whenComplete { it, err ->
			future.complete(it)
		}
		return future
	}
}