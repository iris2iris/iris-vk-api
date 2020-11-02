package iris.vk.test

import iris.vk.api.future.VkApiFuture
import iris.vk.api.simple.VkApi

/**
 * @created 27.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */


val props = TestUtil.getProperties()
val token = props.getProperty("group.token")
val userToId = props.getProperty("userTo.id").toInt()

fun main() {
	TestUtil.init()

	warmupFuture() // инициализация и прогрев
	warmupSimple() // инициализация и прогрев

	testFuture(5) // проверим
	Thread.sleep(5_000L) // чтобы не поймать флуд-контроль
	testSimple(5)
}

fun testFuture(repeats: Int) {
	val vk = VkApiFuture(token)
	val futures = ArrayList<VkApiFuture.VkFuture>(repeats)
	val start = System.nanoTime()
	println("Запускаем шалости")
	for (i in 1..repeats)
		futures += vk.messages.send(userToId, "Извините, я поспамлю: $i из $repeats [VkApiFuture]")
	println("До этого места мы дошли без задержки на ожидание ответов.")
	println()
	println("А при получении результатов нужно подождать:")
	for (future in futures) {
		val result = future.get()
		println(result?.obj())
	}
	val end = System.nanoTime()
	println("Выполнение $repeats запросов методом VkApiFuture заняло нам: ${(end - start)/1000_000} ms")
}

fun testSimple(repeats: Int) {
	val vk = VkApi(token)
	val start = System.nanoTime()
	println("Запускаем шалости")
	for (i in 1..repeats){
		val result = vk.messages.send(userToId, "Извините, я поспамлю: $i из $repeats [VkApi]")
		println(result?.obj())
	}
	println("Закончили водные процедуры")
	val end = System.nanoTime()
	println("Выполнение $repeats запросов методом VkApi заняло нам: ${(end - start)/1000_000} ms")
}

fun warmupFuture() {
	val vk = VkApiFuture(token)
	vk.messages.send(userToId, "Извините, я разогреваюсь [VkApiFuture]").get()
}

fun warmupSimple() {
	val vk = VkApi(token)
	vk.messages.send(userToId, "Извините, я разогреваюсь [VkApi]")
}