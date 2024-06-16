package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;

public interface IOutputProcessor {
    public FinalizedOutput processCSVToOutput(PrefinishedOutput prefinishedOutput);
}
