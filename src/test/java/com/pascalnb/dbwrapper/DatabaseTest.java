package com.pascalnb.dbwrapper;

import com.pascalnb.dbwrapper.action.DatabaseAction;
import com.pascalnb.dbwrapper.annotation.ParseField;
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
                .query(Mapper.stringValue())
                .thenAccept(System.out::println)
                .join();
        });
    }

    @Test
    public void testObjectMapping() {
        DatabaseAction.allOf(
                DatabaseAction.of("CREATE TABLE IF NOT EXISTS test_table (id INT, text TEXT)"),
                DatabaseAction.of("INSERT INTO test_table VALUES (?, ?)", 12, "test")
            )
            .execute()
            .join();
        Parsable parsable = DatabaseAction.of("SELECT * FROM test_table")
            .query(Mapper.toObject(Parsable.class))
            .join();
        DatabaseAction.of("DROP TABLE test_table")
            .execute();
        Assertions.assertEquals(12, parsable.getId());
        Assertions.assertEquals("test", parsable.getValue());
        Assertions.assertNull(parsable.getTest());
    }

    private static class Parsable {

        @ParseField private int id;
        @ParseField("text") private String value;
        @ParseField private String test;

        public int getId() {
            return id;
        }

        public String getValue() {
            return value;
        }

        public String getTest() {
            return test;
        }

    }

}
