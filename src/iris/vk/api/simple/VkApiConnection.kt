package iris.vk.api.simple

import iris.vk.Options

/**
 * @created 07.09.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface VkApiConnection {
	fun request(url:String, data:  Map<String, Any>?): VkApiConnectResponse?
	fun request(url:String, data:String? = null): VkApiConnectResponse?
	fun requestUpload(url:String, files:Map<String, Options>, data:  Map<String, Any>? = null): VkApiConnectResponse?

	data class VkApiConnectResponse(val code: Int, val responseText:String)
}