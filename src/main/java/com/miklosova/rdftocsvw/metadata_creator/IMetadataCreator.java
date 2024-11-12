package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;

public interface IMetadataCreator {
    public Metadata addMetadata(PrefinishedOutput<? extends Object> info);
}
