package com.pascalnb.dbwrapper.action;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class CompletedAction<T> {

    private final CompletableFuture<T> future;

    protected CompletedAction(CompletableFuture<T> future) {
        this.future = future;
    }

    public <U> CompletedAction<U> map(Function<T, U> function) {
        return new CompletedAction<>(future.thenApply(function));
    }

    public CompletableFuture<Void> queue(Consumer<T> consumer) {
        return future.thenAccept(consumer);
    }

    public T complete() {
        return future.join();
    }

}