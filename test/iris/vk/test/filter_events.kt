package iris.vk.test

import iris.vk.*
import iris.vk.command.RegexCommand
import iris.vk.command.SimpleCommand
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

	commandsHandler += SimpleCommand("пинг") {
		vk.messages.send(it.peerId, "ПОНГ!")
	}

	// Отфильтруем все сообщения, которые поступают только от конкретного пользователя
	val personalFilter = object : VkEventFilterAdapter() {
		override fun filterMessages(messages: List<Message>): List<Message> {
			return messages.filter { it.fromId == userToId  }
		}
	}

	// Передаём в параметрах слушателя событий токен и созданный обработчик команд
	val listener = VkEngineGroup(
			token,
			VkFilterHandler(
					arrayOf(personalFilter),
					commandsHandler
			)
	)
	listener.run() // блокирует дальнейшее продвижение, пока не будет остановлено

	exitProcess(0)
}