package com.miklosova.rdftocsvw.metadata;

import com.miklosova.rdftocsvw.convertor.Row;
import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;

public class FileUrlDescriptor {
    /**
     * Name for the file that is connected to the metadata
     */
    private String url;
    /**
     * Object containing all the information about the given table in file from url attribute
     */
    private TableSchema tableSchema;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public TableSchema getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(TableSchema tableSchema) {
        this.tableSchema = tableSchema;
    }

    public FileUrlDescriptor(String url){
        this.url = url;

    }

    public void addTableMetadata(ArrayList<Value> keys, ArrayList<Row> rows) {

        this.tableSchema = new TableSchema(keys, rows);
        this.tableSchema.addTableSchemaMetadata();
    }
}
