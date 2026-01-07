package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.Row;
import com.miklosova.rdftocsvw.converter.data_structure.RowAndKey;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.TableSchema;
import com.miklosova.rdftocsvw.output_processor.FileWrite;
import com.miklosova.rdftocsvw.support.AppConfig;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
     * The output directory path (parent directory of the output file).
     */
    String outputDirectory;
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
     * Set to track used simple filenames (without path) to prevent collisions.
     */
    Set<String> usedSimpleFilenames;

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
        this.usedSimpleFilenames = new HashSet<>();
        this.metadata = new Metadata(config);
        this.data = data;
        this.config = config;
        this.allRows = new ArrayList<>();
        this.fileNumberX = 0;
        if (config == null) {
            throw new IllegalStateException("AppConfig is required");
        }
        // Use getOutputFilePath() which is always initialized, not getOutput() which can be null
        // This will be the base name for all CSV files (e.g., "custom_name" for custom_name0.csv, custom_name1.csv)
        String outputFilename = config.getOutputFilePath();
        File f = new File(outputFilename);
        // Store output directory for constructing full paths later
        this.outputDirectory = f.getParent();
        // Use just the filename without path, this becomes the base name for numbered CSV files
        CSVFileTOWriteTo = f.getName();
        System.out.println("outputFilename " + outputFilename);
                System.out.println("outputDirectory " + outputDirectory);

                        System.out.println("CSVFileTOWriteTo " + CSVFileTOWriteTo);


    }

    @Override
    public Metadata addMetadata(PrefinishedOutput<?> info) {
        com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(
            com.miklosova.rdftocsvw.support.ProgressLogger.Stage.METADATA, 10, "Extracting table data"
        );
        
        RowsAndKeys rnk = (RowsAndKeys) info.getPrefinishedOutput();
        
        // Consolidate duplicate RowAndKey objects with the same type
        consolidateDuplicateTypes(rnk);
        
        int totalTables = rnk.getRowsAndKeys().size();
        int currentTable = 0;
        
        for (RowAndKey rowAndKey : rnk.getRowsAndKeys()) {
            currentTable++;
            int progress = 10 + (currentTable * 50 / totalTables);
            com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(
                com.miklosova.rdftocsvw.support.ProgressLogger.Stage.METADATA, progress,
                String.format("Building metadata for table %d/%d (%d rows)", currentTable, totalTables, rowAndKey.getRows().size())
            );

            // Generate descriptive filename based on subject type
            String newFileName = generateDescriptiveFileName(rowAndKey);

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

    /**
     * Generate a descriptive filename based on the subject type.
     * Extracts the type from the first row and converts to PascalCase.
     * Handles collisions by appending numbers.
     * Falls back to numbered naming if type cannot be extracted.
     *
     * @param rowAndKey the row and key data for this table
     * @return the generated filename (e.g., "People.csv", "Point.csv", "Point1.csv", "output0.csv")
     */
    private String generateDescriptiveFileName(RowAndKey rowAndKey) {
        String simpleFileName = null;
        
        try {
            // Check if rows exist and have type information
            if (rowAndKey.getRows() != null && !rowAndKey.getRows().isEmpty()) {
                Row firstRow = rowAndKey.getRows().get(0);
                Value type = firstRow.type;
                
                // Check if type exists and all rows have the same type
                if (type != null && type.isIRI() && TableSchema.isTypeTheSameForAllPrimary(rowAndKey.getRows())) {
                    IRI typeIri = (IRI) type;
                    String localName = typeIri.getLocalName();
                    
                    // Convert to PascalCase
                    String pascalCaseName = toCamelCase(localName);
                    String baseName = pascalCaseName;
                    simpleFileName = baseName + ".csv";
                    
                    // Handle collisions by appending numbers
                    int suffix = 1;
                    while (usedSimpleFilenames.contains(simpleFileName)) {
                        simpleFileName = baseName + suffix + ".csv";
                        suffix++;
                    }
                    
                    // Mark this filename as used
                    usedSimpleFilenames.add(simpleFileName);
                    
                    // If there's an output directory, prepend it to the filename
                    if (outputDirectory != null && !outputDirectory.isEmpty()) {
                        return new File(outputDirectory, simpleFileName).getPath();
                    }
                    return simpleFileName;
                }
            }
        } catch (Exception e) {
            // Fall back to numbered naming on any error
            System.err.println("Warning: Could not extract type for table naming, using numbered fallback: " + e.getMessage());
        }
        
        // Fallback: use numbered naming for entities without types
        File baseFile = new File(CSVFileTOWriteTo);
        String baseFileName = baseFile.getName();
        
        // Remove .csv extension if present to build base name
        String baseName = baseFileName;
        if (FileWrite.hasExtension(baseName, "csv")) {
            baseName = baseName.substring(0, baseName.length() - 4);
        }
        
        // Generate numbered filename and check for collisions
        simpleFileName = baseName + fileNumberX + ".csv";
        while (usedSimpleFilenames.contains(simpleFileName)) {
            fileNumberX++;
            simpleFileName = baseName + fileNumberX + ".csv";
        }
        
        // Mark this filename as used
        usedSimpleFilenames.add(simpleFileName);
        
        // If there's an output directory, prepend it to match type-based file locations
        if (outputDirectory != null && !outputDirectory.isEmpty()) {
            return new File(outputDirectory, simpleFileName).getPath();
        }
        return simpleFileName;
    }

    /**
     * Convert a string to PascalCase format (same as CamelCase but first letter capitalized).
     * Preserves existing PascalCase format (e.g., "PostalAddress" stays "PostalAddress").
     * Handles various input formats:
     * - "Person" -> "Person"
     * - "person" -> "Person"
     * - "PERSON" -> "Person"
     * - "person_name" -> "PersonName"
     * - "Postal Address" -> "PostalAddress"
     * - "PostalAddress" -> "PostalAddress" (preserved)
     *
     * @param input the input string
     * @return the PascalCase formatted string
     */
    private String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        String trimmed = input.trim();
        
        // If already in PascalCase (starts with uppercase, contains only alphanumeric, 
        // and has at least one more uppercase letter), keep it as-is
        if (trimmed.matches("^[A-Z][a-zA-Z0-9]*$") && trimmed.matches(".*[A-Z].*[A-Z].*")) {
            return trimmed;
        }
        
        // If it's a single word with only first letter capitalized, keep as-is
        if (trimmed.matches("^[A-Z][a-z0-9]*$")) {
            return trimmed;
        }
        
        // Split by common delimiters (underscore, hyphen, space, dot)
        String[] parts = trimmed.split("[_\\-\\s.]+");
        
        // If no delimiters found and it's already capitalized, return as-is
        if (parts.length == 1 && Character.isUpperCase(trimmed.charAt(0))) {
            return trimmed;
        }
        
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part != null && !part.isEmpty()) {
                // Capitalize first letter, lowercase the rest
                String capitalizedPart = part.substring(0, 1).toUpperCase();
                if (part.length() > 1) {
                    capitalizedPart += part.substring(1).toLowerCase();
                }
                result.append(capitalizedPart);
            }
        }
        
        // If result is empty, just capitalize the first letter of original input
        if (result.length() == 0 && trimmed.length() > 0) {
            result.append(trimmed.substring(0, 1).toUpperCase());
            if (trimmed.length() > 1) {
                result.append(trimmed.substring(1).toLowerCase());
            }
        }
        
        return result.toString();
    }

    /**
     * Consolidate duplicate RowAndKey objects that have the same entity type.
     * This fixes the issue where SplitFilesQueryConverter creates multiple RowAndKey objects
     * for the same type when entities become "roots" in different iterations.
     *
     * @param rnk the RowsAndKeys object containing all RowAndKey objects to consolidate
     */
    private void consolidateDuplicateTypes(RowsAndKeys rnk) {
        ArrayList<RowAndKey> originalList = rnk.getRowsAndKeys();
        int originalSize = originalList.size();
        ArrayList<RowAndKey> consolidatedList = new ArrayList<>();
        
        // Map to track which types we've already seen: type string -> index in consolidatedList
        HashMap<String, Integer> typeMap = new HashMap<>();
        
        for (RowAndKey rowAndKey : originalList) {
            String typeKey = extractTypeKey(rowAndKey);
            
            if (typeMap.containsKey(typeKey)) {
                // We've seen this type before - merge with existing RowAndKey
                int existingIndex = typeMap.get(typeKey);
                RowAndKey existing = consolidatedList.get(existingIndex);
                
                // Merge rows
                existing.getRows().addAll(rowAndKey.getRows());
                
                // Merge keys (avoid duplicates)
                for (Value key : rowAndKey.getKeys()) {
                    if (!existing.getKeys().contains(key)) {
                        existing.getKeys().add(key);
                    }
                }
            } else {
                // First time seeing this type - add to consolidated list
                typeMap.put(typeKey, consolidatedList.size());
                consolidatedList.add(rowAndKey);
            }
        }
        
        // Replace the original list with consolidated list
        originalList.clear();
        originalList.addAll(consolidatedList);
        
        if (originalSize > consolidatedList.size()) {
            System.out.println("Consolidated duplicate types: " + originalSize + " -> " + consolidatedList.size() + " tables");
        }
    }
    
    /**
     * Extract a type key from a RowAndKey for consolidation purposes.
     * Returns the type IRI string if all rows have the same type, or a special key for untyped entities.
     *
     * @param rowAndKey the RowAndKey to extract type from
     * @return a string key representing the type
     */
    private String extractTypeKey(RowAndKey rowAndKey) {
        if (rowAndKey.getRows().isEmpty()) {
            return "EMPTY_TABLE";
        }
        
        // Check if all rows have the same type
        Value type = TableSchema.returnTypeIfAllHaveSameType(rowAndKey.getRows());
        
        if (type != null && type.isIRI()) {
            // All rows have the same type - use the full IRI as key
            return type.stringValue();
        } else {
            // Mixed types or no type - use first row's ID as key to keep them separate
            // This ensures we don't accidentally merge unrelated entities
            Value firstRowId = rowAndKey.getRows().get(0).id;
            return "UNTYPED_" + (firstRowId != null ? firstRowId.stringValue() : "NULL");
        }
    }
}