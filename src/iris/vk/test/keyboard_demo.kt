package iris.vk.test

import iris.vk.Options
import iris.vk.VkApi
import iris.vk.VkKeyboard

/**
 * @created 05.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
fun main() {
	TestUtil.init()
	val props = TestUtil.getProperties()
	val token = props.getProperty("group.token")
	val userToId = props.getProperty("userTo.id").toInt()

	run {
		val keyboard = VkKeyboard.createJson(arrayOf(
				arrayOf(
						VkKeyboard.text("Simple"), // простая кнопка без payload
						VkKeyboard.text("Yes", payload = "{\"command\": \"yes\"}", color = VkKeyboard.COLOR_POSITIVE), // payload тектом
						VkKeyboard.text("No", payload = VkKeyboard.cmd(command = "no"), color = VkKeyboard.COLOR_NEGATIVE) // payload через функцию
				),
				arrayOf(
						VkKeyboard.textCommand(label = "Long button", command = "long"), // payload внутри метода textCommand
						VkKeyboard.text(label = "Long button", payload = Options("test" to "yes")) // произвольный payload
				)
		), inline = true /* default value true */)

		val vk = VkApi(token)
		val res = vk.messages.send(userToId, "Клавиатура на простых кнопках", Options("keyboard" to keyboard))
		println(res)
	}

	// Пример с callback кнопками
	run {
		val keyboard = VkKeyboard.createJson(arrayOf(
				arrayOf(
						VkKeyboard.callback("Simple"), // простая кнопка без payload
						VkKeyboard.callback("Yes", payload = "{\"command\": \"yes\"}", color = VkKeyboard.COLOR_POSITIVE), // payload тектом
						VkKeyboard.callback("No", payload = VkKeyboard.cmd(command = "no"), color = VkKeyboard.COLOR_NEGATIVE) // payload через функцию
				),
				arrayOf(
						VkKeyboard.callbackCommand(label = "Long button", command = "long"), // payload внутри метода textCommand
						VkKeyboard.callback(label = "Long button", payload = Options("test" to "yes")) // произвольный payload
				)
		), inline = true /* default value true */)

		val vk = VkApi(token)
		val res = vk.messages.send(userToId, "Клавиатура на callback-кнопках", Options("keyboard" to keyboard))
		println(res)
	}
}