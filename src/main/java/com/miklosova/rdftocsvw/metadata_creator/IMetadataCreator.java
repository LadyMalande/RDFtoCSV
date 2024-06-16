package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public interface IMetadataCreator {
    public Metadata addMetadata(PrefinishedOutput<? extends Object> info);
}
