package iris.vk.test

import iris.json.JsonItem
import iris.vk.VkEventHandlerAdapter
import iris.vk.callback.VkAddressTesterDefault
import iris.vk.callback.VkCallbackServer
import iris.vk.callback.VkCallbackServer.GroupbotSource.Groupbot
import iris.vk.VkGroupSourceList
import iris.vk.api.future.VkApiPack
import iris.vk.api.simple.VkApi
import iris.vk.callback.VkCallbackGroup
import iris.vk.event.Message

/**
 * @created 29.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
fun main() {
	TestUtil.init()
	val props = TestUtil.getProperties()
	val secret = props.getProperty("group.secret").ifBlank { null }
	val confirmation = props.getProperty("group.confirmation")
	val groupId = props.getProperty("group.id").toInt()
	val token = props.getProperty("group.token")

	val groupSource = VkGroupSourceList(listOf(
		Groupbot(groupId, confirmation, secret)/*,
		Groupbot(111111, "41541541", null),
		Groupbot(111112, "41541541", null)*/
	))

	val api = VkApiPack(token)

	val cbEngine = VkCallbackServer(
			gbSource = groupSource,
			path = "/kotlin/callback",
			addressTester = VkAddressTesterDefault(),
			vkTimeVsLocalTimeDiff = api.utils.getServerTime().get()!!["response"].asLong()*1000L - System.currentTimeMillis()
	)

	val messageHandler = object : VkEventHandlerAdapter() {
		override fun processMessage(message: Message) {
			println("Событие получено. Group ID: ${message.sourcePeerId} текст: ${message.text}")
			if (message.text == "пинг")
				api.messages.send(message.peerId, "ПОНГ!")
		}
	}

	val listener = VkCallbackGroup(cbEngine, VkCallbackGroup.defaultUpdateProcessor(messageHandler))
	listener.run()
}