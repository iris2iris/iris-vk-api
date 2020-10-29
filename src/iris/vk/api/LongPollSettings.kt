package iris.vk.api

import iris.vk.Options

/**
 * @created 28.10.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
open class LongPollSettings(server: String, key: String, mode: String, wait: Int = 10) {

	private var link = "$server?act=a_check&key=$key&wait=$wait&mode=$mode"

	open fun getUpdatesLink(ts: String): String {
		return "$link&ts=$ts"
	}

	companion object {
		fun build(data: Options): LongPollSettings {
			return LongPollSettings(data.getString("server"), data.getString("key"), data.getString("mode"), data.getIntOrNull("wait")?: 10)
		}
	}

}