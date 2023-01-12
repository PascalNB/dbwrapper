package com.pascalnb.dbwrapper;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public interface Mapper<T> extends Function<Table, T> {

    static @NotNull Mapper<String> stringValue() {
        return singleValue(Function.identity());
    }

    @Contract(pure = true)
    static <T> @NotNull Mapper<T> singleValue(Function<String, T> mapper) {
        return table -> table.isEmpty()
            ? null
            : mapper.apply(table.getRow(0).get(0));
    }

    static @NotNull Mapper<List<String>> stringList() {
        return valueList(Function.identity());
    }

    @Contract(pure = true)
    static <T> @NotNull Mapper<List<T>> valueList(Function<String, T> mapper) {
        return table -> {
            if (table.isEmpty()) {
                return List.of();
            }
            List<T> list = new ArrayList<>();
            for (Tuple tuple : table) {
                list.add(mapper.apply(tuple.get(0)));
            }
            return Collections.unmodifiableList(list);
        };
    }

    @Contract(pure = true)
    static <T> @NotNull Mapper<T> fromFunction(@NotNull Function<Table, T> function) {
        return function::apply;
    }

    @Contract(pure = true)
    static @NotNull Mapper<Table> identity() {
        return t -> t;
    }

    @Contract(pure = true)
    static @NotNull Mapper<StringMapping> toMapping() {
        return singleValue(StringMapping::of);
    }

    @Contract(pure = true)
    static <T> @NotNull Mapper<T> toPrimitive(Class<T> clazz) {
        return t -> toMapping().andThen(s -> s.as(clazz)).apply(t);
    }

    static <T> @NotNull Mapper<T> toObject(Class<T> clazz) {
        return new ObjectMapper<>(clazz);
    }

    @Contract(pure = true)
    static @NotNull Mapper<Tuple> firstRow() {
        return table -> {
            if (table.isEmpty()) {
                return null;
            }
            return table.getRow(0);
        };
    }

    @Contract(pure = true)
    static @NotNull Mapper<List<Tuple>> allRows() {
        return Table::getTuples;
    }

}
