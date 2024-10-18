package com.miklosova.rdftocsvw.convertor;


import com.miklosova.rdftocsvw.support.IFactory;

import java.util.ArrayList;

public class RowsAndKeys {
    public ArrayList<RowAndKey> getRowsAndKeys() {
        return rowsAndKeys;
    }

    public void setRowsAndKeys(ArrayList<RowAndKey> rowsAndKeys) {
        this.rowsAndKeys = rowsAndKeys;
    }

    public void setHasRDFType(boolean hasRDFType) {
        this.hasRDFType = hasRDFType;
    }

    ArrayList<RowAndKey> rowsAndKeys;

    public boolean isHasRDFType() {
        return hasRDFType;
    }

    private boolean hasRDFType;

    public RowsAndKeys() {
        this.rowsAndKeys = new ArrayList<>();
    }

    //static inner class for Factory<T> implementation
    public static class RowsAndKeysFactory implements IFactory<RowsAndKeys> {
        public RowsAndKeys factory() {
            return new RowsAndKeys();
        }
    }

    public String toString() {
        return "Rows and keys for metadata creation";
    }
}
