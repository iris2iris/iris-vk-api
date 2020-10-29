package iris.vk

import iris.json.JsonArray
import iris.json.JsonItem
import iris.vk.api.LongPollSettings
import iris.vk.api.VK_API_VERSION
import iris.vk.api.VkApis
import iris.vk.api.simple.VkApi
import iris.vk.event.*
import iris.vk.event.group.*
import java.util.*

/**
 * @created 08.09.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class VkEngineGroup(commander: VkApi, eventHandler: VkHandler, groupId: Int = 0): VkEngineUser(commander, eventHandler) {

	constructor(token: String, messageHandler: VkHandler, version: String? = null) : this(VkApi(token, version?: VK_API_VERSION), messageHandler)

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

	override fun getUpdates(lpSettings: LongPollSettings, ts: String): JsonItem? {
		return vkApi.groups.getUpdates(lpSettings, ts)
	}

	override fun getLongPollSettings(server: String, key: String, accessMode: String): LongPollSettings {
		return LongPollSettings(server, key, accessMode)
	}

	override fun processUpdates(updates: JsonArray) {

		var checkMessages: MutableList<Message>? = null
		var checkInvites: MutableList<ChatEvent>? = null
		var checkLeave: MutableList<ChatEvent>? = null
		var titleUpdaters: MutableList<TitleUpdate>? = null
		var pinUpdaters: MutableList<PinUpdate>? = null
		var checkCallbacks: MutableList<CallbackEvent>? = null

		for (update in updates) {
			val type = update["type"].asString()
			if (type == "message_new") { // это сообщение
				val message = update["object"]
				if (message["action"]["type"].isNotNull()) {
					when(message["action"]["type"].asString()) {
						"chat_invite_user" -> {
							if (checkInvites == null) checkInvites = mutableListOf()
							checkInvites!! += GroupMessage(message)
						}
						"chat_title_update" -> {
							if (titleUpdaters == null) titleUpdaters = mutableListOf()
							titleUpdaters!! += GroupTitleUpdate(message)
						}
						"chat_invite_user_by_link" -> {
							if (checkInvites == null) checkInvites = mutableListOf()
							checkInvites!! += GroupChatEvent(message)
						}
						"chat_kick_user" -> {
							if (checkLeave == null) checkLeave = mutableListOf()
							checkLeave!! += GroupChatEvent(message)
						}
						"chat_pin_message" -> {
							if (pinUpdaters == null) pinUpdaters = mutableListOf()
							pinUpdaters!! += GroupPinUpdate(message)
						}
						else -> {
							if (checkMessages == null) checkMessages = mutableListOf()
							checkMessages!! += GroupMessage(message)
						}

					}
				} else {
					if (checkMessages == null) checkMessages = mutableListOf()
					checkMessages!! += GroupMessage(message)
				}
			} else if (type == "message_event") {
				if (checkCallbacks == null) checkCallbacks = mutableListOf()
				checkCallbacks!! += GroupCallbackEvent(update["object"])
			} else {
				logger.info("Unknown type of event: $type")
			}
		}
		if (checkMessages != null)
			this.processMessages(checkMessages)
		if (checkInvites != null)
			this.processInvites(checkInvites)
		if (titleUpdaters != null)
			this.processTitleUpdates(titleUpdaters)
		if (pinUpdaters != null)
			this.processPinUpdates(pinUpdaters)
		if (checkLeave != null)
			this.processLeaves(checkLeave)
		if (checkCallbacks != null)
			processCallbacks(checkCallbacks)
	}

	private inline fun <E>mutableListOf() = LinkedList<E>()

}