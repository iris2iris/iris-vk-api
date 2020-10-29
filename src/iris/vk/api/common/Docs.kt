package iris.vk.api.common

import iris.vk.Options
import iris.vk.api.IDocs
import iris.vk.api.Requester

/**
 * @created 29.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
abstract class Docs<SingleType, ListType>(api: Requester<SingleType, ListType>) : SectionAbstract<SingleType, ListType>(api), IDocs<SingleType, ListType> {

	override fun getMessagesUploadServer(peerId: Int, type: String?, token: String?): SingleType {
		val options = Options("peer_id" to peerId)
		 if (type != null)
			 options["type"] =type
		return request("docs.getMessagesUploadServer", options, token)
	}

	override fun save(file: String, title: String?, tags: String?, token: String?): SingleType {
		return request("docs.save", Options("file" to file), token)
	}

	override fun getWallUploadServer(groupId: Int, token: String?): SingleType {
		return request("docs.getWallUploadServer", Options("group_id" to groupId), token)
	}


	override fun add(ownerId: Int, docId: Int, accessKey: String?, token: String?): SingleType {
		val params = Options("owner_id" to ownerId, "doc_id" to docId)
		if (accessKey != null)
			params["access_key"] = accessKey
		return request("docs.add", params, token)
	}
}