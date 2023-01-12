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
    public CompletableFuture<T> query() {
        return CompletableFuture.supplyAsync(() -> {
            AtomicReference<Table> reference = new AtomicReference<>();
            Database database = Database.getInstance().connect();
            try {
                database.queryStatement(reference::set, query);
                return mapper.apply(reference.get());
            } finally {
                database.close();
            }
        }, getExecutor());
    }

    @Override
    public CompletableFuture<Void> execute() {
        return CompletableFuture.runAsync(() -> {
            Database database = Database.getInstance().connect();
            try {
                database.executeStatement(query);
            } finally {
                database.close();
            }
        }, getExecutor());
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    @Override
    public DatabaseAction<T> withExecutor(Executor executor) {
        return new SingleDatabaseAction<>(query, mapper, executor);
    }

}
