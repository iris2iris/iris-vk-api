package iris.vk.test

import iris.vk.VkApiFuture

/**
 * @created 27.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
fun main() {
	TestUtil.init()
	val props = TestUtil.getProperties()
	val token = props.getProperty("group.token")
	val userToId = props.getProperty("userTo.id").toInt()

	val vk = VkApiFuture(token)
	vk.messages.send(userToId, "Привет. Это сообщение с Kotlin").thenAccept {
		println("Это сообщение появится вторым")
		println(it?.obj())
	}
	println("Это сообщение появится первым, т.к. метод Future неблокирующий")

	// А можно сделать последовательное исполнение
	val future = vk.messages.send(userToId, "Привет. Это сообщение с Kotlin")
	val result = future.get() // дожидаемся ответа
	println(result?.obj()) // выводим результат
}