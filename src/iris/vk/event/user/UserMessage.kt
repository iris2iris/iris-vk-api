package iris.vk.event.user

import iris.json.*
import iris.json.flow.JsonFlowParser
import iris.json.plain.IrisJsonArray
import iris.json.plain.IrisJsonObject
import iris.json.proxy.JsonProxyString
import iris.vk.event.Message
import kotlin.LazyThreadSafetyMode.NONE

/**
 * @created 27.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class UserMessage(fullItemSource: ApiSource, source: JsonItem, sourcePeerId: Int) : UserChatEvent(fullItemSource, source, sourcePeerId), Message {

	override val text by lazy(NONE) { source[6].asStringOrNull()?.replace("<br>", "\n") }

	override val attachments: List<JsonItem>? by lazy(NONE) {
		val m = source
		if (m[7]["attach1_type"].asStringOrNull() == "wall") {
			return@lazy (fullItem?.get("attachments") as JsonArray?)?.getList()
		}

		if (m[7].isNotNull()) {
			val m7 = m[7] as JsonObject

			val attachmentsFull = m7["attachments"]
			val attachments = (if (attachmentsFull.isNull()) {
				val attachments = ArrayList<MutableMap<String, JsonItem>>()
				for (el in m7) {
					val key = el.first
					if (!key.startsWith("attach")) continue
					val addValue = el.second
					val data = key.split("_")
					val num = data[0].substring("attach".length).toInt() - 1
					if (num + 1 > attachments.size) {
						extendCapacity(attachments as ArrayList<Any?>, num + 1)
						attachments[num] = mutableMapOf<String, JsonItem>()
					}
					val addKey = if (data.size > 1) data[1] else "id"
					attachments[num][addKey] = addValue
				}
				IrisJsonArray(attachments.map { IrisJsonObject(it.toList(), Configuration.globalConfiguration) })
			} else {
				JsonFlowParser.start(attachmentsFull.asString()) as JsonArray
			}).getList() as Collection<JsonObject>

			if (attachments.isNotEmpty()) {
				val resAttachments = ArrayList<JsonItem>(attachments.size)
				for (a in attachments) {
					val entries = (a.getEntries() as MutableCollection<JsonEntry>)
					var type: String? = null
					if (!entries.removeIf { if (it.first == "type") {type = it.second.asString(); true} else false }) continue
					if (type != null) {
						resAttachments.add(IrisJsonObject(listOf("type" to JsonProxyString(type), type!! to a), Configuration.globalConfiguration))
					}
				}
				return@lazy resAttachments
			}
		}

		null
	}

	private fun extendCapacity(array: ArrayList<Any?>, newCapacity: Int) {
		if (newCapacity > array.size) {
			for (i in array.size until newCapacity) {
				array.add(null)
			}
		}
	}

	override val forwardedMessages: List<JsonItem>? by lazy(NONE) {
		val m = source
		if (m[7]["reply"].isNotNull())
			return@lazy null
		if (m[7]["fwd"].isNotNull()) {
			return@lazy (fullItem?.get("fwd_messages") as JsonArray?)?.getList()
		}
		null
	}

	override val replyMessage: JsonObject? by lazy {
		val m = source
		if (m[7]["reply"].isNotNull()) {
			val fullItem = fullItem ?: return@lazy null
			return@lazy (fullItem["reply_message"] as? JsonObject)
		}
		null
	}
}