package com.miklosova.rdftocsvw.converter.data_structure;

/**
 * The type Prefinished output.
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
        System.out.println("Constructor with IFactory<T> parameter start");
        prefinishedOutput = fact.factory();
        System.out.println("Constructor with IFactory<T> parameter end");
    }

    /**
     * Instantiates a new Prefinished output.
     *
     * @param prefinishedOutput the prefinished output
     */
    public PrefinishedOutput(T prefinishedOutput) {
        System.out.println("Constructor with T parameter");
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
