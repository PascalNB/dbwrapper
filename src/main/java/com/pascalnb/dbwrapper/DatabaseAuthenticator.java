package com.pascalnb.dbwrapper;

import java.util.function.Supplier;

/**
 * Abstract singleton that is used to get the database credentials and set up the database
 * connection. Requires an implementation that extends {@link DatabaseAuthenticator}.
 */
abstract class DatabaseAuthenticator {

    private static DatabaseAuthenticator instance;
    private static Supplier<DatabaseAuthenticator> supplier;

    public static DatabaseAuthenticator getInstance() {
        if (instance == null) {
            if (supplier == null) {
                supplier = ConfigAuthenticator::new;
            }
            instance = supplier.get();
        }
        return instance;
    }

    public static void setSupplier(Supplier<DatabaseAuthenticator> supplier) {
        DatabaseAuthenticator.supplier = supplier;
    }

    public static void invalidate() {
        instance = null;
    }

    /**
     * Sets the username, password and url for all database connections.
     *
     * @throws DatabaseException when a database error occurs
     */
    public final void authenticate() throws DatabaseException {
        String[] credentials = getCredentials();

        Database.setUsername(credentials[0]);
        Database.setPassword(credentials[1]);
        Database.setUrl(credentials[2]);

        // test connection
        Database.getInstance().connect().close();
    }

    /**
     * Returns the username, password and connection url in an array.
     *
     * @return the credentials
     */
    protected abstract String[] getCredentials();

}