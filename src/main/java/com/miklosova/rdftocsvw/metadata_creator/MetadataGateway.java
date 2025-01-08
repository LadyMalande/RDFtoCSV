package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;

public class MetadataGateway {
    private IMetadataCreator metadataCreator;

    public void setMetadataCreator(IMetadataCreator metadataCreator) {
        this.metadataCreator = metadataCreator;
    }

    public Metadata processInput(PrefinishedOutput prefinishedOutput) {
        return metadataCreator.addMetadata(prefinishedOutput);
    }
}
