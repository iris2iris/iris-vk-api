package iris.vk.test

import iris.vk.*
import java.io.File
import java.io.Reader
import java.util.*
import kotlin.system.exitProcess

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

	// Создаём класс для отправки сообщений
	val vk = VkApiPack(token)

	// Определяем простой обработчик событий
	val simpleMessageHandler = object : VkHandlerAdapter() {

		override fun processMessage(message: VkMessage) {
			// message содержит информацию о полученном JsonItem (message.source) и вспомогательную информацию, которую
			// добавит сам программист по мере продвижения события (message.options)

			// message.text — это метод, подготавливает текст для дальнейшей работы
			val text = message.text
			val messageItem = message.source["message"]
			if (text.equals("пинг", true))
				vk.messages.send(messageItem["from_id"].asInt(), "ПОНГ")
		}
	}

	// Передаём в параметрах слушателя событий токен и созданный обработчик событий
	val listener = VkEngineGroup(token, simpleMessageHandler)
	listener.run() // блокирует дальнейшее продвижение, пока не будет остановлено

	exitProcess(0)
}