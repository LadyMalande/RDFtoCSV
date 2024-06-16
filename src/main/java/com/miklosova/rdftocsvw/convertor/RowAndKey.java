package com.miklosova.rdftocsvw.convertor;

import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;

public class RowAndKey {
    ArrayList<Value> keys;
    ArrayList<Row> rows;

    public ArrayList<Value> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<Value> keys) {
        this.keys = keys;
    }

    public ArrayList<Row> getRows() {
        return rows;
    }

    public void setRows(ArrayList<Row> rows) {
        this.rows = rows;
    }

    public RowAndKey(ArrayList<Value> keys, ArrayList<Row> rows) {
        this.keys = keys;
        this.rows = rows;
    }
}
