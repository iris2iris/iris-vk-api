package iris.vk.callback

import iris.json.JsonItem
import iris.json.flow.JsonFlowParser
import iris.vk.callback.VkCallbackRequestHandler.Request
import java.util.logging.Logger

/**
 * @created 26.12.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class VkCallbackRequestHandlerDefault(
		private val gbSource: GroupbotSource,
		private var eventConsumer: VkCallbackEventConsumer,
		private val addressTester: AddressTester? = AddressTesterDefault(),
		expireEventTime: Long = 25_000L,
		vkTimeVsLocalTimeDiff: Long = 0L
) : VkCallbackRequestHandler {

	private val expireEventTime = expireEventTime - vkTimeVsLocalTimeDiff
	private var expired = 0
	private val exp = Any()

	companion object {
		var loggingExpired = true

		private val logger = Logger.getLogger("iris.vk")
	}

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
			val eventWriter = eventConsumer ?: return

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