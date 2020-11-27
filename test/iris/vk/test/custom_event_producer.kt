package iris.vk.test

import iris.json.JsonItem
import iris.vk.*
import iris.vk.api.VkApis
import iris.vk.api.simple.VkApi
import iris.vk.command.CommandMatcherSimple
import iris.vk.command.VkCommandHandler
import iris.vk.event.*
import iris.vk.event.group.GroupMessage
import iris.vk.event.group.GroupMessageWithoutChatInfo
import kotlin.system.exitProcess

/**
 * @created 25.11.2020
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
			(messages as List<GroupProcessorExt.GroupMessageExt>).forEach {
				println("Message: [${it.conversationMessageId}] isChat = ${it.isChat()} | ${it.text}")
			}
			return messages
		}
	}

	// Передаём в параметрах слушателя событий токен и созданный обработчик команд
	val listener = VkPollingGroup(
			api = vk,
			updateProcessor = VkUpdateProcessorGroupDefault(
				VkEventFilterHandler(
					filters = arrayOf(personalFilter),
					handler = commandsHandler
				),
				eventProducerFactory = GroupProcessorExt()
			)
	)
	listener.startPolling() // Можно запустить неблокирующего слушателя
	listener.join() // Даст дождаться завершения работы слушателя
	//listener.run() // Можно заблокировать дальнейшую работу потока, пока не будет остановлено

	exitProcess(0)
}

class GroupProcessorExt : VkUpdateProcessorGroupDefault.GroupEventProducerDefault() {

	class GroupMessageExt(source: JsonItem, sourcePeerId: Int) : GroupMessageWithoutChatInfo(source, sourcePeerId) {
		fun isChat(): Boolean = VkApis.isChat(peerId)
		fun isPavelDurov(): Boolean = fromId == 1
	}

	override fun message(obj: JsonItem, sourcePeerId: Int): Message {
		return GroupMessageExt(obj["message"], sourcePeerId)
	}

	override fun messageWithoutChatInfo(obj: JsonItem, sourcePeerId: Int): Message {
		return GroupMessageExt(obj["object"], sourcePeerId)
	}
}

