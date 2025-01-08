package com.miklosova.rdftocsvw.converter.data_structure;

import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.List;

public class TypeIdAndValues {
    public Value id;
    public TypeOfValue type;
    public List<Value> values;

    public TypeIdAndValues(Value id, TypeOfValue newType, ArrayList<Value> o) {
        this.type = newType;
        this.values = o;
        this.id = id;
    }
}
