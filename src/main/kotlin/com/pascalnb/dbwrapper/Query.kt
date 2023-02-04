package com.pascalnb.dbwrapper

import org.jetbrains.annotations.Contract

/**
 * Class that represents a database query with options to insert values.
 *
 * @param query the query
 */
class Query(private val query: String, vararg args: Any?) {
    val args: Array<Any?>

    init {
        this.args = arrayOf(*args)
    }

    @Contract("_ -> new")
    fun withArgs(vararg args: Any?): Query {
        return Query(query, *args)
    }

    override fun toString(): String {
        return query
    }
}