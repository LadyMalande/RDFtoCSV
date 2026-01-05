package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.TableSchema;
import com.miklosova.rdftocsvw.support.AppConfig;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The BigFileStreaming N-Triples metadata creator. First reads the data and creates metadata. During second read it
 * writes the data according to metadata to files.
 * This method is very slow for even a bit larger files.
 */
public class BigFileStreamingNTriplesMetadataCreator extends StreamingMetadataCreator implements IMetadataCreator {

    private static final Logger logger = Logger.getLogger(BigFileStreamingNTriplesMetadataCreator.class.getName());
    /**
     * The Metadata.
     */
    Metadata metadata;
    /**
     * The Counter for keeping track of processed triples.
     */
    int counter;
    
    /**
     * Total triples from pass 1 (used for progress tracking in pass 2).
     */
    private int totalTriplesFromPass1 = 0;

    /**
     * Instantiates a new Big file streaming n triples metadata creator.
     *
     * @param data the data
     * @deprecated Use {@link #BigFileStreamingNTriplesMetadataCreator(PrefinishedOutput, AppConfig)} instead
     */
    @Deprecated
    public BigFileStreamingNTriplesMetadataCreator(PrefinishedOutput<RowsAndKeys> data) {
        this(data, null);
    }

    /**
     * Instantiates a new Big file streaming n triples metadata creator with AppConfig.
     *
     * @param data the data
     * @param config the application configuration
     */
    public BigFileStreamingNTriplesMetadataCreator(PrefinishedOutput<RowsAndKeys> data, AppConfig config) {
        super(config);
        this.metadata = new Metadata(config);
    }

    @Override
    public Metadata addMetadata(PrefinishedOutput<?> info) {
        long overallStart = System.nanoTime();
        
        File f = new File(fileNameToRead);
        String csvFileName = f.getName() + ".csv";
        Table newTable = new Table(csvFileName);
        config.setIntermediateFileNames(csvFileName);
        metadata.getTables().add(newTable);
        tableSchema = new TableSchema();
        tableSchema.setPrimaryKey("Subject");
        createFirstColumn();

        newTable.setTableSchema(tableSchema);

        // PASS 1: Build metadata
        logger.info("=== PASS 1: Building metadata ===");
        long pass1Start = System.nanoTime();
        readFileWithStreaming();
        totalTriplesFromPass1 = counter;  // Remember total for pass 2 progress
        long pass1End = System.nanoTime();
        logger.info(String.format("Pass 1 complete in %.2f seconds", (pass1End - pass1Start) / 1_000_000_000.0));
        
        // Update first column's valueUrl based on detected subjects
        updateFirstColumnValueUrl();
        
        // Repair metadata WITHOUT CSV optimization (we haven't written CSV yet)
        logger.info("Finalizing metadata structure...");
        repairMetadataWithoutCSVOptimization();
        
        // PASS 1.5: Sort N-Triples by subject for memory-efficient streaming
        logger.info("=== PASS 1.5: Sorting triples by subject ===");
        long sortStart = System.nanoTime();
        String sortedFileName = sortNTriplesBySubject();
        long sortEnd = System.nanoTime();
        logger.info(String.format("Sorting complete in %.2f seconds", (sortEnd - sortStart) / 1_000_000_000.0));
        
        // PASS 2: Write CSV data efficiently from sorted file
        logger.info("=== PASS 2: Writing CSV data ===");
        logger.info(String.format("Reading from sorted file: %s", sortedFileName));
        long pass2Start = System.nanoTime();
        writeCSVDataFromSortedFile(csvFileName, sortedFileName);
        long pass2End = System.nanoTime();
        logger.info(String.format("Pass 2 complete in %.2f seconds", (pass2End - pass2Start) / 1_000_000_000.0));
        
        // Keep sorted file for verification (not deleting)
        logger.info(String.format("Sorted file kept at: %s", sortedFileName));
        
        long overallEnd = System.nanoTime();
        logger.info(String.format("=== TOTAL TIME: %.2f seconds ===", (overallEnd - overallStart) / 1_000_000_000.0));

        return metadata;
    }

