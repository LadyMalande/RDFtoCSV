package com.miklosova.rdftocsvw.output_processor;

/**
 * The class for Finalized output at the very end of the conversion process.
 *
 * @param <T> the type parameter
 */
@SuppressWarnings("unused")
public class FinalizedOutput<T> {
    /**
     * The Output data.
     */
    T outputData;

    /**
     * Instantiates a new Finalized output.
     *
     * @param outputData the output data
     */
    public FinalizedOutput(T outputData) {
        this.outputData = outputData;
    }

    /**
     * Gets output data.
     *
     * @return the output data
     */
    public T getOutputData() {
        return outputData;
    }
}
