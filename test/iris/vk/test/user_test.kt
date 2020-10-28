package iris.vk.test

import iris.vk.*
import iris.vk.event.Message
import kotlin.system.exitProcess

/**
 * @created 26.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

fun main() {
	TestUtil.init()
	val props = TestUtil.getProperties()
	val token = props.getProperty("user.token")

	// Создаём класс для отправки сообщений
	val vk = VkApiPack(token)

	// Определяем простой обработчик событий
	val simpleMessageHandler = object : VkHandlerAdapter() {

		override fun processMessage(message: Message) {
			// message содержит информацию о полученном JsonItem (message.source) и вспомогательную информацию, которую
			// добавит сам программист по мере продвижения события (message.options)

			// message.text — это метод, подготавливает текст для дальнейшей работы
			val text = message.text
			println("Получено сообщение: $text")

			if (text =="пинг") {
				println("Команда пинг получена")
				// Шлём ответ
				vk.messages.send(message.peerId, "ПОНГ")
			}
			val attachments = message.attachments
			if (!attachments.isNullOrEmpty())
				println("	Attachment: ${attachments.joinToString { it.obj().toString() }}")
			val forwardedMessages = message.forwardedMessages
			if (!forwardedMessages.isNullOrEmpty())
				println("	Forwarded: ${forwardedMessages.joinToString { it.obj().toString() }}")
		}
	}

	// Передаём в параметрах слушателя событий токен и созданный обработчик событий
	val listener = VkEngineUser(token, simpleMessageHandler)
	while (true)
		try {
			listener.run() // блокирует дальнейшее продвижение, пока не будет остановлено
		} catch (e: Throwable) {
			e.printStackTrace()
		}

}