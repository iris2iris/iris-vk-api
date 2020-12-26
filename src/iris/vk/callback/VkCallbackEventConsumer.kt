package iris.vk.callback

import iris.json.JsonItem

interface VkCallbackEventConsumer {
	fun send(event: JsonItem)
}