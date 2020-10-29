package iris.vk.api.common

import iris.vk.Options
import iris.vk.api.IBoard
import iris.vk.api.Requester

/**
 * @created 29.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class Board<SingleType, ListType>(api: Requester<SingleType, ListType>) : SectionAbstract<SingleType, ListType>(api), IBoard<SingleType> {
	override fun getComments(groupId: Int, topicId: Int, startCommentId: Int, options: Options?, token: String?): SingleType {
		val options = options?: Options()
		options["group_id"] = groupId
		options["topic_id"] = topicId
		options["start_comment_id"] = startCommentId
		if (!options.containsKey("count"))
			options["count"] = 1
		return request("board.getComments", options, token)
	}
}