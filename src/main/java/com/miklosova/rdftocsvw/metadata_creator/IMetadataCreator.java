package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;

public interface IMetadataCreator {
    Metadata addMetadata(PrefinishedOutput<?> info);
}
