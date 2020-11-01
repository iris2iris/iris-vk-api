package iris.vk.test

import iris.vk.VkEngineGroup
import iris.vk.VkEventHandlerTrigger
import iris.vk.api.future.VkApiPack
import iris.vk.command.VkCommandHandler
import iris.vk.command.commands
import kotlin.system.exitProcess

/**
 * @created 01.11.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
fun main() {
	TestUtil.init()
	val props = TestUtil.getProperties()
	val token = props.getProperty("group.token")

	// Создаём класс для отправки сообщений
	val vk = VkApiPack(token)



	// Определяем обработчик триггеров
	val triggerHandler = VkEventHandlerTrigger {

		onMessage {
			for (message in it)
				println("Получено сообщение от ${message.peerId}: ${message.text}")
		}

		onMessageEdit {
			for (message in it)
				println("Сообщение исправлено ${message.id}: ${message.text}")
		}

		onMessage(VkCommandHandler().addAll(
				commands {
					"пинг" to {
						vk.messages.send(it.peerId, "ПОНГ!")
					}
					"мой ид" to {
						vk.messages.send(it.peerId, "Ваш ID равен: ${it.fromId}")
					}
					regex("""рандом (\d+) (\d+)""") to { vkMessage, params ->

						var first = params[1].toInt()
						var second = params[2].toInt()
						if (second < first)
							first = second.also { second = first }

						vk.messages.send(vkMessage.peerId, "🎲 Случайное значение в диапазоне [$first..$second] выпало на ${(first..second).random()}")
					}
				}
		))
	}

	// Передаём в параметрах слушателя событий токен и созданный обработчик команд
	val listener = VkEngineGroup(token, triggerHandler)
	listener.run() // блокирует дальнейшее продвижение, пока не будет остановлено

	exitProcess(0)
}