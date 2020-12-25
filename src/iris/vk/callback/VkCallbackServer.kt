package iris.vk.callback

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import iris.json.JsonItem
import iris.json.flow.JsonFlowParser
import iris.vk.callback.VkCallbackServer.Server.Request
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.logging.Logger

/**
 * Движок, получающий события от VK Callback API.
 *
 * Собирает события в очередь для дальнейшей выдачи их методом [retrieve]
 *
 * @created 08.02.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

open class VkCallbackServer(
		private val server: Server,
		private val gbSource: GroupbotSource,
		path: String = "/callback",
		private val addressTester: AddressTester? = VkAddressTesterDefault(),
		private var eventWriter: VkCallbackEventWriter? = null,
		expireEventTime: Long = 25_000L,
		vkTimeVsLocalTimeDiff: Long = 0L
)  {

	constructor(
		gbSource: GroupbotSource,
		addressTester: AddressTester? = VkAddressTesterDefault(),
		port: Int = 80,
		path: String = "/callback",
		requestsExecutor: Executor = Executors.newFixedThreadPool(4),
		eventWriter: VkCallbackEventWriter? = null,
		expireEventTime: Long = 25_000L,
		vkTimeVsLocalTimeDiff: Long = 0L
	) : this(initDefaultServer(port, requestsExecutor), gbSource, path, addressTester, eventWriter, expireEventTime, vkTimeVsLocalTimeDiff)

	private val expireEventTime = expireEventTime - vkTimeVsLocalTimeDiff

	init {
		this.server.setHandler(path, CallbackHandler())
	}

	interface VkCallbackEventWriter {
		fun send(event: JsonItem)
	}

	/**
	 * Проверяет подлинность источника входящего запроса
	 *
	 * [VkAddressTesterDefault] — реализует проверку входящего адреса на принадлежность указанным подсетям. По умолчанию
	 * `95.142.192.0/21` и `2a00:bdc0::/32`
	 *
	 * @see VkAddressTesterDefault
	 */
	interface AddressTester {

		/**
		 * Проверяет подлинность источника.
		 */
		fun isGoodHost(request: Request): Boolean

		/**
		 * Должен вернуть IP адрес реального источника. Если запрос происходит от источника через прокси,например, Cloudflare
		 * или локальный проброс порта.
		 *
		 * Вызывается исключительно для логгирования неизвестных IP адресов
		 */
		fun getRealHost(request: Request): String
	}

	/**
	 * Можно взаимодействовать с любой реализацией сервера входящих запросов через данный интерфейс
	 * в метод `setHandler` передаётся обработчик запросов по указанному URI
	 * Данный сервер должен вызывать метод `Handler.handle(request: Request)` каждый раз, как получает входящий запрос
	 * @see DefaultServer — базовая реализация сервера входящих запросов
	 */
	interface Server {

		fun setHandler(path: String, handler: Handler)
		fun start()
		fun stop(seconds: Int)

		interface Request {
			/**
			 * Необходимо для запроса
			 */
			fun findHeader(key: String): String?
			val requestUri: URI
			val remoteAddress: InetSocketAddress

			fun writeResponse(response: String, code: Int = 200)

			/**
			 * Текст входящего запроса
			 */
			fun body(): String

		}

		interface Handler {
			fun handle(request: Request)
		}
	}

	interface GroupbotSource {
		/**
		 * Указывает, содержится ли информация о группе в URI или query запроса.
		 *
		 * Полезно фильтровать ложные запросы, не тратя ресурсы на извлечение информации из JSON
		 */
		fun isGetByRequest(): Boolean

		/**
		 * Извлекает информацию о группе из запроса, содержащуюся в URI или query.
		 *
		 * Например, URI может содержать такую информацию `/callback/fa33a6`, где код `fa33a6` сопоставляется с
		 * одной из имеющихся групп.
		 *
		 * Выполняется в случае `isGetByRequest() == true`
		 */
		fun getGroupbot(request: Request): Groupbot?

		/**
		 * Извлекает информацию о группе по её ID.
		 *
		 * Выполняется в случае `isGetByRequest() == false`
		 */
		fun getGroupbot(groupId: Int): Groupbot?

		class Groupbot(val id: Int, val confirmation: String, val secret: String?)

		class SimpleGroupSource(private val gb: Groupbot) : GroupbotSource {
			override fun isGetByRequest() = false
			override fun getGroupbot(request: Request) = gb
			override fun getGroupbot(groupId: Int) = gb
		}
	}

	companion object {
		var loggingExpired = true

		private val logger = Logger.getLogger("iris.vk")

		private fun initDefaultServer(port: Int, requestsExecutor: Executor): Server {
			val server = HttpServer.create()
			server.bind(InetSocketAddress(port), 0)
			server.executor = requestsExecutor
			return DefaultServer(server)
		}
	}

	/**
	 * Обёртка для HttpServer и обработки входящих запросов
	 */
	private class DefaultServer(private val server: HttpServer): HttpHandler, Server {

		private class DefaultRequest(private val request: HttpExchange) : Request {

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

		private lateinit var handler: Server.Handler

		override fun setHandler(path: String, handler: Server.Handler) {
			this.handler = handler
			server.createContext(path, this)
		}

		override fun handle(exchange: HttpExchange) {
			handler.handle(DefaultRequest(exchange))
		}

		override fun start() {
			server.start()
		}

		override fun stop(seconds: Int) {
			server.stop(seconds)
		}
	}

	fun start() {
		server.start()
	}

	fun stop() {
		server.stop(5)
	}

	fun setEventWriter(eventWriter: VkCallbackEventWriter) {
		this.eventWriter = eventWriter
	}

	private inner class CallbackHandler : Server.Handler {

		private var expired = 0
		private val exp = Any()

		private inline fun ok(request: Request) {
			request.writeResponse("ok", 200)
		}

		override fun handle(request: Request) {
			logger.finest {"Callback API event from " + request.remoteAddress }

			if (addressTester != null) {
				if (!addressTester.isGoodHost(request)) {
					logger.info { "Unknown host trying to send Callback API event: " + addressTester.getRealHost(request) }
					ok(request)
					return
				}
			}

			var groupbot = if (gbSource.isGetByRequest()) {
				val groupbot = gbSource.getGroupbot(request)
				if (groupbot == null) {
					logger.info { "Groupbot not found. " + request.requestUri }
					ok(request)
					return
				}
				groupbot
			} else
				null // значит информацию о группе возьмём позже из JSON ответа в поле group_id

			val body = request.body()

			try {
				if (body.isEmpty()) {
					logger.fine {"Body was empty" }
					ok(request)
					return
				}

				val event: JsonItem = JsonFlowParser.start(body)

				val groupId = event["group_id"].asInt()
				if (groupbot == null) {
					groupbot = gbSource.getGroupbot(groupId)
					if (groupbot == null) {
						logger.info { "Groupbot not found. " + request.requestUri }
						ok(request)
						return
					}

				} else { // groupbot был получен из информации запроса (URI/query), поэтому нужно дополнительно проверить, совпадают ли данные группы
					if (groupId != groupbot.id) {
						logger.info { "Group ID from code is not identical with response object: obj=" + groupId + " vs gb=" + groupbot.id }
						ok(request)
						return
					}
				}

				val type = event["type"].asString()
				if (type == "confirmation") {
					val res = groupbot.confirmation
					logger.finest {"Test confirmation. Group ID: $groupId" }
					request.writeResponse(res, 200)
					return
				}

				ok(request)
				val eventWriter = eventWriter ?: return

				if (groupbot.secret != null && groupbot.secret != event["secret"].asStringOrNull()) {
					logger.info {"Secret is wrong: group id ${groupbot.id}" }
					return
				}

				var testDate = true
				val obj = try {
					when (type) {
						"message_new" -> {
							val obj = event["object"]
							obj["message"].let {
								when {
									it.isNotNull() -> it
									obj["text"].isNotNull() -> obj
									else -> return
								}
							}
						}
						"message_reply" -> event["object"]
						"message_edit" -> event["object"]
						"message_event" -> {
							testDate = false; event["object"]
						}
						else -> {
							logger.info { "Unknown event type $type" }
							return
						}
					}
				} catch (e: Exception) {
					logger.severe { e.stackTraceToString() }
					return
				}
				val suitsTime = if (testDate) {
					val date = obj["date"].asLong()
					val curTime = System.currentTimeMillis()
					date * 1000 > curTime - expireEventTime
				} else
					true

				if (suitsTime) {
					// отправляем событие
					eventWriter.send(event)

					if (loggingExpired) {
						synchronized(exp) {
							expired = 0
						}
					}
				} else {
					if (loggingExpired) {
						synchronized(exp) {
							expired++
						}
						if (expired % 50 == 1)
							logger.info { "Expired $expired" }
					}
				}
			} catch (e: Exception) {
				logger.severe { e.stackTraceToString() }
				ok(request)
			}
		}
	}
}