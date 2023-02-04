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

            if (clazz == String::class.javaObjectType) {
                return string
            }
            if (clazz == Int::class.javaObjectType || clazz == Int::class.javaPrimitiveType) {
                return string.toInt()
            }
            if (clazz == Boolean::class.javaObjectType || clazz == Boolean::class.javaPrimitiveType) {
                return string.toBoolean() || "t" == string || "1" == string
            }
            if (clazz == Double::class.javaObjectType || clazz == Double::class.javaPrimitiveType) {
                return string.toDouble()
            }
            if (clazz == Long::class.javaObjectType || clazz == Long::class.javaPrimitiveType) {
                return string.toLong()
            }
            if (clazz == Float::class.javaObjectType || clazz == Float::class.javaPrimitiveType) {
                return string.toFloat()
            }
            if (clazz == Char::class.javaObjectType || clazz == Char::class.javaPrimitiveType) {
                return string[0]
            }
            if (clazz == Short::class.javaObjectType || clazz == Short::class.javaPrimitiveType) {
                return string.toShort()
            }
            throw UnsupportedOperationException("Cannot cast string to $clazz")
        }
    }
}