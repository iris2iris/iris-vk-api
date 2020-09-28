package iris.vk.test

import iris.vk.VkApi

/**
 * @created 27.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
fun main() {
	TestUtil.init()
	val props = TestUtil.getProperties()
	val token = props.getProperty("group.token")
	val userToId = props.getProperty("userTo.id").toInt()

	val vk = VkApi(token)
	val res = vk.messages.send(userToId, "Привет. Это сообщение с Kotlin")
	println(res?.obj())
}