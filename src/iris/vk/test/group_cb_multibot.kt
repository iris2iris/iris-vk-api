package iris.vk.test

import iris.vk.VkAddressTesterDefault
import iris.vk.VkEngineGroupCallback
import iris.vk.VkEngineGroupCallback.GroupbotSource.Groupbot
import iris.vk.VkGroupSourceList

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

	val groupSource = VkGroupSourceList(listOf(
		Groupbot(groupId, confirmation, secret)/*,
		Groupbot(111111, "41541541", null),
		Groupbot(111112, "41541541", null)*/
	))

	val cbEngine = VkEngineGroupCallback(
			gbSource = groupSource,
			path = "/kotlin/callback",
			addressTester = VkAddressTesterDefault()
	)
	cbEngine.start() // Запускаем сервер. Открываем порт для входящих. Неблокирующий вызов

	while (true) {
		val events = cbEngine.retrieve(wait = true) // ожидаем получения хотя бы одного события
		for (event in events) {
			println("Входящее сообщение")
		}
	}
}