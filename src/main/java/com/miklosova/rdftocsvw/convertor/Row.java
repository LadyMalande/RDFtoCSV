package com.miklosova.rdftocsvw.convertor;

import org.eclipse.rdf4j.model.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Row {
    public Value id;
    public Value type;
    public Map<Value, List<Value>> map;

    public Row(Value id, Value type) {
        this.id = id;
        this.type = type;
        this.map = new HashMap<>();
    }
}
