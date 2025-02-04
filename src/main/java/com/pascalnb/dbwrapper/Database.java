package com.pascalnb.dbwrapper;

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
     * Connects the database object to the database with auto commit enabled.
     *
     * @return the same [Database]
     */
    public abstract Database connect();

    /**
     * Connects the database object.
     *
     * @param autoCommit auto commit
     * @return the same [Database]
     */
    public abstract Database connect(boolean autoCommit);

    /**
     * Commits all changes.
     *
     * @return the same [Database]
     */
    public abstract Database commit();

    /**
     * @throws DatabaseException if this [Database] instance is not connected to a database.
     */
    protected abstract void checkConnection() throws DatabaseException;

    /**
     * Queries a prepared statement on the database.
     *
     * @param query the prepared query
     * @return the database result
     */
    public abstract Table queryStatement(Query query);

    /**
     * Executes a prepared statement on the database.
     *
     * @param query the prepared statement
     */
    public abstract void executeStatement(Query query);

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