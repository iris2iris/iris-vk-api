package iris.vk.test

import iris.vk.VkApiPack
import iris.vk.VkEngineGroup
import iris.vk.VkHandlerAdapter
import iris.vk.VkMessage
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.system.exitProcess

/**
 * @created 27.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

class T

fun main() {
	val props = TestUtil.getProperties()

	val ist = T::javaClass.javaClass.getResourceAsStream("logger.properties")
	LogManager.getLogManager().readConfiguration(ist)
	ist.close()
	val logger = Logger.getLogger("iris.vk")

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
			logger.finest { "Получено сообщение: $text" }

			val messageItem = message.source["message"]
			if (text =="пинг") {
				logger.fine("Команда пинг получена")

				// Шлём ответ
				vk.messages.send(messageItem["from_id"].asInt(), "ПОНГ")
			}
		}
	}

	// Передаём в параметрах слушателя событий токен и созданный обработчик событий
	val listener = VkEngineGroup(token, simpleMessageHandler)
	listener.run() // блокирует дальнейшее продвижение, пока не будет остановлено

	exitProcess(0)
}