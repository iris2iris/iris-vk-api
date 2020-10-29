package iris.vk.test

import iris.vk.api.future.VkApiPack
import iris.vk.VkEngineGroup
import iris.vk.VkHandlerAdapter
import iris.vk.event.CallbackEvent
import iris.vk.event.Message
import kotlin.system.exitProcess

/**
 * @created 27.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

fun main() {
	TestUtil.init()
	val props = TestUtil.getProperties()
	val token = props.getProperty("group.token")

	// Создаём класс для отправки сообщений
	val vk = VkApiPack(token)

	// Определяем обработчик событий
	val simpleMessageHandler = object : VkHandlerAdapter() {

		override fun processMessage(message: Message) {
			val text = message.text
			println("Получено сообщение: $text")

			if (text =="пинг") {
				println("Команда пинг получена")

				// Шлём ответ
				vk.messages.send(message.peerId, "ПОНГ")
			}
		}

		override fun processCallbacks(callbacks: List<CallbackEvent>) {
			for (callback in callbacks) {
				println("Получено callback-событие: ${callback.eventId} payload=${callback.payload}")
			}
		}
	}

	// Передаём в параметрах слушателя событий токен и созданный обработчик событий
	val listener = VkEngineGroup(token, simpleMessageHandler)
	listener.run() // блокирует дальнейшее продвижение, пока не будет остановлено

	exitProcess(0)
}