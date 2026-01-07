package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.TableSchema;
import com.miklosova.rdftocsvw.output_processor.FileWrite;
import com.miklosova.rdftocsvw.support.AppConfig;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Streaming n triples metadata creator.
 */
public class StreamingNTriplesMetadataCreator extends StreamingMetadataCreator implements IMetadataCreator {
    private static final Logger logger = Logger.getLogger(StreamingNTriplesMetadataCreator.class.getName());

    /**
     * The Map of known subjects. <CSV file url, Set of Subject IRIs>: the set of subjects that are used as IDs in a given csv file
     * Changed from List to Set for O(1) contains() performance instead of O(n)
     */

    Map<String, Set<IRI>> mapOfKnownSubjects;
    /**
     * The Map of known predicates. <CSV file url, Set of Predicate IRIs>: the set of predicates that are used as IDs in a given csv file
     * Changed from List to HashSet for O(1) contains() performance
     */

    Map<String, Set<IRI>> mapOfKnownPredicates;
    /**
     * The Table schema by files. Table Schema objects mapped to their CSV file urls
     */
    Map<String, TableSchema> tableSchemaByFiles;
    
    /**
     * Cache mapping: CSV file path -> (predicate IRI string -> column index)
     * Avoids O(n) linear search through columns for every triple
     */
    private Map<String, Map<String, Integer>> predicateToColumnIndex;
    
    /**
     * Cache mapping: CSV file path -> (subject IRI string -> row index in buffer)
     * Avoids O(n) linear search through buffered rows for every triple
     */
    private Map<String, Map<String, Integer>> subjectToRowIndex;
    
    /**
     * In-memory buffer for CSV data. Maps CSV file path to list of rows.
     * This eliminates the O(n²) file I/O bottleneck by buffering all data in memory.
     */
    private Map<String, List<String[]>> csvDataBuffer;
    
    /**
     * Map of subject to row number for subjects already flushed to disk.
     * Maps: CSV file path -> (subject IRI -> row number in file).
     * Used to handle scattered subjects by reading back specific rows when needed.
     */
    private Map<String, Map<String, Integer>> subjectRowIndex;
    
    /**
     * Rows that need to be updated in the CSV file after being read back from disk.
     * Maps: CSV file path -> Map(row number -> updated row data).
     */
    private Map<String, Map<Integer, String[]>> rowsToUpdate;
    
    /**
     * Open file writers for CSV files. Kept open during processing to avoid O(n) append overhead.
     * Maps: CSV file path -> BufferedWriter.
     */
    private Map<String, BufferedWriter> openWriters;
    
    /**
     * Batch size for periodic flushing to disk (to limit memory usage)
     */
    private static final int BATCH_FLUSH_SIZE = 5000;
    
    /**
     * Counter for last flush operation
     */
    private int lastFlushAt = 0;
    
    /**
     * Counter for processed triples (for timing output only)
     */
    private int processedTriplesCount = 0;
    
    /**
     * Timestamp of last timing measurement (in nanoseconds)
     */
    private long lastTimingMeasurement = 0;

    /**
     * The Unified by subject. Is the triple being unified by Subject or Predicate (if it's unified to previously known data)?
     */
    boolean unifiedBySubject = false;

    /**
     * The Current csv name. The url for the csv file name that the triple has been unified to
     */

    String currentCSVName = null;

    /**
     * Instantiates a new Streaming n triples metadata creator.
     * @deprecated Use {@link #StreamingNTriplesMetadataCreator(AppConfig)} instead
     */
    @Deprecated
    public StreamingNTriplesMetadataCreator() {
        this(null);
    }

    /**
     * Instantiates a new Streaming n triples metadata creator with AppConfig.
     * @param config the application configuration
     */
    public StreamingNTriplesMetadataCreator(AppConfig config) {
        super(config);
        
        tableSchemaByFiles = new HashMap<>();
        mapOfKnownPredicates = new HashMap<>();
        mapOfKnownSubjects = new HashMap<>();
        csvDataBuffer = new HashMap<>();
        subjectRowIndex = new HashMap<>();
        rowsToUpdate = new HashMap<>();
        openWriters = new HashMap<>();
        predicateToColumnIndex = new HashMap<>();
        subjectToRowIndex = new HashMap<>();
        this.metadata = new Metadata(config);
    }

    @Override
    public Metadata addMetadata(PrefinishedOutput<?> info) {
        // Clear failed hosts cache at the start of metadata creation
        // This prevents stale failures from persisting across multiple runs in the same JVM
        Dereferencer.clearFailedHostsCache();

        if (config.getStreamingContinuous()) {
            readInputStream();
        } else {
            readFileWithStreaming();
        }

        // Flush all buffered CSV data to disk
        flushAllBuffers();

        repairMetadataAndMakeItJsonld(metadata);

        return metadata;
    }

