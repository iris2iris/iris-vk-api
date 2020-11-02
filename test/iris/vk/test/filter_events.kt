package iris.vk.test

import iris.vk.*
import iris.vk.api.simple.VkApi
import iris.vk.command.CommandMatcherSimple
import iris.vk.command.VkCommandHandler
import iris.vk.event.Message
import kotlin.system.exitProcess

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
fun main() {
	TestUtil.init()
	val props = TestUtil.getProperties()
	val token = props.getProperty("group.token")
	val userToId = props.getProperty("userTo.id").toInt()
	// Создаём класс для отправки сообщений
	val vk = VkApi(token)

	// Определяем обработчик команд
	val commandsHandler = VkCommandHandler()

	commandsHandler += CommandMatcherSimple("пинг") {
		vk.messages.send(it.peerId, "ПОНГ!")
	}

	// Отфильтруем все сообщения, которые поступают только от конкретного пользователя
	val personalFilter = object : VkEventFilterAdapter() {
		override fun filterMessages(messages: List<Message>): List<Message> {
			return messages.filter { it.fromId == userToId  }
		}
	}

	// Передаём в параметрах слушателя событий токен и созданный обработчик команд
	val listener = VkPollingGroup(
			token,
			VkEventFilterHandler(
					arrayOf(personalFilter),
					commandsHandler
			)
	)
	listener.startPolling() // Можно запустить неблокирующего слушателя
	listener.join() // Даст дождаться завершения работы слушателя
	//listener.run() // Можно заблокировать дальнейшую работу потока, пока не будет остановлено

	exitProcess(0)
}