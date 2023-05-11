package com.pascalnb.dbwrapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * All query consumers accept this data type.
 * Any implementation of Database should create a Table for each query.
 */
@SuppressWarnings("unused")
public class Table implements Iterable<Tuple> {

    private final String[] attributes;
    private final List<Tuple> tuples;

    private final Map<String, Integer> index;
    private String string = null;

    public Table(String[] attributes, List<String[]> tuples) {
        this.attributes = attributes;
        this.tuples = tuples.stream().map(Row::new).collect(Collectors.toUnmodifiableList());
        this.index = new HashMap<>();

        for (int i = 0; i < attributes.length; i++) {
            index.put(attributes[i], i);
        }
    }

    @Override
    public String toString() {
        if (string != null) {
            return string;
        }
        StringBuilder builder = new StringBuilder()
            .append(String.join(", ", attributes))
            .append("\n");
        for (Tuple tuple : this.tuples) {
            builder.append(tuple.toString());
        }
        builder.deleteCharAt(builder.length() - 1);
        this.string = builder.toString();
        return this.string;
    }

    public Stream<Tuple> stream() {
        return tuples.stream();
    }

    /**
     * Returns a list of all tuples in the table.
     *
     * @return a list of tuples
     */
    public List<Tuple> getTuples() {
        return tuples;
    }

    public String[] getAttributes() {
        return attributes;
    }

    /**
     * @param index the index of the column
     * @return the attribute name of the column
     */
    public String getAttribute(int index) {
        return attributes[index];
    }

    /**
     * Returns a column of the given index.
     *
     * @param index the index of the column
     * @return the column
     */
    public String[] getColumn(int index) {
        String[] result = new String[tuples.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = tuples.get(i).get(index);
        }
        return result;
    }

    /**
     * Returns the rowindex of the given attribute name.
     * Returns -1 when the attribute name is not found in the table.
     *
     * @param attributeName the attribute name
     * @return the index
     */
    int indexOf(String attributeName) {
        return index.getOrDefault(attributeName, -1);
    }

    /**
     * Returns a column with the given attribute name.
     *
     * @param attributeName the name of the column
     * @return the column
     */
    String[] getColumn(String attributeName) {
        int index = indexOf(attributeName);
        return index == -1 ? new String[0] : getColumn(index);
    }

    /**
     * Loops over each row and executes the given function.
     *
     * @param consumer the consumer that accepts the row
     */
    @Override
    public void forEach(Consumer<? super Tuple> consumer) {
        for (Tuple tuple : tuples) {
            consumer.accept(tuple);
        }
    }

    /**
     * Returns the row for the given row index.
     *
     * @param index the index of the row
     * @return an array with the row values
     */
    public Tuple get(int index) {
        return tuples.get(index);
    }

    /**
     * @return whether the table is empty
     */
    public boolean isEmpty() {
        return tuples.isEmpty();
    }

    /**
     * @return the row count
     */
    public int getRowCount() {
        return tuples.size();
    }

    public int getColumnCount() {
        return attributes.length;
    }

    @Override
    public Iterator<Tuple> iterator() {
        return tuples.iterator();
    }

    private class Row implements Tuple {

        private final String[] cells;

        public Row(String... cells) {
            this.cells = cells;
        }

        @Override
        public String get(String attributeName) {
            int i = indexOf(attributeName);
            return (i == -1) ? null : cells[i];
        }

        @Override
        public String get(int index) {
            return cells[index];
        }

        @Override
        public String toString() {
            return String.join(", ", cells);
        }

    }

}