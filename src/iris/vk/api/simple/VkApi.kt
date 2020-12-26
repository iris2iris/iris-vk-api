package iris.vk.api.simple

import iris.json.JsonItem
import iris.json.flow.JsonFlowParser
import iris.vk.Options
import iris.vk.api.*
import iris.vk.api.common.*
import java.util.Collections.emptyList
import kotlin.LazyThreadSafetyMode.NONE

/**
 * @created 06.09.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

open class VkApi(val token: String, val version: String = VK_API_VERSION, internal val connection: VkApiConnection = VkApiConnectionHttpClient()) : VkApiInterface<JsonItem?, List<JsonItem?>>, Requester<JsonItem?, List<JsonItem?>> {

	override val messages = Messages(this)
	override val friends by lazy(NONE) { Friends(this) }
	override val groups by lazy(NONE) { Groups(this) }
	override val users = Users(this)
	override val photos by lazy(NONE) { PhotosSimple(this) }
	override val docs by lazy(NONE) { DocsSimple(this) }
	override val wall by lazy(NONE) { Wall(this) }
	override val utils by lazy(NONE) { Utils(this) }

	//////////////////////////////////////

	open fun request(req: VkRequestData): JsonItem? {
		return request(req.method, req.options, req.token)
	}

	override fun request(method: String, options: Options?, token: String?): JsonItem? {
		val token = token ?: this.token

		val sb = StringBuilder()
		if (options != null)
			VkApis.encodeOptions(options, sb)

		sb.append("access_token=").append(token).append("&v=").append(version)
		val res = connection.request(Method2UrlCache.getUrl(method), sb.toString())?.responseText ?: return null
		return parser(res)
	}

	override fun execute(data: List<VkRequestData>, token: String?): List<JsonItem> {
		val codes = VkApis.generateExecuteCode(data, token?: this.token)
		val response = mutableListOf<JsonItem>()
		for (i in codes) {
			val res = request(i.method, i.options, i.token)?: continue
			val data = VkApis.prepareExecuteResponses(res)
			response.addAll(data)
		}
		return response
	}

	fun parser(res: String): JsonItem {
		return JsonFlowParser.start(res)
	}

	override fun emptyOfListType(): List<JsonItem?> {
		return emptyList()
	}
}

