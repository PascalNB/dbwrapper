package com.pascalnb.dbwrapper;

import com.pascalnb.dbwrapper.action.DatabaseAction;
import com.pascalnb.dbwrapper.annotation.ParseField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DatabaseTest {

    @Test
    public void testConnection() {
        Assertions.assertDoesNotThrow(() -> Database.getInstance().connect().close());
    }

    @Test
    public void testQuery() {
        Assertions.assertDoesNotThrow(() -> {
            String version = DatabaseAction.of("SELECT version();")
                .query()
                .map(Mapper.toMapping())
                .map(StringMapper::toString)
                .complete();
            System.out.println(version);
        });
    }

    @Test
    public void testObjectMapping() {
        DatabaseAction.allOf(
                DatabaseAction.of("CREATE TABLE IF NOT EXISTS test_table (id INT, text TEXT)"),
                DatabaseAction.of("INSERT INTO test_table VALUES (?, ?)", 12, "test"),
                DatabaseAction.of("INSERT INTO test_table VALUES (?, ?)", 12, null)
            )
            .execute()
            .complete();
        List<Parsable> parsables = DatabaseAction.allOf(
                Mapper.toObjects(Parsable.class),
                DatabaseAction.of("SELECT * FROM test_table")
            )
            .query()
            .map(l -> l.get(0))
            .complete();
        DatabaseAction.of("DROP TABLE test_table")
            .execute()
            .complete();
        System.out.println(parsables);
        Parsable parsable = parsables.get(0);
        Assertions.assertEquals(12, parsable.getId());
        Assertions.assertEquals("test", parsable.getValue());
        Assertions.assertNull(parsable.getTest());
    }

    @SuppressWarnings("unused")
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

        @Override
        public String toString() {
            return "[" + id + ", " + value + ", " + test + "]";
        }

    }

}
