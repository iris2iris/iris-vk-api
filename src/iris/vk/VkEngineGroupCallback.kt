package iris.vk

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import iris.json.JsonItem
import iris.json.flow.JsonFlowParser
import java.net.InetSocketAddress
import java.util.concurrent.ArrayBlockingQueue
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

open class VkEngineGroupCallback (
		private val gbSource: GroupbotSource,
		private val addressTester: AddressTester? = null,
		private val port: Int = 80,
		private val path: String = "/callback",
		private val requestsExecutor: Executor = Executors.newFixedThreadPool(4),
		queueSize: Int = 1000,
		expireEventTime: Long = 25_000L,
		vkTimeVsLocalTimeDiff: Long = 0L
) : VkRetrievable {

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
		fun isGoodHost(request: HttpExchange): Boolean

		/**
		 * Должен вернуть IP адрес реального источника. Если запрос происходит от источника через прокси,например, Cloudflare
		 * или локальный проброс порта.
		 *
		 * Вызывается исключительно для логгирования неизвестных IP адресов
		 */
		fun getRealHost(request: HttpExchange): String
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
		fun getGroupbot(request: HttpExchange): Groupbot?

		/**
		 * Извлекает информацию о группе по её ID.
		 *
		 * Выполняется в случае `isGetByRequest() == false`
		 */
		fun getGroupbot(groupId: Int): Groupbot?

		class Groupbot(val id: Int, val confirmation: String, val secret: String?)

		class SimpleGroupSource(private val gb: Groupbot) : GroupbotSource {
			override fun isGetByRequest() = false
			override fun getGroupbot(request: HttpExchange) = gb
			override fun getGroupbot(groupId: Int) = gb
		}
	}

	companion object {
		var loggingExpired = false

		private val logger = Logger.getLogger("iris.vk")
	}

	private val queue: ArrayBlockingQueue<JsonItem> = ArrayBlockingQueue(queueSize)
	private val queueWait = Object()
	private val expireEventTime = expireEventTime - vkTimeVsLocalTimeDiff

	override fun start() {
		val server = HttpServer.create()
		server.bind(InetSocketAddress(port), 0)

		server.executor = requestsExecutor
		server.createContext(path, CallbackHandler())
		server.start()
		logger.fine {"CALLBACK SERVER STARTED. PORT: $port" }
	}

	override fun retrieve(wait: Boolean): Array<JsonItem> {
		synchronized(queueWait) {
			do {
				if (queue.size != 0) {
					val res = queue.toArray(arrayOfNulls<JsonItem>(queue.size)) as Array<JsonItem>
					queue.clear()
					return res
				}
				if (!wait)
					return emptyArray()
				queueWait.wait()
			} while (true)
		}
	}

	inner class CallbackHandler : HttpHandler {

		private var expired: Long = 0
		private val exp = Any()

		override fun handle(request: HttpExchange) {
			logger.finest {"Callback API event from " + request.remoteAddress }

			if (addressTester != null) {
				if (!addressTester.isGoodHost(request)) {
					logger.info { "Unknown host trying to send Callback API event: " + addressTester.getRealHost(request) }
					writeResponse(request, "ok")
					return
				}
			}

			var groupbot = if (gbSource.isGetByRequest()) {
				val groupbot = gbSource.getGroupbot(request)
				if (groupbot == null) {
					logger.info { "Groupbot not found. " + request.requestURI }
					writeResponse(request, "ok")
					return
				}
				groupbot
			} else
				null // значит информацию о группе возьмём позже из JSON ответа в поле group_id

			val body = getBody(request)

			try {
				if (body.isEmpty()) {
					logger.fine {"Body was empty" }
					writeResponse(request, "ok")
					return
				}

				val event: JsonItem = JsonFlowParser.start(body)

				val groupId = event["group_id"].asInt()
				if (groupbot == null) {
					groupbot = gbSource.getGroupbot(groupId)
					if (groupbot == null) {
						logger.info { "Groupbot not found. " + request.requestURI }
						writeResponse(request, "ok")
						return
					}

				} else { // groupbot был получен из информации запроса (URI/query), поэтому нужно дополнительно проверить, совпадают ли данные группы
					if (groupId != groupbot.id) {
						logger.info { "Group ID from code is not identical with response object: obj=" + groupId + " vs gb=" + groupbot.id }
						writeResponse(request, "ok")
						return
					}
				}

				val type = event["type"].asString()
				if (type == "confirmation") {
					val res = groupbot.confirmation
					logger.finest {"Test confirmation. Group ID: $groupId" }
					writeResponse(request, res)
					return
				}

				writeResponse(request, "ok")

				if (groupbot.secret != null && groupbot.secret != event["secret"].asStringOrNull()) {
					logger.info {"Secret is wrong: group id ${groupbot.id}" }
					return
				}

				var testDate = true
				val obj = try {
					when (type) {
						"message_new" -> (if (event["object"]["message"].isNotNull()) event["object"]["message"] else if (event["object"]["text"].isNotNull()) event["object"] else return)
						"message_reply" -> event["object"]
						"message_edit" -> event["object"]
						"message_event" -> {
							testDate = false; event["object"]
						}
						else -> return
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
					synchronized(queueWait) {
						if (queue.offer(event))
							queueWait.notify()
						else {
							logger.warning { "Callback API queue is full (${queue.size} elements). Clearing..." }
							queue.clear()
						}
					}
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
						if (expired % 50 == 0L)
							logger.info { "Expired $expired" }
					}
				}
			} catch (e: Exception) {
				logger.severe { e.stackTraceToString() }
				writeResponse(request, "ok")
			}

		}

		private fun writeResponse(request: HttpExchange, str: String, rCode: Int = 200) {
			val bytes = str.toByteArray()
			request.sendResponseHeaders(rCode, bytes.size.toLong())
			request.responseBody.use { it.write(bytes) }
			request.close()
		}

		private fun getBody(request: HttpExchange): String {
			return request.requestBody.reader().use { it.readText() }
		}
	}


}