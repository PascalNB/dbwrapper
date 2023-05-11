package com.pascalnb.dbwrapper.action;

import com.pascalnb.dbwrapper.Database;
import com.pascalnb.dbwrapper.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public class MultiDatabaseAction<B, T> implements DatabaseAction<List<T>> {

    private final Collection<DatabaseAction<? extends B>> actions;
    private final Function<B, T> mapper;
    private final Executor executor;
    private final Supplier<ExecutorService> serviceSupplier;

    public MultiDatabaseAction(Collection<DatabaseAction<? extends B>> actions, Function<B, T> mapper,
        Executor executor, Supplier<ExecutorService> serviceSupplier) {
        this.actions = actions;
        this.mapper = mapper;
        this.executor = executor;
        this.serviceSupplier = serviceSupplier;
    }

    @Override
    public CompletedAction<List<T>> query() {
        return new CompletedAction<>(CompletableFuture.supplyAsync(() -> {
            ExecutorService service = serviceSupplier.get();
            List<CompletedAction<? extends B>> futures = new ArrayList<>();
            Database database = Database.getInstance().connect();

            try {
                for (var action : actions) {
                    if (action instanceof SingleDatabaseAction<? extends B> singleDatabaseAction) {

                        CompletableFuture<? extends B> future = CompletableFuture.supplyAsync(() -> {
                            AtomicReference<Table> reference = new AtomicReference<>();
                            database.queryStatement(reference::set, singleDatabaseAction.getQuery());
                            return singleDatabaseAction.getMapper().apply(reference.get());
                        }, service);

                        futures.add(new CompletedAction<>(future));
                    } else {
                        futures.add(action.withExecutor(service).query());
                    }
                }

                List<T> result = new ArrayList<>();
                for (var future : futures) {
                    result.add(mapper.apply(future.complete()));
                }

                return result;
            } finally {
                service.shutdown();
                database.close();
            }
        }, executor));
    }

    @Override
    public CompletedAction<Void> execute() {
        return new CompletedAction<>(CompletableFuture.runAsync(() -> {
            ExecutorService service = serviceSupplier.get();
            List<CompletedAction<Void>> futures = new ArrayList<>();
            Database database = Database.getInstance().connect();
            try {
                for (var action : actions) {
                    if (action instanceof SingleDatabaseAction<? extends B> singleDatabaseAction) {
                        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                            database.executeStatement(singleDatabaseAction.getQuery());
                            return null;
                        }, service);
                        futures.add(new CompletedAction<>(future));
                    } else {
                        futures.add(action.withExecutor(service).execute());
                    }
                }

                for (var future : futures) {
                    future.complete();
                }
            } finally {
                service.shutdown();
                database.close();
            }
        }, executor));
    }

    @Override
    public DatabaseAction<List<T>> withExecutor(Executor executor) {
        return new MultiDatabaseAction<>(actions, mapper, executor, serviceSupplier);
    }

}