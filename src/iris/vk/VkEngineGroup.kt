package iris.vk

import iris.json.JsonItem
import iris.vk.api.LongPollSettings
import iris.vk.api.VK_API_VERSION
import iris.vk.api.VkApis
import iris.vk.api.simple.VkApi

/**
 * @created 08.09.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class VkEngineGroup(commander: VkApi, updateProcessor: VkUpdateProcessor, groupId: Int = 0): VkEngineUser(commander, updateProcessor) {

	constructor(token: String, messageHandler: VkHandler, version: String? = null) : this(VkApi(token, version?: VK_API_VERSION), VkUpdateProcessorGroupDefault(messageHandler))

	private val groupId =
		if (groupId == 0) {
			val res = commander.groups.getById(emptyList()) ?: throw IllegalStateException("Can't connect to vk.com")
			if (VkApis.isError(res)) {
				throw IllegalStateException(VkApis.errorString(res))
			}
			res["response"][0]["id"].asInt()
		} else
			groupId

	override fun getLongPollServer(): JsonItem? {
		return vkApi.groups.getLongPollServer(groupId)
	}

	override fun getLongPollSettings(server: String, key: String, accessMode: String): LongPollSettings {
		return LongPollSettings(server, key, accessMode)
	}

}