    private void readFileWithStreaming() {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileNameToRead))) {
            String line;
            // Read file line by line
            while ((line = reader.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "There was an exception while trying to read file with method Streaming. File: " + fileNameToRead, e);
        }
    }

    private void readInputStream() {
        Scanner scanner = new Scanner(System.in);
        String inputLine;
        String endingString = "END";

        // Loop until the termination line is encountered
        while (scanner.hasNextLine()) {
            inputLine = scanner.nextLine();
            if (endingString.equals(inputLine)) {
                break;  // Exit the loop when the termination line is entered
            } else {
                processLine(inputLine);
            }
        }
        scanner.close();
    }

    @Override
    public void processLine(String line) {
        currentCSVName = null;
        unifiedBySubject = false;
        tableSchema = null;

        // Parse the single line
        Statement statement = processNTripleLine(line);
        
        // Skip if parsing returned null (comment lines, blank lines, or parse errors)
        if (statement == null) {
            return;
        }
        
        Statement statementWithIRIs = replaceBlankNodesWithIRI(statement, line);
        Triple triple = new Triple((IRI) statementWithIRIs.getSubject(), statementWithIRIs.getPredicate(), statementWithIRIs.getObject());
        addMetadataToTableSchema(triple);
        try {
            writeTripleToCSV(currentCSVName, triple);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lineCounter++;
    }

    @Override
    void addMetadataToTableSchema(Triple triple) {
        processedTriplesCount++;
        /* 
        // Debug output every 500 triples
        if (processedTriplesCount % 500 == 0) {
            System.err.println("[DEBUG-NT] Processing triple #" + processedTriplesCount);
        }
        */
        long startTotal = System.nanoTime();
        
        if (config == null) {
            throw new IllegalStateException("Config is null in StreamingNTriplesMetadataCreator.addMetadataToTableSchema! This should never happen.");
        }
        
        long afterConfigCheck = System.nanoTime();
        
        Column newColumn = new Column(config);
        long afterColumnCreation = System.nanoTime();
        
        newColumn.createLangFromLiteral(triple.object);
        long afterLang = System.nanoTime();
        
        newColumn.createNameFromIRI(triple.predicate);
        long afterName = System.nanoTime();
        
        newColumn.setPropertyUrl(triple.predicate.stringValue());
        // Only set valueUrl for IRI or blank node objects (not literals)
        if (triple.object.isIRI()) {
            newColumn.setValueUrl(((IRI) triple.object).getNamespace() + "{+" + newColumn.getName() + "}");
        } else if (triple.object.isBNode()) {
            newColumn.setValueUrl("{+" + newColumn.getName() + "}");
        }
        long afterUrls = System.nanoTime();
        
        newColumn.createDatatypeFromValue(triple.object);
        long afterDatatype = System.nanoTime();
        
        newColumn.setAboutUrl("{+Subject}");
        
        newColumn.setTitles(newColumn.createTitles(triple.predicate, triple.object));
        long afterTitles = System.nanoTime();
        
        currentCSVName = getCSVNameIfSubjectOrPredicateKnown(triple.getSubject(), triple.getPredicate());
        long afterCSVName = System.nanoTime();
        
        if (blankNodeRegisteredToConfig) {
            if (metadata.getTables().stream().anyMatch(table -> table.getUrl().equalsIgnoreCase(currentCSVName))) {
                metadata.getTables().stream().filter(table -> table.getUrl().equalsIgnoreCase(currentCSVName)).findAny().get().addTransformations(config);
            }
        }
        long afterBlankNode = System.nanoTime();
        
        // Find the tableSchema that describes either Subject or Predicate in the triple
        tableSchema = getTableSchemaOfMatchingMetadata(triple);
        long afterTableSchema = System.nanoTime();
        
        // There is no matching column found in any existing metadata -> Add the column
        if (!thereIsMatchingColumnAlready(newColumn, triple, tableSchema)) {
            long afterMatching = System.nanoTime();

            tableSchema.getColumns().add(newColumn);
            
            // Update the predicate-to-column-index cache
            Map<String, Integer> columnCache = predicateToColumnIndex.get(currentCSVName);
            if (columnCache != null) {
                String cacheKey = triple.getPredicate().stringValue();
                if (triple.getObject().isLiteral() && ((Literal) triple.getObject()).getLanguage().isPresent()) {
                    cacheKey += "@" + ((Literal) triple.getObject()).getLanguage().get();
                }
                columnCache.put(cacheKey, tableSchema.getColumns().size() - 1);
            }

            try {
                rewriteTheHeadersInCSV(currentCSVName, newColumn.getTitles());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            long afterRewrite = System.nanoTime();
            
            // Detailed timing output every 500 triples
            if (processedTriplesCount % 500 == 0) {
                long configMicros = (afterConfigCheck - startTotal) / 1000;
                long columnMicros = (afterColumnCreation - afterConfigCheck) / 1000;
                long langMicros = (afterLang - afterColumnCreation) / 1000;
                long nameMicros = (afterName - afterLang) / 1000;
                long urlsMicros = (afterUrls - afterName) / 1000;
                long datatypeMicros = (afterDatatype - afterUrls) / 1000;
                long titlesMicros = (afterTitles - afterDatatype) / 1000;
                long csvNameMicros = (afterCSVName - afterTitles) / 1000;
                long blankNodeMicros = (afterBlankNode - afterCSVName) / 1000;
                long tableSchemaMicros = (afterTableSchema - afterBlankNode) / 1000;
                long matchingMicros = (afterMatching - afterTableSchema) / 1000;
                long rewriteMicros = (afterRewrite - afterMatching) / 1000;
                long totalMicros = (afterRewrite - startTotal) / 1000;
                
                // Calculate elapsed time since last measurement
                long elapsedSinceLastMs = (lastTimingMeasurement == 0) ? 0 : (afterRewrite - lastTimingMeasurement) / 1_000_000;
                lastTimingMeasurement = afterRewrite;
                
                if (elapsedSinceLastMs > 0) {
                    //System.err.println(String.format("[TIMING-NT] Triple %d: TOTAL=%dμs | 500 triples in %dms", processedTriplesCount, totalMicros, elapsedSinceLastMs));
                } else {
                    //System.err.println(String.format("[TIMING-NT] Triple %d: TOTAL=%dμs", processedTriplesCount, totalMicros));
                }
                System.err.println(String.format("  Config=%dμs, Column=%dμs, Lang=%dμs, Name=%dμs", 
                    configMicros, columnMicros, langMicros, nameMicros));
                System.err.println(String.format("  URLs=%dμs, Datatype=%dμs, Titles=%dμs, CSVName=%dμs", 
                    urlsMicros, datatypeMicros, titlesMicros, csvNameMicros));
                System.err.println(String.format("  BlankNode=%dμs, TableSchema=%dμs, Matching=%dμs, Rewrite=%dμs", 
                    blankNodeMicros, tableSchemaMicros, matchingMicros, rewriteMicros));
                System.err.flush();
            }
        } else {
            // Column already exists - just timing
            if (processedTriplesCount % 500 == 0) {
                long currentTime = System.nanoTime();
                long totalMicros = (currentTime - startTotal) / 1000;
                
                // Calculate elapsed time since last measurement
                long elapsedSinceLastMs = (lastTimingMeasurement == 0) ? 0 : (currentTime - lastTimingMeasurement) / 1_000_000;
                lastTimingMeasurement = currentTime;
                
                if (elapsedSinceLastMs > 0) {
                    System.err.println(String.format("[TIMING-NT] Triple %d: TOTAL=%dμs (column already exists) | 500 triples in %dms", processedTriplesCount, totalMicros, elapsedSinceLastMs));
                } else {
                    System.err.println(String.format("[TIMING-NT] Triple %d: TOTAL=%dμs (column already exists)", processedTriplesCount, totalMicros));
                }
                System.err.flush();
            }
        }
    }

    private void rewriteTheHeadersInCSV(String filePath, String titles) throws FileNotFoundException {
        // PERFORMANCE FIX: Instead of reading and rewriting the entire CSV file,
        // just add an empty column to all buffered rows in memory.
        // The header will be built from metadata when flushing to disk.
        
        List<String[]> lines = csvDataBuffer.get(filePath);
        if (lines != null && !lines.isEmpty()) {
            // Extend all buffered rows with an empty column
            for (int i = 0; i < lines.size(); i++) {
                String[] row = lines.get(i);
                String[] extendedRow = Arrays.copyOf(row, row.length + 1);
                extendedRow[row.length] = "";
                lines.set(i, extendedRow);
            }
            // Note: subjectToRowIndex remains valid since row indices don't change
        }
        
        // If file was already flushed to disk, update the pending row updates
        Map<Integer, String[]> updates = rowsToUpdate.get(filePath);
        if (updates != null && !updates.isEmpty()) {
            for (Map.Entry<Integer, String[]> entry : updates.entrySet()) {
                String[] row = entry.getValue();
                String[] extendedRow = Arrays.copyOf(row, row.length + 1);
                extendedRow[row.length] = "";
                entry.setValue(extendedRow);
            }
        }
        
        // Note: The actual header with column titles is now written in flushBufferToDisk()
        // by reading from the TableSchema metadata, so we don't need to maintain it separately
    }

    private String getCSVNameIfSubjectOrPredicateKnown(IRI subject, IRI predicate) {

        for (Map.Entry<String, Set<IRI>> entry : mapOfKnownSubjects.entrySet()) {
            if (entry.getValue().contains(subject)) {

                Set<IRI> knownPredicates = mapOfKnownPredicates.get(entry.getKey());
                knownPredicates.add(predicate);
                return entry.getKey();
            }
        }

        for (Map.Entry<String, Set<IRI>> entry : mapOfKnownPredicates.entrySet()) {
            if (entry.getValue().contains(predicate)) {
                Set<IRI> knownSubjects = mapOfKnownSubjects.get(entry.getKey());
                knownSubjects.add(subject);
                return entry.getKey();
            }
        }
        // Neither subject nor predicate are known, create new CSVName and add new table to metadata;
        // Use the output filepath base name from config instead of input filename
        File outputFile = new File(config.getOutputFilePath());
        String newCSVname = outputFile.getName() + fileNumber + ".csv";
        String previousFiles = config.getIntermediateFileNames();
        String allFilesUpToNow = (previousFiles != null && !previousFiles.isEmpty()) ? previousFiles + "," + newCSVname : newCSVname;
        config.setIntermediateFileNames(allFilesUpToNow);
        fileNumber++;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(newCSVname))) {
            writer.write("Subject");
            writer.newLine();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return newCSVname;
    }

    private TableSchema getTableSchemaOfMatchingMetadata(Triple triple) {
        for (Map.Entry<String, Set<IRI>> entry : mapOfKnownSubjects.entrySet()) {
            if (entry.getValue().contains(triple.getSubject())) {
                return tableSchemaByFiles.get(entry.getKey());
            }
        }
        for (Map.Entry<String, Set<IRI>> entry : mapOfKnownPredicates.entrySet()) {
            if (entry.getValue().contains(triple.getPredicate())) {
                return tableSchemaByFiles.get(entry.getKey());
            }
        }
        // Neither subject nor predicate were found -> create new table and new tableSchema
        return createNewTableSchema(triple);
    }

    private TableSchema createNewTableSchema(Triple triple) {
        HashSet<IRI> knownPredicates = new HashSet<>();
        knownPredicates.add(triple.getPredicate());
        HashSet<IRI> knownSubjects = new HashSet<>();
        knownSubjects.add(triple.getSubject());
        mapOfKnownPredicates.put(currentCSVName, knownPredicates);
        mapOfKnownSubjects.put(currentCSVName, knownSubjects);
        
        // Initialize caches for this new CSV file
        predicateToColumnIndex.put(currentCSVName, new HashMap<>());
        subjectToRowIndex.put(currentCSVName, new HashMap<>());

        // Create new Table and tableSchema inside it
        Table newTable = new Table(currentCSVName);
        metadata.getTables().add(newTable);
        tableSchema = new TableSchema();
        tableSchema.setPrimaryKey("Subject");
        tableSchemaByFiles.put(currentCSVName, tableSchema);
        createFirstColumn();
        newTable.setTableSchema(tableSchema);
        return tableSchema;
    }

    /**
     * There is matching column already boolean.
     *
     * @param newColumn   the new column
     * @param triple      the triple
     * @param tableSchema the table schema that contains the new column
     * @return the boolean
     */
    boolean thereIsMatchingColumnAlready(Column newColumn, Triple triple, TableSchema tableSchema) {

        if (tableSchema.getColumns().isEmpty()) {
            return false;
        }
        for (Column col : tableSchema.getColumns()) {
            if (!col.getName().equalsIgnoreCase("subject")) {
                if (!col.getName().equalsIgnoreCase(newColumn.getName())) {
                    continue;
                }

                if (!col.getTitles().equalsIgnoreCase(newColumn.getTitles())) {

                    continue;
                }
                if (!col.getPropertyUrl().equalsIgnoreCase(newColumn.getPropertyUrl())) {
                    continue;
                }
                if (col.getLang() != null && newColumn.getLang() != null && !col.getLang().equalsIgnoreCase(newColumn.getLang())) {
                    continue;
                }
                if (col.getDatatype() != null && newColumn.getDatatype() != null && !col.getDatatype().equalsIgnoreCase(newColumn.getDatatype())) {
                    continue;
                }
                if (!col.getAboutUrl().equalsIgnoreCase(newColumn.getAboutUrl())
                        && (col.getAboutUrl().indexOf(triple.getSubject().getNamespace()) != 0 || col.getAboutUrl().length() != newColumn.getAboutUrl().length())) {
                    // Adjust the metadata so that they are general as the namespaces are not matching

                    col.setAboutUrl("{+Subject}");

                }
                if (col.getValueUrl() != null && newColumn.getValueUrl() != null && !col.getValueUrl().equalsIgnoreCase(newColumn.getValueUrl())) {
                    // Adjust the metadata so that they are general as the namespaces are not matching
                    String oldValueUrl = col.getValueUrl();
                    String newValueUrl = "{+" + col.getName() + "}";
                    col.setValueUrl(newValueUrl);
                    
                    // Update previously buffered rows to use full IRIs instead of local names
                    int columnIndex = tableSchema.getColumns().indexOf(col);
                    updateBufferedRowsForValueUrlChange(currentCSVName, columnIndex, oldValueUrl, newValueUrl);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Write triple to CSV.
     *
     * @param filePath the file path
     * @param triple   the triple
     * @throws IOException the io exception
     */
    public void writeTripleToCSV(String filePath, Triple triple) throws IOException {
        // Debug: Log every triple being written
        /* 
        logger.info("writeTripleToCSV: predicate=" + triple.getPredicate().stringValue() + 
                   ", object=" + (triple.getObject().isIRI() ? "IRI:" + triple.getObject().stringValue() : 
                                 triple.getObject().isLiteral() ? "Literal:..." : "Other"));
        */
        long startWrite = System.nanoTime();
        
        List<String[]> rowDataVariationsForSubject = new ArrayList<>();
        boolean isNeedForAddingDataVariations = false;
        int indexOfDataVariationColumn = -1;
        boolean isModified = false;
        long afterInit = System.nanoTime();

        unifiedBySubject = isThereTheSameSubject(triple.getSubject());
        long afterUnified = System.nanoTime();

        // Get or initialize the in-memory buffer for this CSV file
        List<String[]> lines = csvDataBuffer.computeIfAbsent(filePath, k -> new ArrayList<>());
        
        // Check if this subject was already flushed to disk
        Map<String, Integer> rowIndex = subjectRowIndex.get(filePath);
        String subjectStr = triple.subject.stringValue();
        boolean subjectWasFlushed = rowIndex != null && rowIndex.containsKey(subjectStr);
        
        if (subjectWasFlushed) {
            // Subject was flushed - need to read it back and update
            int rowNumber = rowIndex.get(subjectStr);
            Map<Integer, String[]> updates = rowsToUpdate.computeIfAbsent(filePath, k -> new HashMap<>());
            
            // Get the row (either from pending updates or read from disk)
            String[] line = updates.get(rowNumber);
            if (line == null) {
                line = readRowFromCSV(filePath, rowNumber);
            }
            
            if (line != null) {
                int indexOfChangeColumn = getIndexOfCurrentPredicate(triple.getPredicate(), triple.getObject());
                
                if (indexOfChangeColumn >= line.length) {
                    Column currentColumn = tableSchema.getColumns().get(indexOfChangeColumn);
                    String formattedValue = formatValueByPattern(triple.getObject(), currentColumn);
                    String[] extendedArray = Arrays.copyOf(line, line.length + 1);
                    extendedArray[line.length] = formattedValue;
                    updates.put(rowNumber, extendedArray);
                } else if (indexOfChangeColumn != -1 && !line[indexOfChangeColumn].isEmpty()) {
                    if (!config.getFirstNormalForm()) {
                        Column currentColumn = tableSchema.getColumns().get(indexOfChangeColumn);
                        String formattedValue = formatValueByPattern(triple.getObject(), currentColumn);
                        String[] updatedLine = line.clone();
                        updatedLine[indexOfChangeColumn] = line[indexOfChangeColumn] + "," + formattedValue;
                        updates.put(rowNumber, updatedLine);
                        currentColumn.setSeparator(",");
                    }
                } else if (indexOfChangeColumn != -1) {
                    Column currentColumn = tableSchema.getColumns().get(indexOfChangeColumn);
                    String formattedValue = formatValueByPattern(triple.getObject(), currentColumn);
                    String[] updatedLine = line.clone();
                    updatedLine[indexOfChangeColumn] = formattedValue;
                    updates.put(rowNumber, updatedLine);
                }
            }
        } else {
            // Subject is in memory - process normally
            // Use index to find the row with matching subject (O(1) instead of O(n))
            Map<String, Integer> memoryRowIndex = subjectToRowIndex.get(filePath);
            Integer existingRowIndex = (memoryRowIndex != null) ? memoryRowIndex.get(subjectStr) : null;
            
            if (unifiedBySubject && existingRowIndex != null && existingRowIndex < lines.size()) {
                // Found existing row - update it directly
                int i = existingRowIndex;
                String[] line = lines.get(i);
                int indexOfChangeColumn = getIndexOfCurrentPredicate(triple.getPredicate(), triple.getObject());
                
                // Row should already be the correct size due to rewriteTheHeadersInCSV
                // but check just in case
                TableSchema currentSchema = tableSchemaByFiles.get(currentCSVName);
                int expectedColumns = currentSchema.getColumns().size();
                
                if (line.length < expectedColumns) {
                    // Shouldn't happen often, but handle it
                    String[] expandedLine = new String[expectedColumns];
                    System.arraycopy(line, 0, expandedLine, 0, line.length);
                    for (int j = line.length; j < expectedColumns; j++) {
                        expandedLine[j] = "";
                    }
                    line = expandedLine;
                    lines.set(i, line); // Update in buffer
                }

                    // Insert the new value between two commas in the middle (adjust index as needed)
                    if (indexOfChangeColumn >= line.length) {
                        // The subject has been matched but the line does not contain the predicate column yet -> add it to the end of the line
                        // Create a new array with one additional element
                        String[] extendedArray = Arrays.copyOf(line, line.length + 1);

                        // Add the new element at the last position - format based on column's valueUrl pattern
                        Column currentColumn = tableSchema.getColumns().get(indexOfChangeColumn);
                        String formattedValue = formatValueByPattern(triple.getObject(), currentColumn);
                        extendedArray[line.length] = formattedValue;
                        lines.set(i, extendedArray);
                        isModified = true;
                    } else if (indexOfChangeColumn != -1 && !line[indexOfChangeColumn].equalsIgnoreCase("")) {
                        if (config.getFirstNormalForm()) {
                            // There is already a value in the column - add the value and add the separator to metadata
                            // Create a new line with the data variation
                            if (dataLineVariationIsNotPresent(rowDataVariationsForSubject, line, indexOfChangeColumn)) {
                                rowDataVariationsForSubject.add(line);
                            }
                            isNeedForAddingDataVariations = true;
                            indexOfDataVariationColumn = indexOfChangeColumn;
                        } else {
                            // Format the new value based on the column's valueUrl pattern
                            Column currentColumn = tableSchema.getColumns().get(indexOfChangeColumn);
                            String formattedValue = formatValueByPattern(triple.getObject(), currentColumn);
                            String[] updatedLine = line.clone();
                            updatedLine[indexOfChangeColumn] = line[indexOfChangeColumn] + "," + formattedValue;
                            lines.set(i, updatedLine);
                            currentColumn.setSeparator(",");
                            isModified = true;
                        }
                    } else {
                        // Add new object at the end of the line
                        Column currentColumn = tableSchema.getColumns().get(indexOfChangeColumn);
                        String formattedValue = formatValueByPattern(triple.getObject(), currentColumn);
                        String[] updatedLine = line.clone();
                        updatedLine[indexOfChangeColumn] = formattedValue;
                        lines.set(i, updatedLine);
                        isModified = true;  // Mark that the line has been modified
                    }
            } else if (!unifiedBySubject || existingRowIndex == null) {
                // No existing row found - will add new row below
                // (existingRowIndex == null means subject not in buffer yet)
            }

            // Handle adding new rows based on the conditions
            if (isNeedForAddingDataVariations) {
                // Add data variations for all the lines that have the same subject in the list
                Column currentColumn = tableSchema.getColumns().get(indexOfDataVariationColumn);
                String formattedValue = formatValueByPattern(triple.getObject(), currentColumn);
                for (String[] lineToVary : rowDataVariationsForSubject) {
                    String[] copiedArray = Arrays.copyOf(lineToVary, lineToVary.length);
                    copiedArray[indexOfDataVariationColumn] = formattedValue;
                    lines.add(copiedArray);
                }
            } else if (!isModified && unifiedBySubject) {
                // If the file is still unmodified, we need to add a new predicate column at the end of the row
                String[] newRow = createLineStringListByMetadata(triple).toArray(new String[0]);
                lines.add(newRow);
                // Update index
                Map<String, Integer> rowIdx = subjectToRowIndex.get(filePath);
                if (rowIdx != null && newRow.length > 0) {
                    rowIdx.put(newRow[0], lines.size() - 1);
                }
                isModified = true;
            } else if (!isModified && !unifiedBySubject) {
                // The match has been made by matching Predicate = we must create a new line at the end of the file and add values accordingly
                String[] newRow = createLineStringListByMetadata(triple).toArray(new String[0]);
                lines.add(newRow);
                // Update index
                Map<String, Integer> rowIdx = subjectToRowIndex.get(filePath);
                if (rowIdx != null && newRow.length > 0) {
                    rowIdx.put(newRow[0], lines.size() - 1);
                }
            }
        }
        
        // Periodic batch flush to limit memory usage for large files
        if (processedTriplesCount - lastFlushAt >= BATCH_FLUSH_SIZE) {
            flushBufferToDisk(filePath);
            lastFlushAt = processedTriplesCount;
            System.err.println("[BUFFER-FLUSH] Flushed buffer at triple " + processedTriplesCount + " to limit memory usage");
        }
        
        long endWrite = System.nanoTime();
        
        // Detailed timing every 500 triples
        if (processedTriplesCount % 500 == 0) {
            long totalMicros = (endWrite - startWrite) / 1000;
            long initMicros = (afterInit - startWrite) / 1000;
            long unifiedMicros = (afterUnified - afterInit) / 1000;
            /* System.err.println(String.format("[TIMING-WRITE] Triple %d: TOTAL=%dμs (Init=%dμs, Unified=%dμs, Rest=%dμs)",
                processedTriplesCount, totalMicros, initMicros, unifiedMicros, 
                totalMicros - initMicros - unifiedMicros));
            System.err.flush();
            */
        }
    }

    /**
     * Flush a single CSV file buffer to disk.
     * Used for periodic batch flushing during processing.
     * Keeps file writer open for constant-time appends.
     * @param filePath the CSV file to flush
     */
    private void flushBufferToDisk(String filePath) {
        List<String[]> lines = csvDataBuffer.get(filePath);
        if (lines == null || lines.isEmpty()) {
            return;
        }
        
        long startFlush = System.nanoTime();
        
        File file = new File(filePath);
        Map<String, Integer> rowIndex = subjectRowIndex.computeIfAbsent(filePath, k -> new HashMap<>());
        
        try {
            BufferedWriter writer = openWriters.get(filePath);
            
            // First time writing this file - create writer and write header
            if (writer == null) {
                writer = new BufferedWriter(new FileWriter(file, false)); // overwrite mode
                openWriters.put(filePath, writer);
                
                // Build header from metadata columns instead of using buffered header row
                TableSchema schema = tableSchemaByFiles.get(filePath);
                if (schema != null && !schema.getColumns().isEmpty()) {
                    List<String> headerColumns = new ArrayList<>();
                    for (Column column : schema.getColumns()) {
                        headerColumns.add(column.getTitles());
                    }
                    String[] header = headerColumns.toArray(new String[0]);
                    writer.write(String.join(",", escapeCSVRow(header)));
                    writer.newLine();
                    logger.log(Level.INFO, "Initial write: created file " + filePath + " with " + header.length + " columns");
                } else {
                    logger.log(Level.WARNING, "No schema found for " + filePath + ", cannot write header!");
                }
            }
            
            // Write data rows (all rows in buffer are data rows now)
            int currentRowNumber = rowIndex.size() + 1; // Continue from last row number
            int rowsWritten = 0;
            
            for (String[] row : lines) {
                writer.write(String.join(",", escapeCSVRow(row)));
                writer.newLine();
                if (row.length > 0) {
                    rowIndex.put(row[0], currentRowNumber);
                }
                currentRowNumber++;
                rowsWritten++;
            }
            
            // Flush writer to ensure data is written
            writer.flush();
            
            lines.clear();
            
            // Clear the memory row index since these rows are now on disk
            Map<String, Integer> memoryIndex = subjectToRowIndex.get(filePath);
            if (memoryIndex != null) {
                memoryIndex.clear();
            }
            
            long endFlush = System.nanoTime();
            long flushMicros = (endFlush - startFlush) / 1000;
            
            /* 
            System.err.println(String.format("[BUFFER-FLUSH] Wrote %d rows in %dμs. Total indexed: %d", 
                rowsWritten, flushMicros, rowIndex.size()));
                */
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error flushing buffer to disk: " + filePath, e);
        }
    }
    
    /**
     * Escape CSV row values for proper CSV formatting.
     * @param row the row to escape
     * @return array of escaped values
     */
    private String[] escapeCSVRow(String[] row) {
        String[] escaped = new String[row.length];
        for (int i = 0; i < row.length; i++) {
            String value = row[i];
            // Quote if contains comma, quote, or newline
            if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                value = "\"" + value.replace("\"", "\"\"") + "\"";
            }
            escaped[i] = value;
        }
        return escaped;
    }
    
    /**
     * Flush all buffered CSV data to disk.
     * This method is called once at the end of processing to write all accumulated data.
     */
    private void flushAllBuffers() {
        // First, apply any pending row updates to files that were already flushed
        for (Map.Entry<String, Map<Integer, String[]>> entry : rowsToUpdate.entrySet()) {
            String filePath = entry.getKey();
            Map<Integer, String[]> updates = entry.getValue();
            
            if (!updates.isEmpty()) {
                applyRowUpdates(filePath, updates);
            }
        }
        rowsToUpdate.clear();
        
        // Then flush any remaining buffered data
        for (Map.Entry<String, List<String[]>> entry : csvDataBuffer.entrySet()) {
            String filePath = entry.getKey();
            List<String[]> lines = entry.getValue();
            
            if (!lines.isEmpty()) {
                flushBufferToDisk(filePath); // This will use the open writer
                //logger.log(Level.INFO, "Final flush: " + lines.size() + " rows to " + filePath);
            }
        }
        
        // Close all open file writers
        for (Map.Entry<String, BufferedWriter> entry : openWriters.entrySet()) {
            try {
                entry.getValue().close();
                logger.log(Level.INFO, "Closed writer for: " + entry.getKey());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing writer: " + entry.getKey(), e);
            }
        }
        openWriters.clear();
        
        // Clear the buffer after flushing
        csvDataBuffer.clear();
    }
    
    /**
     * Read a specific row from a CSV file.
     * @param filePath the CSV file path
     * @param rowNumber the row number to read (0-based)
     * @return the row data or null if not found
     */
    private String[] readRowFromCSV(String filePath, int rowNumber) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            int currentRow = 0;
            while ((line = reader.readNext()) != null) {
                if (currentRow == rowNumber) {
                    return line;
                }
                currentRow++;
            }
        } catch (IOException | CsvValidationException e) {
            logger.log(Level.SEVERE, "Failed to read row " + rowNumber + " from " + filePath, e);
        }
        return null;
    }
    
    /**
     * Apply pending row updates to a CSV file by rewriting it.
     * @param filePath the CSV file to update
     * @param updates map of row number to updated row data
     */
    private void applyRowUpdates(String filePath, Map<Integer, String[]> updates) {
        List<String[]> allRows = new ArrayList<>();
        
        // Read entire file
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            int rowNumber = 0;
            while ((line = reader.readNext()) != null) {
                // Use updated row if available, otherwise use original
                if (updates.containsKey(rowNumber)) {
                    allRows.add(updates.get(rowNumber));
                } else {
                    allRows.add(line);
                }
                rowNumber++;
            }
        } catch (IOException | CsvValidationException e) {
            logger.log(Level.SEVERE, "Failed to read file for updates: " + filePath, e);
            return;
        }
        
        // Rewrite entire file with updates
        File file = new File(filePath);
        FileWrite.writeLinesToCSVFile(file, allRows, false);
        logger.log(Level.INFO, "Applied " + updates.size() + " row updates to " + filePath);
    }

    private boolean dataLineVariationIsNotPresent(List<String[]> rowDataVariationsForSubject, String[] line, int indexOfChangeColumn) {

        if (rowDataVariationsForSubject.isEmpty()) {
            return true;
        }
        for (String[] value : rowDataVariationsForSubject) {
            int same = 0;
            for (int i = 0; i < line.length; i++) {
                if (i != indexOfChangeColumn && line[i].equalsIgnoreCase(value[i])) {
                    same++;
                }
            }
            if (same == line.length - 1) {
                return false;
            }
        }
        return true;
    }

    private int getIndexOfCurrentPredicate(IRI predicate, Value object) {
        // Use cache for O(1) lookup instead of O(n) linear search
        Map<String, Integer> columnCache = predicateToColumnIndex.get(currentCSVName);
        if (columnCache != null) {
            String cacheKey = predicate.stringValue();
            if (object.isLiteral() && ((Literal) object).getLanguage().isPresent()) {
                cacheKey += "@" + ((Literal) object).getLanguage().get();
            }
            Integer cachedIndex = columnCache.get(cacheKey);
            if (cachedIndex != null) {
                return cachedIndex;
            }
        }
        
        // Fallback to linear search if not in cache (shouldn't happen often)
        for (int i = 0; i < tableSchema.getColumns().size(); i++) {
            if (!tableSchema.getColumns().get(i).getTitles().equalsIgnoreCase("Subject") && isSameLanguagePredicate(tableSchema.getColumns().get(i).getPropertyUrl(), tableSchema.getColumns().get(i).getTitles(), predicate, object)) {
                // Add to cache for next time
                if (columnCache != null) {
                    String cacheKey = predicate.stringValue();
                    if (object.isLiteral() && ((Literal) object).getLanguage().isPresent()) {
                        cacheKey += "@" + ((Literal) object).getLanguage().get();
                    }
                    columnCache.put(cacheKey, i);
                }
                return i;
            }
        }
        return -1;
    }

    private boolean isSameLanguagePredicate(String predicateToCompare, String titlesToCompare, IRI predicate, Value object) {
        if (predicateToCompare.equalsIgnoreCase(predicate.stringValue())) {
            if (titlesToCompare.charAt(titlesToCompare.length() - 1) == ')' && object.isLiteral()) {
                if (((Literal) object).getLanguage().isPresent()) {
                    String languageTagToCompare = titlesToCompare.substring(titlesToCompare.length() - 3, titlesToCompare.length() - 1);
                    String languageTag = ((Literal) object).getLanguage().get();
                    return languageTagToCompare.equalsIgnoreCase(languageTag);
                } else {
                    return false;
                }
            } else return !object.isLiteral() || (object.isLiteral() && ((Literal) object).getLanguage().isEmpty());
        } else {
            return false;
        }

    }

    private boolean isThereTheSameSubject(IRI subject) {

        return mapOfKnownSubjects.get(currentCSVName).contains(subject);
    }

    private List<String> createLineStringListByMetadata(Triple triple) {
        List<String> list = new ArrayList<>();
        
        TableSchema relevantTS = tableSchemaByFiles.get(currentCSVName);
        
        // Debug: Log the predicate we're looking for
        //logger.info("createLineStringListByMetadata: Looking for predicate: " + triple.getPredicate().stringValue() + 
         //          ", Object type: " + (triple.getObject().isIRI() ? "IRI" : triple.getObject().isLiteral() ? "Literal" : "Other"));
        
        // Debug: Log all columns in the table schema
        //logger.info("TableSchema has " + relevantTS.getColumns().size() + " columns:");
        for (int j = 0; j < relevantTS.getColumns().size(); j++) {
            Column col = relevantTS.getColumns().get(j);
            //logger.info("  [" + j + "] name=" + col.getName() + ", titles=" + col.getTitles() + 
             //          ", propertyUrl=" + col.getPropertyUrl() + ", valueUrl=" + col.getValueUrl());
        }
        
        // Create a row with ALL columns, filling in values where we have them
        for (int i = 0; i < relevantTS.getColumns().size(); i++) {
            Column column = relevantTS.getColumns().get(i);
            
            if (column.getTitles().equalsIgnoreCase("Subject")) {
                // Subject column - use the triple's subject
                list.add(triple.subject.stringValue());
            } else if (column.getPropertyUrl() != null && column.getPropertyUrl().equalsIgnoreCase(triple.getPredicate().stringValue())) {
                // This column matches the current triple's predicate
                //logger.info("MATCHED Column: name=" + column.getName() + ", titles=" + column.getTitles() + 
                 //          ", propertyUrl=" + column.getPropertyUrl() + ", valueUrl=" + column.getValueUrl());
                
                // Check if column has a valueUrl pattern like {+variableName}
                String valueToWrite;
                if (column.getValueUrl() != null && column.getValueUrl().contains("{+")) {
                    // Check if it's a full pattern like {+type} or partial like https://example.com#{+type}
                    boolean isFullPattern = column.getValueUrl().trim().startsWith("{+");
                    
                    //logger.info("Processing column: " + column.getName() + ", ValueUrl: " + column.getValueUrl() + 
                       //        ", isFullPattern: " + isFullPattern + ", Object type: " + 
                         //      (triple.getObject().isIRI() ? "IRI" : triple.getObject().isLiteral() ? "Literal" : "Other"));
                    
                    if (isFullPattern) {
                        // Full pattern: use complete IRI
                        if (triple.getObject().isLiteral()) {
                            valueToWrite = ((Literal) triple.getObject()).getLabel();
                        } else {
                            valueToWrite = triple.getObject().stringValue();
                        }
                        //logger.info("  → Full pattern, writing: " + valueToWrite);
                    } else {
                        // Partial pattern: extract local name for template
                        if (triple.getObject().isIRI()) {
                            IRI objectIRI = (IRI) triple.getObject();
                            valueToWrite = objectIRI.getLocalName();
                            //logger.info("  → Partial pattern + IRI, Full: " + objectIRI.stringValue() + ", Local: " + valueToWrite);
                        } else if (triple.getObject().isLiteral()) {
                            valueToWrite = ((Literal) triple.getObject()).getLabel();
                            //logger.info("  → Partial pattern + Literal, writing: " + valueToWrite);
                        } else {
                            valueToWrite = triple.getObject().stringValue();
                            //logger.info("  → Partial pattern + Other, writing: " + valueToWrite);
                        }
                    }
                } else {
                    // No pattern - use full value
                    if (triple.getObject().isLiteral()) {
                        valueToWrite = ((Literal) triple.getObject()).getLabel();
                    } else {
                        valueToWrite = triple.getObject().stringValue();
                    }
                }
                list.add(valueToWrite);
            } else {
                // This column doesn't match - add empty value for proper CSV alignment
                // Check if this column has a partial pattern in valueUrl
                // Empty cells with partial patterns would incorrectly expand to just the prefix URL
                if (column.getValueUrl() != null && column.getValueUrl().contains("{+") && !column.getValueUrl().trim().startsWith("{+")) {
                    // Partial pattern detected - convert to simple pattern since we have empty values
                   // logger.info("Converting partial pattern to simple pattern for column '" + column.getName() + 
                     //          "' because of empty value. Old: " + column.getValueUrl());
                    String oldValueUrl = column.getValueUrl();
                    String newValueUrl = "{+" + column.getName() + "}";
                    column.setValueUrl(newValueUrl);
                    //logger.info("New valueUrl: " + newValueUrl);
                    
                    // Update all previously buffered rows to use full IRIs instead of local names
                    int columnIndex = relevantTS.getColumns().indexOf(column);
                    updateBufferedRowsForValueUrlChange(currentCSVName, columnIndex, oldValueUrl, newValueUrl);
                }
                list.add("");
            }
        }
        
        //logger.info("Created row with " + list.size() + " values for " + relevantTS.getColumns().size() + " columns");
        
        return list;
    }

    /**
     * Format a value based on the column's valueUrl pattern.
     * - If valueUrl is null or starts with "{+", use full value (IRI or literal)
     * - If valueUrl contains "{+" but doesn't start with it (partial pattern), extract local name from IRI
     * 
     * @param value the RDF value to format
     * @param column the column with valueUrl pattern
     * @return the formatted string value
     */
    private String formatValueByPattern(Value value, Column column) {
        if (column.getValueUrl() != null && column.getValueUrl().contains("{+")) {
            boolean isFullPattern = column.getValueUrl().trim().startsWith("{+");
            
            if (isFullPattern) {
                // Full pattern: use complete value
                if (value.isLiteral()) {
                    return ((Literal) value).getLabel();
                } else {
                    return value.stringValue();
                }
            } else {
                // Partial pattern: extract local name for IRI values
                if (value.isIRI()) {
                    return ((IRI) value).getLocalName();
                } else if (value.isLiteral()) {
                    return ((Literal) value).getLabel();
                } else {
                    return value.stringValue();
                }
            }
        } else {
            // No pattern - use full value
            if (value.isLiteral()) {
                return ((Literal) value).getLabel();
            } else {
                return value.stringValue();
            }
        }
    }

    /**
     * Update previously buffered CSV rows when a column's valueUrl changes from partial to full pattern.
     * This happens when the same predicate has objects with different namespaces.
     * Previously written values need to be expanded from local names to full IRIs.
     *
     * @param filePath the CSV file path
     * @param columnIndex the index of the column that changed
     * @param oldValueUrl the old partial pattern (e.g., "http://example.org/vocab/{+type}")
     * @param newValueUrl the new full pattern (e.g., "{+type}")
     */
    private void updateBufferedRowsForValueUrlChange(String filePath, int columnIndex, String oldValueUrl, String newValueUrl) {
        // Only process if changing from partial pattern to full pattern
        boolean wasPartialPattern = oldValueUrl != null && oldValueUrl.contains("{+") && !oldValueUrl.trim().startsWith("{+");
        boolean isNowFullPattern = newValueUrl != null && newValueUrl.trim().startsWith("{+");
        
        if (!wasPartialPattern || !isNowFullPattern) {
            return; // No update needed
        }
        
        // Extract the namespace prefix from the old partial pattern
        // e.g., "http://example.org/vocab/{+type}" -> "http://example.org/vocab/"
        String namespacePrefix = oldValueUrl.substring(0, oldValueUrl.indexOf("{+"));
        
        //logger.info("Updating buffered rows: column " + columnIndex + " changed from partial (" + 
        //           oldValueUrl + ") to full (" + newValueUrl + "). Prepending namespace: " + namespacePrefix);
        
        // Update in-memory buffer
        List<String[]> lines = csvDataBuffer.get(filePath);
        if (lines != null) {
            int updatedCount = 0;
            for (String[] row : lines) {
                if (columnIndex < row.length && row[columnIndex] != null && !row[columnIndex].isEmpty()) {
                    String cellValue = row[columnIndex];
                    
                    // Check if the cell contains comma-separated values (multi-valued cell)
                    if (cellValue.contains(",")) {
                        // Split by comma and expand each value individually
                        String[] values = cellValue.split(",");
                        StringBuilder expandedValue = new StringBuilder();
                        for (String value : values) {
                            value = value.trim();
                            // Only prepend namespace if not already a full IRI
                            if (!value.startsWith("http://") && !value.startsWith("https://") && !value.isEmpty()) {
                                expandedValue.append(namespacePrefix).append(value);
                            } else {
                                expandedValue.append(value);
                            }
                            expandedValue.append(",");
                        }
                        // Remove trailing comma
                        if (expandedValue.length() > 0) {
                            expandedValue.deleteCharAt(expandedValue.length() - 1);
                        }
                        String oldValue = row[columnIndex];
                        row[columnIndex] = expandedValue.toString();
                        updatedCount++;
                        //logger.fine("Updated row: column " + columnIndex + " changed '" + oldValue + "' to '" + row[columnIndex] + "'");
                    } else {
                        // Single value - check if value is just a local name (doesn't already contain the full IRI)
                        if (!cellValue.startsWith("http://") && !cellValue.startsWith("https://")) {
                            // Prepend the namespace to convert local name to full IRI
                            String oldValue = row[columnIndex];
                            row[columnIndex] = namespacePrefix + oldValue;
                            updatedCount++;
                            //logger.fine("Updated row: column " + columnIndex + " changed '" + oldValue + "' to '" + row[columnIndex] + "'");
                        }
                    }
                }
            }
            //logger.info("Updated " + updatedCount + " buffered rows in memory for column " + columnIndex);
        }
        
        // Update already-flushed rows on disk (if any exist)
        Map<Integer, String[]> updates = rowsToUpdate.get(filePath);
        if (updates != null && !updates.isEmpty()) {
            int diskUpdatedCount = 0;
            for (Map.Entry<Integer, String[]> entry : updates.entrySet()) {
                String[] row = entry.getValue();
                if (columnIndex < row.length && row[columnIndex] != null && !row[columnIndex].isEmpty()) {
                    String cellValue = row[columnIndex];
                    
                    // Check if the cell contains comma-separated values (multi-valued cell)
                    if (cellValue.contains(",")) {
                        // Split by comma and expand each value individually
                        String[] values = cellValue.split(",");
                        StringBuilder expandedValue = new StringBuilder();
                        for (String value : values) {
                            value = value.trim();
                            // Only prepend namespace if not already a full IRI
                            if (!value.startsWith("http://") && !value.startsWith("https://") && !value.isEmpty()) {
                                expandedValue.append(namespacePrefix).append(value);
                            } else {
                                expandedValue.append(value);
                            }
                            expandedValue.append(",");
                        }
                        // Remove trailing comma
                        if (expandedValue.length() > 0) {
                            expandedValue.deleteCharAt(expandedValue.length() - 1);
                        }
                        String oldValue = row[columnIndex];
                        row[columnIndex] = expandedValue.toString();
                        diskUpdatedCount++;
                        logger.fine("Updated disk row " + entry.getKey() + ": column " + columnIndex + 
                                   " changed '" + oldValue + "' to '" + row[columnIndex] + "'");
                    } else {
                        // Single value - check if not already a full IRI
                        if (!cellValue.startsWith("http://") && !cellValue.startsWith("https://")) {
                            String oldValue = row[columnIndex];
                            row[columnIndex] = namespacePrefix + oldValue;
                            diskUpdatedCount++;
                            logger.fine("Updated disk row " + entry.getKey() + ": column " + columnIndex + 
                                       " changed '" + oldValue + "' to '" + row[columnIndex] + "'");
                        }
                    }
                }
            }
            if (diskUpdatedCount > 0) {
                //logger.info("Updated " + diskUpdatedCount + " flushed rows on disk for column " + columnIndex);
            }
        }
    }
}
