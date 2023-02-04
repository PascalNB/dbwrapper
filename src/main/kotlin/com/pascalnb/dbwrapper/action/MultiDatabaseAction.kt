package com.pascalnb.dbwrapper.action;

import com.pascalnb.dbwrapper.Database;
import com.pascalnb.dbwrapper.Mapper;
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

    private final Collection<? extends DatabaseAction<? extends B>> actions;
    private final Function<B, T> mapper;
    private final Executor executor;
    private final Supplier<ExecutorService> serviceSupplier;

    public MultiDatabaseAction(Collection<? extends DatabaseAction<? extends B>> actions,
        Function<B, T> mapper, Executor executor, Supplier<ExecutorService> serviceSupplier) {
        this.actions = actions;
        this.mapper = mapper;
        this.executor = executor;
        this.serviceSupplier = serviceSupplier;
    }

    @Override
    public CompletableFuture<List<T>> query() {
        return CompletableFuture.supplyAsync(() -> {

            ExecutorService service = serviceSupplier.get();
            List<CompletableFuture<? extends B>> futures = new ArrayList<>();

            Database database = Database.getInstance().connect();

            for (DatabaseAction<? extends B> action : actions) {

                if (action instanceof SingleDatabaseAction<?> single) {

                    CompletableFuture<? extends B> future = CompletableFuture.supplyAsync(() -> {
                        AtomicReference<Table> reference = new AtomicReference<>();
                        database.queryStatement(reference::set, single.getQuery());
                        //noinspection unchecked
                        return ((Mapper<? extends B>) single.getMapper()).apply(reference.get());
                    }, service);

                    futures.add(future);
                } else {
                    futures.add(action.withExecutor(service).query());
                }
            }

            List<T> result = new ArrayList<>();
            for (CompletableFuture<? extends B> future : futures) {
                result.add(mapper.apply(future.join()));
            }

            database.close();

            service.shutdown();
            return result;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> execute() {
        return CompletableFuture.runAsync(() -> {
            ExecutorService service = serviceSupplier.get();
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            Database database = Database.getInstance().connect();

            for (DatabaseAction<?> action : actions) {

                if (action instanceof SingleDatabaseAction<?> single) {

                    CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                        database.executeStatement(single.getQuery());
                        return null;
                    }, service);

                    futures.add(future);

                } else {
                    futures.add(action.withExecutor(service).execute());
                }
            }

            for (CompletableFuture<Void> future : futures) {
                future.join();
            }

            database.close();

            service.shutdown();
        }, executor);
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    @Override
    public DatabaseAction<List<T>> withExecutor(Executor executor) {
        return new MultiDatabaseAction<>(actions, mapper, executor, serviceSupplier);
    }

}
