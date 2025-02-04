package com.pascalnb.dbwrapper;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Abstract singleton that is used to get the database credentials and set up the database
 * connection. Requires an implementation that extends [DatabaseAuthenticator].
 */
@SuppressWarnings("unused")
public abstract class DatabaseAuthenticator {

    private static DatabaseAuthenticator instance = null;
    private static Supplier<DatabaseAuthenticator> supplier = null;

    protected abstract String @NotNull [] getCredentials();

    /**
     * Sets the username, password and url for all database connections.
     *
     * @throws DatabaseException when a database error occurs
     */
    public void authenticate() throws DatabaseException {
        String[] credentials = getCredentials();

        Database.setUsername(credentials[0]);
        Database.setPassword(credentials[1]);
        if (credentials[2] == null || credentials[2].isBlank()) {
            invalidate();
            throw new DatabaseException("URL cannot be null or blank");
        }
        Database.setUrl(credentials[2]);
        Database.getInstance().connect().close();
    }

    public static DatabaseAuthenticator getInstance() {
        if (instance == null) {
            if (supplier == null) {
                supplier = () -> new ConfigAuthenticator("config.cfg");
            }
            instance = supplier.get();
        }
        return instance;
    }

    public static void setImplementation(Supplier<DatabaseAuthenticator> supplier) {
        DatabaseAuthenticator.supplier = supplier;
    }

    public static void invalidate() {
        instance = null;
        Database.setUsername(null);
        Database.setPassword(null);
        Database.setUrl(null);
    }

}