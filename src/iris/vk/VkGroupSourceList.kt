package iris.vk

import iris.vk.callback.VkCallbackServer
import iris.vk.callback.VkCallbackServer.GroupbotSource
import iris.vk.callback.VkCallbackServer.GroupbotSource.Groupbot

/**
 * @created 29.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class VkGroupSourceList(groups: List<Groupbot>) : GroupbotSource {

	private val groups = groups.associateBy { it.id }

	override fun isGetByRequest() = false

	override fun getGroupbot(request: VkCallbackServer.Server.Request): Groupbot? = null

	override fun getGroupbot(groupId: Int): Groupbot? {
		return groups[groupId]
	}
}