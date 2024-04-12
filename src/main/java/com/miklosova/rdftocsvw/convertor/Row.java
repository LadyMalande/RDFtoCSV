package com.miklosova.rdftocsvw.convertor;

import java.util.HashMap;
import java.util.Map;

public class Row {
    public String id;
    public Map<String, String> map;

    public Row(String id) {
        this.id = id;
        this.map = new HashMap<>();
    }
}
