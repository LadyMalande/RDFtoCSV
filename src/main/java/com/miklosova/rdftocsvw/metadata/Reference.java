package com.miklosova.rdftocsvw.metadata;

public class Reference {
    /**
     * The name of the file that contains referenced value
     */
    private String resource;
    /**
     * The <name> of the column that this foreign key references
     */
    private String columnReference;

    public Reference(String resource, String columnReference) {
        this.resource = resource;
        this.columnReference = columnReference;
    }
}
