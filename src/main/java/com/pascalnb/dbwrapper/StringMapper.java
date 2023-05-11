package com.pascalnb.dbwrapper;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

@SuppressWarnings({"unused", "unchecked"})
public class StringMapper {

    private final String string;

    public StringMapper(Object object) {
        this.string = object == null ? null : object.toString();
    }

    @Nullable
    public <T> T to(@Nullable T type) {
        return type == null ? null : (T) cast(string, type.getClass());
    }

    public <T> T to(@NotNull Class<T> clazz) {
        return (T) cast(string, clazz);
    }

    @Nullable
    public <T> T applyIfNotNull(Function<String, T> mapper) {
        return string == null ? null : mapper.apply(string);
    }

    public <T> T apply(Function<String, T> mapper) {
        return mapper.apply(string);
    }

    public <T> T asOrDefault(T fallback) {
        return string == null ? fallback : (T) cast(string, fallback.getClass());
    }

    @Nullable
    public String toNullableString() {
        return string;
    }

    @Override
    @NotNull
    public String toString() {
        return Objects.requireNonNull(string);
    }

    public int toInt() {
        return (int) cast(string, Integer.TYPE);
    }

    public boolean toBoolean() {
        return (boolean) cast(string, Boolean.TYPE);
    }

    public double toDouble() {
        return (double) cast(string, Double.TYPE);
    }

    public long toLong() {
        return (long) cast(string, Long.TYPE);
    }

    public float toFloat() {
        return (float) cast(string, Float.TYPE);
    }

    public char toChar() {
        return (char) cast(string, Character.TYPE);
    }

    public short toShort() {
        return (short) cast(string, Short.TYPE);
    }

    public byte toByte() {
        return (byte) cast(string, Byte.TYPE);
    }

    public boolean isNull() {
        return string == null;
    }

    @Contract(value = "null, _ -> null; !null, _ -> !null", pure = true)
    private static Object cast(@Nullable String string, @NotNull Class<?> clazz) {
        if (string == null) {
            return null;
        }
        if (clazz == String.class) {
            return string;
        }
        if (clazz == Integer.TYPE || clazz == Integer.class) {
            return Integer.parseInt(string);
        }
        if (clazz == Boolean.TYPE || clazz == Boolean.class) {
            return Boolean.parseBoolean(string) || "t".equals(string) || "1".equals(string);
        }
        if (clazz == Double.TYPE || clazz == Double.class) {
            return Double.parseDouble(string);
        }
        if (clazz == Long.TYPE || clazz == Long.class) {
            return Long.parseLong(string);
        }
        if (clazz == Float.TYPE || clazz == Float.class) {
            return Float.parseFloat(string);
        }
        if (clazz == Character.TYPE || clazz == Character.class) {
            return string.charAt(0);
        }
        if (clazz == Short.TYPE || clazz == Short.class) {
            return Short.parseShort(string);
        }
        if (clazz == Byte.TYPE || clazz == Byte.class) {
            return Byte.parseByte(string);
        }
        throw new UnsupportedOperationException("Cannot cast string to " + clazz.getName());
    }

}