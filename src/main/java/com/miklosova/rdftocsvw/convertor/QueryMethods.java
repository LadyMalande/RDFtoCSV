package com.miklosova.rdftocsvw.convertor;

public enum QueryMethods {
    BASIC_QUERY("basicQuery"),
    SPLIT_QUERY("splitQuery");


    private final String value;

    QueryMethods(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
