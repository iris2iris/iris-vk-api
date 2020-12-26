package iris.vk.callback

import com.sun.net.httpserver.HttpServer
import iris.vk.VkEventHandler
import iris.vk.VkUpdateProcessor
import iris.vk.VkUpdateProcessorGroupDefault
import java.net.InetSocketAddress
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Главный класс для VK Callback API. Создаёт сервер VkCallbackGroup из переданных настроек, готовый к запуску
 *
 * @created 26.12.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

@Suppress("MemberVisibilityCanBePrivate")
class VkCallbackGroupBuilder {
	var server: VkCallbackRequestServer? = null
	var groupbotSource: GroupbotSource? = null
	var groupbot: GroupbotSource.Groupbot? = null
	var eventReadWriteBuffer: VkCallbackReadWriteBuffer? = null
	var updateProcessor: VkUpdateProcessor? = null
	var eventHandler: VkEventHandler? = null
	var requestHandler: VkCallbackRequestHandler? = null
	var path: String = "/callback"
	var port: Int = 80
	var requestsExecutor: Executor? = null
	var addressTester: AddressTester? = AddressTesterDefault()
	var expireEventTime: Long = 25_000L
	var vkTimeVsLocalTimeDiff: Long = 0L

	companion object {
		fun build(initializer: VkCallbackGroupBuilder.() -> Unit): VkCallbackGroup {
			return VkCallbackGroupBuilder().apply(initializer).buildGroupCallback()
		}
	}

	fun buildGroupCallback() : VkCallbackGroup {
		val updateProcessor = this.updateProcessor ?: VkUpdateProcessorGroupDefault(this.eventHandler ?: throw IllegalStateException("Event processor is not set"))
		val server = server ?: initDefaultServer(port, requestsExecutor?: Executors.newFixedThreadPool(4))


		val buffer = eventReadWriteBuffer?: let {

			VkCallbackReadWriteBufferDefault(1000)
		}

		val requestHandler = this.requestHandler
		if (requestHandler != null) {
			server.setHandler(path, requestHandler)
		} else {
			server.setHandler(path, VkCallbackRequestHandlerDefault(
				groupbotSource ?: GroupSourceSimple(groupbot ?: throw IllegalStateException("Neither groupbot nor groupbotSource were not set"))
				, buffer
				, addressTester
				, expireEventTime, vkTimeVsLocalTimeDiff
			))
		}

		return VkCallbackGroup(server, buffer, updateProcessor)
	}

	private fun initDefaultServer(port: Int, requestsExecutor: Executor): VkCallbackRequestServer {
		val server = HttpServer.create()
		server.bind(InetSocketAddress(port), 0)
		server.executor = requestsExecutor
		return VkCallbackRequestServerDefault(server)
	}
}