package com.miklosova.rdftocsvw.metadata_creator;

/**
 * Reference is a class describing object for annotation "reference" found in "foreignKey".
 * Its goal is to describe in which file and column the foreign key is located.
 * This class is compliant to description by https://www.w3.org/TR/2015/REC-tabular-metadata-20151217/#schemas,
 * in the part called "reference".
 * A link property "schemaReference" was not implemented as its mutually exclusive with "resource" link property.
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class Reference {
    /**
     * The name of the file that contains referenced value
     */
    private final String resource;
    /**
     * The <name> of the column that this foreign key references
     */
    private final String columnReference;

    public Reference(String resource, String columnReference) {
        this.resource = resource;
        this.columnReference = columnReference;
    }
}
