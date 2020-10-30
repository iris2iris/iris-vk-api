package iris.vk.api

import iris.vk.Options

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface IGroups<SingleType, ListType> {

	fun leave(groupId: Int): SingleType

	fun get(userId: Int? = null, extended: Boolean = false, filter: String? = null, fields: String? = null, offset: Int = 0, count: Int = 0, token: String? = null): SingleType

	fun getById(ids: Collection<Int>, groupId: Int = 0, fields: String? = null, token: String? = null): SingleType

	fun getLongPollSettings(groupId: Int, token: String? = null): SingleType

	fun setLongPollSettings(groupId: Int, options: Options?, token: String? = null): SingleType

	fun getLongPollServer(groupId: Int = 0): SingleType

	fun getBanned(groupId: Int, offset: Int = 0, count: Int = 0, fields: String? = null, ownerId: Int = 0, token: String? = null): SingleType

	fun addCallbackServer(groupId: Int, url: String, title: String, secret: String): SingleType

	fun deleteCallbackServer(groupId: Int, serverId: Int): SingleType

	fun getCallbackConfirmationCode(groupId: Int): SingleType

	fun getCallbackSettings(groupId: Int): SingleType

	fun getMembers(groupId: Int, filter: String? = null, offset: Int = 0, count: Int = 0, token: String? = null): SingleType

	fun setCallbackSettings(groupId: Int, serverId: Int, options: Options? = null): SingleType

	fun getCallbackServers(groupId: Int, serverIds: Collection<Int>? = null): SingleType

	fun isMember(groupId: Int, usersId: Collection<Int>, extended: Boolean = false, token: String? = null): SingleType

	fun isMember(groupId: Int, userId: Int, extended: Boolean = false, token: String? = null): SingleType
}