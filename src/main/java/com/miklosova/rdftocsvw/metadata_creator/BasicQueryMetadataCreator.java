package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.Row;
import com.miklosova.rdftocsvw.converter.data_structure.RowAndKey;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import com.miklosova.rdftocsvw.output_processor.FileWrite;
import com.miklosova.rdftocsvw.support.AppConfig;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The Basic query metadata creator. For RDF4J method, for ONE table creation.
 */
public class BasicQueryMetadataCreator extends MetadataCreator implements IMetadataCreator {
    /**
     * The Metadata.
     */
    Metadata metadata;
    /**
     * The Data = inner representation of the CSV with additional information apart from the cell values.
     */
    PrefinishedOutput<RowsAndKeys> data;
    /**
     * The application configuration.
     */
    AppConfig config;
    /**
     * The Csv file to write to.
     */
    String CSVFileTOWriteTo;
    /**
     * The All rows = The whole Table representation.
     */
    ArrayList<ArrayList<Row>> allRows;

    /**
     * The All file names.
     */
    ArrayList<String> allFileNames;

    /**
     * Instantiates a new Basic query metadata creator.
     *
     * @param data the data
     * @deprecated Use {@link #BasicQueryMetadataCreator(PrefinishedOutput, AppConfig)} instead
     */
    @Deprecated
    public BasicQueryMetadataCreator(PrefinishedOutput<RowsAndKeys> data) {
        this(data, null);
    }

    /**
     * Instantiates a new Basic query metadata creator with AppConfig.
     *
     * @param data the data
     * @param config the application configuration
     */
    public BasicQueryMetadataCreator(PrefinishedOutput<RowsAndKeys> data, AppConfig config) {
        this.allFileNames = new ArrayList<>();
        this.metadata = new Metadata(config);
        this.data = data;
        this.config = config;
        this.allRows = new ArrayList<>();
        if (config == null || config.getOutputFilePath() == null) {
            throw new IllegalStateException("AppConfig with outputFilePath is required");
        }
        
        // Construct CSV filename the same way as metadata filename
        // Metadata: config.getOutputFilePath() + ".csv-metadata.json"
        // CSV:      config.getOutputFilePath() + ".csv"
        // This ensures they are placed in the same directory
        CSVFileTOWriteTo = config.getOutputFilePath() + ".csv";
    }

    @Override
    public Metadata addMetadata(PrefinishedOutput<?> info) {
        // Clear failed hosts cache at the start of metadata creation
        // This prevents stale failures from persisting across multiple runs in the same JVM
        Dereferencer.clearFailedHostsCache();
        
        com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(
            com.miklosova.rdftocsvw.support.ProgressLogger.Stage.METADATA, 10, "Extracting table data"
        );
        
        RowAndKey rnk;

        try {
            RowsAndKeys rnks = (RowsAndKeys) info.getPrefinishedOutput();
            rnk = rnks.getRowsAndKeys().get(0);
        } catch (ClassCastException ex) {
            SplitFilesMetadataCreator smc = config != null ? 
                new SplitFilesMetadataCreator(null, config) : 
                new SplitFilesMetadataCreator(null);
            return smc.addMetadata(info);
        }
        String newFileName;
        if (FileWrite.hasExtension(CSVFileTOWriteTo, "csv")) {
            newFileName = CSVFileTOWriteTo;
        } else {
            newFileName = CSVFileTOWriteTo + ".csv";
        }

        com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(
            com.miklosova.rdftocsvw.support.ProgressLogger.Stage.METADATA, 30,
            String.format("Building metadata for %d rows", rnk.getRows().size())
        );

        // Write the rows with respective keys to the current file

        //ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES, ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES) + "," + newFileName);
        // Use only the basename in metadata so CSV and metadata are in same directory
        String csvBasename = new File(newFileName).getName();
        metadata.addMetadata(csvBasename, rnk.getKeys(), rnk.getRows());
        
        com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(
            com.miklosova.rdftocsvw.support.ProgressLogger.Stage.METADATA, 60, "Creating table schema"
        );
        
        allRows.add(rnk.getRows());
        allFileNames.add(newFileName);

        FileWrite.writeFilesToConfigFile(allFileNames, config);

        com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(
            com.miklosova.rdftocsvw.support.ProgressLogger.Stage.METADATA, 80, "Adding foreign keys"
        );
        
        metadata.addForeignKeys(allRows);
        
        com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(
            com.miklosova.rdftocsvw.support.ProgressLogger.Stage.METADATA, 85, "Fixing valueUrl patterns"
        );
        
        // Fix valueUrl patterns for columns with empty values
        fixValueUrlForColumnsWithEmptyValues(metadata, rnk.getRows());
        
        com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(
            com.miklosova.rdftocsvw.support.ProgressLogger.Stage.METADATA, 90, "Generating JSON-LD context"
        );
        
        metadata.jsonldMetadata();
        return metadata;
    }
    
    /**
     * Fix valueUrl patterns for columns that have empty values in the data.
     * When a column has a prefix pattern like "http://example.org/allergen/{+var}" 
     * and some rows have empty/null values, csv2rdf will generate malformed URIs like 
     * "http://example.org/allergen/". This method detects such columns by checking 
     * the actual Row data and changes their valueUrl to the simple pattern "{+var}".
     */
    private void fixValueUrlForColumnsWithEmptyValues(Metadata metadata, ArrayList<Row> rows) {
        for (Table table : metadata.getTables()) {
            List<Column> columns = table.getTableSchema().getColumns();
            
            // For each column, check if any row has a null/empty value for it
            for (Column column : columns) {
                // Skip if no valueUrl or already using simple pattern
                if (column.getValueUrl() == null || column.getValueUrl().startsWith("{+")) {
                    continue;
                }
                
                // Check if this column has a prefix pattern (contains {+ but doesn't start with it)
                if (column.getValueUrl().contains("{+")) {
                    // Check if any row has null/empty value for this column's property
                    boolean hasEmptyValue = hasEmptyValueForColumn(rows, column);
                    
                    if (hasEmptyValue) {
                        // Change to simple pattern to avoid malformed URIs
                        column.setValueUrl("{+" + column.getName() + "}");
                    }
                }
            }
        }
    }
    
    /**
     * Check if any row has a null or empty value for the given column's property.
     */
    private boolean hasEmptyValueForColumn(ArrayList<Row> rows, Column column) {
        String propertyUrl = column.getPropertyUrl();
        if (propertyUrl == null) {
            return false; // Subject column or virtual column
        }
        
        for (Row row : rows) {
            // Check if this row has a value for this property
            boolean hasValue = row.columns.entrySet().stream()
                .anyMatch(entry -> entry.getKey().stringValue().equals(propertyUrl) && 
                                   entry.getValue() != null && 
                                   entry.getValue().values != null && 
                                   !entry.getValue().values.isEmpty());
            
            if (!hasValue) {
                // This row doesn't have a value for this column
                return true;
            }
        }
        
        return false;
    }
}
