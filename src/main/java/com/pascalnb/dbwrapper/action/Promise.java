package com.pascalnb.dbwrapper.action;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class Promise<T> {

    private final Supplier<T> supplier;
    private final Executor executor;

    protected Promise(Supplier<T> supplier, Executor executor) {
        this.supplier = supplier;
        this.executor = executor;
    }

    public void async(Consumer<T> consumer) {
        CompletableFuture.runAsync(
            () -> consumer.accept(supplier.get()),
            this.executor
        );
    }

    public void async() {
        async(result -> {});
    }

    public T await() {
        return supplier.get();
    }

    public Promise<T> stage() {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier, executor);
        return new StagedPromise<>(future);
    }

    public <U> Promise<U> map(Function<T, U> function) {
        return new Promise<>(() -> function.apply(supplier.get()), executor);
    }

    public Promise<Void> consume(Consumer<T> consumer) {
        return new Promise<>(() -> {
            consumer.accept(supplier.get());
            return null;
        }, executor);
    }

    public Promise<T> catching(Function<Throwable, ? extends T> function) {
        return new Promise<>(
            () -> {
                try {
                    return supplier.get();
                } catch (Throwable e) {
                    return function.apply(e);
                }
            },
            executor);
    }

}