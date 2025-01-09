package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.TableSchema;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.opencsv.CSVReader;
import org.jruby.ir.Tuple;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type Metadata consolidator used to create only one table after parsing data with streaming method which puts the data into multiple CSV files.
 */
public class MetadataConsolidator {
    private static final Logger logger = Logger.getLogger(MetadataConsolidator.class.getName());


    /**
     * Gets matching column for the given Column from tables.
     *
     * @param tables        the tables that are already in metadata
     * @param currentTable  the current table that is being built
     * @param columnToCheck the column to check if there is a matching one to this one
     * @return the matching column's Table. Returns null if there is no matching column in any of the tables.
     */
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

    /**
     * Consolidate metadata metadata.
     *
     * @param oldMetadata the old metadata
     * @return the metadata
     */
    public Metadata consolidateMetadata(Metadata oldMetadata) {
        Metadata newMetadata = new Metadata();
        Map<String, Integer> occurrencesOfColumnName = new HashMap<>();
        File f = new File(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_FILENAME));

        String nameExtension = "_merged.csv";
        Table table = new Table(f.getName() + nameExtension);
        newMetadata.getTables().add(table);
        TableSchema tableSchema = new TableSchema();
        table.setTableSchema(tableSchema);

        for (Table t : oldMetadata.getTables()) {
            for (Column c : t.getTableSchema().getColumns()) {
                boolean exists = tableSchema.getColumns().stream()
                        .anyMatch(obj -> (((c.getSuppressOutput() == null || c.getSuppressOutput() == obj.getSuppressOutput()))
                                && (c.getPropertyUrl() == null || c.getPropertyUrl().equals(obj.getPropertyUrl())) &&
                                (c.getLang() == null || c.getLang().equalsIgnoreCase(obj.getLang()))));
                if (!exists) {
                    if (occurrencesOfColumnName.containsKey(c.getName())) {
                        int occurrenceNumber = occurrencesOfColumnName.get(c.getName());
                        String newName = c.getName() + "_" + occurrenceNumber;
                        c.setName(newName);
                        c.setValueUrl("{+" + newName + "}");
                        occurrencesOfColumnName.put(c.getName(), occurrenceNumber + 1);
                    } else {
                        occurrencesOfColumnName.put(c.getName(), 1);
                    }
                    tableSchema.getColumns().add(c);
                }
            }
        }
        newMetadata.jsonldMetadata();
        return newMetadata;
    }

    /**
     * First column has links to another column tuple.
     *
     * @param oldMetadata the old metadata
     * @param table       the table
     * @return the tuple
     */
    public Tuple<String, String> firstColumnHasLinksToAnotherColumn(Metadata oldMetadata, Table table) {
        for (Table t : oldMetadata.getTables()) {
            if (t != table) {
                try {

                    // Create an object of file reader
                    // class with CSV file as a parameter.
                    String fullFilePath = getFilePathForFileName(t.getUrl());
                    assert fullFilePath != null;
                    FileReader filereader = new FileReader(fullFilePath);
                    String fullFilePathOfGivenColumn = getFilePathForFileName(table.getUrl());
                    assert fullFilePathOfGivenColumn != null;
                    // create csvReader object passing
                    // file reader as a parameter
                    CSVReader csvReader = new CSVReader(filereader);

                    String[] nextRecord;

                    // we are going to read data line by line
                    while ((nextRecord = csvReader.readNext()) != null) {
                        for (int i = 1; i < nextRecord.length; i++) {
                            // Read File 2 line by line
                            try (CSVReader csvReaderFile2 = new CSVReader(new FileReader(fullFilePathOfGivenColumn))) {
                                String[] nextRecordFile2;
                                String cellValue = nextRecord[i];
                                while ((nextRecordFile2 = csvReaderFile2.readNext()) != null) {
                                    if (nextRecordFile2.length == 0) continue; // Skip empty rows
                                    String subjectOfGivenTable = nextRecordFile2[0]; // Always the first column


                                    if (!cellValue.isEmpty() && !subjectOfGivenTable.isEmpty() && cellValue.equalsIgnoreCase(subjectOfGivenTable)) {
                                        return new Tuple<>(t.getUrl(), t.getTableSchema().getColumns().get(i).getName());
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "There was an exception while trying to ascertain if first Column has links to another column.");
                }
            }
        }
        return null;
    }

    /**
     * Gets file path for file name.
     *
     * @param url the url
     * @return the file path for file name
     */
    public static String getFilePathForFileName(String url) {
        String intermediateFiles = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES);
        String[] files = intermediateFiles.split(",");
        for (String file : files) {
            if (file.endsWith(url)) {
                return file;
            }
        }
        return null;
    }
}