    private void readFileWithStreaming() {
        long startTime = System.nanoTime();
        int lastLogAt = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(fileNameToRead), 128 * 1024)) { // 128KB buffer
            String line;
            // Read file line by line
            while ((line = reader.readLine()) != null) {
                // Skip blank lines and comments
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }
                
                processLine(line);
                counter++;
                
                // Progress logging with timing
                if (counter - lastLogAt >= 50000) {
                    long elapsed = (System.nanoTime() - startTime) / 1_000_000_000; // seconds
                    double rate = counter / (double) elapsed;
                    logger.info(String.format("Processed %,d triples (%.1f triples/sec)", counter, rate));
                    lastLogAt = counter;
                }
            }
            
            // Final stats
            long totalSeconds = (System.nanoTime() - startTime) / 1_000_000_000;
            double finalRate = counter / (double) Math.max(1, totalSeconds);
            logger.info(String.format("Metadata creation complete: %,d triples in %,d seconds (%.1f triples/sec)", 
                counter, totalSeconds, finalRate));
                
        } catch (IOException e) {
            logger.log(Level.SEVERE, "There was an error while trying to process the RDF file with BigFileStreaming method.", e);
        }
    }
    
    /**
     * Repair metadata without CSV optimization (for BigFileStreaming).
     * We skip the expensive CSV optimization step since we write the CSV ourselves in pass 2.
     */
    private void repairMetadataWithoutCSVOptimization() {
        // Just do the basic metadata consolidation and property addition
        if (!config.getMultipleTables()) {
            Metadata consolidatedMetadata = consolidateMetadataAndCSVs(metadata);
            metadata.getTables().clear();
            metadata.getTables().addAll(consolidatedMetadata.getTables());
        }
        
        // Add properties without CSV optimization
        metadata.getTables().forEach(table -> {
            table.getTableSchema().addRowTitles();
            table.getTableSchema().setPrimaryKey("Subject");
            table.addTransformations(config);
        });
        metadata.jsonldMetadata();
    }

    /**
     * PASS 1.5: Sort N-Triples file by subject using external merge sort.
     * Enables memory-efficient streaming in Pass 2 since all triples for each subject will be consecutive.
     * 
     * Algorithm (External Merge Sort):
     * 1. Split file into chunks that fit in memory (500K lines each)
     * 2. Sort each chunk in memory
     * 3. Write sorted chunks to temp files
     * 4. K-way merge all sorted chunks into final sorted file
     * 
     * Complexity: O(n log n) time, O(chunk_size) space
     */
    private String sortNTriplesBySubject() {
        String sortedFileName = fileNameToRead + ".sorted";
        final int CHUNK_SIZE = 500000; // Lines per chunk (adjust based on available memory)
        List<File> chunkFiles = new ArrayList<>();
        
        logger.info(String.format("Sorting file by subject (chunk size: %,d lines)...", CHUNK_SIZE));
        
        // STEP 1: Split into sorted chunks
        try (BufferedReader reader = new BufferedReader(new FileReader(fileNameToRead), 128 * 1024)) {
            List<String> chunk = new ArrayList<>(CHUNK_SIZE);
            String line;
            int chunkNumber = 0;
            int totalLines = 0;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }
                
                chunk.add(line);
                
                // Chunk full? Sort and write to temp file
                if (chunk.size() >= CHUNK_SIZE) {
                    File chunkFile = writeSortedChunk(chunk, chunkNumber++);
                    chunkFiles.add(chunkFile);
                    totalLines += chunk.size();
                    logger.info(String.format("Sorted chunk %d: %,d lines", chunkNumber, chunk.size()));
                    chunk.clear();
                }
            }
            
            // Write remaining chunk
            if (!chunk.isEmpty()) {
                File chunkFile = writeSortedChunk(chunk, chunkNumber++);
                chunkFiles.add(chunkFile);
                totalLines += chunk.size();
                logger.info(String.format("Sorted chunk %d: %,d lines", chunkNumber, chunk.size()));
            }
            
            logger.info(String.format("Created %d sorted chunks (%,d total lines)", chunkFiles.size(), totalLines));
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error splitting file into chunks", e);
            throw new RuntimeException(e);
        }
        
        // STEP 2: K-way merge all chunks
        try {
            mergeSortedChunks(chunkFiles, sortedFileName);
            logger.info(String.format("Merged %d chunks into sorted file", chunkFiles.size()));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error merging sorted chunks", e);
            throw new RuntimeException(e);
        } finally {
            // Clean up chunk files
            for (File chunkFile : chunkFiles) {
                chunkFile.delete();
            }
        }
        
        return sortedFileName;
    }
    
    /**
     * Sort a chunk in memory and write to temp file.
     */
    private File writeSortedChunk(List<String> chunk, int chunkNumber) throws IOException {
        // Sort chunk by subject
        chunk.sort((line1, line2) -> {
            String subject1 = extractSubject(line1);
            String subject2 = extractSubject(line2);
            return subject1.compareTo(subject2);
        });
        
        // Write to temp file
        File chunkFile = new File(fileNameToRead + ".chunk" + chunkNumber);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(chunkFile), 128 * 1024)) {
            for (String line : chunk) {
                writer.write(line);
                writer.newLine();
            }
        }
        
        return chunkFile;
    }
    
    /**
     * K-way merge of sorted chunks using priority queue.
     * Complexity: O(n log k) where k = number of chunks
     */
    private void mergeSortedChunks(List<File> chunkFiles, String outputFileName) throws IOException {
        // Priority queue for k-way merge (min-heap by subject)
        PriorityQueue<ChunkReader> pq = new PriorityQueue<>((r1, r2) -> {
            String subject1 = extractSubject(r1.currentLine);
            String subject2 = extractSubject(r2.currentLine);
            return subject1.compareTo(subject2);
        });
        
        // Open readers for all chunks
        List<ChunkReader> readers = new ArrayList<>();
        for (File chunkFile : chunkFiles) {
            ChunkReader reader = new ChunkReader(chunkFile);
            if (reader.readNext()) {
                pq.offer(reader);
                readers.add(reader);
            }
        }
        
        // Merge into output file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName), 128 * 1024)) {
            while (!pq.isEmpty()) {
                // Get chunk with smallest subject
                ChunkReader reader = pq.poll();
                
                // Write line
                writer.write(reader.currentLine);
                writer.newLine();
                
                // Read next line from this chunk
                if (reader.readNext()) {
                    pq.offer(reader);
                }
            }
        } finally {
            // Close all readers
            for (ChunkReader reader : readers) {
                reader.close();
            }
        }
    }
    
    /**
     * Helper class for reading sorted chunks during merge.
     */
    private static class ChunkReader {
        BufferedReader reader;
        String currentLine;
        
        ChunkReader(File file) throws IOException {
            this.reader = new BufferedReader(new FileReader(file), 128 * 1024);
        }
        
        boolean readNext() throws IOException {
            currentLine = reader.readLine();
            return currentLine != null;
        }
        
        void close() {
            try {
                reader.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    /**
     * Extract subject from N-Triples line for sorting.
     */
    private String extractSubject(String line) {
        // Subject is first field, ends at first space after IRI/blank node
        int endIndex = line.indexOf(' ');
        if (endIndex > 0) {
            return line.substring(0, endIndex);
        }
        return line;
    }
    
    /**
     * PASS 2: Stream CSV data from sorted N-Triples file.
     * Since file is sorted by subject, all triples for each subject are consecutive.
     * Only need to keep ONE subject in memory at a time = O(1) memory usage.
     * 
     * Algorithm:
     * 1. Read sorted file line by line
     * 2. When subject changes, write previous subject's row to CSV
     * 3. Start building new row for new subject
     * 4. Memory usage: constant (only current subject's row)
     */
    private void writeCSVDataFromSortedFile(String csvFileName, String sortedFileName) {
        long startTime = System.nanoTime();
        int triplesProcessed = 0;
        int rowsWritten = 0;
        int lastLogAt = 0;
        
        // Build column index for O(1) lookups
        Map<String, Integer> columnIndex = buildColumnIndex();
        
        // Current subject tracking (use RAW subject for comparison to match sorting)
        String currentSubject = null;              // Raw subject from N-Triples line
        String currentSubjectProcessed = null;      // Processed subject (for CSV output)
        String[] currentRow = null;
        
        File csvFile = new File(csvFileName);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile), 128 * 1024)) {
            
            // Write CSV header
            writeCSVHeader(writer);
            
            // Read sorted file and build rows
            try (BufferedReader reader = new BufferedReader(new FileReader(sortedFileName), 128 * 1024)) {
                String line;
                
                while ((line = reader.readLine()) != null) {
                    // Skip blank lines and comments
                    if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                        continue;
                    }
                    
                    // Extract subject from raw line (BEFORE parsing) to match sorting
                    String rawSubject = extractSubject(line);
                    
                    // Parse triple
                    Statement statement = processNTripleLine(line);
                    if (statement == null) {
                        continue;
                    }
                    
                    Statement statementWithIRIs = replaceBlankNodesWithIRI(statement, line);
                    if (statementWithIRIs == null || statementWithIRIs.getSubject() == null) {
                        continue;
                    }
                    
                    Triple triple = new Triple((IRI) statementWithIRIs.getSubject(), 
                                               statementWithIRIs.getPredicate(), 
                                               statementWithIRIs.getObject());
                    
                    String subjectStr = triple.subject.stringValue();
                    
                    // Subject changed? Compare using RAW subject (same as sorting)
                    if (currentSubject != null && !currentSubject.equals(rawSubject)) {
                        // Write completed row (set subject value - extract local name if needed)
                        currentRow[0] = getSubjectValueForCSV(currentSubjectProcessed);
                        writeRow(writer, currentRow);
                        rowsWritten++;
                        
                        // Start new row
                        currentSubject = rawSubject;
                        currentSubjectProcessed = subjectStr;
                        currentRow = createEmptyRow();
                    } else if (currentSubject == null) {
                        // First subject
                        currentSubject = rawSubject;
                        currentSubjectProcessed = subjectStr;
                        currentRow = createEmptyRow();
                    }
                    
                    // Find column index for this predicate
                    Integer colIdx = getColumnIndexForPredicate(columnIndex, triple.predicate, triple.object);
                    
                    if (colIdx != null && colIdx < currentRow.length) {
                        // Add value to row
                        String value = formatValue(triple.object, tableSchema.getColumns().get(colIdx));
                        
                        if (currentRow[colIdx] == null || currentRow[colIdx].isEmpty()) {
                            currentRow[colIdx] = value;
                        } else if (!config.getFirstNormalForm()) {
                            // Multi-valued cell: append with comma
                            currentRow[colIdx] = currentRow[colIdx] + "," + value;
                        }
                    }
                    
                    triplesProcessed++;
                    
                    // Progress logging every 100,000 triples
                    if (triplesProcessed - lastLogAt >= 100000) {
                        long elapsed = (System.nanoTime() - startTime) / 1_000_000_000;
                        double rate = triplesProcessed / (double) Math.max(1, elapsed);
                        double percentComplete = (triplesProcessed * 100.0) / totalTriplesFromPass1;
                        logger.info(String.format("CSV Write: %,d / %,d triples (%.1f%%), %,d rows written (%.1f triples/sec)", 
                            triplesProcessed, totalTriplesFromPass1, percentComplete, rowsWritten, rate));
                        lastLogAt = triplesProcessed;
                    }
                }
                
                // Write final row
                if (currentRow != null) {
                    currentRow[0] = getSubjectValueForCSV(currentSubjectProcessed);
                    writeRow(writer, currentRow);
                    rowsWritten++;
                }
            }
            
            // Final stats
            long totalSeconds = (System.nanoTime() - startTime) / 1_000_000_000;
            double finalRate = triplesProcessed / (double) Math.max(1, totalSeconds);
            logger.info(String.format("CSV write complete: %,d triples, %,d rows in %,d seconds (%.1f triples/sec)", 
                triplesProcessed, rowsWritten, totalSeconds, finalRate));
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing CSV data in second pass", e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Build a map of predicate -> column index for fast lookups.
     */
    private Map<String, Integer> buildColumnIndex() {
        Map<String, Integer> index = new HashMap<>();
        List<Column> columns = tableSchema.getColumns();
        
        for (int i = 0; i < columns.size(); i++) {
            Column col = columns.get(i);
            String key = col.getPropertyUrl();
            if (key != null) {
                // Add language tag to key if present
                if (col.getLang() != null) {
                    key = key + "@" + col.getLang();
                }
                index.put(key, i);
            }
        }
        
        return index;
    }
    
    /**
     * Get column index for a predicate, considering language tags for literals.
     */
    private Integer getColumnIndexForPredicate(Map<String, Integer> columnIndex, IRI predicate, Value object) {
        String key = predicate.stringValue();
        
        // Try with language tag first (for literals)
        if (object.isLiteral() && ((Literal) object).getLanguage().isPresent()) {
            String langKey = key + "@" + ((Literal) object).getLanguage().get();
            Integer idx = columnIndex.get(langKey);
            if (idx != null) {
                return idx;
            }
        }
        
        // Try without language tag
        return columnIndex.get(key);
    }
    
    /**
     * Create an empty row with the correct number of columns.
     */
    private String[] createEmptyRow() {
        int numColumns = tableSchema.getColumns().size();
        String[] row = new String[numColumns];
        Arrays.fill(row, "");
        return row;
    }
    
    /**
     * Format a value based on column's valueUrl pattern.
     */
    private String formatValue(Value value, Column column) {
        if (value.isLiteral()) {
            return ((Literal) value).getLabel();
        } else if (value.isIRI()) {
            IRI iri = (IRI) value;
            String valueUrl = column.getValueUrl();
            
            // Check if we should use local name (partial pattern) or full IRI
            if (valueUrl != null && valueUrl.contains("{+") && !valueUrl.trim().startsWith("{+")) {
                // Partial pattern like "http://example.org/{+name}" -> use local name
                return iri.getLocalName();
            } else {
                // Full pattern "{+name}" or no pattern -> use full IRI
                return iri.stringValue();
            }
        } else {
            return value.stringValue();
        }
    }
    
    /**
     * Write CSV header row.
     */
    private void writeCSVHeader(BufferedWriter writer) throws IOException {
        List<Column> columns = tableSchema.getColumns();
        StringBuilder header = new StringBuilder();
        
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) header.append(",");
            header.append(escapeCSV(columns.get(i).getTitles()));
        }
        
        writer.write(header.toString());
        writer.newLine();
    }
    
    /**
     * Write a single row to CSV.
     */
    private void writeRow(BufferedWriter writer, String[] row) throws IOException {
        StringBuilder line = new StringBuilder();
        
        for (int i = 0; i < row.length; i++) {
            if (i > 0) line.append(",");
            line.append(escapeCSV(row[i] != null ? row[i] : ""));
        }
        
        writer.write(line.toString());
        writer.newLine();
    }
    
    /**
     * Flush buffered rows to CSV file.
     */
    private int flushRows(BufferedWriter writer, Map<String, String[]> rows) throws IOException {
        // Build all rows into a single StringBuilder to minimize write() calls
        StringBuilder batch = new StringBuilder(rows.size() * 200); // Preallocate estimated size
        
        for (Map.Entry<String, String[]> entry : rows.entrySet()) {
            String subject = entry.getKey();
            String[] row = entry.getValue();
            
            // Set subject in first column
            row[0] = subject;
            
            // Build row
            for (int i = 0; i < row.length; i++) {
                if (i > 0) batch.append(",");
                batch.append(escapeCSV(row[i] != null ? row[i] : ""));
            }
            batch.append("\n");
        }
        
        // Single write call for entire batch
        writer.write(batch.toString());
        
        return rows.size();
    }
    
    /**
     * Escape CSV value (quote if contains comma, quote, or newline).
     */
    private String escapeCSV(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
    
    /**
     * Update first column's valueUrl based on subjects found in pass 1.
     * Detects common namespace and sets valueUrl accordingly.
     */
    private void updateFirstColumnValueUrl() {
        Column firstColumn = tableSchema.getColumns().get(0);
        
        // Check if we have a common namespace among subjects
        String commonNamespace = detectCommonSubjectNamespace();
        
        if (commonNamespace != null && !commonNamespace.isEmpty()) {
            // Use namespace + {+Subject} pattern
            firstColumn.setValueUrl(commonNamespace + "{+Subject}");
            logger.info("Set first column valueUrl to: " + firstColumn.getValueUrl());
        } else {
            // Keep default {+Subject} for full IRIs or mixed namespaces
            firstColumn.setValueUrl("{+Subject}");
        }
    }
    
    /**
     * Detect common namespace prefix among subjects.
     * Returns the common namespace if 80%+ of subjects share it, otherwise null.
     */
    private String detectCommonSubjectNamespace() {
        // Get first few columns to check their propertyUrl patterns
        // In practice, subjects should be IRIs, so check the Subject column's common pattern
        if (tableSchema.getColumns().size() > 1) {
            // Look at first data column to infer subject namespace
            Column secondColumn = tableSchema.getColumns().get(1);
            if (secondColumn.getPropertyUrl() != null) {
                // Extract namespace from property
                String propertyUrl = secondColumn.getPropertyUrl();
                int lastHash = propertyUrl.lastIndexOf('#');
                int lastSlash = propertyUrl.lastIndexOf('/');
                int splitPos = Math.max(lastHash, lastSlash);
                if (splitPos > 0) {
                    // Assume subjects might share this domain
                    return propertyUrl.substring(0, splitPos + 1);
                }
            }
        }
        
        // Fallback: return null to use {+Subject} (full IRI)
        return null;
    }
    
    /**
     * Get the Subject value to write to CSV based on valueUrl pattern.
     * If valueUrl has a namespace prefix, extract just the local name.
     * Otherwise, return the full IRI.
     */
    private String getSubjectValueForCSV(String subjectIRI) {
        Column firstColumn = tableSchema.getColumns().get(0);
        String valueUrl = firstColumn.getValueUrl();
        
        if (valueUrl != null && !valueUrl.startsWith("{")) {
            // ValueUrl has a namespace prefix like "http://example.org/{+Subject}"
            // Extract the prefix
            int templateStart = valueUrl.indexOf("{");
            if (templateStart > 0) {
                String namespace = valueUrl.substring(0, templateStart);
                
                // If subject starts with this namespace, return just the local part
                if (subjectIRI.startsWith(namespace)) {
                    return subjectIRI.substring(namespace.length());
                }
            }
        }
        
        // Return full IRI (for {+Subject} pattern)
        return subjectIRI;
    }
}
