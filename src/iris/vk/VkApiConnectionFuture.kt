package iris.vk

import iris.vk.VkApiConnection.VkApiConnectResponse
import java.util.concurrent.CompletableFuture

/**
 * @created 07.09.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface VkApiConnectionFuture {
	fun request(url:String, data: Map<String, Any>?): CompletableFuture<VkApiConnectResponse?>
	fun request(url:String, data:String? = null): CompletableFuture<VkApiConnectResponse?>
	fun requestUpload(url:String, files:Map<String, Options>, data: Map<String, Any>? = null): CompletableFuture<VkApiConnectResponse?>
}