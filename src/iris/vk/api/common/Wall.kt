package iris.vk.api.common

import iris.vk.Options
import iris.vk.api.IWall
import iris.vk.api.Requester

/**
 * @created 29.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class Wall<SingleType, ListType>(api: Requester<SingleType, ListType>) : SectionAbstract<SingleType, ListType>(api), IWall<SingleType, ListType> {

	override fun get(ownerId: Int, offset: Int, count: Int): SingleType {
		return request("wall.get", Options("count" to count, "filter" to "all", "owner_id" to ownerId, "offset" to offset))
	}

	override fun delete(id: Int): SingleType {
		return request("wall.delete", Options("post_id" to id))
	}

	override fun deleteComment(ownerId: Int, commentId: Int, token: String?): SingleType {
		return request("wall.deleteComment", Options("owner_id" to ownerId, "comment_id" to commentId), token)
	}

	override fun reportComment(ownerId: Int, commentId: Int, reason: Int, token: String?): SingleType {
		return request("wall.reportComment", Options("owner_id" to ownerId, "comment_id" to commentId, "reason" to reason), token)
	}

	override fun post(ownerId: Int, message: String?, fromGroup: Boolean, options: Options?): SingleType {
		val params = options?: Options()
		params["owner_id"] = ownerId
		params["from_group"] = if (fromGroup) "1" else "0"
		if (message != null)
			params["message"] = message

		return request("wall.post", params)
	}

	override fun getComments(ownerId: Int, postId: Int, offset: Int, count: Int): SingleType {
		return request("wall.getComments", Options("owner_id" to ownerId, "post_id" to postId, "offset" to offset, "count" to count))
	}

	override fun createComment(ownerId: Int, postId: Int, text: String?, options: Options?, token: String?): SingleType {
		val params = Options()
		if (options != null)
			params.putAll(options)
		params["owner_id"] = ownerId
		params["post_id"] =postId
		if (text != null)
			params["text"] = text
		return request("wall.createComment", params, token)
	}

	override fun getReposts(ownerId: Int, postId: Int, offset: Int, count: Int, token: String?): SingleType {
		val options = Options("owner_id" to ownerId, "post_id" to postId)
		if (offset != 0)
			options["offset"] = offset
		if (count != 0)
			options["count"] = count
		return request("wall.getReposts", options, token)
	}
}