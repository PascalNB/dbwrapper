package com.pascalnb.dbwrapper.action;

import com.pascalnb.dbwrapper.Database;
import com.pascalnb.dbwrapper.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

public class MultiDatabaseAction<B, T> implements DatabaseAction<T> {

    private final Collection<? extends DatabaseAction<? extends B>> actions;
    private final Function<List<B>, T> mapper;
    private final Executor executor;
    private final Supplier<ExecutorService> serviceSupplier;

    public MultiDatabaseAction(Collection<? extends DatabaseAction<? extends B>> actions,
        Function<List<B>, T> mapper,
        Executor executor, Supplier<ExecutorService> serviceSupplier) {
        this.actions = actions;
        this.mapper = mapper;
        this.executor = executor;
        this.serviceSupplier = serviceSupplier;
    }

    @Override
    public Promise<T> query() {
        return new Promise<>(
            () -> {
                ExecutorService service = serviceSupplier.get();
                List<CompletableFuture<? extends B>> futures = new ArrayList<>();
                Database database = Database.getInstance().connect();
                List<B> result = new ArrayList<>();

                try {
                    for (var action : actions) {
                        if (action instanceof SingleDatabaseAction<? extends B> singleDatabaseAction) {

                            Supplier<? extends B> future = () -> {
                                Table table = database.queryStatement(singleDatabaseAction.getQuery());
                                return singleDatabaseAction.getMapper().apply(table);
                            };

                            futures.add(new Promise<>(future, service).stage());
                        } else {
                            futures.add(action.withExecutor(service).query().stage());
                        }
                    }

                    for (var future : futures) {
                        result.add(future.join());
                    }

                } finally {
                    service.shutdown();
                    database.close();
                }

                return mapper.apply(result);
            },
            executor
        );
    }

    @Override
    public Promise<Void> execute() {
        return new Promise<>(
            () -> {
                ExecutorService service = serviceSupplier.get();
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                Database database = Database.getInstance().connect();

                try {
                    for (var action : actions) {
                        if (action instanceof SingleDatabaseAction<? extends B> singleDatabaseAction) {
                            Supplier<Void> future = () -> {
                                database.executeStatement(singleDatabaseAction.getQuery());
                                return null;
                            };
                            futures.add(new Promise<>(future, service).stage());
                        } else {
                            futures.add(action.withExecutor(service).execute().stage());
                        }
                    }

                    for (var future : futures) {
                        future.join();
                    }
                } finally {
                    service.shutdown();
                    database.close();
                }
                return null;
            },
            executor);
    }

    @Override
    public <U> DatabaseAction<U> mapping(Function<T, U> mapper) {
        return new MultiDatabaseAction<>(actions, this.mapper.andThen(mapper), executor, serviceSupplier);
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    @Override
    public DatabaseAction<T> withExecutor(Executor executor) {
        return new MultiDatabaseAction<>(actions, mapper, executor, serviceSupplier);
    }

}