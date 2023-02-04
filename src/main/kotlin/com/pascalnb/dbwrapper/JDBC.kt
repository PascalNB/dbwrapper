package com.pascalnb.dbwrapper

import org.jetbrains.annotations.Contract
import java.sql.*
import java.util.function.Consumer

/**
 * The JDBC implementation of [Database].
 */
@Suppress("unused")
internal class JDBC : Database() {

    private var connection: Connection? = null
    private var statement: Statement? = null

    @Contract("-> this")
    override fun connect(): Database {
        if (url == null) {
            throw DatabaseException("URL for database connection not set.")
        }
        try {
            connection = DriverManager.getConnection(url, username, password)
            connection!!.transactionIsolation = Connection.TRANSACTION_SERIALIZABLE
            connection!!.autoCommit = false
            statement = connection!!.createStatement()
        } catch (e: SQLException) {
            throw DatabaseException(e)
        }
        return this
    }

    @Throws(DatabaseException::class)
    override fun checkConnection() {
        if (connection == null || statement == null) {
            throw DatabaseException(
                "No connection to the database exists" +
                        "or it has already been closed."
            )
        }
    }

    @Contract("_ -> this")
    override fun execute(query: Query): Database {
        checkConnection()
        try {
            statement!!.execute(query.toString())
        } catch (e: SQLException) {
            throw DatabaseException(e)
        }
        return this
    }

    @Contract("_, _ -> this")
    override fun query(callback: Consumer<Table>, query: Query): Database {
        checkConnection()
        try {
            statement!!.fetchSize = FETCH_SIZE
            callback.accept(parseResult(statement!!.executeQuery(query.toString())))
        } catch (e: SQLException) {
            throw DatabaseException(e)
        }
        return this
    }

    @Contract("_, _ -> param1")
    private fun setVariables(statement: PreparedStatement, variables: Array<Any?>): PreparedStatement {
        try {
            statement.fetchSize = FETCH_SIZE

            for ((i, variable) in variables.withIndex()) {
                val index = i + 1

                if (variable == null) {
                    statement.setObject(index, null)
                    continue
                }

                when (variable) {
                    is Int -> {
                        statement.setInt(index, variable)
                    }

                    is Double -> {
                        statement.setDouble(index, variable)
                    }

                    is Boolean -> {
                        statement.setBoolean(index, variable)
                    }

                    is Long -> {
                        statement.setLong(index, variable)
                    }

                    is Float -> {
                        statement.setFloat(index, variable)
                    }

                    is Byte -> {
                        statement.setByte(index, variable)
                    }

                    is Short -> {
                        statement.setShort(index, variable)
                    }

                    else -> {
                        statement.setString(index, variable.toString())
                    }
                }
            }
        } catch (e: SQLException) {
            throw DatabaseException(e)
        }
        return statement
    }

    @Contract("_, _ -> this")
    override fun queryStatement(callback: Consumer<Table>, preparedStatement: Query): Database {
        checkConnection()
        try {
            callback.accept(
                parseResult(
                    setVariables(
                        connection!!.prepareStatement(preparedStatement.toString()),
                        preparedStatement.args
                    ).executeQuery()
                )
            )
        } catch (e: SQLException) {
            throw DatabaseException(e)
        }
        return this
    }

    @Contract("_ -> this")
    override fun executeStatement(preparedStatement: Query): Database {
        checkConnection()
        try {
            setVariables(connection!!.prepareStatement(preparedStatement.toString()), preparedStatement.args).execute()
        } catch (e: SQLException) {
            throw DatabaseException(e)
        }
        return this
    }

    @Contract("_, _ -> this")
    fun executeBatch(preparedStatement: String?, vararg variablesArray: Array<Any?>): Database {
        checkConnection()
        try {
            val statement = connection!!.prepareStatement(preparedStatement)
            for (variables in variablesArray) {
                setVariables(statement, variables).addBatch()
            }
            statement.executeBatch()
        } catch (e: SQLException) {
            throw DatabaseException(e)
        }
        return this
    }

    override fun close() {
        checkConnection()
        try {
            connection!!.commit()
            connection!!.autoCommit = true
            connection!!.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        connection = null
        statement = null
    }

    companion object {
        private const val FETCH_SIZE = 500

        // specific implementation to parse a ResultSet to a Table
        @Contract(value = "_ -> new")
        private fun parseResult(resultSet: ResultSet): Table {
            return try {
                val metaData = resultSet.metaData
                val columnCount = metaData.columnCount
                val attributes = Array(columnCount) { "" }

                for (i in 0 until columnCount) {
                    attributes[i] = metaData.getColumnName(i + 1)
                }

                val tuples: MutableList<Array<String?>> = ArrayList()

                while (resultSet.next()) {
                    val tuple = arrayOfNulls<String>(columnCount)

                    for (i in 0 until columnCount) {
                        tuple[i] = resultSet.getString(i + 1)
                    }

                    tuples.add(tuple)
                }

                Table(attributes, tuples)
            } catch (e: SQLException) {
                throw DatabaseException(e)
            }
        }
    }
}