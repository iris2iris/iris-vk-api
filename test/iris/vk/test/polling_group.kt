package iris.vk.test

import iris.vk.api.future.VkApiPack
import iris.vk.VkPollingGroup
import iris.vk.VkEventHandlerAdapter
import iris.vk.event.CallbackEvent
import iris.vk.event.ChatEvent
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



	// Определяем обработчик событий
	val simpleMessageHandler = object : VkEventHandlerAdapter() {

		private val vk = VkApiPack(token)

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

		override fun processScreenshots(screenshots: List<ChatEvent>) {
			for (screenshot in screenshots) {
				println("Получено screenshot-событие: ${screenshot.peerId} fromId=${screenshot.fromId}")
			}
		}
	}

	// Передаём в параметрах слушателя событий токен и созданный обработчик событий
	val listener = VkPollingGroup(token, simpleMessageHandler)
	listener.startPolling() // Можно запустить неблокирующего слушателя
	listener.join() // Даст дождаться завершения работы слушателя
	//listener.run() // Можно заблокировать дальнейшую работу потока, пока не будет остановлено

	exitProcess(0)
}