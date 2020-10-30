package iris.vk.test

import java.io.File
import java.util.*
import java.util.logging.LogManager

/**
 * @created 28.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
object TestUtil {

	const val confPath = "excl/cfg.properties"

	fun getProperties(): Properties {
		val props = Properties()
		File(confPath).reader().use { props.load(it) }
		return props
	}

	fun init() {
		initLogger()
	}

	private fun initLogger() {
		val ist = this.javaClass.getResourceAsStream("logger.properties")
		LogManager.getLogManager().readConfiguration(ist)
		ist.close()
	}
}