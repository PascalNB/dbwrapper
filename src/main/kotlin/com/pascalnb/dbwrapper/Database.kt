package com.pascalnb.dbwrapper

import org.jetbrains.annotations.Contract
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * Abstract class that specifies all the methods needed for a database connection.
 */
@Suppress("unused")
abstract class Database {
    /**
     * Connects the database.Database object to the database.
     *
     * @return the same [Database]
     */
    abstract fun connect(): Database

    /**
     * @throws DatabaseException if this [Database] instance is not connected to a database.
     */
    @Throws(DatabaseException::class)
    protected abstract fun checkConnection()

    /**
     * Executes a query without receiving data back.
     *
     * @param query the query to be executed
     * @return the same [Database]
     */
    @Contract("_ -> this")
    abstract fun execute(query: Query): Database

    /**
     * Executes an SQL query on the database.
     *
     * @param callback the consumer that accepts the result from the database
     * @param query    the query
     * @return the same [Database]
     */
    @Contract("_, _ -> this")
    abstract fun query(callback: Consumer<Table>, query: Query): Database

    /**
     * Queries a prepared statement on the database.
     *
     * @param callback          the consumer that accepts the result from the database
     * @param preparedStatement the prepared query
     * @return the same [Database]
     */
    @Contract("_, _ -> this")
    abstract fun queryStatement(callback: Consumer<Table>, preparedStatement: Query): Database

    /**
     * Executes a prepared statement on the database.
     *
     * @param preparedStatement the prepared statement
     * @return the same [Database]
     */
    @Contract("_ -> this")
    abstract fun executeStatement(preparedStatement: Query): Database

    /**
     * Closes the connection to the database.
     */
    abstract fun close()

    companion object {

        private var implementation: Supplier<Database>? = null

        @JvmStatic
        var url: String? = null
            internal set

        @JvmStatic
        var username: String? = null
            internal set

        @JvmStatic
        var password: String? = null
            internal set

        @JvmStatic
        val instance: Database
            /**
             * @return an instance of database.Database based on the implementation
             */
            get() {
                if (url == null) {
                    DatabaseAuthenticator.getInstance().authenticate()
                }
                if (implementation == null) {
                    implementation = Supplier { JDBC() }
                }
                return implementation!!.get()
            }

        @JvmStatic
        fun setImplementation(supplier: Supplier<Database>?) {
            implementation = supplier
        }

        /**
         * Prints a Table in readable form to the given output.
         *
         * @param table the query result
         */
        @JvmStatic
        fun printQueryResult(table: Table) {
            val result = StringBuilder()
            val attributes = table.attributes
            for (i in 0 until table.getColumnCount()) {
                result.append(attributes[i]).append(", ")
            }
            result.delete(result.length - 2, result.length)
            result.append("\n")

            // empty resultSet
            if (table.getColumnCount() == 0) {
                println("Number of rows: 0")
                return
            }
            table.forEach(Consumer { row: Tuple ->
                for (i in 0 until table.getColumnCount()) {
                    result.append(row[i]).append(", ")
                }
                result.delete(result.length - 2, result.length)
                result.append("\n")
            })
            println(result)
            println("Number of rows: " + table.rowCount)
        }
    }
}