package iris.vk

import iris.json.JsonItem
import java.net.URLDecoder
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Future
import kotlin.math.min

/**
 * @created 16.09.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class VkApiPack(val sender: Sender): VkApiFuture(sender.vkApi.token) {

	constructor(token: String) : this(Sender(VkApiFuture(token)))

	override fun request(method: String, options: Options?, token: String?, version: String?): VkFuture {
		return request(VkRequestData(method, options, token?: this.token, version?: this.version))
	}

	override fun request(method: String, options: String?, token: String?, version: String?): VkFuture {
		return request(VkRequestData(method, if (options != null ) Options(prepareParams(options)) else null, token?: this.token, version ?: this.version))
	}

	fun requestWithResult(req: VkRequestData): JsonItem? {
		val future = sender.execute(req)
		return future.get()
	}

	private fun prepareParams(params: String): Map<String, String> {
		if (params.isEmpty())
			return emptyMap()
		val params2 = params
			.split('&')
		val res = mutableMapOf<String, String>()
		for (param in params2) {
			val data = param.split('=')
			res[URLDecoder.decode(data[0], Charsets.UTF_8)] = URLDecoder.decode(data[1], Charsets.UTF_8)
		}
		return res
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
					var items: Array<JobData>
					synchronized(pillow) {
						items = queue.toArray(ex)
						if (items.isEmpty())
							pillow.wait()
						else
							queue.clear()
					}

					if (items.isNotEmpty()) {
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

		class JobData(val request: VkRequestData, val future: VkFuture)

		private fun processAll(items: Array<JobData>) {

			val tokened = mutableMapOf<String, MutableList<JobData>>()
			for (data in items) {
				val d = data.request
				val token = d.token ?: ""
				val list = tokened.getOrPut(token) { mutableListOf() }
				list += data
			}

			val localFutures = mutableListOf<Future<JsonItem?>>()
			for ((token, data) in tokened) {
				val token = if (token.isEmpty()) vkApi.token else token
				val futures = mutableListOf<VkFuture>()
				val requests = mutableListOf<VkRequestData>()
				for (it in data) {
					futures.add(it.future)
					requests.add(it.request)
				}
				val codes = vkApi.generateExecuteCode(requests, token)
				for (i in codes.indices) {
					val code = codes[i]
					val ind = i

					val f = vkApi.request(code).whenComplete { res, error ->
							if (error != null) {
								error.printStackTrace()
								for (f in futures)
									if (!f.isDone)
										f.complete(null)
							}

							val offset = ind * 25
							if (res == null || VkApi.isError(res)) {
								for (i in offset until min(futures.size, offset + 25))
									futures[i].complete(res)
							} else {
								val responses = VkApi.prepareExecuteResponses(res)
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