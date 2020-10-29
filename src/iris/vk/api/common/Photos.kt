package iris.vk.api.common

import iris.vk.Options
import iris.vk.api.IPhotos
import iris.vk.api.Requester

/**
 * @created 29.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
abstract class Photos<SingleType, ListType>(api: Requester<SingleType, ListType>) : SectionAbstract<SingleType, ListType>(api), IPhotos<SingleType, ListType> {

	override fun saveMessagesPhotoByObject(responseImage: Options, token: String?): SingleType {
		return request("photos.saveMessagesPhoto",
			Options("photo" to responseImage["photo"],
					"server" to responseImage["server"],
					"hash" to responseImage["hash"]
			),
			token
		)
	}

	override fun getMessagesUploadServer(peerId: Int, token: String?): SingleType {
		return request("photos.getMessagesUploadServer", if (peerId != 0 ) Options("peer_id" to peerId) else null, token)
	}

	override fun getUploadServer(albumId: Int, groupId: Int?, token: String?): SingleType {
		val params = Options("album_id" to albumId)
		if (groupId != null)
			params["group_id"] = groupId
		return request("photos.getUploadServer", params, token)
	}

	override fun saveByObject(responseImage: Options, albumId: Int, groupId: Int?, caption: String?, options: Options?, token: String?): SingleType {
		val params = options ?: Options()
		params["album_id"] = albumId
		if (groupId != null)
			params["group_id"] = groupId
		if (caption != null)
			params["caption"] = caption
		params["photos_list"] = responseImage["photos_list"]
		params["server"] = responseImage["server"]
		params["hash"] = responseImage["hash"]

		return request("photos.save", params, token)
	}

	override fun getWallUploadServer(userId: Int?, groupId: Int?, token: String?): SingleType {
		val params = Options()
		if (userId != null)
			params["user_id"] = userId
		if (groupId != null)
			params["group_id"] = groupId
		return request("photos.getWallUploadServer", params, token)
	}

	override fun saveWallPhotoByObject(responseImage: Options, userId: Int?, groupId: Int?, token: String?): SingleType {
		return request(
				"photos.saveWallPhoto",
				Options("user_id" to userId,
						"group_id" to groupId,
						"photo" to responseImage["photo"],
						"server" to responseImage["server"],
						"hash" to responseImage["hash"]
				),
				token
		)
	}

	override fun copy(ownerId: Int, photoId: Int, accessKey: Int?, token: String?): SingleType {
		val params = Options("owner_id" to ownerId, "photo_id" to photoId)
		if (accessKey != null)
			params["access_key"] = accessKey
		return request("photos.copy", params, token)
	}

}