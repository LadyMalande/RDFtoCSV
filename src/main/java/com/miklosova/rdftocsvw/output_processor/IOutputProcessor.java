package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;

/**
 * The interface Output processor. Used to standardize the object that is being sent to the output.
 */
public interface IOutputProcessor {
    /**
     * Process CSV to finalized byte[] output.
     *
     * @param prefinishedOutput the prefinished output
     * @return the finalized output
     */
    FinalizedOutput<byte[]> processCSVToOutput(PrefinishedOutput<?> prefinishedOutput);
}
