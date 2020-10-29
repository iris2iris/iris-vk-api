package iris.vk.api.common

import iris.vk.Options
import iris.vk.api.IGroups
import iris.vk.api.LongPollSettings
import iris.vk.api.Requester

open class Groups<SingleType, ListType>(api: Requester<SingleType, ListType>) : SectionAbstract<SingleType, ListType>(api), IGroups<SingleType, ListType> {
	override fun leave(groupId: Int): SingleType {
		return request("groups.leave", Options("group_id" to groupId))
	}

	override fun get(userId: Int?, extended: Boolean, filter: String?, fields: String?, offset: Int, count: Int, token: String?): SingleType {
		val params = Options()
		if (userId != null)
			params["user_id"] = userId
		if (extended)
			params["extended"] = extended
		if (filter != null)
			params["filter"] = filter
		if (fields != null)
			params["fields"] = fields
		if (offset != 0)
			params["offset"] = offset
		if (count != 0)
			params["count"] = count
		return request("groups.get", params, token)
	}

	override fun getById(ids: List<String>, fields: String?, token: String?): SingleType {
		return request("groups.getById", Options("group_ids" to ids.joinToString(","), "fields" to fields), token)
	}

	override fun getLongPollSettings(groupId: Int, token: String?): SingleType {
		return request("groups.getLongPollSettings", Options("group_id" to groupId), token)
	}

	override fun setLongPollSettings(groupId: Int, options: Options?, token: String?): SingleType {
		val options = options?: Options()
		options["group_id"] = groupId
		return request("groups.setLongPollSettings", options, token)
	}

	override fun getLongPollServer(groupId: Int): SingleType {
		return request("groups.getLongPollServer", Options("group_id" to groupId))
	}

	override fun getUpdates(lpSettings: LongPollSettings, ts: String): SingleType {
		return api.requestUrl(lpSettings.getUpdatesLink(ts), "groups.getUpdates")
	}

	override fun getBanned(groupId: Int, token: String?): SingleType {
		return request("groups.getBanned", Options("group_id" to groupId), token)
	}

	override fun addCallbackServer(groupId: Int, url: String, title: String, secret: String): SingleType {
		return request("groups.addCallbackServer", Options("group_id" to groupId, "url" to url, "title" to title, "secret_key" to secret))
	}

	override fun deleteCallbackServer(groupId: Int, serverId: Int): SingleType {
		return request("groups.deleteCallbackServer", Options("group_id" to groupId, "server_id" to serverId))
	}

	override fun getCallbackConfirmationCode(groupId: Int): SingleType {
		return request("groups.getCallbackConfirmationCode", Options("group_id" to groupId))
	}

	override fun getMembers(groupId: Int, filter: String?, offset: String?, count: String?, token: String?): SingleType {
		val options = Options("group_id" to groupId)
		if (filter != null)
			options["filter"] = filter
		if (offset != null)
			options["offset"] = offset
		if (count != null)
			options["count"] = count

		return request("groups.getMembers", options, token)
	}

	override fun setCallbackSettings(groupId: Int, serverId: Int, options: Options?): SingleType {
		val params = Options("group_id" to groupId, "server_id" to serverId)
		if (options != null)
			params.putAll(options)
		return request("groups.setCallbackSettings", params)

	}

	override fun getCallbackSettings(groupId: Int): SingleType {
		return request("groups.getCallbackSettings", Options("group_id" to groupId))
	}

	override fun getCallbackServers(groupId: Int, serverIds: List<Int>?): SingleType {
		val options = Options("group_id" to groupId)
		if (serverIds != null)
			options["server_ids"] = serverIds.joinToString(",")
		return request("groups.getCallbackServers", options)
	}

	override fun isMember(idUsers: List<Int>, groupId: Int): SingleType {
		return request("groups.isMember", Options("user_ids" to idUsers.joinToString(","), "group_id" to groupId))
	}

	override fun isMember(idUser: Int, groupId: Int): SingleType {
		return request("groups.isMember", Options("user_id" to idUser, "group_id" to groupId))
	}


}