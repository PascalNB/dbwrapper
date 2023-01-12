package com.pascalnb.dbwrapper;

import com.pascalnb.dbwrapper.action.DatabaseAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseTest {

    @Test
    public void testConnection() {
        Assertions.assertDoesNotThrow(() -> Database.getInstance().connect().close());
    }

    @Test
    public void testQuery() {
        Assertions.assertDoesNotThrow(() -> {
            DatabaseAction.of("SELECT version();")
                .query(Mapper.stringValue())
                .thenAccept(System.out::println)
                .join();
        });

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        DatabaseAction.of("DELETE FROM users WHERE id=?;", 154)
            .withExecutor(executorService)
            .execute();

    }

}
