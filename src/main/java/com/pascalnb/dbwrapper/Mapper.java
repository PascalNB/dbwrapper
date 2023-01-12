package com.pascalnb.dbwrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public interface Mapper<T> extends Function<Table, T> {

    static Mapper<String> stringValue() {
        return singleValue(Function.identity());
    }

    static <T> Mapper<T> singleValue(Function<String, T> mapper) {
        return table -> table.isEmpty()
            ? null
            : mapper.apply(table.getRow(0).get(0));
    }

    static Mapper<List<String>> stringList() {
        return valueList(Function.identity());
    }

    static Mapper<List<Tuple>> tupleList() {
        return Table::getTuples;
    }

    static <T> Mapper<List<T>> valueList(Function<String, T> mapper) {
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

    static <T> Mapper<T> fromFunction(Function<Table, T> function) {
        return function::apply;
    }

    static Mapper<Table> identity() {
        return t -> t;
    }

    static Mapper<StringMapping> toMapping() {
        return singleValue(StringMapping::of);
    }

    static <T> Mapper<T> to(Class<T> clazz) {
        return t -> toMapping().andThen(s -> s.as(clazz)).apply(t);
    }

    static Mapper<Tuple> firstRow() {
        return table -> {
            if (table.isEmpty()) {
                return null;
            }
            return table.getRow(0);
        };
    }

}
