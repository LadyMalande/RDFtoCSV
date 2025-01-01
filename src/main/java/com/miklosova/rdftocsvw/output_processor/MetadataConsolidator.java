package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.metadata_creator.Column;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.Table;
import com.miklosova.rdftocsvw.metadata_creator.TableSchema;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.opencsv.CSVReader;
import org.jruby.ir.Tuple;

import java.io.File;
import java.io.FileReader;
import java.util.*;

public class MetadataConsolidator {

    private final String nameExtension = "_merged.csv";

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
        System.out.println("Consolidating metadata ");
        Metadata newMetadata = new Metadata();
        Map<String, Integer> occurancesOfColumnName = new HashMap<>();
        Map<String, Tuple<String, String>> newNamesMappingToFilesAndNames = new HashMap<>();
        File f = new File(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_FILENAME));

        Table table = new Table(f.getName() + nameExtension);
        newMetadata.getTables().add(table);
        TableSchema tableSchema = new TableSchema();
        table.setTableSchema(tableSchema);
        Tuple<String, String> fileAndNameOfTheNewAboutUrlColumn;

        for (Table t : oldMetadata.getTables()) {
            for(Column c : t.getTableSchema().getColumns()){
                boolean exists = tableSchema.getColumns().stream()
                        .anyMatch(obj -> (((c.getSuppressOutput() == null || c.getSuppressOutput() == obj.getSuppressOutput()))
                                && ( c.getPropertyUrl() == null || c.getPropertyUrl().equals(obj.getPropertyUrl())) &&
                                (c.getLang() == null || c.getLang().equalsIgnoreCase(obj.getLang())) ));
                if(!exists){
                    if(occurancesOfColumnName.containsKey(c.getName())){
                        int occurrenceNumber = occurancesOfColumnName.get(c.getName());
                        String newName = c.getName() + "_" + occurrenceNumber;
                        c.setName(newName);
                        c.setValueUrl("{+" + newName + "}");
                        occurancesOfColumnName.put(c.getName(), occurrenceNumber+1);
                    } else {
                        occurancesOfColumnName.put(c.getName(), 1);
                    }
                    tableSchema.getColumns().add(c);
                }
            }
        }
