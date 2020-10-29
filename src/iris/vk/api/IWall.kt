package iris.vk.api

import iris.vk.Options

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface IWall<SingleType, ListType> {
	fun get(ownerId: Int, offset: Int = 0, count: Int = 100): SingleType

	fun delete(id: Int): SingleType

	fun deleteComment(ownerId: Int, commentId: Int, token: String? = null): SingleType

	fun reportComment(ownerId: Int, commentId: Int, reason: Int = 0, token: String? = null): SingleType

	fun post(ownerId: Int, message: String?, fromGroup: Boolean = false, options: Options? = null): SingleType

	fun getComments(ownerId: Int, postId: Int, offset: Int = 0, count: Int = 100): SingleType

	fun createComment(ownerId: Int, postId: Int, text: String?, options: Options? = null, token: String? = null): SingleType

	fun getReposts(ownerId: Int, postId: Int, offset: Int = 0, count: Int = 10, token: String? = null): SingleType
}