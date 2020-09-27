package iris.vk.test

import java.text.SimpleDateFormat
import java.util.logging.Formatter
import java.util.logging.LogRecord
import java.util.logging.StreamHandler

/**
 * @created 27.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class MyLoggerConsole : StreamHandler(System.out, F()) {

	companion object {
		val dateFormat = SimpleDateFormat("YYYY-MM-dd HH:mm:ss")
	}

	override fun publish(record: LogRecord?) {
		super.publish(record)
		flush()
	}

	private class F : Formatter() {
		override fun format(record: LogRecord): String? {
			var source: String?
			if (record.sourceClassName != null) {
				source = record.sourceClassName
				if (record.sourceMethodName != null) {
					source += "::" + record.sourceMethodName
				}
			} else {
				source = record.loggerName
			}
			val message = formatMessage(record)
			val throwable = record.thrown?.stackTraceToString()

			return record.level.name + "\t" + dateFormat.format((record.instant.toEpochMilli())) + " $source\n-- $message" + (if (!throwable.isNullOrEmpty()) "\n" +  throwable else "") + '\n'
		}
	}
}