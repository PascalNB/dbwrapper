package com.pascalnb.dbwrapper.action

import com.pascalnb.dbwrapper.Database
import com.pascalnb.dbwrapper.Query
import com.pascalnb.dbwrapper.Table
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Function

class SingleDatabaseAction<T>(val query: Query, val mapper: Function<Table, T?>, override val executor: Executor) :
    DatabaseAction<T> {

    override fun query(): CompletableFuture<T> {
        return CompletableFuture.supplyAsync({
            val reference = AtomicReference<Table>()
            val database = Database.instance.connect()
            try {
                database.queryStatement({ newValue -> reference.set(newValue) }, query)
                mapper.apply(reference.get())
            } finally {
                database.close()
            }
        }, executor)
    }

    override fun execute(): CompletableFuture<Void> {
        return CompletableFuture.runAsync({
            val database = Database.instance.connect()
            try {
                database.executeStatement(query)
            } finally {
                database.close()
            }
        }, executor)
    }

    override fun withExecutor(executor: Executor): DatabaseAction<T> {
        return SingleDatabaseAction(query, mapper, executor)
    }
}