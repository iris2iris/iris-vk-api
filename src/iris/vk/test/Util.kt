package iris.vk.test

import java.io.File
import java.util.*

/**
 * @created 28.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
object TestUtil {

	const val confPath = "excl/cfg.properties"

	fun getProperties(): Properties {
		val reader = File(confPath).reader()
		val props = Properties()
		props.load(reader)
		reader.close()
		return props
	}
}