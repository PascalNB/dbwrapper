package com.pascalnb.dbwrapper.action;

import com.pascalnb.dbwrapper.Database;
import com.pascalnb.dbwrapper.Query;
import com.pascalnb.dbwrapper.Table;

import java.util.concurrent.Executor;
import java.util.function.Function;

public class SingleDatabaseAction<T> implements DatabaseAction<T> {

    private final Query query;
    private final Function<Table, T> mapper;
    private final Executor executor;

    public SingleDatabaseAction(Query query, Function<Table, T> mapper, Executor executor) {
        this.query = query;
        this.mapper = mapper;
        this.executor = executor;
    }

    @Override
    public Promise<T> query() {
        return new Promise<>(() -> {
            Table table;
            Database database = Database.getInstance().connect();
            try {
                table = database.queryStatement(query);
            } finally {
                database.close();
            }
            return mapper.apply(table);
        }, executor);
    }

    @Override
    public Promise<Void> execute() {
        return new Promise<>(() -> {
            Database database = Database.getInstance().connect();
            try {
                database.executeStatement(query);
            } finally {
                database.close();
            }
            return null;
        }, executor);
    }

    @Override
    public <U> DatabaseAction<U> mapping(Function<T, U> mapper) {
        return new SingleDatabaseAction<>(query, this.mapper.andThen(mapper), executor);
    }

    @Override
    public DatabaseAction<T> withExecutor(Executor executor) {
        return new SingleDatabaseAction<>(query, mapper, executor);
    }

    public Query getQuery() {
        return query;
    }

    public Function<Table, T> getMapper() {
        return mapper;
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

}