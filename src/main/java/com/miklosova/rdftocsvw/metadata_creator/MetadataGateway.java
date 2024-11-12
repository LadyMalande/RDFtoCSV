package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;

public class MetadataGateway {
    private IMetadataCreator metadataCreator;

    public void setMetadataCreator(IMetadataCreator metadataCreator) {
        this.metadataCreator = metadataCreator;
    }

    public Metadata processInput(PrefinishedOutput prefinishedOutput) {
        return metadataCreator.addMetadata(prefinishedOutput);
    }
}
