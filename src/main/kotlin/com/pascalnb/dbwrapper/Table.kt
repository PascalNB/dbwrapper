package com.pascalnb.dbwrapper

import org.jetbrains.annotations.Contract
import java.util.*
import java.util.function.Consumer

/**
 * All query consumers accept this data type.
 * Any implementation of Database should create a Table for each query.
 *
 * @param attributes the names of the attributes
 * @param tuples     the tuples with values
 */
class Table(
    val attributes: Array<String>, tuples: List<Array<String?>>
) : Iterable<Tuple> {

    /**
     * Returns a list of all tuples in the table.
     *
     * @return a list of tuples
     */
    val tuples: MutableList<Tuple>
    private val index: MutableMap<String, Int>
    private var string: String

    init {
        val list: MutableList<Tuple> = ArrayList()
        index = HashMap()

        for (i in attributes.indices) {
            index[attributes[i]] = i
        }

        tuples.forEach {
            list.add(Row(*it))
        }
        this.tuples = Collections.unmodifiableList(list)

        val builder = StringBuilder()
            .append(attributes.joinToString(", "))
            .append("\n")
        for (tuple in tuples) {
            builder.append(tuple.toString())
        }
        builder.deleteCharAt(builder.length - 1)
        this.string = builder.toString()
    }

    /**
     * @param index the index of the column
     * @return the attribute name of the column
     */
    fun getAttribute(index: Int): String {
        return attributes[index]
    }

    /**
     * Returns a column of the given index.
     *
     * @param index the index of the column
     * @return the column
     */
    fun getColumn(index: Int): Array<String?> {
        val result = arrayOfNulls<String>(rowCount)
        for (i in result.indices) {
            result[i] = tuples[i][index]
        }
        return result
    }

    /**
     * Returns the rowindex of the given attribute name.
     * Returns -1 when the attribute name is not found in the table.
     *
     * @param attributeName the attribute name
     * @return the index
     */
    fun indexOf(attributeName: String): Int {
        return index.getOrDefault(attributeName, -1)
    }

    /**
     * Returns a column with the given attribute name.
     *
     * @param attributeName the name of the column
     * @return the column
     */
    fun getColumn(attributeName: String): Array<String?> {
        val index = indexOf(attributeName)
        return if (index == -1) {
            arrayOfNulls(0)
        } else getColumn(index)
    }

    /**
     * Loops over each row and executes the given function.
     *
     * @param consumer the consumer that accepts the row
     */
    override fun forEach(consumer: Consumer<in Tuple>) {
        for (tuple in tuples) {
            consumer.accept(tuple)
        }
    }

    /**
     * Returns the row for the given row index.
     *
     * @param index the index of the row
     * @return an array with the row values
     */
    operator fun get(index: Int): Tuple {
        return tuples[index]
    }

    val isEmpty
        /**
         * @return whether the table is empty
         */
        get() = rowCount == 0

    val rowCount: Int
        /**
         * @return the row count
         */
        get() = tuples.size

    val columnCount: Int
        /**
         * @return the column count
         */
        get() = attributes.size

    override fun toString(): String {
        return string
    }

    override fun iterator(): MutableIterator<Tuple> {
        return tuples.iterator()
    }

    private inner class Row(vararg cells: String?) : Tuple {
        private val cells: Array<out String?>

        init {
            this.cells = cells
        }

        override fun get(attributeName: String): String? {
            val i = indexOf(attributeName)
            return if (i == -1) null else cells[i]
        }

        override fun get(index: Int): String? {
            return cells[index]
        }

        @Contract(pure = true)
        override fun toString(): String {
            return cells.joinToString(", ")
        }
    }
}