package com.miklosova.rdftocsvw.metadata;

public class FileUrlDescriptor {
    /**
     * Name for the file that is connected to the metadata
     */
    private String url;
    /**
     * Object containing all the information about the given table in file from url attribute
     */
    private TableSchema tableSchema;

    public FileUrlDescriptor(String url){
        this.url = url;

    }
}
