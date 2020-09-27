@file:Suppress("UNCHECKED_CAST", "unused")

package iris.vk

/**
 * @created 26.09.2020
 * @author [Ivan Ivanov](https://vk.com/irisism)
 */
class Options : HashMap<String, Any?> {

    constructor() : super()

    constructor(vararg pairs: Pair<String, Any?>): super(pairs.size) {
        putAll(pairs)
    }

    constructor(m: Map<out String, Any?>) : super(m)

    fun getString(key: String): String = this[key] as String
    fun getStringOrNull(key: String): String? = this[key] as? String
    fun <T>getList(key: String): List<T> {
        return when (val obj = this[key]) {
            is List<*> -> obj as List<T>
            else -> (obj as Collection<T>).toList()
        }
    }

    fun getIntOrNull(key: String): Int? {
        return when (val it = this[key]) {
            is Int -> it
            is Number -> it.toInt()
            else -> null
        }
    }
    fun getInt(key: String): Int {
        return when (val it = this[key]) {
            is Int -> it
            else -> (it as Number).toInt()
        }
    }

    fun getLongOrNull(key: String): Long? {
        return when (val it = this[key]) {
            is Long -> it
            is Number -> it.toLong()
            else -> null
        }
    }
    fun getLong(key: String): Long {
        return when (val it = this[key]) {
            is Long -> it
            else -> (it as Number).toLong()
        }
    }

    fun getDoubleOrNull(key: String): Double? {
        return when (val it = this[key]) {
            is Double -> it
            is Number -> it.toDouble()
            else -> null
        }
    }
    fun getDouble(key: String): Double {
        return when (val it = this[key]) {
            is Double -> it
            else -> (it as Number).toDouble()
        }
    }

    fun getFloatOrNull(key: String): Float? {
        return when (val it = this[key]) {
            is Float -> it
            is Number -> it.toFloat()
            else -> null
        }
    }
    fun getFloat(key: String): Float {
        return when (val it = this[key]) {
            is Float -> it
            else -> (it as Number).toFloat()
        }
    }

    fun getByteOrNull(key: String): Byte? {
        return when (val it = this[key]) {
            is Byte -> it
            is Number -> it.toByte()
            else -> null
        }
    }
    fun getByte(key: String): Byte {
        return when (val it = this[key]) {
            is Byte -> it
            else -> (it as Number).toByte()
        }
    }
}