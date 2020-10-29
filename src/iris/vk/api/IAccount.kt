package iris.vk.api

/**
 * @created 29.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
interface IAccount<SingleType> {
	fun ban(id: Int): SingleType
}