package com.miklosova.rdftocsvw.support;

import java.util.Objects;

/**
 * A simple immutable pair of two values.
 * Replacement for org.jruby.ir.Tuple to eliminate JRuby dependency.
 *
 * @param <T> Type of the first value
 * @param <U> Type of the second value
 */
public class Pair<T, U> {
    private final T first;
    private final U second;

    /**
     * Constructs a new Pair.
     *
     * @param first  the first value
     * @param second the second value
     */
    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Gets the first value.
     *
     * @return the first value
     */
    public T getFirst() {
        return first;
    }

    /**
     * Gets the second value.
     *
     * @return the second value
     */
    public U getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
