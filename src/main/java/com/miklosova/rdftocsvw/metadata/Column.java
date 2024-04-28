package com.miklosova.rdftocsvw.metadata;

public class Column {
    /**
     * Title for the column
     */
    private String titles;
    /**
     * Column name for the foreign key feature to work. Also used as the property key name when transforming to JSON/RD
     */
    private String name;
    /**
     * Name to map when transforming back to JSON/RDF - can be the same at multiple columns (will differ by language for example)
     */
    private String propertyUrl;
    /**
     * The value of the cell is combined by the template in this value and a substitute given in the template from given name column
     * http://example.org/country/{country}
     */
    private String valueUrl;
    /**
     * datatype from the literal value. Helps with transforming the data back to JSON/RDF.
     */
    private String datatype;
    /**
     * Separator for the case that the cell contains multiple values
     */
    private String separator;
    /**
     * Used for list of values to indicate that the order of the values when transforming should not be changed.
     */
    private Boolean ordered;
    /**
     * Denotes in which language the given column has the values. Helps with transforming the data back to JSON/RDF.
     */
    private LanguageTag lang;
    /**
     * Record for column that is not displayed in the .csv but has significance for the meaning of data.
     * Namely useful for denoting the rdf:type of all the rows in one file.
     * {
     *   "virtual": true,
     *   "propertyUrl": "rdf:type",
     *   "valueUrl": "schema:Country"
     * }
     */
    private Boolean virtual;

}
