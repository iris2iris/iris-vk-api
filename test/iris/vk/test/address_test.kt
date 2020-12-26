package iris.vk.test

import iris.json.JsonItem
import iris.vk.VkUpdateProcessor
import iris.vk.callback.AddressTesterDefault
import iris.vk.callback.VkCallbackGroupBuilder
import iris.vk.callback.GroupbotSource.Groupbot
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

	val addressTester = AddressTesterDefault(
		ipSubnets = arrayOf("95.142.192.0/21", "2a00:bdc0::/32")
	)

	val updateProcessor = object : VkUpdateProcessor {
		override fun processUpdates(updates: List<JsonItem>) {
			updates.forEach {
				println("Новое событие:" + it.obj())
			}
		}
	}

	val groupCb = VkCallbackGroupBuilder.build {
		groupbot = Groupbot(groupId, confirmation, secret)
		this.addressTester = addressTester
		path = "/kotlin/callback"
		this.updateProcessor = updateProcessor
	}


	groupCb.startPolling() // Запускаем сервер. Открываем порт для входящих. Неблокирующий вызов */
}