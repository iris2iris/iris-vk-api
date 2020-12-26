package iris.vk

import iris.vk.callback.GroupbotSource
import iris.vk.callback.GroupbotSource.Groupbot
import iris.vk.callback.VkCallbackRequestHandler

/**
 * @created 29.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class VkGroupSourceList(groups: List<Groupbot>) : GroupbotSource {

	private val groups = groups.associateBy { it.id }

	override fun isGetByRequest() = false

	override fun getGroupbot(request: VkCallbackRequestHandler.Request): Groupbot? = null

	override fun getGroupbot(groupId: Int): Groupbot? {
		return groups[groupId]
	}
}