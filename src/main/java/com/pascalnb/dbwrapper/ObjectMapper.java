package com.pascalnb.dbwrapper;

import com.pascalnb.dbwrapper.annotation.ParseField;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjectMapper<T> implements Mapper<T> {

    private final Constructor<T> constructor;
    private final List<Object[]> pairs = new ArrayList<>();

    public ObjectMapper(Class<T> clazz) {
        try {
            this.constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new DatabaseException("No constructor with 0 parameters found for " + clazz.getName());
        }

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            ParseField parseField = field.getAnnotation(ParseField.class);
            if (parseField == null) {
                continue;
            }
            if ((field.getModifiers() & Modifier.FINAL) != 0) {
                throw new DatabaseException("Final fields cannot be annotated with " + ParseField.class.getName());
            }

            String parseFieldName = parseField.value();
            pairs.add(new Object[]{field, "".equals(parseFieldName) ? field.getName() : parseFieldName});
        }

        if (pairs.isEmpty()) {
            throw new DatabaseException(clazz + " does not have non-final fields annotated with " +
                ParseField.class.getName());
        }
    }

    private T rowToInstance(Tuple row) {
        try {
            T instance = constructor.newInstance();
            for (Object[] pair : pairs) {
                StringMapper value = new StringMapper(row.get((String) pair[1]));
                Field field = (Field) pair[0];
                Object parsed = value.to(field.getType());
                field.set(instance, parsed);
            }
            return instance;
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new DatabaseException(e);
        }
    }

    public List<T> applyAll(Table table) {
        if (table.isEmpty()) {
            return List.of();
        }
        List<T> result = new ArrayList<>(table.getRowCount());
        for (Tuple row : table) {
            result.add(rowToInstance(row));
        }
        return Collections.unmodifiableList(result);
    }

    public Mapper<List<T>> all() {
        return this::applyAll;
    }

    @Override
    public T apply(Table table) {
        return table.isEmpty() ? null : rowToInstance(table.get(0));
    }

}