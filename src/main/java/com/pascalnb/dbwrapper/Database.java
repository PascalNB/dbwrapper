package com.pascalnb.dbwrapper;

import org.jetbrains.annotations.Contract;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Abstract class that specifies all the methods needed for a database connection.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class Database {

    private static Supplier<Database> implementation = null;
    private static String username;
    private static String password;
    private static String url;

    protected static void setUsername(String username) {
        Database.username = username;
    }

    protected static String getUsername() {
        return username;
    }

    protected static void setUrl(String url) {
        Database.url = url;
    }

    protected static String getUrl() {
        return url;
    }

    protected static void setPassword(String password) {
        Database.password = password;
    }

    protected static String getPassword() {
        return password;
    }

    /**
     * Connects the database.Database object to the database.
     *
     * @return the same [Database]
     */
    public abstract Database connect();

    /**
     * @throws DatabaseException if this [Database] instance is not connected to a database.
     */
    protected abstract void checkConnection() throws DatabaseException;

    /**
     * Executes a query without receiving data back.
     *
     * @param query the query to be executed
     * @return the same [Database]
     */
    @Contract("_ -> this")
    public abstract Database execute(Query query);

    /**
     * Executes an SQL query on the database.
     *
     * @param callback the consumer that accepts the result from the database
     * @param query    the query
     * @return the same [Database]
     */
    @Contract("_, _ -> this")
    public abstract Database query(Consumer<Table> callback, Query query);

    /**
     * Queries a prepared statement on the database.
     *
     * @param callback the consumer that accepts the result from the database
     * @param query    the prepared query
     * @return the same [Database]
     */
    @Contract("_, _ -> this")
    public abstract Database queryStatement(Consumer<Table> callback, Query query);

    /**
     * Executes a prepared statement on the database.
     *
     * @param query the prepared statement
     * @return the same [Database]
     */
    @Contract("_ -> this")
    public abstract Database executeStatement(Query query);

    /**
     * Closes the connection to the database.
     */
    public abstract void close();

    /**
     * @return an instance of database.Database based on the implementation
     */
    public static Database getInstance() {
        if (url == null) {
            DatabaseAuthenticator.getInstance().authenticate();
        }
        if (implementation == null) {
            implementation = JDBC::new;
        }
        return implementation.get();
    }

    public static void setImplementation(Supplier<Database> supplier) {
        implementation = supplier;
    }

    /**
     * Prints a Table in readable form to the given output.
     *
     * @param table the query result
     */
    public void printQueryResult(Table table) {
        System.out.println(table);
        System.out.printf("Number of rows: %d%n", table.getRowCount());
    }

}