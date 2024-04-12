package com.miklosova.rdftocsvw.convertor;

public class PrefinishedOutput<T> {
    T prefinishedOutput;

    public PrefinishedOutput(T prefinishedOutput) {
        this.prefinishedOutput = prefinishedOutput;
    }

    public T getPrefinishedOutput() {
        return prefinishedOutput;
    }

    public void setPrefinishedOutput(T prefinishedOutput) {
        this.prefinishedOutput = prefinishedOutput;
    }
}
