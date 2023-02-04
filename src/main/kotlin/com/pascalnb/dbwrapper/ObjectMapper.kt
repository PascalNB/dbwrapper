package com.pascalnb.dbwrapper;

import com.pascalnb.dbwrapper.annotation.ParseField;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjectMapper<T> implements Mapper<T> {

    private final Constructor<T> constructor;
    private final List<Object[]> pairs;

    public ObjectMapper(Class<T> clazz) {
        Constructor<?> constructor = null;
        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            c.setAccessible(true);
            if (c.getParameterCount() == 0) {
                constructor = c;
                break;
            }
        }
        if (constructor == null) {
            throw new IllegalArgumentException(clazz + " does not have a constructor with 0 parameters");
        }

        List<Object[]> pairs = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            ParseField parseField = field.getAnnotation(ParseField.class);

            if (parseField != null) {
                if ((field.getModifiers() & Modifier.FINAL) != 0) {
                    throw new IllegalArgumentException(
                        "Final fields cannot be annotated with " + ParseField.class.getName());
                }

                String parseFieldName = parseField.value();
                pairs.add(new Object[]{field, "".equals(parseFieldName) ? field.getName() : parseFieldName});
            }
        }

        if (pairs.isEmpty()) {
            throw new IllegalArgumentException(
                clazz + " does not have non-final fields annotated with " + ParseField.class);
        }

        //noinspection unchecked
        this.constructor = (Constructor<T>) constructor;
        this.pairs = pairs;
    }

    public List<T> applyAll(@NotNull Table table) {
        try {
            List<T> list = new ArrayList<>();

            for (Tuple row : table) {
                T instance = constructor.newInstance();

                for (Object[] pair : pairs) {
                    StringMapper value = new StringMapper(row.get((String) pair[1]));
                    Field field = (Field) pair[0];
                    Object parsed = value.as(field.getType());
                    field.set(instance, parsed);
                }

                list.add(instance);
            }

            return Collections.unmodifiableList(list);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T apply(@NotNull Table table) {
        List<T> list = applyAll(table);
        return list.isEmpty() ? null : list.get(0);
    }

}
