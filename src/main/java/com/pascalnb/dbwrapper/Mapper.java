package com.pascalnb.dbwrapper;

import org.jetbrains.annotations.Contract;

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

    default Mapper<T> orDefault(T defaultValue) throws ClassCastException {
        return t -> {
            T tt = this.apply(t);
            return tt == null ? defaultValue : tt;
        };
    }

    /**
     * Returns a mapper that will map a resulting table to a single string value.
     * <br><br>
     * Returns the value located at (0,0) in the table, or null if the table is empty.
     *
     * @return a new mapper
     */
    static Mapper<String> stringValue() {
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
    static <T> Mapper<T> singleValue(Function<String, T> mapper) {
        return t -> t.isEmpty() ? null : mapper.apply(t.get(0).get(0));
    }

    /**
     * Returns a mapper that will map the string value at (0,0) of the table to a value of type T based on the given
     * map function.
     * <br><br>
     * As opposed to [Mapper.singleValue], the mapper does not check if the table is empty, so the
     * given function can receive null values.
     *
     * @param mapper the map function
     * @param <T>    the type of the mapped values
     * @return a new mapper
     */
    static <T> Mapper<T> singleNullableValue(Function<String, T> mapper) {
        return t -> mapper.apply(stringValue().apply(t));
    }

    /**
     * Returns a mapper that will map a resulting table to a list of string values.
     * <br></br><br></br>
     * If the table is empty, this will return an empty list. The string values correspond to the values in the first
     * column of the table.
     *
     * @return a new mapper
     */
    static Mapper<List<String>> stringList() {
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
    static <T> Mapper<List<T>> valueList(Function<String, T> mapper) {
        return t -> {
            if (t.isEmpty()) {
                return List.of();
            }
            List<T> list = new ArrayList<>();
            for (Tuple tuple : t) {
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
    static <T> Mapper<T> fromFunction(Function<Table, T> function) {
        return function::apply;
    }

    /**
     * Returns a mapper that always returns the input table.
     *
     * @return a new mapper
     */
    @Contract(pure = true)
    static Mapper<Table> identity() {
        return t -> t;
    }

    /**
     * Returns a mapper that turns the first value into a {@link StringMapper}
     *
     * @return a new mapper
     */
    @Contract(pure = true)
    static Mapper<StringMapper> toMapping() {
        return singleNullableValue(StringMapper::new);
    }

    @Contract(pure = true)
    static <T> Mapper<T> toPrimitive(Class<? extends T> clazz) {
        return t -> toMapping().apply(t).to(clazz);
    }

    @Contract(pure = true)
    static <T> Mapper<T> toObject(Class<T> clazz) {
        return new ObjectMapper<>(clazz);
    }

    @Contract(pure = true)
    static <T> Mapper<List<T>> toObjects(Class<T> clazz) {
        return t -> new ObjectMapper<>(clazz).applyAll(t);
    }

    @Contract(pure = true)
    static Mapper<Tuple> firstRow() {
        return t -> t.isEmpty() ? null : t.get(0);
    }

    @Contract(pure = true)
    static Mapper<List<Tuple>> allRows() {
        return Table::getTuples;
    }

    @Contract(pure = true)
    static Mapper<Stream<Tuple>> stream() {
        return Table::stream;
    }

}