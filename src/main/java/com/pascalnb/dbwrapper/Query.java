package com.pascalnb.dbwrapper;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Class that represents a database query with options to insert values.
 */
public class Query {

    private final String query;
    private final Object[] args;

    /**
     * Creates a new query object for the given query string.
     *
     * @param query the query
     */
    public Query(@NotNull String query, @NotNull Object... args) {
        this.query = query;
        this.args = args;
    }

    @Contract("_ -> new")
    @NotNull
    public Query withArgs(@NotNull Object... args) {
        return new Query(query, args);
    }

    @NotNull
    @Override
    public String toString() {
        return query;
    }

    @NotNull
    public Object[] getArgs() {
        return args;
    }

}