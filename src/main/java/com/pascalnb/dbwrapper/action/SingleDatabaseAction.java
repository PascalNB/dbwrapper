package com.pascalnb.dbwrapper.action;

import com.pascalnb.dbwrapper.Database;
import com.pascalnb.dbwrapper.Query;
import com.pascalnb.dbwrapper.Table;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
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
        return new Promise<>(CompletableFuture.supplyAsync(() -> {
            AtomicReference<Table> reference = new AtomicReference<>();
            Database database = Database.getInstance().connect();
            try {
                database.queryStatement(reference::set, query);
                return mapper.apply(reference.get());
            } finally {
                database.close();
            }
        }, executor));
    }

    @Override
    public Promise<Void> execute() {
        return new Promise<>(CompletableFuture.runAsync(() -> {
            Database database = Database.getInstance().connect();
            try {
                database.executeStatement(query);
            } finally {
                database.close();
            }
        }, executor));
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