package com.pascalnb.dbwrapper.action;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class StagedPromise<T> extends Promise<T> {

    private final CompletableFuture<T> future;

    protected StagedPromise(CompletableFuture<T> future) {
        super(null, null);
        this.future = future;
    }

    @Override
    public void async(Consumer<T> consumer) {
        future.thenAccept(consumer);
    }

    @Override
    public T await() {
        return future.join();
    }

    @Override
    public void async() {
    }

    @Override
    public Promise<T> stage() {
        throw new IllegalStateException("Promise is already staged");
    }

    @Override
    public <U> Promise<U> map(Function<T, U> function) {
        return new StagedPromise<>(future.thenApply(function));
    }

    public Promise<Void> consume(Consumer<T> consumer) {
        return new StagedPromise<>(future.thenAccept(consumer));
    }

    public Promise<T> catching(Function<Throwable, ? extends T> function) {
        return new StagedPromise<>(future.exceptionally(function));
    }

}
