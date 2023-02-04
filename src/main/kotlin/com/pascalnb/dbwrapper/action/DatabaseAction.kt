package com.pascalnb.dbwrapper.action

import com.pascalnb.dbwrapper.Mapper
import com.pascalnb.dbwrapper.Mapper.Companion.identity
import com.pascalnb.dbwrapper.Query
import com.pascalnb.dbwrapper.Table
import org.jetbrains.annotations.Contract
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Function
import java.util.function.Supplier

@Suppress("unused")
interface DatabaseAction<T> {

    fun query(): CompletableFuture<T>

    fun <U> query(map: Function<T?, U>): CompletableFuture<U> {
        return query().thenApply(map)
    }

    fun execute(): CompletableFuture<Void>

    val executor: Executor

    @Contract("_ -> new")
    fun withExecutor(executor: Executor): DatabaseAction<T>

    companion object {

        @Contract(value = "_, _ -> new", pure = true)
        @JvmStatic
        fun <T> of(query: Query, mapper: Mapper<T>): DatabaseAction<T> {
            return SingleDatabaseAction(query, mapper, DEFAULT_EXECUTOR)
        }

        @Contract(value = "_ -> new", pure = true)
        @JvmStatic
        fun of(query: Query): DatabaseAction<Table> {
            return of(query, identity())
        }

        @Contract(value = "_, _ -> new", pure = true)
        @JvmStatic
        fun of(query: String, vararg args: Any?): DatabaseAction<Table> {
            return of(Query(query, *args), identity())
        }

        @Contract(value = "_, _, _ -> new", pure = true)
        @JvmStatic
        fun <U, T> allOf(
            actions: Collection<DatabaseAction<U>>, mapper: Function<U, T>,
            service: Supplier<ExecutorService>
        ): DatabaseAction<List<T>> {
            return MultiDatabaseAction(actions, mapper, DEFAULT_EXECUTOR, service)
        }

        @Contract(value = "_, _ -> new", pure = true)
        @JvmStatic
        fun <U, T> allOf(
            actions: Collection<DatabaseAction<U>>,
            mapper: Function<U, T>
        ): DatabaseAction<List<T>> {
            return allOf(actions, mapper) { Executors.newSingleThreadExecutor() }
        }

        @Contract(value = "_ -> new", pure = true)
        @JvmStatic
        fun <T> allOf(actions: Collection<DatabaseAction<out T>>): DatabaseAction<List<T>> {
            return MultiDatabaseAction(
                actions,
                Function.identity(),
                DEFAULT_EXECUTOR
            ) { Executors.newSingleThreadExecutor() }
        }

        @Contract(value = "_, _ -> new", pure = true)
        @SafeVarargs
        @JvmStatic
        fun <U, T> allOf(mapper: Function<U, T>, vararg actions: DatabaseAction<U>): DatabaseAction<List<T>> {
            return allOf(listOf(*actions), mapper) { Executors.newSingleThreadExecutor() }
        }

        @Contract(value = "_ -> new", pure = true)
        @SafeVarargs
        @JvmStatic
        fun <T> allOf(vararg actions: DatabaseAction<out T>): DatabaseAction<List<T>> {
            return allOf(listOf(*actions))
        }

        @JvmStatic
        val DEFAULT_EXECUTOR = Executor { r -> Thread(r).start() }
    }
}