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

	val keyboard = VkKeyboard.createJson(arrayOf(
		arrayOf(
			VkKeyboard.text("Yes", color = VkKeyboard.COLOR_POSITIVE),
			VkKeyboard.text("No", color = VkKeyboard.COLOR_NEGATIVE)
		),
		arrayOf(
			VkKeyboard.text("Long button")
		)
	))

	val vk = VkApi(token)
	val res = vk.messages.send(userToId, "Клава", Options("keyboard" to keyboard))
	println(res)
}