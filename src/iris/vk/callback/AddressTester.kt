package iris.vk.callback

/**
 * Проверяет подлинность источника входящего запроса
 *
 * [AddressTesterDefault] — реализует проверку входящего адреса на принадлежность указанным подсетям. По умолчанию
 * `95.142.192.0/21` и `2a00:bdc0::/32`
 *
 * @see AddressTesterDefault
 */
interface AddressTester {

	/**
	 * Проверяет подлинность источника.
	 */
	fun isGoodHost(request: VkCallbackRequestHandler.Request): Boolean

	/**
	 * Должен вернуть IP адрес реального источника. Если запрос происходит от источника через прокси,например, Cloudflare
	 * или локальный проброс порта.
	 *
	 * Вызывается исключительно для логгирования неизвестных IP адресов
	 */
	fun getRealHost(request: VkCallbackRequestHandler.Request): String
}