/*
        Metadata newMetadata = new Metadata();
        Map<String, Integer> occurancesOfColumnName = new HashMap<>();
        Map<String, Tuple<String, String>> newNamesMappingToFilesAndNames = new HashMap<>();
        File f = new File(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_FILENAME));

        Table table = new Table(f.getName() + nameExtension);
        newMetadata.getTables().add(table);
        TableSchema tableSchema = new TableSchema();
        table.setTableSchema(tableSchema);
        Tuple<String, String> fileAndNameOfTheNewAboutUrlColumn;

        for (Table t : oldMetadata.getTables()) {
            System.out.println("Going through table " + t.getUrl());
            TableSchema ts = t.getTableSchema();
            fileAndNameOfTheNewAboutUrlColumn = firstColumnHasLinksToAnotherColumn(oldMetadata, ts.getColumnByName("Subject"), t);
            if(fileAndNameOfTheNewAboutUrlColumn == null){
                // Column has no ties to different CSVs so it can be transfered 1:1 with all its columns, there will be no changes in them
                String oldName;
                for(Column c : ts.getColumns()){
                    oldName = c.getName();
                    if(occurancesOfColumnName.containsKey(c.getName())){
                        int occurrenceNumber = occurancesOfColumnName.get(c.getName());
                        c.setName(c.getName() + "_" + occurrenceNumber);
                        occurancesOfColumnName.put(c.getName(), occurrenceNumber+1);
                    } else {
                        occurancesOfColumnName.put(c.getName(), 1);
                    }
                    newNamesMappingToFilesAndNames.put(c.getName(), new Tuple<>(t.getUrl(),oldName));
                    tableSchema.getColumns().add(c);
                    System.out.println("Column not anywhere else added to new columns: " + c.getName());
                }
            } else {
                // First column has ties to a different column meaning the columns will be relinked to different aboutUrls
                for (Column c : ts.getColumns()) {

                        // The subject column is not being transfered to merged data as its tied to another column that will stay
                        String oldName = c.getName();
                        if(occurancesOfColumnName.containsKey(c.getName())){
                            int occurrenceNumber = occurancesOfColumnName.get(c.getName());
                            String newName = c.getName() + "_" + occurrenceNumber;
                            c.setName(newName);
                            occurancesOfColumnName.put(c.getName(), occurrenceNumber+1);
                            newNamesMappingToFilesAndNames.put(newName,new Tuple<>(t.getUrl(),oldName));
                        } else {
                            occurancesOfColumnName.put(c.getName(), 1);
                            newNamesMappingToFilesAndNames.put(c.getName(),new Tuple<>(t.getUrl(),oldName));
                        }

                        tableSchema.getColumns().add(c);
                        System.out.println("Column found somewhere else added to new columns: " + c.getName() + " from file " + fileAndNameOfTheNewAboutUrlColumn.a + " new  aboutUrl=" + fileAndNameOfTheNewAboutUrlColumn.b);
                    if(!c.getTitles().equalsIgnoreCase("Subject") && (c.getSuppressOutput() == null)){
                        c.setAboutUrl("{+" + fileAndNameOfTheNewAboutUrlColumn.b + "}");
                    }
                }
            }
        }
        System.out.println(newMetadata.jsonldMetadata());
        // Save the new merged metadata
        newMetadata.jsonldMetadata();
        System.out.println("newNamesMappingToFilesAndNames.size = " + newNamesMappingToFilesAndNames.size());
        String filePath = "newNamesMappingToFilesAndNames.txt";
        writeMapToFile(newNamesMappingToFilesAndNames, filePath);
        */
        newMetadata.jsonldMetadata();
        return newMetadata;
    }

    public Tuple<String,String> firstColumnHasLinksToAnotherColumn(Metadata oldMetadata, Column subject, Table table) {
        for(Table t : oldMetadata.getTables()){
            if(t != table){
                try {

                    // Create an object of filereader
                    // class with CSV file as a parameter.
                    String fullFilePath = getFilePathForFileName(t.getUrl());
                    assert fullFilePath != null;
                    FileReader filereader = new FileReader(fullFilePath);
                    String fullFilePathOfGivenColumn = getFilePathForFileName(table.getUrl());
                    assert fullFilePathOfGivenColumn != null;
                    FileReader filereaderOfGivenColumn = new FileReader(fullFilePathOfGivenColumn);
                    //System.out.println("fullFilePath " + fullFilePath);
                    //System.out.println("fullFilePathOfGivenColumn " + fullFilePathOfGivenColumn);
                    // create csvReader object passing
                    // file reader as a parameter
                    CSVReader csvReader = new CSVReader(filereader);

                    String[] nextRecord, nextRecordOfGivenColumn;

                    // we are going to read data line by line
                    while ((nextRecord = csvReader.readNext()) != null) {
                        for (int i = 1; i < nextRecord.length; i++) {
                            //System.out.println("nextRecord.length " + nextRecord.length);
                            // Read File 2 line by line
                            try (CSVReader csvReaderFile2 = new CSVReader(new FileReader(fullFilePathOfGivenColumn))) {
                                String[] nextRecordFile2;
                                String cellValue = nextRecord[i];
                                while ((nextRecordFile2 = csvReaderFile2.readNext()) != null) {
                                    if (nextRecordFile2.length == 0) continue; // Skip empty rows
                                    String subjectOfGivenTable = nextRecordFile2[0]; // Always the first column

                                    //System.out.println("Comparing cell: " + cellValue + " with subject: " + subjectOfGivenTable);

                                    if (!cellValue.isEmpty() && !subjectOfGivenTable.isEmpty() && cellValue.equalsIgnoreCase(subjectOfGivenTable)) {
                                        System.out.println("Match found! Cell: " + cellValue + " == " + subjectOfGivenTable +" found in column "+ t.getTableSchema().getColumns().get(i).getName());
                                        return new Tuple<>(t.getUrl(), t.getTableSchema().getColumns().get(i).getName());
                                    }
                                }
                            }
                        }
                        //System.out.println(Arrays.toString(nextRecord));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static String getFilePathForFileName(String url) {
        String intermediateFiles = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES);
        System.out.println("getFilePathForFileName =  " + intermediateFiles);
        String[] files = intermediateFiles.split(",");
        for(String file : files){
            if(file.endsWith(url)){
                return file;
            }
        }
        return null;
    }
}
