package iris.vk.test

import iris.vk.VkEventHandlerAdapter
import iris.vk.api.simple.VkApi
import iris.vk.callback.VkCallbackGroup
import iris.vk.callback.VkCallbackServer
import iris.vk.callback.VkCallbackServer.GroupbotSource.Groupbot
import iris.vk.callback.VkCallbackServer.GroupbotSource.SimpleGroupSource
import iris.vk.event.Message

/**
 * @created 28.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
fun main() {
	TestUtil.init()
	val props = TestUtil.getProperties()

	val secret = props.getProperty("group.secret").ifBlank { null }
	val confirmation = props.getProperty("group.confirmation")
	val groupId = props.getProperty("group.id").toInt()
	val token = props.getProperty("group.token")

	val vk = VkApi(token)

	val cbEngine = VkCallbackServer(
			gbSource = SimpleGroupSource(Groupbot(groupId, confirmation, secret))
			, path = "/kotlin/callback"
			, vkTimeVsLocalTimeDiff = vk.request("utils.getServerTime", null)!!["response"].asLong()*1000L - System.currentTimeMillis()
	)

	val messageHandler = object : VkEventHandlerAdapter() {
		override fun processMessage(message: Message) {
			println("Событие получено. Group ID: ${message.sourcePeerId} текст: ${message.text}")
		}
	}

	val listener = VkCallbackGroup(cbEngine, messageHandler)
	listener.run()
}