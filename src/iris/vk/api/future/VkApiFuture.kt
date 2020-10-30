package iris.vk.api.future

import iris.json.JsonItem
import iris.json.flow.JsonFlowParser
import iris.vk.Options
import iris.vk.api.*
import iris.vk.api.common.*
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.LazyThreadSafetyMode.NONE

/**
 * @created 25.10.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class VkApiFuture(val token: String, val version: String = VK_API_VERSION, connection: VkApiConnectionFuture? = null) : Requester<VkApiFuture.VkFuture, VkApiFuture.VkFutureList> {

	companion object {
		val defaultConnection = VkApiConnectionFutureHttpClient(VkApiConnectionFutureHttpClient.newClient())
	}
	val connection = connection ?: defaultConnection

	open val messages by lazy(NONE) { Messages(this) }
	open val groups by lazy(NONE) { Groups(this) }
	open val users by lazy(NONE) { Users(this) }
	open val photos by lazy(NONE) { PhotosFuture(this) }
	open val docs by lazy(NONE) { DocsFuture(this) }
	open val utils by lazy(NONE) { Utils(this) }
	open val board by lazy(NONE) { Board(this) }
	open val wall by lazy(NONE) { Wall(this) }

	//////////////////////////////////////

	open fun request(req: VkRequestData): VkFuture {
		return request(req.method, req.options, req.token)
	}

	override fun request(method: String, options: Options?, token: String?): VkFuture {
		val token = token ?: this.token

		val sb = StringBuilder()
		if (options != null)
			VkApis.encodeOptions(options, sb)

		sb.append("access_token=").append(token).append("&v=").append(version)

		val future = VkFuture()
		connection.request(Method2UrlCache.getUrl(method), sb.toString()).whenComplete { it, u ->
			future.complete(it?.let { parser(it.responseText) })
		}
		return future
	}

	override fun execute(data: List<VkRequestData>, token: String?): VkFutureList {
		val codes = VkApis.generateExecuteCode(data, token?: this.token)
		val futures = codes.map { request(it.method, it.options, it.token) }
		return VkExecuteFuture(futures)
	}

	fun parser(res: String): JsonItem {
		return JsonFlowParser.start(res)
	}

	override fun emptyOfListType(): VkFutureList {
		return VkFutureList.empty
	}

	class VkFuture(val request: VkRequestData? = null) : CompletableFuture<JsonItem?>()

	open class VkFutureList(val futures: Collection<VkFuture>) {
		companion object {
			val empty = VkFutureList(emptyList())
		}
		open fun join(): List<JsonItem?> {
			val data = LinkedList<JsonItem?>()
			for (l in futures)
				data.add(l.get())
			return data
		}
	}

	internal class VkExecuteFuture(futures: Collection<VkFuture>) : VkFutureList(futures) {
		override fun join(): List<JsonItem?> {
			val data = super.join()
			val response = mutableListOf<JsonItem?>()
			for (res in data) {
				if (res == null) continue
				val data = VkApis.prepareExecuteResponses(res)
				response.addAll(data)
			}
			return response
		}
	}
}