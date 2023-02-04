package com.pascalnb.dbwrapper

import com.pascalnb.dbwrapper.annotation.ParseField
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.*

class ObjectMapper<T>(clazz: Class<T>) : Mapper<T> {
    private val constructor: Constructor<T>
    private val pairs: MutableList<Array<Any>> = ArrayList()

    init {
        try {
            constructor = clazz.getDeclaredConstructor()
            constructor.isAccessible = true
        } catch (e: NoSuchMethodException) {
            throw NoSuchMethodException("No constructor with 0 parameters found")
        }

        for (field in clazz.declaredFields) {
            field.isAccessible = true
            val parseField = field.getAnnotation(ParseField::class.java) ?: continue

            require(field.modifiers.and(Modifier.FINAL) == 0) {
                "Final fields cannot be annotated with " + ParseField::class.java.name
            }

            val parseFieldName = parseField.value
            pairs.add(arrayOf(field, if ("" == parseFieldName) field.name else parseFieldName))
        }

        require(pairs.isNotEmpty()) {
            clazz.toString() + " does not have non-final fields annotated with " + ParseField::class.java
        }

    }

    private fun rowToInstance(row: Tuple): T {
        val instance = constructor.newInstance()
        for (pair in pairs) {
            val value = StringMapper(row[pair[1] as String])
            val field = pair[0] as Field
            val parsed = value.to(field.type)
            field[instance] = parsed
        }
        return instance
    }

    fun applyAll(table: Table): List<T> {
        return try {
            val list: MutableList<T> = ArrayList()
            for (row in table) {
                list.add(rowToInstance(row))
            }
            Collections.unmodifiableList(list)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun apply(table: Table): T? {
        if (table.isEmpty) {
            return null
        }
        val row = table.getRow(0)
        return rowToInstance(row)
    }
}