package iris.vk.callback

import java.net.InetSocketAddress
import java.net.URI

/**
 * @created 26.12.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface VkCallbackRequestHandler {
	fun handle(request: Request)

	interface Request {
		fun findHeader(key: String): String?
		val requestUri: URI
		val remoteAddress: InetSocketAddress

		fun writeResponse(response: String, code: Int = 200)

		fun body(): String
	}
}