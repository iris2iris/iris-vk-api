package iris.vk.test

import iris.vk.multibot.VkMultibotCallbackEngine
import iris.vk.multibot.VkMultibotCallbackEngine.GroupbotSource.Groupbot
import iris.vk.multibot.VkMultibotCallbackEngine.GroupbotSource.SimpleGroupSource

/**
 * @created 28.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
fun main() {
	val props = TestUtil.getProperties()

	val token = props.getProperty("group.token")
	val secret = props.getProperty("group.secret")
	val confirmation = props.getProperty("group.confirmation")
	val groupId = props.getProperty("group.id").toInt()

	val cbEngine = VkMultibotCallbackEngine(
			gbSource = SimpleGroupSource(Groupbot(groupId, confirmation, secret?.ifBlank { null }))
			, path = "/kotlin/callback"
			, port = 80
	)
	cbEngine.start()

	while (true) {
		val items = cbEngine.retrieve(wait = true)
		for (item in items)
			println("Событие получено: " + item.obj())
	}
}