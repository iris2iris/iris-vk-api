package iris.vk

import iris.json.JsonArray
import iris.json.JsonItem
import iris.vk.api.LongPollSettings
import iris.vk.api.VK_BOT_ERROR_WRONG_TOKEN
import iris.vk.api.simple.VkApi
import java.util.logging.Logger

/**
 * @created 08.09.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */

open class VkPollingUser(protected val vkApi: VkApi, protected val updateProcessor: VkUpdateProcessor) : Runnable {

	constructor(api: VkApi, eventHandler: VkEventHandler) : this(api, VkUpdateProcessorUserDefault(api, eventHandler))

	constructor(token: String, eventHandler: VkEventHandler) : this(VkApi(token), eventHandler)

	private var isWorking = true

	companion object {
		val logger = Logger.getLogger("iris.vk")!!
	}

	private var thread: Thread? = null

	open fun startPolling() {
		thread = Thread(this)
		thread!!.start()
	}

	open fun join() {
		thread?.join()
	}

	override fun run() {
		isWorking = true
		val thisThread = Thread.currentThread()
		while (!thisThread.isInterrupted && isWorking) {
			try {
				runInternal()
			} catch (e: Throwable) {
				errorCaught(e)
			}
		}
	}

	protected fun errorCaught(e: Throwable) {
		e.printStackTrace()
	}

	private fun runInternal() {
		var longPoll = this.getLongPollServer()
		if (longPoll == null) {
			logger.warning("FAIL AUTH")
			return
		}
		if (longPoll["response"].isNull()) {
			logger.warning("No start response!")
			return
		}

		logger.fine("Server received. Starting listening")

		var lastTs = longPoll["response"]["ts"].asString()
		val accessMode = (2 + 8).toString()
		var longPollSettings = getLongPollSettings(longPoll["response"]["server"].asString(), longPoll["response"]["key"].asString(), accessMode)

		val thisThread = Thread.currentThread()
		loop@ while (!thisThread.isInterrupted && this.isWorking)  {
			val updates = getUpdates(longPollSettings, lastTs)

			if (updates == null) {
				longPoll = getLongPollServer()

				if (longPoll == null) {
					logger.warning("FAIL AUTH")
					return
				}

				if (longPoll["response"].isNull()) {
					logger.warning("NO RESPONSE")
					return
				}
				val response = longPoll["response"]
				longPollSettings = getLongPollSettings(response["server"].asString(), response["key"].asString(), accessMode)
				lastTs = response["ts"].asString()
				continue
			}

			if (updates["updates"].isNull()) {
				if (!updates["failed"].isNull()) {
					when (updates["failed"].asInt()) {
						2 -> {
							// истёк срок ссылки
							logger.info("Long poll connection expired. Rebuilding")
							longPoll = getLongPollServer()

							if (longPoll == null) {
								logger.warning("FAIL AUTH")
								return
							}

							if (longPoll["response"].isNull()) {
								logger.warning("NO RESPONSE")
								return
							}

							val response = longPoll["response"]
							longPollSettings = getLongPollSettings(response["server"].asString(), response["key"].asString(), accessMode)
							lastTs = longPoll["response"]["ts"].asString()
							continue@loop
						} 1 -> { // обновляем TS
							lastTs = longPoll!!["response"]["ts"].asString()
							continue@loop
						} 3 -> { // обновляем TS
							logger.info { updates["error"].asString() + ". Try to rebuild" }
							longPoll = getLongPollServer()

							if (longPoll == null) {
								logger.warning("FAIL AUTH")
								return
							}

							if (longPoll["response"].isNull()) {
								logger.warning("NO RESPONSE")
								return
							}

							val response = longPoll["response"]
							longPollSettings = getLongPollSettings(response["server"].asString(), response["key"].asString(), accessMode)
							lastTs = response["ts"].asString()
							continue@loop
						} else -> {
							logger.warning("Как мы здесь оказались???")
							return
						}
					}

				} else if (!updates["error"].isNull()) {
					if (updates["error"]["error_code"].asInt() == VK_BOT_ERROR_WRONG_TOKEN
						|| updates["error"]["error_msg"].asString() == "User authorization failed: access_token has expired."
					) {
						logger.warning("Нет токена?")
						return
					} else {
						return
					}
				} else {
					logger.warning("YOU ARE HERE. SEEMS SOMETHING WRONG")
					return
				}
			}
			lastTs = updates["ts"].asString()
			updateProcessor.processUpdates((updates["updates"] as JsonArray).getList())
		}
	}

	protected open fun getLongPollSettings(server: String, key: String, accessMode: String): LongPollSettings {
		return LongPollSettings("https://$server", key, accessMode)
	}

	protected open fun getLongPollServer(): JsonItem? {
		return vkApi.messages.getLongPollServer()
	}

	protected open fun getUpdates(lpSettings: LongPollSettings, ts: String): JsonItem? {
		val response = vkApi.connection.request(lpSettings.getUpdatesLink(ts)) ?: return null
		if (response.code != 200) return null
		return vkApi.parser(response.responseText)
	}

	fun stop() {
		this.isWorking = false
		thread?.interrupt()
	}

}


