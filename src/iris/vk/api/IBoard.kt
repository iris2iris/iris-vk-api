package iris.vk.api

import iris.vk.Options

/**
 * @created 29.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface IBoard<SingleType> {
	fun getComments(groupId: Int, topicId: Int, startCommentId: Int, options: Options? = null, token: String? = null): SingleType
}