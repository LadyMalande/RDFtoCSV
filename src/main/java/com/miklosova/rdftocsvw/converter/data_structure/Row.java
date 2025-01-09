package com.miklosova.rdftocsvw.converter.data_structure;

import org.eclipse.rdf4j.model.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * The Row for inner CSV representation with types and values of data.
 */
public class Row {
    /**
     * Gets id.
     *
     * @return the id = the subject of the row (the subject leading all main triples gathered in the row)
     */
    public Value getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id of the row (the subject of all main triples connected to the id in the row)
     */
    public void setId(Value id) {
        this.id = id;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public Value getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(Value type) {
        this.type = type;
    }

    /**
     * Gets columns.
     *
     * @return the columns
     */
    public Map<Value, TypeIdAndValues> getColumns() {
        return columns;
    }

    /**
     * Sets columns.
     *
     * @param columns the columns
     */
    public void setColumns(Map<Value, TypeIdAndValues> columns) {
        this.columns = columns;
    }


    /**
     * The Id.
     */
    public Value id;
    /**
     * The type of the row. Can be either rdf:type or a dominant predicate
     */
    public Value type;
    /**
     * The Is rdf type. True if the type of the row is from rdf:type
     */
    public boolean isRdfType;
    /**
     * The Columns. Represented by propertyUrl (predicate), and tied objects (type of the object, its subject and its value)
     */
    public Map<Value, TypeIdAndValues> columns;

    /**
     * Instantiates a new Row.
     *
     * @param id      the id = the subject of the row
     * @param type    the type of the row is there is any
     * @param rdfType the rdf type is true, if the type is tied to rdf:type
     */
    public Row(Value id, Value type, boolean rdfType) {
        this.id = id;
        this.type = type;
        this.isRdfType = rdfType;
        this.columns = new HashMap<>();
    }

    /**
     * Instantiates a new Row.
     *
     * @param id      the id - the aboutUrl, the subject
     * @param rdfType if the type of the row is rdf type
     */
    public Row(Value id, boolean rdfType) {
        this.id = id;
        this.type = null;
        this.isRdfType = rdfType;
        this.columns = new HashMap<>();
    }
}
