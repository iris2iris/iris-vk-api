package iris.vk.api.common

import iris.vk.Options
import iris.vk.api.IGroups
import iris.vk.api.Requester

open class Groups<SingleType, ListType>(api: Requester<SingleType, ListType>) : SectionAbstract<SingleType, ListType>(api), IGroups<SingleType, ListType> {

	override fun leave(groupId: Int): SingleType {
		return request("groups.leave", Options("group_id" to groupId))
	}

	override fun get(userId: Int?, extended: Boolean, filter: String?, fields: String?, offset: Int, count: Int, token: String?): SingleType {
		val params = Options()
		if (userId != null) params["user_id"] = userId
		if (extended) params["extended"] = "1"
		if (filter != null) params["filter"] = filter
		if (fields != null) params["fields"] = fields
		if (offset != 0) params["offset"] = offset
		if (count != 0) params["count"] = count
		return request("groups.get", params, token)
	}

	override fun getById(ids: Collection<Int>, groupId: Int, fields: String?, token: String?): SingleType {
		val options = Options("group_ids" to ids.joinToString { it.toString() })
		if (groupId != 0) options["group_id"] to groupId
		if (fields != null) options["fields"] to fields
		return request("groups.getById", options, token)
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

	override fun getBanned(groupId: Int, offset: Int, count: Int, fields: String?, ownerId: Int, token: String?): SingleType {
		val options = Options()
		options["group_id"] = groupId
		if (offset != 0) options["offset"] = offset
		if (count != 0) options["count"] = count
		if (fields != null) options["fields"] = fields
		if (ownerId != 0) options["owner_id"] = ownerId
		return request("groups.getBanned", options, token)
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

	override fun getMembers(groupId: Int, filter: String?, offset: Int, count: Int, token: String?): SingleType {
		val options = Options("group_id" to groupId)
		if (filter != null) options["filter"] = filter
		if (offset != 0) options["offset"] = offset
		if (count != 0) options["count"] = count

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

	override fun getCallbackServers(groupId: Int, serverIds: Collection<Int>?): SingleType {
		val options = Options("group_id" to groupId)
		if (serverIds != null)
			options["server_ids"] = serverIds.joinToString(",")
		return request("groups.getCallbackServers", options)
	}

	override fun isMember(groupId: Int, usersId: Collection<Int>, extended: Boolean, token: String?): SingleType {
		val options = Options("user_ids" to usersId.joinToString(","), "group_id" to groupId)
		if (extended) options += "extended" to extended
		return request("groups.isMember", options, token)
	}

	override fun isMember(groupId: Int, userId: Int, extended: Boolean, token: String?): SingleType {
		val options = Options("user_id" to userId, "group_id" to groupId)
		if (extended) options += "extended" to extended
		return request("groups.isMember", options, token)
	}


}