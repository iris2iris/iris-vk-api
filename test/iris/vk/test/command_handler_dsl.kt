package iris.vk.test

import iris.vk.api.future.VkApiPack
import iris.vk.VkPollingGroup
import iris.vk.command.*
import kotlin.system.exitProcess

/**
 * @created 27.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
fun main() {
	TestUtil.init()
	val props = TestUtil.getProperties()
	val token = props.getProperty("group.token")

	// Создаём класс для отправки сообщений
	val api = VkApiPack(token)

	// Определяем обработчик команд
	val commandsHandler = VkCommandHandler()

	// Конфигурирование команд в стиле DSL
	commandsHandler += commands {
		"пинг" runs {
			api.messages.send(it.peerId, "ПОНГ!")
		}

		"мой ид" runs {
			api.messages.send(it.peerId, "Ваш ID равен: ${it.fromId}")
		}

		regex("""рандом (\d+) (\d+)""") runs { vkMessage, params ->

			var first = params[1].toInt()
			var second = params[2].toInt()
			if (second < first)
				first = second.also { second = first }

			api.messages.send(vkMessage.peerId, "🎲 Случайное значение в диапазоне [$first..$second] выпало на ${(first..second).random()}")
		}
	}

	// Передаём в параметрах слушателя событий токен и созданный обработчик команд
	val listener = VkPollingGroup(token, commandsHandler)
	listener.startPolling() // Можно запустить неблокирующего слушателя
	listener.join() // Даст дождаться завершения работы слушателя
	//listener.run() // Можно заблокировать дальнейшую работу потока, пока не будет остановлено

	exitProcess(0)
}