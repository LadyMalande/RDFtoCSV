package com.miklosova.rdftocsvw.metadata_creator.metadata_structure;

/**
 * Reference is a class describing object for annotation "reference" found in "foreignKey".
 * Its goal is to describe in which file and column the foreign key is located.
 * This class is compliant to description by <a href="https://www.w3.org/TR/2015/REC-tabular-metadata-20151217/#schemas">#schemas</a>,
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

    /**
     * Reference for another table value
     * @param resource From which column
     * @param columnReference To which column
     */
    public Reference(String resource, String columnReference) {
        this.resource = resource;
        this.columnReference = columnReference;
    }
}
