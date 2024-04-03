package com.pascalnb.dbwrapper.action;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class Promise<T> {

    private final CompletableFuture<T> future;

    protected Promise(CompletableFuture<T> future) {
        this.future = future;
    }

    public void async(Consumer<T> consumer) {
        future.thenAccept(consumer);
    }

    public T await() {
        return future.join();
    }

    public <U> Promise<U> then(Function<T, U> function) {
        return new Promise<>(future.thenApply(function));
    }

    public Promise<T> catching(Function<Throwable, ? extends T> function) {
        return new Promise<>(future.exceptionally(function));
    }

}