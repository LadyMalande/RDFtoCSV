package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;

/**
 * The base Metadata creator, holds Metadata.
 */
public class MetadataCreator {
    /**
     * The Metadata.
     */
    Metadata metadata;

    /**
     * Instantiates a new Metadata creator.
     */
    public MetadataCreator() {
        this.metadata = new Metadata();
    }


}
