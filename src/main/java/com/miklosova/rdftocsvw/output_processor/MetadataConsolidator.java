package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.metadata_creator.Column;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.Table;
import com.miklosova.rdftocsvw.metadata_creator.TableSchema;
import com.miklosova.rdftocsvw.support.ConfigurationManager;

import java.util.ArrayList;
import java.util.List;

public class MetadataConsolidator {

    private final String nameExtension = "_merged";

    public static Table getMatchingColumn(List<Table> tables, Table currentTable, Column columnToCheck) {
        for (Table table : tables) {
            // Exclude the table being investigated
            if (table.equals(currentTable)) {
                continue;
            }

            // Check if any column in the table matches the propertyUrl
            List<Column> columns = table.getTableSchema().getColumns();
            for (Column column : columns) {
                if (!column.getName().equalsIgnoreCase("subject") && column.getPropertyUrl().equals(columnToCheck.getPropertyUrl())) {
                    return table;
                }
            }
        }
        return null;
    }

    private boolean columnAlreadyInColumns(List<Column> columns, Column column){
        for(Column col: columns){
            if((column.getSuppressOutput() != null && column.getSuppressOutput()) && (col.getSuppressOutput() != null && col.getSuppressOutput())  ) {
                if(col.getName().equalsIgnoreCase(column.getName())){
                    return true;
                }
            } else {
                System.out.println("Column name " + column.getName() + " colname: " + col.getName());
                if ((col.getPropertyUrl() != null && column.getPropertyUrl() != null) && col.getPropertyUrl().equalsIgnoreCase(column.getPropertyUrl())) {
                    if (col.getLang() != null && column.getLang() != null && col.getLang().equalsIgnoreCase(column.getLang())) {
                        System.out.println("Column " + column.getName() + " already in columns");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Metadata consolidateMetadata(Metadata oldMetadata) {
        Metadata newMetadata = new Metadata();

        Table table = new Table(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_FILENAME) + nameExtension);
        newMetadata.getTables().add(table);
        TableSchema tableSchema = new TableSchema();
        table.setTableSchema(tableSchema);
        for (Table t : oldMetadata.getTables()) {
            TableSchema ts = t.getTableSchema();
            for (Column c : ts.getColumns()) {
                if(!columnAlreadyInColumns(tableSchema.getColumns(), c)){
                    tableSchema.getColumns().add(c);
                }
            }
        }
        return newMetadata;
    }
}
