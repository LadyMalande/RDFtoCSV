package com.miklosova.rdftocsvw.metadata_creator.metadata_structure;

/**
 * This class is compliant to description by https://www.w3.org/TR/2015/REC-tabular-metadata-20151217/#schemas,
 * in the part called "foreignKeys"
 */
public class ForeignKey {
    /**
     * The name of the column that is a foreign key in this file
     */
    private final String columnReference;

    public String getColumnReference() {
        return columnReference;
    }

    public Reference getReference() {
        return reference;
    }

    /**
     * Reference for the foreign key containing the file name and the column name of the reference.
     */
    private final Reference reference;

    public ForeignKey(String columnReference, Reference reference) {
        this.columnReference = columnReference;
        this.reference = reference;
    }
}
