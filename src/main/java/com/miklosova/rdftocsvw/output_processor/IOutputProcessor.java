package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;

public interface IOutputProcessor {
    FinalizedOutput<byte[]> processCSVToOutput(PrefinishedOutput<?> prefinishedOutput);
}
