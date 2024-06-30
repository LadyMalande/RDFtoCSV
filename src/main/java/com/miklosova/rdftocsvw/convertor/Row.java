package com.miklosova.rdftocsvw.convertor;

import org.eclipse.rdf4j.model.Value;

import java.util.HashMap;
import java.util.Map;

public class Row {
    public Value id;
    public Value type;
    public Map<Value, TypeIdAndValues> columns;

    public Row(Value id, Value type) {
        this.id = id;
        this.type = type;
        this.columns = new HashMap<>();
    }
}
