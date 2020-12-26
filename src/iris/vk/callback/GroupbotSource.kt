package iris.vk.callback

interface GroupbotSource {
		/**
		 * Указывает, содержится ли информация о группе в URI или query запроса.
		 *
		 * Полезно фильтровать ложные запросы, не тратя ресурсы на извлечение информации из JSON
		 */
		fun isGetByRequest(): Boolean

		/**
		 * Извлекает информацию о группе из запроса, содержащуюся в URI или query.
		 *
		 * Например, URI может содержать такую информацию `/callback/fa33a6`, где код `fa33a6` сопоставляется с
		 * одной из имеющихся групп.
		 *
		 * Выполняется в случае `isGetByRequest() == true`
		 */
		fun getGroupbot(request: VkCallbackRequestHandler.Request): Groupbot?

		/**
		 * Извлекает информацию о группе по её ID.
		 *
		 * Выполняется в случае `isGetByRequest() == false`
		 */
		fun getGroupbot(groupId: Int): Groupbot?

		class Groupbot(val id: Int, val confirmation: String, val secret: String?)


	}