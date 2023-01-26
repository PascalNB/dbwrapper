package com.pascalnb.dbwrapper;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Represents a function that maps the table resulting from a query to a given type.
 *
 * @param <T> the type
 */
@SuppressWarnings("unused")
public interface Mapper<T> extends Function<Table, T> {

    /**
     * Returns a mapper that will map a resulting table to a single string value.
     * <br><br>
     * Returns the value located at (0,0) in the table, or null if the table is empty.
     *
     * @return a new mapper
     */
    static @NotNull Mapper<String> stringValue() {
        return singleValue(Function.identity());
    }

    /**
     * Returns a mapper that will map the string value at (0,0) of the table to a value of type T based on the given
     * map function.
     * <br><br>
     * If the table is empty, the mapper will return null. So the mapper will always receive a nonnull string.
     *
     * @param mapper the map function
     * @param <T>    the type of the mapped value
     * @return a new mapper
     */
    @Contract(pure = true)
    static <T> @NotNull Mapper<T> singleValue(Function<String, T> mapper) {
        return table -> table.isEmpty()
            ? null
            : mapper.apply(table.getRow(0).get(0));
    }

    /**
     * Returns a mapper that will map the string value at (0,0) of the table to a value of type T based on the given
     * map function.
     * <br><br>
     * As opposed to {@link Mapper#singleValue(Function)}, the mapper does not check if the table is empty, so the
     * given function can receive null values.
     *
     * @param mapper the map function
     * @param <T>    the type of the mapped values
     * @return a new mapper
     */
    static <T> @NotNull Mapper<T> singleNullableValue(Function<String, T> mapper) {
        return table -> mapper.apply(stringValue().apply(table));
    }

    /**
     * Returns a mapper that will map a resulting table to a list of string values.
     * <br><br>
     * If the table is empty, this will return an empty list. The string values correspond to the values in the first
     * column of the table.
     *
     * @return a new mapper
     */
    static @NotNull Mapper<List<String>> stringList() {
        return valueList(Function.identity());
    }

    /**
     * Returns a mapper that will map a resulting table to a list of T values based on the given map function.
     * <br><br>
     * If the table is empty, this will return an empty list. The values correspond to the values in the first column
     * of the table.
     *
     * @param mapper the map function
     * @param <T>    the type of the mapped values
     * @return a new mapper
     */
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

    /**
     * Makes a mapper out of the given function.
     *
     * @param function the function
     * @param <T>      the type of the mapper
     * @return a new mapper
     */
    @Contract(pure = true)
    static <T> @NotNull Mapper<T> fromFunction(@NotNull Function<Table, T> function) {
        return function::apply;
    }

    /**
     * Returns a mapper that always returns the input table.
     *
     * @return a new mapper
     */
    @Contract(pure = true)
    static @NotNull Mapper<Table> identity() {
        return t -> t;
    }

    /**
     * @return a new mapper
     */
    @Contract(pure = true)
    static @NotNull Mapper<StringMapper> toMapping() {
        return singleNullableValue(StringMapper::new);
    }

    @Contract(pure = true)
    static <T> @NotNull Mapper<T> toPrimitive(Class<? extends T> clazz) {
        return t -> toMapping().apply(t).as(clazz);
    }

    static <T> @NotNull Mapper<T> toObject(Class<T> clazz) {
        return new ObjectMapper<>(clazz);
    }

    static <T> @NotNull Mapper<List<T>> toObjects(Class<T> clazz) {
        return t -> new ObjectMapper<>(clazz).applyAll(t);
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

    @Contract(pure = true)
    static @NotNull Mapper<Stream<Tuple>> stream() {
        return t -> t.getTuples().stream();
    }

    @SuppressWarnings("unchecked")
    default Mapper<T> orDefault(Object defaultValue) throws ClassCastException {
        return table -> {
            T t = this.apply(table);
            if (t == null) {
                if (defaultValue == null) {
                    return null;
                }
                return (T) defaultValue;
            }
            return t;
        };
    }

}
