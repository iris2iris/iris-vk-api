package iris.vk.multibot

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import iris.json.JsonItem
import iris.json.flow.JsonFlowParser

import java.net.InetSocketAddress
import java.util.Date
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors

/**
 * @created 08.02.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

// TODO: Проверить
class VkMultibotCallbackEngine(
		private val gbSource: GroupbotSource,
		private val addressTester: AddressTester? = null,
		private val port: Int = 8000,
		private val path: String = "/callback",
		private val queryKeyTest: Regex? = null,
		private val expireEventTime: Long = 25_000L,
) : VkMultibotRetrieveEngine {

	interface AddressTester {
		fun isGoodAddress(address: String): Boolean
	}

	interface GroupbotSource {
		fun getGroupbot(code: String): Groupbot?
		fun getGroupbot(): Groupbot?

		class Groupbot(val id: Int, val confirmation: String, val secret: String?)

		class SimpleGroupSource(private val gb: Groupbot) : GroupbotSource {
			override fun getGroupbot(code: String) = gb
			override fun getGroupbot() = gb
		}
	}

	companion object {
		private const val DEBUG = false
	}

	private val queue: ArrayBlockingQueue<JsonItem> = ArrayBlockingQueue(1000)
	private val queueWait = Object()

	override fun start() {
		val server = HttpServer.create()
		server.bind(InetSocketAddress(port), 0)

		server.executor = Executors.newFixedThreadPool(20)
		server.createContext(path, CallbackHandler())
		server.start()
		if (DEBUG)
			println("CALLBACK SERVER STARTED. PORT: $port")
	}

	override fun retrieve(wait: Boolean): Array<JsonItem> {
		synchronized(queueWait) {
			do {
				if (queue.size != 0) {
					val res = queue.toTypedArray()
					if (DEBUG)
						println("Sending ${queue.size}")
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
			val fwd = request.requestHeaders.getFirst("X-Real-IP")
			val host = fwd ?: request.remoteAddress.address.hostAddress

			if (addressTester?.isGoodAddress(host) == false) {
				if (DEBUG)
					println("Bad test address: $host")
				writeResponse(request, "ok")
				return
			}

			val groupbot =
				if (queryKeyTest != null) {
					val requestGroupCode = queryKeyTest.matchEntire(request.requestURI.path)?.groupValues?.get(1)
					if (requestGroupCode == null) {
						println("Bad request query: " + request.requestURI.path)
						writeResponse(request, "ok")
						return
					}

					gbSource.getGroupbot(requestGroupCode)

				} else
					gbSource.getGroupbot()

				if (groupbot == null) {
					writeResponse(request, "ok")
					return
				}

			val body = getBody(request)

			try {
				if (body.isEmpty()) {
					writeResponse(request, "ok")
					return
				}
				val groupId: Int
				val event = JsonFlowParser.start(body)

				groupId = event["group_id"].asInt()
				if (groupId != groupbot.id) {
					println("Group ID from code is not identical with response object: obj=" + groupId + " vs gb=" + groupbot.id)
					writeResponse(request, "ok")
					return
				}

				if (event["type"].equals("confirmation")) {
					val res = groupbot.confirmation
					println("Test confirmation. Group ID: vk.com/club$groupId")
					writeResponse(request, res)
					return
				}

				if (groupbot.secret != event["secret"].asString()) {
					println("Secret is wrong: group id ${groupbot.id}")
					writeResponse(request, "ok")
					return
				}

				writeResponse(request, "ok")
				event["groupbot"] = groupbot
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
					println(body + "\n" + event)
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
					if (DEBUG) {
						synchronized(exp) {
							expired = 0
						}
					}
				} else {
					if (DEBUG) {
						synchronized(exp) {
							expired++
						}
						if (expired % 50 == 0L)
							print("Expired " + Date() + " " + expired)
					}
				}
			} catch (e: Exception) {
				e.printStackTrace()
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