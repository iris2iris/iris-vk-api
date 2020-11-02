package iris.vk.callback

import iris.vk.*

/**
 * @created 02.11.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class VkCallbackGroup(
		private val callbackServer: VkCallbackServer,
		private val updateProcessor: VkUpdateProcessor,
		private val retriever: VkRetrievable = createQueue(callbackServer, 1000)
) : Runnable {

	constructor(callbackServer: VkCallbackServer, messageHandler: VkEventHandler, queueSize: Int = 1000)
			: this(callbackServer, defaultUpdateProcessor(messageHandler), createQueue(callbackServer, queueSize))

	companion object {

		fun defaultUpdateProcessor(messageHandler: VkEventHandler): VkUpdateProcessor {
			return VkUpdateProcessorGroupDefault(messageHandler)
		}

		private fun createQueue(callbackServer: VkCallbackServer, queueSize: Int): VkRetrievable {
			val queue = VkCallbackEventReaderWriterDefault(queueSize)
			callbackServer.setEventWriter(queue)
			return queue
		}
	}

	protected var working = true

	override fun run() {
		working = true
		callbackServer.start()
		val thisThread = Thread.currentThread()
		while (!thisThread.isInterrupted && working) {
			val items = retriever.retrieve()
			if (items.isEmpty()) continue
			updateProcessor.processUpdates(items)
		}
	}

	private var thread: Thread? = null

	fun startPolling() {
		thread = Thread(this)
		thread!!.start()
	}

	fun stop() {
		working = false
		thread?.interrupt()
		callbackServer.stop()
	}
}