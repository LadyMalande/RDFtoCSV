package com.miklosova.rdftocsvw.converter.data_structure;

/**
 * The class for storing one of three possible states of a resource in RDF
 */
public enum TypeOfValue {
    /**
     * IRI type of value.
     */
    IRI,
    /**
     * Blank Node type of value.
     */
    BNODE,
    /**
     * Literal type of value.
     */
    LITERAL
}
