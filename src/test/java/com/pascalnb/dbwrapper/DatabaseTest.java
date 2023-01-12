package com.pascalnb.dbwrapper;

import com.pascalnb.dbwrapper.action.DatabaseAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DatabaseTest {

    @Test
    public void testConnection() {
        Assertions.assertDoesNotThrow(() -> Database.getInstance().connect().close());
    }

    @Test
    public void testQuery() {
        Assertions.assertDoesNotThrow(() -> {
            DatabaseAction.of("SELECT version();")
                .query(Mapper.SINGLE_VALUE)
                .thenAccept(System.out::println)
                .join();
        });
    }

}
