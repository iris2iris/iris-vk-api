package iris.vk

import iris.json.JsonItem
import iris.vk.event.*

interface VkEventProducer {
	fun message(obj: JsonItem, sourcePeerId: Int): Message
	fun messageWithoutChatInfo(obj: JsonItem, sourcePeerId: Int): Message
	fun invite(obj: JsonItem, sourcePeerId: Int): ChatEvent
	fun leave(obj: JsonItem, sourcePeerId: Int): ChatEvent
	fun titleUpdate(obj: JsonItem, sourcePeerId: Int): TitleUpdate
	fun pin(obj: JsonItem, sourcePeerId: Int): PinUpdate
	fun unpin(obj: JsonItem, sourcePeerId: Int): PinUpdate
	fun screenshot(obj: JsonItem, sourcePeerId: Int): ChatEvent
	fun callback(obj: JsonItem, sourcePeerId: Int): CallbackEvent
	fun otherEvent(obj: JsonItem, sourcePeerId: Int): OtherEvent
}

interface VkEventProducerFactory {
	fun producer(): VkEventProducer
}