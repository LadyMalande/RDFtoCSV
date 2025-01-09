package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;

/**
 * The Metadata gateway. Used in Strategy design pattern for choosing Metadata Creators according to chosen conversion method.
 */
public class MetadataGateway {
    private IMetadataCreator metadataCreator;

    /**
     * Sets metadata creator.
     *
     * @param metadataCreator the metadata creator
     */
    public void setMetadataCreator(IMetadataCreator metadataCreator) {
        this.metadataCreator = metadataCreator;
    }

    /**
     * Process input metadata.
     *
     * @param prefinishedOutput the prefinished output - inner representation of the CSV
     * @return the metadata
     */
    public Metadata processInput(PrefinishedOutput prefinishedOutput) {
        return metadataCreator.addMetadata(prefinishedOutput);
    }
}
