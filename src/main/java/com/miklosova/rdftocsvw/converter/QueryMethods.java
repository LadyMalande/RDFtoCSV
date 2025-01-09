package com.miklosova.rdftocsvw.converter;

/**
 * The enum Query methods.
 */
public enum QueryMethods {
    /**
     * Basic query query methods.
     */
    BASIC_QUERY("basicQuery"),
    /**
     * Split query query methods.
     */
    SPLIT_QUERY("splitQuery");

    private final String value;

    QueryMethods(String value) {
        this.value = value;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }
}
