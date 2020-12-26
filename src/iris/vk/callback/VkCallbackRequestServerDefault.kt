package iris.vk.callback

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.URI

/**
 * Обёртка для HttpServer и обработки входящих запросов
 */
class VkCallbackRequestServerDefault(private val server: HttpServer): HttpHandler, VkCallbackRequestServer {

	private class DefaultRequest(private val request: HttpExchange) : VkCallbackRequestHandler.Request {

		override val requestUri: URI = request.requestURI

		override val remoteAddress: InetSocketAddress = request.remoteAddress

		override fun findHeader(key: String): String? {
			return request.requestHeaders.getFirst(key)
		}

		override fun writeResponse(response: String, code: Int) {
			writeResponsePrivate(request, response, code)
		}

		override fun body(): String {
			return request.requestBody.reader().use { it.readText() }
		}

		private fun writeResponsePrivate(request: HttpExchange, str: String, rCode: Int = 200) {
			val bytes = str.toByteArray()
			request.sendResponseHeaders(rCode, bytes.size.toLong())
			request.responseBody.use { it.write(bytes) }
			request.close()
		}
	}

	private lateinit var handler: VkCallbackRequestHandler

	override fun setHandler(path: String, handler: VkCallbackRequestHandler) {
		this.handler = handler
		server.createContext(path, this)
	}

	override fun handle(exchange: HttpExchange) {
		handler.handle(DefaultRequest(exchange))
	}

	override fun start() {
		try {
			server.start()
		} catch (e: IllegalStateException) {
			e.printStackTrace()
		}
	}

	override fun stop(seconds: Int) {
		server.stop(seconds)
	}
}