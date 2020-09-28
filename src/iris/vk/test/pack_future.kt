package iris.vk.test

import iris.vk.Options
import iris.vk.VkApiPack
import iris.vk.VkRequestData
import kotlin.system.exitProcess

/**
 * @created 27.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
fun main() {
	TestUtil.init()
	val props = TestUtil.getProperties()
	val token = props.getProperty("group.token")
	val userToId = props.getProperty("userTo.id").toInt()

	val vk = VkApiPack(token)
	val futuresList = vk.messages.sendMulti(listOf(
			Options("peer_id" to userToId, "message" to "Привет. Это сообщение с Kotlin\nОно почти работает!", "attachment" to "photo-181070115_457239553"),
			Options("peer_id" to 2, "message" to "Привет. Это сообщение с Kotlin\nОно почти работает!", "attachment" to "photo-181070115_457239553"),
	)
	)
	println("Прошёл сюда без задержек")
	val secondFutures = vk.execute(listOf(
			VkRequestData("messages.send", Options("peer_id" to userToId, "message" to "Привет. Это сообщение с Kotlin\nОно почти работает!", "attachment" to "photo-181070115_457239553"))
			, VkRequestData("messages.edit", Options("peer_id" to userToId, "conversation_message_id" to 1, "message" to "Привет. Это сообщение с Kotlin\nОно почти работает!", "attachment" to "photo-181070115_457239553"))
	))

	println("И сюда тоже без задержек. Но вот ниже нужно подождать")
	for (it in futuresList.futures)
		println(it.get()?.obj())

	println("Получили данные, пошли дальше")
	for (it in secondFutures.futures)
		println(it.get()?.obj())
	println("Завершились")

	// У нас была создана фабрика потоков, поэтому так просто программа не завершится. Нужно принудительно
	exitProcess(0)
}