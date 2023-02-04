package com.pascalnb.dbwrapper

import org.jetbrains.annotations.Contract
import java.util.*
import java.util.function.Function

@Suppress("unused")
class StringMapper(`object`: Any?) {
    private var string: String?

    init {
        string = `object`?.toString()
    }

    fun <T> to(type: T?): T? {
        @Suppress("UNCHECKED_CAST")
        return if (type == null) {
            null
        } else cast(string, type.javaClass) as T?
    }

    fun <T> to(clazz: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return cast(string, clazz) as T?
    }

    fun <T> applyIfNotNull(mapper: Function<String, T>): T? {
        return if (string == null) {
            null
        } else mapper.apply(string!!)
    }

    fun <T> apply(mapper: Function<String?, T>): T {
        return mapper.apply(string)
    }

    fun <T> asOrDefault(fallback: T): T {
        @Suppress("UNCHECKED_CAST")
        return if (string == null) {
            fallback
        } else cast(string, fallback!!::class.java) as T
    }

    fun toNullableString(): String? {
        return string
    }

    override fun toString(): String {
        return string!!
    }

    fun toInt(): Int {
        return cast(string!!, Integer.TYPE) as Int
    }

    fun toBoolean(): Boolean {
        return cast(string!!, java.lang.Boolean.TYPE) as Boolean
    }

    fun toDouble(): Double {
        return cast(string!!, java.lang.Double.TYPE) as Double
    }

    fun toLong(): Long {
        return cast(string!!, java.lang.Long.TYPE) as Long
    }

    fun toFloat(): Float {
        return cast(string!!, java.lang.Float.TYPE) as Float
    }

    fun toChar(): Char {
        return cast(string!!, Character.TYPE) as Char
    }

    fun toShort(): Short {
        return cast(string!!, java.lang.Short.TYPE) as Short
    }

    val isNull = string == null

    companion object {

        @Contract(value = "null, _ -> null; !null, _ -> !null", pure = true)
        private fun cast(string: String?, clazz: Class<*>): Any? {
            if (string == null) {
                return null
            }
            if (clazz == String::class.java) {
                return string
            }
            if (clazz == Integer.TYPE || clazz == Int::class.java) {
                return string.toInt()
            }
            if (clazz == java.lang.Boolean.TYPE || clazz == Boolean::class.java) {
                return string.toBoolean() || "t" == string || "1" == string
            }
            if (clazz == java.lang.Double.TYPE || clazz == Double::class.java) {
                return string.toDouble()
            }
            if (clazz == java.lang.Long.TYPE || clazz == Long::class.java) {
                return string.toLong()
            }
            if (clazz == java.lang.Float.TYPE || clazz == Float::class.java) {
                return string.toFloat()
            }
            if (clazz == Character.TYPE || clazz == Char::class.java) {
                return string[0]
            }
            if (clazz == java.lang.Short.TYPE || clazz == Short::class.java) {
                return string.toShort()
            }
            throw UnsupportedOperationException("Cannot cast string to $clazz")
        }
    }
}