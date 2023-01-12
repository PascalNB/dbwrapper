package com.pascalnb.dbwrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public interface Mapper<T> extends Function<Table, T> {

    Mapper<String> SINGLE_VALUE = table ->
        table.isEmpty() ? null : table.getRow(0).get(0);

    Mapper<List<String>> VALUE_LIST = table -> {
        if (table.isEmpty()) {
            return List.of();
        }
        List<String> list = new ArrayList<>();
        for (Tuple tuple : table) {
            list.add(tuple.get(0));
        }
        return Collections.unmodifiableList(list);
    };

    static <T> Mapper<T> singleValue(Function<String, T> mapper) {
        return t -> mapper.apply(SINGLE_VALUE.apply(t));
    }

    static <T> Mapper<List<T>> valueList(Function<String, T> mapper) {
        return t -> VALUE_LIST.apply(t).stream().map(mapper).toList();
    }

    static <T> Mapper<T> fromFunction(Function<Table, T> function) {
        return function::apply;
    }

}
