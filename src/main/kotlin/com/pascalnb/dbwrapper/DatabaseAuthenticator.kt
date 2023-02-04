package com.pascalnb.dbwrapper

import java.util.function.Supplier

/**
 * Abstract singleton that is used to get the database credentials and set up the database
 * connection. Requires an implementation that extends [DatabaseAuthenticator].
 */
abstract class DatabaseAuthenticator {
    /**
     * Sets the username, password and url for all database connections.
     *
     * @throws DatabaseException when a database error occurs
     */
    @Throws(DatabaseException::class)
    fun authenticate() {
        val credentials = credentials
        Database.username = (credentials[0])
        Database.password = credentials[1]
        if (credentials[2] == null) {
            invalidate()
            throw DatabaseException("url cannot be null")
        }
        Database.url = credentials[2]

        // test connection
        Database.instance.connect().close()
    }

    protected abstract val credentials: Array<String?>

    companion object {

        private var instance: DatabaseAuthenticator? = null
        private var supplier: Supplier<DatabaseAuthenticator>? = null

        @JvmStatic
        fun getInstance(): DatabaseAuthenticator {
            if (instance == null) {
                if (supplier == null) {
                    supplier = Supplier { ConfigAuthenticator("config.cfg") }
                }
                instance = supplier!!.get()
            }
            return instance!!
        }

        @JvmStatic
        fun setImplementation(supplier: Supplier<DatabaseAuthenticator>?) {
            Companion.supplier = supplier
        }

        @JvmStatic
        fun invalidate() {
            instance = null
            Database.username = null
            Database.password = null
            Database.url = null
        }
    }
}