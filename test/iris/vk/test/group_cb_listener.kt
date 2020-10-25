package iris.vk.test

import iris.vk.VkEngineGroupCallback
import iris.vk.VkEngineGroupCallback.GroupbotSource.Groupbot
import iris.vk.VkEngineGroupCallback.GroupbotSource.SimpleGroupSource

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

	val cbEngine = VkEngineGroupCallback(
			gbSource = SimpleGroupSource(Groupbot(groupId, confirmation, secret))
			, path = "/kotlin/callback"
	)
	cbEngine.start() // Запускаем сервер. Открываем порт для входящих. Неблокирующий вызов

	while (true) {
		val events = cbEngine.retrieve(wait = true) // ожидаем получения хотя бы одного события
		for (event in events) {
			println("Событие получено: " + event.obj())
		}
	}
}