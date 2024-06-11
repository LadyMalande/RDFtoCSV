package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.IQueryParser;
import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class MetadataGateway {
    private IMetadataCreator metadataCreator;

    public void setMetadataCreator(IMetadataCreator metadataCreator) {
        this.metadataCreator = metadataCreator;
    }

    public Metadata processInput(PrefinishedOutput prefinishedOutput) {
        return metadataCreator.addMetadata(prefinishedOutput);
    }
}
