package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.support.AppConfig;

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
     * @deprecated Use {@link #MetadataCreator(AppConfig)} instead
     */
    @Deprecated
    public MetadataCreator() {
        this(null);
    }

    /**
     * Instantiates a new Metadata creator with AppConfig.
     * @param config the application configuration
     */
    public MetadataCreator(AppConfig config) {
        this.metadata = new Metadata(config);
    }

}
