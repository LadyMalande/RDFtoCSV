package com.miklosova.rdftocsvw.output_processor;

public class FinalizedOutput<T> {
    T outputData;

    public FinalizedOutput(T outputData) {
        this.outputData = outputData;
    }

    public T getOutputData() {
        return outputData;
    }
}
