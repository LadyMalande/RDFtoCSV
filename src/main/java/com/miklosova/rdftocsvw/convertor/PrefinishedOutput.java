package com.miklosova.rdftocsvw.convertor;

import com.miklosova.rdftocsvw.support.IFactory;

public class PrefinishedOutput<T> {
    T prefinishedOutput;

    PrefinishedOutput(IFactory<T> fact) {
        System.out.println("Constructor with IFactory<T> parameter");
        prefinishedOutput = fact.factory();
    }
    public PrefinishedOutput(T prefinishedOutput) {
        System.out.println("Constructor with T parameter");
        this.prefinishedOutput = prefinishedOutput;
    }

    T get() { return prefinishedOutput; }


    public T getPrefinishedOutput() {
        return prefinishedOutput;
    }

    public void setPrefinishedOutput(T prefinishedOutput) {
        this.prefinishedOutput = prefinishedOutput;
    }
}
