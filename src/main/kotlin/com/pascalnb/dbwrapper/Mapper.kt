package com.pascalnb.dbwrapper

import org.jetbrains.annotations.Contract
import java.util.*
import java.util.function.Function
import java.util.stream.Stream

/**
 * Represents a function that maps the table resulting from a query to a given type.
 *
 * @param <T> the type
 */
@Suppress("unused")
fun interface Mapper<T> : Function<Table, T?> {

    @Throws(ClassCastException::class)
    fun orDefault(defaultValue: Any): Mapper<T> {
        @Suppress("UNCHECKED_CAST")
        return Mapper {
            this.apply(it) ?: defaultValue as T
        }
    }

    companion object {
        /**
         * Returns a mapper that will map a resulting table to a single string value.
         * <br><br>
         * Returns the value located at (0,0) in the table, or null if the table is empty.
         *
         * @return a new mapper
         */
        @JvmStatic
        fun stringValue(): Mapper<String?> {
            return singleValue(Function.identity())
        }

        /**
         * Returns a mapper that will map the string value at (0,0) of the table to a value of type T based on the given
         * map function.
         * <br><br>
         * If the table is empty, the mapper will return null. So the mapper will always receive a nonnull string.
         *
         * @param mapper the map function
         * @param <T>    the type of the mapped value
         * @return a new mapper
         */
        @Contract(pure = true)
        @JvmStatic
        fun <T> singleValue(mapper: Function<String, T>): Mapper<T> {
            return Mapper {
                if (it.isEmpty) null else mapper.apply(it[0][0]!!)
            }
        }

        /**
         * Returns a mapper that will map the string value at (0,0) of the table to a value of type T based on the given
         * map function.
         * <br><br>
         * As opposed to [Mapper.singleValue], the mapper does not check if the table is empty, so the
         * given function can receive null values.
         *
         * @param mapper the map function
         * @param <T>    the type of the mapped values
         * @return a new mapper
         */
        @JvmStatic
        fun <T> singleNullableValue(mapper: Function<String?, T>): Mapper<T> {
            return Mapper { mapper.apply(stringValue().apply(it)) }
        }

        /**
         * Returns a mapper that will map a resulting table to a list of string values.
         * <br></br><br></br>
         * If the table is empty, this will return an empty list. The string values correspond to the values in the first
         * column of the table.
         *
         * @return a new mapper
         */
        @JvmStatic
        fun stringList(): Mapper<List<String?>> {
            return valueList(Function.identity())
        }

        /**
         * Returns a mapper that will map a resulting table to a list of T values based on the given map function.
         * <br><br>
         * If the table is empty, this will return an empty list. The values correspond to the values in the first column
         * of the table.
         *
         * @param mapper the map function
         * @param <T>    the type of the mapped values
         * @return a new mapper
         */
        @Contract(pure = true)
        @JvmStatic
        fun <T> valueList(mapper: Function<String?, T>): Mapper<List<T>> {
            return Mapper {
                if (it.isEmpty) {
                    return@Mapper listOf()
                }
                val list: MutableList<T> = ArrayList()
                for (tuple in it) {
                    list.add(mapper.apply(tuple[0]))
                }
                Collections.unmodifiableList(list)
            }
        }

        /**
         * Makes a mapper out of the given function.
         *
         * @param function the function
         * @param <T>      the type of the mapper
         * @return a new mapper
         */
        @Contract(pure = true)
        @JvmStatic
        fun <T> fromFunction(function: Function<Table, T?>): Mapper<T> {
            return Mapper { function.apply(it) }
        }

        /**
         * Returns a mapper that always returns the input table.
         *
         * @return a new mapper
         */
        @Contract(pure = true)
        @JvmStatic
        fun identity(): Mapper<Table> {
            return Mapper { it }
        }

        /**
         * Returns a mapper that turns the first value into a [StringMapper]
         *
         * @return a new mapper
         */
        @Contract(pure = true)
        @JvmStatic
        fun toMapping(): Mapper<StringMapper> {
            return singleNullableValue {
                StringMapper(it)
            }
        }

        @Contract(pure = true)
        @JvmStatic
        fun <T> toPrimitive(clazz: Class<out T>): Mapper<T> {
            return Mapper {
                toMapping().apply(it)!!.to(clazz)
            }
        }

        @Contract(pure = true)
        @JvmStatic
        fun <T> toObject(clazz: Class<T>): Mapper<T> {
            return ObjectMapper(clazz)
        }

        @Contract(pure = true)
        @JvmStatic
        fun <T> toObjects(clazz: Class<T>): Mapper<List<T>> {
            return Mapper {
                ObjectMapper(clazz).applyAll(it)
            }
        }

        @Contract(pure = true)
        @JvmStatic
        fun firstRow(): Mapper<Tuple?> {
            return Mapper {
                if (it.isEmpty) null else it[0]
            }
        }

        @Contract(pure = true)
        @JvmStatic
        fun allRows(): Mapper<List<Tuple>> {
            return Mapper { it.tuples }
        }

        @Contract(pure = true)
        @JvmStatic
        fun stream(): Mapper<Stream<Tuple>> {
            return Mapper { it.tuples.stream() }
        }
    }
}