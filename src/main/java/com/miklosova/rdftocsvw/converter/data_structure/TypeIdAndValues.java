package com.miklosova.rdftocsvw.converter.data_structure;

import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * The Type, id and values. Inner representation of a Cell inside a Row.
 */
public class TypeIdAndValues {
    /**
     * The Id. Subject of the value in the cell.
     */
    public Value id;
    /**
     * The Type of the value in the cell.
     */
    public TypeOfValue type;
    /**
     * The Values present in the cell.
     */
    public List<Value> values;

    /**
     * Instantiates a new Type id and values.
     *
     * @param id      the id - the subject
     * @param newType the new type
     * @param o       the objects
     */
    public TypeIdAndValues(Value id, TypeOfValue newType, ArrayList<Value> o) {
        this.type = newType;
        this.values = o;
        this.id = id;
    }
}
