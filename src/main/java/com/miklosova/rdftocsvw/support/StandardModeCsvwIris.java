package com.miklosova.rdftocsvw.support;

/**
 * Class that contains all keywords that help recognize a Standard Mode made RDF. The Standard mode is tied to conversion
 * from CSV to RDF.
 */
public class StandardModeCsvwIris {
    private static final String csvwNamespace = "http://www.w3.org/ns/csvw#";
    
    /**
     * IRI for CSVW TableGroup - represents a group of one or more annotated tables.
     */
    public static String CSVW_TableGroup = csvwNamespace + "TableGroup";
    
    /**
     * IRI for CSVW table - links a TableGroup to its annotated tables.
     */
    public static String CSVW_table = csvwNamespace + "table";
    
    /**
     * IRI for CSVW row - links a table to its row resources.
     */
    public static String CSVW_row = csvwNamespace + "row";
    
    /**
     * IRI for CSVW describes - indicates the subject that a row describes.
     */
    public static String CSVW_describes = csvwNamespace + "describes";
    
    /**
     * IRI for CSVW rownum - the position of the row within the table (1-based).
     */
    public static String CSVW_rownum = csvwNamespace + "rownum";
    
    /**
     * IRI for CSVW url - the URL of a table or other resource.
     */
    public static String CSVW_url = csvwNamespace + "url";

}
