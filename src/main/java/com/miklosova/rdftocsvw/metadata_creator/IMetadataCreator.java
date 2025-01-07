package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;

public interface IMetadataCreator {
    Metadata addMetadata(PrefinishedOutput<?> info);
}
