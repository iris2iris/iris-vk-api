package iris.vk.callback

/**
 * Можно взаимодействовать с любой реализацией сервера входящих запросов через данный интерфейс
 * в метод `setHandler` передаётся обработчик запросов по указанному URI
 * Данный сервер должен вызывать метод `VkCallbackRequestHandler.handle(request: Request)` каждый раз, как получает входящий запрос
 * @see VkCallbackRequestServerDefault — базовая реализация сервера входящих запросов
 */
interface VkCallbackRequestServer {

	fun setHandler(path: String, handler: VkCallbackRequestHandler)
	fun start()
	fun stop(seconds: Int)

}