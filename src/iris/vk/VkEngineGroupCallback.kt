package iris.vk

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import iris.json.JsonItem
import iris.json.flow.JsonFlowParser
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors
import java.util.logging.Logger

/**
 * @created 08.02.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

open class VkEngineGroupCallback (
		private val gbSource: GroupbotSource,
		private val addressTester: AddressTester? = null,
		private val port: Int = 80,
		private val path: String = "/callback",
		private val expireEventTime: Long = 25_000L,
) : VkRetrievable {

	interface AddressTester {
		fun isGoodHost(address: String): Boolean
	}

	interface GroupbotSource {
		fun getGroupbot(uri: URI): Groupbot?

		class Groupbot(val id: Int, val confirmation: String, val secret: String?)

		class SimpleGroupSource(private val gb: Groupbot) : GroupbotSource {
			override fun getGroupbot(uri: URI) = gb
		}
	}

	companion object {
		private val logger = Logger.getLogger("iris.vk")
		var loggingExpired = false
	}

	private val queue: ArrayBlockingQueue<JsonItem> = ArrayBlockingQueue(1000)
	private val queueWait = Object()

	override fun start() {
		val server = HttpServer.create()
		server.bind(InetSocketAddress(port), 0)

		server.executor = Executors.newFixedThreadPool(20)
		server.createContext(path, CallbackHandler())
		server.start()
		logger.fine {"CALLBACK SERVER STARTED. PORT: $port" }
	}

	override fun retrieve(wait: Boolean): Array<JsonItem> {
		synchronized(queueWait) {
			do {
				if (queue.size != 0) {
					val res = queue.toTypedArray()
					queue.clear()
					return res
				}
				if (!wait)
					return emptyArray()
				queueWait.wait()
			} while (true)
		}
	}

	protected fun getHost(request: HttpExchange): String {
		val fwd = request.requestHeaders.getFirst("X-Real-IP")
		val host = fwd ?: request.remoteAddress.address.hostAddress
		return host
	}

	inner class CallbackHandler : HttpHandler {

		private var expired: Long = 0
		private val exp = Any()

		override fun handle(request: HttpExchange) {
			logger.finest("Callback API event")

			if (addressTester != null) {
				val host = getHost(request)
				if (!addressTester.isGoodHost(host)) {
					logger.info {"Bad request address: $host" }
					writeResponse(request, "ok")
					return
				}
			}

			val groupbot = gbSource.getGroupbot(request.requestURI)
			if (groupbot == null) {
				logger.info {"Groupbot not found. " + request.requestURI }
				writeResponse(request, "ok")
				return
			}

			val body = getBody(request)

			try {
				if (body.isEmpty()) {
					logger.fine {"Body was empty" }
					writeResponse(request, "ok")
					return
				}
				val groupId: Int
				val event = JsonFlowParser.start(body)

				groupId = event["group_id"].asInt()
				if (groupId != groupbot.id) {
					logger.warning {"Group ID from code is not identical with response object: obj=" + groupId + " vs gb=" + groupbot.id }
					writeResponse(request, "ok")
					return
				}

				if (event["type"].equals("confirmation")) {
					val res = groupbot.confirmation
					logger.fine {"Test confirmation. Group ID: $groupId" }
					writeResponse(request, res)
					return
				}

				if (!groupbot.secret.isNullOrEmpty() && groupbot.secret != event["secret"].asStringOrNull()) {
					logger.info {"Secret is wrong: group id ${groupbot.id}" }
					writeResponse(request, "ok")
					return
				}

				writeResponse(request, "ok")
				//event["groupbot"] = groupbot
				//event["groupbot"] = groupbot
				var testDate = true
				val obj = try {
					when (event["type"].asString()) {
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
						queue.offer(event)
						queueWait.notify()
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
			val os = request.responseBody
			os.write(bytes)
			os.close()
			request.close()
		}

		private fun getBody(request: HttpExchange): String {
			val reader = request.requestBody.reader()
			val res= reader.readText()
			reader.close()
			return res
		}
	}


}