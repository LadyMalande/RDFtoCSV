package com.miklosova.rdftocsvw.metadata;

public class TableSchema {
    /**
     * Array of column objects containing information about each column
     */
    private Column[] columns;
    /**
     * Array of objects containing information about foreign keys in this table
     */
    private ForeignKey[] foreignKeys;
    /**
     * Designates the primary key of the table (usually will be the first column)
     */
    private String primaryKey;
    /**
     *
     * http://example.org/country/{code}
     */
    private String aboutUrl;
    /**
     * Array of names of columns that should summarize what the row is about (usually the primary key)
     */
    private String[] rowTitles;
 }
