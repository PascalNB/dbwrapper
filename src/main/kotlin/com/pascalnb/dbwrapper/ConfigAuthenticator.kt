package com.pascalnb.dbwrapper

import java.io.*
import java.util.*

/**
 * Implementation of [DatabaseAuthenticator] that reads the credentials from a cfg file.
 *
 * @see DatabaseAuthenticator
 */
@Suppress("unused")
internal class ConfigAuthenticator(configFile: String) : DatabaseAuthenticator() {

    private val credentialsArray: Array<String?>

    init {
        val properties = Properties()
        try {
            val jarPath = File(javaClass.protectionDomain.codeSource.location.path)
            val propertiesPath = jarPath.parentFile.absolutePath
            properties.load(FileInputStream("$propertiesPath/$configFile"))
        } catch (e: FileNotFoundException) {
            try {
                javaClass.classLoader.getResourceAsStream(configFile).use { config -> properties.load(config) }
            } catch (e2: IOException) {
                throw UncheckedIOException(e2)
            }
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }

        val username = properties.getProperty("username")
        val password = properties.getProperty("password")
        val url = properties.getProperty("host")

        try {
            Class.forName(properties.getProperty("driver"))
        } catch (e: ClassNotFoundException) {
            throw DatabaseException("JDBC driver not loaded")
        }

        credentialsArray = arrayOf(username, password, url)
    }

    /**
     * Returns the database credentials from the credentials file.
     */
    override val credentials: Array<String?> get() = credentialsArray
}