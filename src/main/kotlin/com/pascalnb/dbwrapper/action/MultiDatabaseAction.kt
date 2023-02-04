package com.pascalnb.dbwrapper.action

import com.pascalnb.dbwrapper.Database
import com.pascalnb.dbwrapper.Mapper
import com.pascalnb.dbwrapper.Table
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Function
import java.util.function.Supplier

class MultiDatabaseAction<B, T>(
    private val actions: Collection<DatabaseAction<out B>>,
    private val mapper: Function<B, T>,
    override val executor: Executor,
    private val serviceSupplier: Supplier<ExecutorService>
) : DatabaseAction<List<T>> {

    override fun query(): CompletableFuture<List<T>> {
        return CompletableFuture.supplyAsync({
            val service = serviceSupplier.get()
            val futures: MutableList<CompletableFuture<out B>> = ArrayList()
            val database = Database.instance.connect()

            for (action in actions) {
                if (action is SingleDatabaseAction) {

                    val future: CompletableFuture<out B?> = CompletableFuture.supplyAsync(Result@{
                        val reference = AtomicReference<Table>()
                        database.queryStatement({ newValue -> reference.set(newValue) }, action.query)

                        @Suppress("UNCHECKED_CAST")
                        return@Result (action.mapper as Mapper<out B>).apply(reference.get())
                    }, service)

                    futures.add(future)
                } else {
                    futures.add(action.withExecutor(service).query())
                }
            }

            val result: MutableList<T> = ArrayList()
            for (future in futures) {
                result.add(mapper.apply(future.join()))
            }

            database.close()
            service.shutdown()
            result
        }, executor)
    }

    override fun execute(): CompletableFuture<Void> {
        return CompletableFuture.runAsync({
            val service = serviceSupplier.get()
            val futures: MutableList<CompletableFuture<Void>> = ArrayList()
            val database = Database.instance.connect()

            for (action in actions) {
                if (action is SingleDatabaseAction) {

                    val future = CompletableFuture.supplyAsync<Void>({
                        database.executeStatement(action.query)
                        null
                    }, service)

                    futures.add(future)
                } else {
                    futures.add(action.withExecutor(service).execute())
                }
            }
            for (future in futures) {
                future.join()
            }
            database.close()
            service.shutdown()
        }, executor)
    }

    override fun withExecutor(executor: Executor): DatabaseAction<List<T>> {
        return MultiDatabaseAction(actions, mapper, executor, serviceSupplier)
    }
}