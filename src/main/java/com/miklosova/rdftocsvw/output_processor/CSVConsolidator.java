package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.miklosova.rdftocsvw.output_processor.MetadataConsolidator.getFilePathForFileName;

/**
 * The CSV consolidator to merge multiple CSVs into one CSV. Is run after consolidating metadata. Is used to create one table with the method streaming, as it produces multiple tables on its own.
 */
public class CSVConsolidator {
    private static final Logger logger = Logger.getLogger(CSVConsolidator.class.getName());
    private AppConfig config;

    /**
     * Default constructor for backward compatibility.
     */
    public CSVConsolidator() {
        this.config = null;
    }

    /**
     * Constructor with AppConfig.
     * @param config The application configuration
     */
    public CSVConsolidator(AppConfig config) {
        this.config = config;
    }

    /**
     * Consolidate CSVd.
     *
     * @param oldMetadata the old metadata of multiple CSVs
     * @param newMetadata the new metadata of one merged Table
     */
    public void consolidateCSVs(Metadata oldMetadata, Metadata newMetadata) {
        File fileToWrite = new File(newMetadata.getTables().get(0).getUrl());
        createHeadersLineForCSV(newMetadata, fileToWrite);
        writeToCSVFromOldMetadataToMerged(oldMetadata, newMetadata, fileToWrite);
    }

    /**
     * Create headers line for csv.
     *
     * @param newMetadata   the new metadata
     * @param fileToWriteTo the file to write to
     */
    public void createHeadersLineForCSV(Metadata newMetadata, File fileToWriteTo) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(fileToWriteTo, false))) {
            // Appends to the file instead of overwriting
            String[] line = new String[newMetadata.getTables().get(0).getTableSchema().getColumns().size()];
            int i = 0;
            for (Column c : newMetadata.getTables().get(0).getTableSchema().getColumns()) {
                line[i] = c.getTitles();
                i++;
            }
            writer.writeNext(line, false);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "There was an exception while trying to write headers into the Consolidated CSV.");
        }
    }

    /**
     * Write to csv from old metadata to merged CSV.
     *
     * @param oldMetadata   the old metadata
     * @param newMetadata   the new metadata
     * @param fileToWriteTo the file to write to
     * @deprecated Use {@link #writeToCSVFromOldMetadataToMerged(Metadata, Metadata, File, AppConfig)} instead
     */
    @Deprecated
    public void writeToCSVFromOldMetadataToMerged(Metadata oldMetadata, Metadata newMetadata, File fileToWriteTo) {
        writeToCSVFromOldMetadataToMerged(oldMetadata, newMetadata, fileToWriteTo, config);
    }

    /**
     * Write to csv from old metadata to merged CSV with AppConfig.
     *
     * @param oldMetadata   the old metadata
     * @param newMetadata   the new metadata
     * @param fileToWriteTo the file to write to
     * @param config the application configuration
     */
    public void writeToCSVFromOldMetadataToMerged(Metadata oldMetadata, Metadata newMetadata, File fileToWriteTo, AppConfig config) {

        try (CSVWriter writer = new CSVWriter(new FileWriter(fileToWriteTo, true))) {
            // Appends to the file instead of overwriting
            String[] lineToWrite = new String[newMetadata.getTables().get(0).getTableSchema().getColumns().size()];

            for (Table t : oldMetadata.getTables()) {
                String fullFilePath = getFilePathForFileName(t.getUrl(), config);
                assert fullFilePath != null;
                File fileToWrite = new File(fullFilePath);
                try (CSVReader reader = new CSVReader(new FileReader(fileToWrite))) {
                    String[] line;
                    boolean isFirstLine = true;
                    while ((line = reader.readNext()) != null) {
                        if (isFirstLine) {
                            // Do not write out header line as merged header has already been written to the file
                            isFirstLine = false;
                        } else {
                            List<Column> columns = newMetadata.getTables().get(0).getTableSchema().getColumns();
                            for (int i = 0; i < columns.size(); i++) {
                                if (i == 0) {
                                    lineToWrite[i] = line[0];
                                } else {
                                    int finalI = i;
                                    if (t.getTableSchema().getColumns().stream().anyMatch(
                                            column -> isMergeable(columns.get(finalI), column))) {
                                        Optional<Column> columnOptional = t.getTableSchema().getColumns().stream().filter(
                                                column -> (isMergeable(columns.get(finalI), column))).findFirst();
                                        lineToWrite[i] = (columnOptional.isPresent()) ? line[t.getTableSchema().getColumns().indexOf(columnOptional.get())] : "";
                                    }
                                }
                            }
                            writer.writeNext(lineToWrite, false);

                        }
                    }
                } catch (CsvValidationException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("newFileName writeToCSVFromOldMetadataToMerged   fileToWriteTo = " + fileToWriteTo.toString());

            // For backward compatibility, also save to ConfigurationManager
            ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES, fileToWriteTo.toString());
            config.setIntermediateFileNames(fileToWriteTo.toString());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "There was an exception while trying to write data into new merged CSV.");
        }


    }

    /**
     * Is mergeable Column 1 with Column 2?
     *
     * @param c1 the Column 1
     * @param c2 the Column 2
     * @return True if columns can be merged into one
     */
    public boolean isMergeable(Column c1, Column c2) {
        // The columns have nonnull popertyUrl, have the same propertyUrl, and if lang is not null, they have the same lang
        return c1.getPropertyUrl() != null && c2.getPropertyUrl() != null
                && c2.getPropertyUrl().equalsIgnoreCase(c1.getPropertyUrl()) &&
                ((c1.getLang() == null && c2.getLang() == null) ||
                        (c1.getLang().equalsIgnoreCase(c2.getLang())));
    }
}
