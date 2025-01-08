package com.miklosova.rdftocsvw.converter.data_structure;

import org.eclipse.rdf4j.model.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Row.
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
    public Value type;
    /**
     * The Is rdf type.
     */
    public boolean isRdfType;
    /**
     * The Columns.
     */
    public Map<Value, TypeIdAndValues> columns;

    /**
     * Instantiates a new Row.
     *
     * @param id      the id
     * @param type    the type
     * @param rdfType the rdf type
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
     * @param id      the id
     * @param rdfType the rdf type
     */
    public Row(Value id, boolean rdfType) {
        this.id = id;
        this.type = null;
        this.isRdfType = rdfType;
        this.columns = new HashMap<>();
    }
}
