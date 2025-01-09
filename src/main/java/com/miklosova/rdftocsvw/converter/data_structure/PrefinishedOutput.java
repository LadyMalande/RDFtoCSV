package com.miklosova.rdftocsvw.converter.data_structure;

/**
 * The Prefinished output = inner CSV representation with types and other metadata.
 *
 * @param <T> the type parameter
 */
public class PrefinishedOutput<T> {
    /**
     * The Prefinished output.
     */
    T prefinishedOutput;

    /**
     * Instantiates a new Prefinished output.
     *
     * @param fact the fact
     */
    PrefinishedOutput(IFactory<T> fact) {
        prefinishedOutput = fact.factory();
    }

    /**
     * Instantiates a new Prefinished output.
     *
     * @param prefinishedOutput the prefinished output
     */
    public PrefinishedOutput(T prefinishedOutput) {
        this.prefinishedOutput = prefinishedOutput;
    }

    /**
     * Gets prefinished output.
     *
     * @return the prefinished output
     */
    public T getPrefinishedOutput() {
        return prefinishedOutput;
    }

    /**
     * Sets prefinished output.
     *
     * @param prefinishedOutput the prefinished output
     */
    public void setPrefinishedOutput(T prefinishedOutput) {
        this.prefinishedOutput = prefinishedOutput;
    }
}
