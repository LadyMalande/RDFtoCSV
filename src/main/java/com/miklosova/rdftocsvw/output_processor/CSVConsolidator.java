package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.metadata_creator.Column;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.Table;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.miklosova.rdftocsvw.output_processor.MetadataConsolidator.getFilePathForFileName;

public class CSVConsolidator {
    public void consolidateCSVs(Metadata oldMetadata, Metadata newMetadata) {
        String fullFilePath = getFilePathForFileName(newMetadata.getTables().get(0).getUrl());
        File fileToWrite = new File(newMetadata.getTables().get(0).getUrl());
        createHeadersLineForCSV(newMetadata, fileToWrite);
        writeToCSVFromOldMetadataToMerged(oldMetadata, newMetadata, fileToWrite);
    }

    private void createHeadersLineForCSV(Metadata newMetadata, File fileToWriteTo) {
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
                String fullFilePath = getFilePathForFileName(t.getUrl());
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
                                            column -> (columns.get(finalI).getPropertyUrl() != null &&  column.getPropertyUrl() != null
                                                    && column.getPropertyUrl().equalsIgnoreCase(columns.get(finalI).getPropertyUrl())))){
                                        Optional<Column> columnOptional = t.getTableSchema().getColumns().stream().filter(
                                                column -> (columns.get(finalI).getPropertyUrl() != null &&  column.getPropertyUrl() != null
                                                        && column.getPropertyUrl().equalsIgnoreCase(columns.get(finalI).getPropertyUrl()))).findFirst();
                                        lineToWrite[i] = (columnOptional.isPresent()) ? line[t.getTableSchema().getColumns().indexOf(columnOptional.get())] : "";
                                        if(counter % 10 == 0)System.out.println(Arrays.toString(lineToWrite));
                                    }
                                }
                            }
                            //System.out.println("Writing newline of merged CSV: " + Arrays.toString(line));
                            writer.writeNext(lineToWrite, false);
                            counter++;
                        }


                    }
                } catch (CsvValidationException e) {
                    throw new RuntimeException(e);
                }


            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
