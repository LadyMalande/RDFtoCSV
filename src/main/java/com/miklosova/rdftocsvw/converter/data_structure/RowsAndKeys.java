package com.miklosova.rdftocsvw.converter.data_structure;


import java.util.ArrayList;

/**
 * The type Rows and keys.
 */
public class RowsAndKeys {
    /**
     * The Rows and keys.
     */
    ArrayList<RowAndKey> rowsAndKeys;

    /**
     * Instantiates a new Rows and keys.
     */
    public RowsAndKeys() {
        this.rowsAndKeys = new ArrayList<>();
    }

    /**
     * Gets rows and keys.
     *
     * @return the rows and keys
     */
    public ArrayList<RowAndKey> getRowsAndKeys() {
        return rowsAndKeys;
    }

    /**
     * Sets rows and keys.
     *
     * @param rowsAndKeys the rows and keys
     */
    public void setRowsAndKeys(ArrayList<RowAndKey> rowsAndKeys) {
        this.rowsAndKeys = rowsAndKeys;
    }

    public String toString() {
        return "Rows and keys for metadata creation";
    }

    /**
     * The type Rows and keys factory.
     */
//static inner class for Factory<T> implementation
    public static class RowsAndKeysFactory implements IFactory<RowsAndKeys> {
        public RowsAndKeys factory() {
            return new RowsAndKeys();
        }
    }
}
