package com.pascalnb.dbwrapper;

import org.jetbrains.annotations.Contract;

/**
 * Class that represents a database query with options to insert values.
 */
@SuppressWarnings("unused")
public class Query {

    private final String query;
    private final Object[] args;

    public Query(String query, Object... args) {
        this.query = query;
        this.args = args;
    }

    @Contract("_ -> new")
    public Query withArgs(Object... args) {
        return new Query(query, args);
    }

    @Override
    public String toString() {
        return query;
    }

    public Object[] getArgs() {
        return args;
    }

}