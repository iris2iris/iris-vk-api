package iris.vk

import iris.json.JsonArray
import iris.json.JsonItem
import iris.json.proxy.JsonProxyValue
import iris.json.plain.IrisJsonObject
import iris.vk.VkApi.Companion.peer2ChatId
import iris.vk.VkApi.LongPollSettings
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
			if (VkApi.isError(res)) {
				throw IllegalStateException(VkApi.errorString(res))
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

		var checkMessages: MutableList<VkMessage>? = null
		var checkInvites: MutableList<VkMessage>? = null
		var titleUpdaters: MutableList<VkMessage>? = null
		var pinUpdaters: MutableList<VkMessage>? = null
		var checkLeave: MutableList<VkMessage>? = null

		for (update in updates) {
			if (update["type"].asString() == "message_new") { // это сообщение
				val message = update["object"]
				if (message["action"]["type"].isNotNull()) {
					when(message["action"]["type"].asString()) {
						"chat_invite_user" -> {
							if (checkInvites == null) checkInvites = mutableListOf()
							checkInvites.add(VkMessage(
								IrisJsonObject(
									"peer_id" to message["peer_id"],
									"user_id" to message["action"]["member_id"],
									"chat_id" to JsonProxyValue(peer2ChatId(message["peer_id"].asInt())),
									"from_id" to message["from_id"],
									"date" to message["date"],
									"conversation_message_id" to message["conversation_message_id"]
								)
							))
						}
						"chat_title_update" -> {
							if (titleUpdaters == null) titleUpdaters = mutableListOf()
							titleUpdaters.add(VkMessage(message))
						}
						"chat_invite_user_by_link" -> {
							if (checkInvites == null) checkInvites = mutableListOf()
							checkInvites.add(VkMessage(
								IrisJsonObject(
									"peer_id" to message["peer_id"],
									"user_id" to message["from_id"],
									"chat_id" to JsonProxyValue(peer2ChatId(message["peer_id"].asInt())),
									"from_id" to message["from_id"],
									"date" to message["date"],
									"conversation_message_id" to message["conversation_message_id"]
								)
							))
						}
						"chat_kick_user" -> {
							if (checkLeave == null) checkLeave = mutableListOf()
							checkLeave.add(VkMessage(
								IrisJsonObject(
									"peer_id" to message["peer_id"],
									"user_id" to message["action"]["member_id"],
									"chat_id" to JsonProxyValue(peer2ChatId(message["peer_id"].asInt())),
									"from_id" to message["from_id"],
									"date" to message["date"],
									"conversation_message_id" to message["conversation_message_id"]
								)
							))
						}
						"chat_pin_message" -> {
							if (pinUpdaters == null) pinUpdaters = mutableListOf()
							pinUpdaters.add(VkMessage(message))
						}
						else -> {
							if (checkMessages == null) checkMessages = mutableListOf()
							checkMessages.add(VkMessage(message))
						}

					}
				} else {
					if (checkMessages == null) checkMessages = mutableListOf()
					checkMessages.add(VkMessage(message))
				}
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
	}

	private inline fun <E>mutableListOf() = LinkedList<E>()

}