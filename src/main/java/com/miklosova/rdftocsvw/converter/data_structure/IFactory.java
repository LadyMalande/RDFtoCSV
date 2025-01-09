package com.miklosova.rdftocsvw.converter.data_structure;

/**
 * The interface Factory.
 *
 * @param <T> the type parameter
 */
public interface IFactory<T> {
    /**
     * Factory t.
     *
     * @return the t
     */
    T factory();
}
