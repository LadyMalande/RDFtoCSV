package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;

/**
 * The interface Metadata creator that all Metadata Creators must implement.
 */
public interface IMetadataCreator {
    /**
     * Add metadata by analyzing the inner structure of the data.
     *
     * @param info the already analyzed inner structure of data
     * @return the created metadata
     */
    Metadata addMetadata(PrefinishedOutput<?> info);
}
