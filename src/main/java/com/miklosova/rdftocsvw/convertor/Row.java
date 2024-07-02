package com.miklosova.rdftocsvw.convertor;

import org.eclipse.rdf4j.model.Value;

import java.util.HashMap;
import java.util.Map;

public class Row {
    public Value id;
    public Value type;
    public boolean isRdfType;
    public Map<Value, TypeIdAndValues> columns;

    public Row(Value id, Value type, boolean rdfType) {
        this.id = id;
        this.type = type;
        this.isRdfType = rdfType;
        this.columns = new HashMap<>();
    }

    public Row(Value id, boolean rdfType) {
        this.id = id;
        this.type = null;
        this.isRdfType = rdfType;
        this.columns = new HashMap<>();
    }
}
