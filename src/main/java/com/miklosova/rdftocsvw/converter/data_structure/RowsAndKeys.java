package com.miklosova.rdftocsvw.converter.data_structure;


import java.util.ArrayList;

public class RowsAndKeys {
    ArrayList<RowAndKey> rowsAndKeys;

    public RowsAndKeys() {
        this.rowsAndKeys = new ArrayList<>();
    }

    public ArrayList<RowAndKey> getRowsAndKeys() {
        return rowsAndKeys;
    }

    public void setRowsAndKeys(ArrayList<RowAndKey> rowsAndKeys) {
        this.rowsAndKeys = rowsAndKeys;
    }

    public String toString() {
        return "Rows and keys for metadata creation";
    }

    //static inner class for Factory<T> implementation
    public static class RowsAndKeysFactory implements IFactory<RowsAndKeys> {
        public RowsAndKeys factory() {
            return new RowsAndKeys();
        }
    }
}
