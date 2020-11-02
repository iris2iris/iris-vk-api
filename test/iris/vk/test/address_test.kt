package iris.vk.test

import iris.json.JsonItem
import iris.vk.callback.VkAddressTesterDefault
import iris.vk.callback.VkCallbackServer
import iris.vk.callback.VkCallbackServer.GroupbotSource.Groupbot
import iris.vk.callback.VkCallbackServer.GroupbotSource.SimpleGroupSource
import java.util.logging.Logger

/**
 * @created 28.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
fun main() {
	TestUtil.init()
	val props = TestUtil.getProperties()
	val logger = Logger.getLogger("iris.vk")
	val secret = props.getProperty("group.secret")
	val confirmation = props.getProperty("group.confirmation")
	val groupId = props.getProperty("group.id").toInt()

	val addressTester = VkAddressTesterDefault(
		ipSubnets = arrayOf("95.142.192.0/21", "2a00:bdc0::/32")
	)

	val cbEngine = VkCallbackServer(
			gbSource = SimpleGroupSource(Groupbot(groupId, confirmation, secret))
			, path = "/kotlin/callback"
			, addressTester = addressTester
	)

	cbEngine.setEventWriter(
		object : VkCallbackServer.VkCallbackEventWriter {
			override fun send(event: JsonItem) {
				println("Новое событие:" + event.obj())
			}
		}
	)

	cbEngine.start() // Запускаем сервер. Открываем порт для входящих. Неблокирующий вызов
}