package com.pascalnb.dbwrapper;

public class DatabaseException extends RuntimeException {

    public DatabaseException(Throwable e) {
        super(e);
    }

    public DatabaseException(String message) {
        super(message);
    }

}
