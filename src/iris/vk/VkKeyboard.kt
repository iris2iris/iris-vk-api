@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package iris.vk

import iris.json.JsonEncoder

/**
 * @created 08.02.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
object VkKeyboard {

	const val COLOR_PRIMARY		= "primary"
	const val COLOR_SECONDARY	= "secondary"
	const val COLOR_NEGATIVE	= "negative"
	const val COLOR_POSITIVE	= "positive"

	const val CALLBACK_DEFAULT	= 0
	const val CALLBACK_SILENT	= 1
	const val CALLBACK_MANUAL_HANDLE	= 2

	fun text(label: String, payload: Options, color: String = COLOR_PRIMARY): Options {
		return text(label, JsonEncoder.encode(payload), color)
	}

	fun text(label: String, payload: String = "", color: String = COLOR_PRIMARY): Options {
		return Options(
			"action" to Options(
				"type" to "text"
				, "label" to label
				, "payload" to payload
			)
			, "color" to color
		)
	}

	fun callback(label: String, payload: Options, color: String = COLOR_PRIMARY): Options {
		return callback(label, JsonEncoder.encode(payload), color)
	}

	fun callback(label: String, payload: String = "", color: String = COLOR_PRIMARY): Options {
		return Options(
			"action" to Options(
				"type" to "callback"
				, "label" to label
				, "payload" to payload
			)
			, "color" to color
		)
	}

	fun callbackCommand(label: String, command: String, forId: Int = 0, type: Int = CALLBACK_DEFAULT, color: String = COLOR_PRIMARY): Options {
		return callback(label, cbCmd(command, forId, type), color)
	}

	fun textCommand(label: String, command: String, forId: Int = 0, color: String = COLOR_PRIMARY): Options {
		return text(label, cmd(command, forId), color)
	}

	fun cbCmd(command: String, forId: Int = 0, type: Int = CALLBACK_DEFAULT): String {
		val std = Options("command" to command)
		if (forId != 0) std["for_id"] = forId
		if (type != 0) std["type"] = type
		return JsonEncoder.encode(std)
	}

	fun cmd(command: String, forId: Any? = 0): String {
		val std = Options("command" to command)
		if (forId != 0)
			std["for_id"] = forId
		return JsonEncoder.encode(std)
	}

	fun location(payload: String = "", color: String = COLOR_PRIMARY): Options {
		return Options("action" to Options(
			"type" to "location"
			, "payload" to payload
		), "color" to color
		)
	}

	fun vkpay(hash: String, payload: String = "", color: String = COLOR_PRIMARY): Options {
		return Options("action" to Options(
			"type" to "vkpay"
			, "payload" to payload
			, "hash" to hash
		), "color" to color
		)
	}

	fun vkapp(appId: Int, label: String, ownerId: Int = 0, hash: String = "", payload: String = "", color: String = COLOR_PRIMARY): Options {
		return Options("action" to Options(
			"type" to "open_app"
			, "app_id" to appId
			, "owner_id" to ownerId
			, "payload" to payload
			, "label" to label
			, "hash" to hash
		), "color" to color
		)
	}

	fun link(link: String, label: String, payload: String = ""): Options {
		return Options("action" to Options(
			"type" to "open_link"
			, "payload" to payload
			, "label" to label
			, "link" to link
		)
		)
	}

	fun create(buttons: List<Any>, inline: Boolean = true, oneTime: Boolean = false): Options {

		val res = Options("buttons" to buttons)
		if (inline) res["inline"] = inline
		else res["one_time"] = oneTime
		return res
	}

	fun create(buttons: Array<Any>, inline: Boolean = true, oneTime: Boolean = false): Options {
		val res = Options("buttons" to buttons)
		if (inline)
			res["inline"] = inline
		else
			res["one_time"] = oneTime
		return res
	}

	fun createJson(buttons: List<Any>, inline: Boolean = true, oneTime: Boolean = false): String {
		return JsonEncoder.encode(create(buttons, inline, oneTime))
	}

	fun createJson(buttons: Array<Any>, inline: Boolean = true, oneTime: Boolean = false): String {
		return JsonEncoder.encode(create(buttons, inline, oneTime))
	}

	fun createSingleButtonJson(button: Any, inline: Boolean = true, oneTime: Boolean = false): String {
		return JsonEncoder.encode(create(arrayOf<Any>(arrayOf(button)), inline, oneTime))
	}

	fun clearKeyboardJson(): String {
		return """{"buttons":[], "one_time":true}"""
	}

	fun carousel(elements: List<CarouselElement>): String {
		val elementsStd = mutableListOf<Options>()
		for (e in elements) {
			val el = Options()
			if (!e.title.isNullOrEmpty())
				el["title"] = e.title
			if (!e.description.isNullOrEmpty())
				el["description"] = e.description
			if (!e.photo_id.isNullOrEmpty())
				el["photo_id"] = e.photo_id
			if (!e.photo_id.isNullOrEmpty())
				el["photo_id"] = e.photo_id
			if (!e.buttons.isNullOrEmpty())
				el["buttons"] = e.buttons
			if (!e.action.isNullOrEmpty())
				el["action"] = e.action

			elementsStd.add(el)
		}
		val data = Options(
			"type" to "carousel"
			, "elements" to elementsStd
		)
		return JsonEncoder.encode(data)
	}

	class CarouselElement(val title: String? = null, val description: String? = null, val photo_id: String? = null, val buttons: List<Options>? = null, val action: Options? = null)
}