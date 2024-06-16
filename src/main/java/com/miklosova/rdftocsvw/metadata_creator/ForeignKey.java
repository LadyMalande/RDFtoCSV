package com.miklosova.rdftocsvw.metadata_creator;

/**
 * This class is compliant to description by https://www.w3.org/TR/2015/REC-tabular-metadata-20151217/#schemas,
 * in the part called "foreignKeys"
 */
public class ForeignKey {
    /**
     * The name of the column that is a foreign key in this file
     */
    private String columnReference;
    /**
     * Reference for the foreign key containing the file name and the column name of the reference.
     */
    private Reference reference;

    public ForeignKey(String columnReference, Reference reference) {
        this.columnReference = columnReference;
        this.reference = reference;
    }
}
