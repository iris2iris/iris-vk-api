package iris.vk.test

import iris.vk.VkApiFuture
import java.io.File
import java.io.Reader
import java.util.*

/**
 * @created 27.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
fun main() {
	val reader: Reader
	val props = Properties()
	props.load(File("conf.properties").reader().also { reader = it })
	reader.close()
	val token = props.getProperty("group.token")
	val userToId = props.getProperty("userTo.id").toInt()

	val vk = VkApiFuture(token)
	vk.messages.send(userToId, "Привет. Это сообщение с Kotlin").thenApply {
		println(it?.obj())
	}
}