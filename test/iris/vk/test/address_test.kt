package iris.vk.test

import iris.vk.VkAddressTesterDefault
import iris.vk.VkEngineGroupCallback
import iris.vk.VkEngineGroupCallback.GroupbotSource.Groupbot
import iris.vk.VkEngineGroupCallback.GroupbotSource.SimpleGroupSource
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

	val cbEngine = VkEngineGroupCallback(
			gbSource = SimpleGroupSource(Groupbot(groupId, confirmation, secret))
			, path = "/kotlin/callback"
			, addressTester = addressTester
	)
	cbEngine.start() // Запускаем сервер. Открываем порт для входящих. Неблокирующий вызов

	while (true) {
		val events = cbEngine.retrieve(wait = true) // ожидаем получения хотя бы одного события
		for (event in events) {
			logger.finest("Входящее сообщение")
		}
	}
}