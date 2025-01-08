package com.miklosova.rdftocsvw.converter.data_structure;

import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;

public class RowAndKey {
    ArrayList<Value> keys;
    ArrayList<Row> rows;

    public RowAndKey() {
        this.keys = new ArrayList<>();
        this.rows = new ArrayList<>();
    }

    public RowAndKey(ArrayList<Value> keys, ArrayList<Row> rows) {
        this.keys = keys;
        this.rows = rows;
    }

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

    //static inner class for Factory<T> implementation
    public static class RowAndKeyFactory implements IFactory<RowAndKey> {
        public RowAndKey factory() {
            return new RowAndKey();
        }
    }
}
