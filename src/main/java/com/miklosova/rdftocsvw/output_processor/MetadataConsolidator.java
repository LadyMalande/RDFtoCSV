package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.metadata_creator.Column;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.Table;
import com.miklosova.rdftocsvw.metadata_creator.TableSchema;

import java.util.List;

public class MetadataConsolidator {
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

    public Metadata consolidateMetadata(Metadata oldMetadata) {
        Metadata newMetadata = new Metadata();
        for (Table t : oldMetadata.getTables()) {
            TableSchema ts = t.getTableSchema();
            for (Column c : ts.getColumns()) {
                Table hasSameColumn = getMatchingColumn(oldMetadata.getTables(), t, c);
                if (hasSameColumn != null) {
                    System.out.println("This Column is multiple times elsewhere: " + hasSameColumn.getUrl() + " column titles = " + c.getTitles());
                }
            }
        }
        return null;
    }
}
