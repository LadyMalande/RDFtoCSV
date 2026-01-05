package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.Row;
import com.miklosova.rdftocsvw.converter.data_structure.RowAndKey;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.output_processor.FileWrite;
import com.miklosova.rdftocsvw.support.AppConfig;


import java.io.File;
import java.util.ArrayList;

/**
 * The Split files metadata creator. It iteratively creates data for multiple tables.
 * At the end it creates foreign keys between the tables.
 */
public class SplitFilesMetadataCreator implements IMetadataCreator {

    /**
     * The Metadata.
     */
    Metadata metadata;
    /**
     * The Data.
     */
    PrefinishedOutput<RowsAndKeys> data;
    /**
     * The application configuration.
     */
    AppConfig config;
    /**
     * The CSV file to write to.
     */
    String CSVFileTOWriteTo;
    /**
     * The File number x. Is iterated to keep the file names unique.
     */
    Integer fileNumberX;
    /**
     * The All rows. (All tables with all rows)
     */
    ArrayList<ArrayList<Row>> allRows;

    /**
     * The All file names.
     */
    ArrayList<String> allFileNames;

    /**
     * Instantiates a new Split files metadata creator.
     *
     * @param data the data in inner CSV representation
     * @deprecated Use {@link #SplitFilesMetadataCreator(PrefinishedOutput, AppConfig)} instead
     */
    @Deprecated
    public SplitFilesMetadataCreator(PrefinishedOutput<RowsAndKeys> data) {
        this(data, null);
    }

    /**
     * Instantiates a new Split files metadata creator with AppConfig.
     *
     * @param data the data in inner CSV representation
     * @param config the application configuration
     */
    public SplitFilesMetadataCreator(PrefinishedOutput<RowsAndKeys> data, AppConfig config) {
        this.allFileNames = new ArrayList<>();
        this.metadata = new Metadata(config);
        this.data = data;
        this.config = config;
        this.allRows = new ArrayList<>();
        this.fileNumberX = 0;
        if (config == null) {
            throw new IllegalStateException("AppConfig is required");
        }
        // Use getOutputFilePath() which is always initialized, not getOutput() which can be null
        String outputFilename = config.getOutputFilePath();
        File f = new File(outputFilename);
        CSVFileTOWriteTo = f.getName();
    }

    @Override
    public Metadata addMetadata(PrefinishedOutput<?> info) {
        com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(
            com.miklosova.rdftocsvw.support.ProgressLogger.Stage.METADATA, 10, "Extracting table data"
        );
        
        RowsAndKeys rnk = (RowsAndKeys) info.getPrefinishedOutput();
        int totalTables = rnk.getRowsAndKeys().size();
        int currentTable = 0;
        
        for (RowAndKey rowAndKey : rnk.getRowsAndKeys()) {
            currentTable++;
            int progress = 10 + (currentTable * 50 / totalTables);
            com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(
                com.miklosova.rdftocsvw.support.ProgressLogger.Stage.METADATA, progress,
                String.format("Building metadata for table %d/%d (%d rows)", currentTable, totalTables, rowAndKey.getRows().size())
            );

            String newFileName;
            if (FileWrite.hasExtension(CSVFileTOWriteTo, "csv")) {
                newFileName = CSVFileTOWriteTo;
            } else {
                newFileName = CSVFileTOWriteTo + fileNumberX + ".csv";
            }

            // Write the rows with respective keys to the current file
            metadata.addMetadata(newFileName, rowAndKey.getKeys(), rowAndKey.getRows());
            fileNumberX = fileNumberX + 1;
            allRows.add(rowAndKey.getRows());
            allFileNames.add(newFileName);
        }
        
        com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(
            com.miklosova.rdftocsvw.support.ProgressLogger.Stage.METADATA, 70, 
            String.format("Writing %d file names to config", allFileNames.size())
        );
        
        FileWrite.writeFilesToConfigFile(allFileNames, config);

        com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(
            com.miklosova.rdftocsvw.support.ProgressLogger.Stage.METADATA, 80, "Adding foreign keys"
        );
        
        metadata.addForeignKeys(allRows);
        
        com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(
            com.miklosova.rdftocsvw.support.ProgressLogger.Stage.METADATA, 90, "Generating JSON-LD context"
        );
        
        metadata.jsonldMetadata();
        return metadata;
    }
}
