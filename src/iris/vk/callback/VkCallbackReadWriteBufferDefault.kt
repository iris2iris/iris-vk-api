package iris.vk.callback

import iris.json.JsonItem
import java.util.concurrent.ArrayBlockingQueue
import java.util.logging.Logger

/**
 * @created 02.11.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class VkCallbackReadWriteBufferDefault(queueSize: Int) : VkCallbackReadWriteBuffer {

	private val queue: ArrayBlockingQueue<JsonItem> = ArrayBlockingQueue(queueSize)
	private val queueWait = Object()

	companion object {
		private val logger = Logger.getLogger("iris.vk")
	}

	override fun send(event: JsonItem) {
		synchronized(queueWait) {
			if (queue.offer(event))
				queueWait.notify()
			else {
				logger.warning { "Callback API queue is full (${queue.size} elements). Clearing..." }
				queue.clear()
			}
		}
	}

	override fun retrieve(wait: Boolean): List<JsonItem> {
		synchronized(queueWait) {
			do {
				if (queue.size != 0) {
					val res = queue.toList()
					queue.clear()
					return res
				}
				if (!wait)
					return emptyList()
				queueWait.wait()
			} while (true)
		}
	}
}