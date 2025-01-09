package com.miklosova.rdftocsvw.converter.data_structure;

import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;

/**
 * The inner representation of a Table. Contains all table rows and all table headers.
 */
public class RowAndKey {
    /**
     * The Keys. Predicates of all the Rows
     */
    ArrayList<Value> keys;
    /**
     * The Rows. All rows
     */
    ArrayList<Row> rows;

    /**
     * Instantiates a new Row and key.
     */
    public RowAndKey() {
        this.keys = new ArrayList<>();
        this.rows = new ArrayList<>();
    }

    /**
     * Instantiates a new Row and key.
     *
     * @param keys the keys
     * @param rows the rows
     */
    public RowAndKey(ArrayList<Value> keys, ArrayList<Row> rows) {
        this.keys = keys;
        this.rows = rows;
    }

    /**
     * Gets keys.
     *
     * @return the keys
     */
    public ArrayList<Value> getKeys() {
        return keys;
    }

    /**
     * Sets keys.
     *
     * @param keys the keys
     */
    public void setKeys(ArrayList<Value> keys) {
        this.keys = keys;
    }

    /**
     * Gets rows.
     *
     * @return the rows
     */
    public ArrayList<Row> getRows() {
        return rows;
    }

    /**
     * Sets rows.
     *
     * @param rows the rows
     */
    public void setRows(ArrayList<Row> rows) {
        this.rows = rows;
    }

    /**
     * The type Row and key factory.
     */
//static inner class for Factory<T> implementation
    public static class RowAndKeyFactory implements IFactory<RowAndKey> {
        public RowAndKey factory() {
            return new RowAndKey();
        }
    }
}
