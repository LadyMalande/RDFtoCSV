package com.miklosova.rdftocsvw.converter.data_structure;

import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Type id and values.
 */
public class TypeIdAndValues {
    /**
     * The Id.
     */
    public Value id;
    /**
     * The Type.
     */
    public TypeOfValue type;
    /**
     * The Values.
     */
    public List<Value> values;

    /**
     * Instantiates a new Type id and values.
     *
     * @param id      the id
     * @param newType the new type
     * @param o       the o
     */
    public TypeIdAndValues(Value id, TypeOfValue newType, ArrayList<Value> o) {
        this.type = newType;
        this.values = o;
        this.id = id;
    }
}
