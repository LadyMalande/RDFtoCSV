package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;

public interface IOutputProcessor<T> {
    public FinalizedOutput<byte[]> processCSVToOutput(PrefinishedOutput<?> prefinishedOutput);
}
