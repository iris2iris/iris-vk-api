package iris.vk.callback

class GroupSourceSimple(private val gb: GroupbotSource.Groupbot) : GroupbotSource {
	override fun isGetByRequest() = false
	override fun getGroupbot(request: VkCallbackRequestHandler.Request) = gb
	override fun getGroupbot(groupId: Int) = gb
}