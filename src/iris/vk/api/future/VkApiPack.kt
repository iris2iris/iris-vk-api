package iris.vk.api.future

import iris.json.JsonItem
import iris.vk.Options
import iris.vk.api.VkApis
import iris.vk.api.VkRequestData
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Future
import kotlin.math.min

/**
 * @created 16.09.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class VkApiPack(private val sender: Sender): VkApiFuture(sender.vkApi.token) {

	constructor(token: String) : this(Sender(VkApiFuture(token)))

	override fun request(method: String, options: Options?, token: String?): VkFuture {
		return request(VkRequestData(method, options, token?: this.token))
	}

	override fun request(req: VkRequestData): VkFuture {
		return sender.execute(req)
	}

	override fun execute(data: List<VkRequestData>, token: String?): VkFutureList {
		return VkFutureList(sender.executeAll(data))
	}

	class Sender(val vkApi: VkApiFuture = VkApiFuture("")) {

		private val queue = ArrayBlockingQueue<JobData>(3000)
		private val pillow = Object()
		private val ex = emptyArray<JobData>()
		private val thread = object : Thread() {

			override fun run() {
				while (true) {
					val items: Array<JobData>? = synchronized(pillow) {
						if (queue.isEmpty()) {
							pillow.wait()
							null
						} else {
							val items = queue.toArray(ex)
							queue.clear()
							items
						}
					}

					if (items != null) {
						this@Sender.processAll(items)
					}
				}
			}
		}

		init {
			thread.start()
		}

		fun execute(req: VkRequestData): VkFuture {
			val fut = VkFuture()
			synchronized(pillow) {
				queue.put(JobData(req, fut))
				pillow.notify()
			}
			return fut
		}

		fun executeAll(data: List<VkRequestData>): List<VkFuture> {
			val futRes = mutableListOf<VkFuture>()
			synchronized(pillow) {
				for (it in data) {
					val fut = VkFuture(it)
					futRes.add(fut)
					queue.put(JobData(it, fut))
				}
				pillow.notify()
			}
			return futRes
		}

		private class JobData(val request: VkRequestData, val future: VkFuture)

		private fun processAll(items: Array<JobData>) {

			val tokenMap = mutableMapOf<String, MutableList<JobData>>()
			for (data in items) {
				val d = data.request
				val token = d.token ?: ""
				val list = tokenMap.getOrPut(token) { mutableListOf() }
				list += data
			}

			val localFutures = mutableListOf<Future<JsonItem?>>()
			for ((token, data) in tokenMap) {
				val token = if (token.isEmpty()) vkApi.token else token
				val futures = mutableListOf<VkFuture>()
				val requests = mutableListOf<VkRequestData>()
				for (it in data) {
					futures.add(it.future)
					requests.add(it.request)
				}
				val codes = VkApis.generateExecuteCode(requests, token, vkApi.version)
				for (i in codes.indices) {
					val code = codes[i]
					val ind = i

					val f = vkApi.request(code).whenCompleteAsync { res, error ->
						if (error != null) {
							error.printStackTrace()
							for (f in futures)
								if (!f.isDone)
									f.complete(null)
						}

						val offset = ind * 25
						if (res == null || VkApis.isError(res)) {
							for (i in offset until min(futures.size, offset + 25))
								futures[i].complete(res)
						} else {
							val responses = VkApis.prepareExecuteResponses(res)
							for (i in responses.indices) {
								futures[i + offset].complete(responses[i])
							}

							if (futures.size < responses.size + offset) {
								for (i in responses.size until futures.size)
									futures[i].complete(null)
							} else if (offset > 0) {
							}
						}
					}

					localFutures.add(f)
				}
			}
			for (f in localFutures)
				f.get()
		}
	}
}