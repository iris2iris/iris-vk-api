package iris.vk.api

import iris.json.JsonArray
import iris.json.JsonEncoder
import iris.json.JsonItem
import iris.json.plain.IrisJsonArray
import iris.json.plain.IrisJsonObject
import iris.json.proxy.JsonProxyObject
import iris.vk.Options
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.max

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
object VkApis {

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

	private val emptyJsonArray = IrisJsonArray(emptyList())

	fun prepareExecuteResponses(data: JsonItem): List<JsonItem> {
		if (data["response"].isNull()) return emptyList()
		var numError = 0
		val result = mutableListOf<JsonItem>()
		val executeErrors = if (data["execute_errors"].isNotNull()) data["execute_errors"] as JsonArray else emptyJsonArray
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
				val str = "return [" + sb.substring(0, sb.length - 1) + "];"
				res.add(VkRequestData("execute", Options("code" to str), token, version))
			}
		}

		if (sb.isNotEmpty()) {
			val str = "return [" + sb.substring(0, sb.length - 1) + "];"
			res.add(VkRequestData("execute", Options("code" to str), token, version))
		}
		return res
	}

	private fun encode(o: String): String? {
		return URLEncoder.encode(o, StandardCharsets.UTF_8)
	}

	fun encodeOptions(obj: Options, sb: StringBuilder = StringBuilder()): StringBuilder {
		for (o in obj.entries) {
			sb.append(encode(o.key)).append('=')
					.append(encode(o.value.toString())).append("&")
		}
		return sb
	}
}