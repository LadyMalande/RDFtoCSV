package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.miklosova.rdftocsvw.output_processor.MetadataConsolidator.getFilePathForFileName;

public class CSVConsolidator {
    public void consolidateCSVs(Metadata oldMetadata, Metadata newMetadata) {
        String fullFilePath = getFilePathForFileName(newMetadata.getTables().get(0).getUrl());
        File fileToWrite = new File(newMetadata.getTables().get(0).getUrl());
        System.out.println("consolidateCSVs " + fileToWrite.getAbsolutePath());
        createHeadersLineForCSV(newMetadata, fileToWrite);
        writeToCSVFromOldMetadataToMerged(oldMetadata, newMetadata, fileToWrite);
    }

    public void createHeadersLineForCSV(Metadata newMetadata, File fileToWriteTo) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(fileToWriteTo, false))) {
            // Appends to the file instead of overwriting
            String[] line = new String[newMetadata.getTables().get(0).getTableSchema().getColumns().size()];
            int i = 0;
            for (Column c :newMetadata.getTables().get(0).getTableSchema().getColumns() ) {
                line[i] = c.getTitles();
                i++;
            }
            writer.writeNext(line, false);
            System.out.println("Header line: " + Arrays.toString(line));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToCSVFromOldMetadataToMerged(Metadata oldMetadata, Metadata newMetadata, File fileToWriteTo){

        try (CSVWriter writer = new CSVWriter(new FileWriter(fileToWriteTo, true))) {
            // Appends to the file instead of overwriting
            String[] lineToWrite = new String[newMetadata.getTables().get(0).getTableSchema().getColumns().size()];
            int counter = 0;
            for(Table t : oldMetadata.getTables()){
                System.out.println("table " + t.getTableSchema() + " url " + t.getUrl());
                String fullFilePath = getFilePathForFileName(t.getUrl());
                System.out.println("fullFilePath " + fullFilePath + " url " + t.getUrl());
                File fileToWrite = new File(fullFilePath);
                try (CSVReader reader = new CSVReader(new FileReader(fileToWrite))) {
                    String[] line;
                    boolean isFirstLine = true;
                    while ((line = reader.readNext()) != null) {
                        if (isFirstLine) {
                            // Do not write out header line as merged header has already been written to the file
                            System.out.println("Is first line, do not write out to file: " + line);
                            System.out.println();
                            isFirstLine = false;
                        } else {
                            List<Column> columns = newMetadata.getTables().get(0).getTableSchema().getColumns();
                            for(int i = 0; i < columns.size();i++){
                                if(i == 0){
                                    lineToWrite[i] = line[0];
                                }
                                else {
                                    int finalI = i;
                                    if(t.getTableSchema().getColumns().stream().anyMatch(
                                            column -> isMergeable(columns.get(finalI), column))){
                                        Optional<Column> columnOptional = t.getTableSchema().getColumns().stream().filter(
                                                column -> (isMergeable(columns.get(finalI), column))).findFirst();
                                        lineToWrite[i] = (columnOptional.isPresent()) ? line[t.getTableSchema().getColumns().indexOf(columnOptional.get())] : "";
                                        if(counter % 10 == 0)System.out.println(Arrays.toString(lineToWrite));
                                    }
                                }
                            }
                            System.out.println("Writing newline of merged CSV to "+ fileToWrite.getName() +" : " + Arrays.toString(line));
                            writer.writeNext(lineToWrite, false);
                            counter++;
                        }


                    }
                } catch (CsvValidationException e) {
                    throw new RuntimeException(e);
                }
            }

            ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES, fileToWriteTo.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public boolean isMergeable(Column c1, Column c2){
        // The columns have nonnull popertyUrl, have the same propertyUrl, and if lang is not null, they have the same lang
        return c1.getPropertyUrl() != null &&  c2.getPropertyUrl() != null
                && c2.getPropertyUrl().equalsIgnoreCase(c1.getPropertyUrl()) &&
                ((c1.getLang() == null && c2.getLang() == null) ||
                        (c1.getLang().equalsIgnoreCase(c2.getLang())));
    }
}
