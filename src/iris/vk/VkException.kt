package iris.vk

import iris.json.JsonItem

/**
 * @created 08.09.2019
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class VkException(val message: String, val code: Int = 0, val requestParams: Any? = null) {

	companion object {
		fun create(error: Any?, options: Options? = null): VkException {
			if (error !is JsonItem) return VkException("", 0)
			val error = error["error"]
			val errorParams = if (options == null && !error["request_params"][0].isNull()) error["request_params"].asMap() else options
			return VkException(error["error_msg"].asString(), error["error_code"].asInt(), errorParams)
		}
	}

	override fun toString(): String {
		return "$message ($code)"
	}
}