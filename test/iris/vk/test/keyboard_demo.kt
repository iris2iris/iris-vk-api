package iris.vk.test

import iris.vk.Options
import iris.vk.VkKeyboard
import iris.vk.api.simple.VkApi

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
		val res = vk.messages.send(userToId, "Клавиатура на простых кнопках", options = Options("keyboard" to keyboard))
		println(res?.obj())
	}

	// Пример с callback кнопками
	run {
		val keyboard = with(VkKeyboard) {
			createJson(arrayOf(
				arrayOf(
					callback("Simple"), // простая кнопка без payload
					callback("Yes", payload = "{\"command\": \"yes\"}", color = COLOR_POSITIVE), // payload тектом
					callback("No", payload = cmd(command = "no"), color = COLOR_NEGATIVE) // payload через функцию
				),
				arrayOf(
					callbackCommand(label = "Long button", command = "long"), // payload внутри метода textCommand
					callback(label = "Long button", payload = Options("test" to "yes")) // произвольный payload
				)
			), inline = true /* default value true */)
		}

		val vk = VkApi(token)
		val res = vk.messages.send(userToId, "Клавиатура на callback-кнопках", options = Options("keyboard" to keyboard))
		println(res?.obj())
	}
}