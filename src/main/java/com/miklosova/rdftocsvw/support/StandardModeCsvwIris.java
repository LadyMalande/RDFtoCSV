package com.miklosova.rdftocsvw.support;

/**
 * Class that contains all keywords that help recognize a Standard Mode made RDF. The Standard mode is tied to conversion
 * from CSV to RDF.
 */
public class StandardModeCsvwIris {
    private static final String csvwNamespace = "http://www.w3.org/ns/csvw#";
    public static String CSVW_TableGroup = csvwNamespace + "TableGroup";
    public static String CSVW_table = csvwNamespace + "table";
    public static String CSVW_row = csvwNamespace + "row";
    public static String CSVW_describes = csvwNamespace + "describes";
    public static String CSVW_rownum = csvwNamespace + "rownum";
    public static String CSVW_url = csvwNamespace + "url";

}
