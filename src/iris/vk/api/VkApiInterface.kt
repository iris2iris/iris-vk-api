package iris.vk.api

import iris.json.JsonItem
import iris.vk.Options

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface VkApiInterface<SingleType, ListType> {
	val messages: IMessages<SingleType, ListType>
	val friends: IFriends<SingleType, ListType>
	val groups: IGroups<SingleType, ListType>
	val users: IUsers<SingleType, ListType>
	val photos: IPhotos<SingleType, ListType>
	val docs: IDocs<SingleType, ListType>
	val wall: IWall<SingleType, ListType>
}