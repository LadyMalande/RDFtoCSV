package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.TableSchema;
import com.miklosova.rdftocsvw.output_processor.CSVConsolidator;
import com.miklosova.rdftocsvw.output_processor.MetadataConsolidator;
import com.miklosova.rdftocsvw.support.AppConfig;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InvalidObjectException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.miklosova.rdftocsvw.support.ConnectionChecker.isUrl;
import static org.eclipse.rdf4j.model.util.Values.iri;

/**
 * The Streaming metadata creator. It reads the triple from input and immediately it updates data object and writes the
 * triples data into a CSV.
 * Because of this approach, it is very slow for any data than small.
 * This class contains shared method for BigFileStreaming and Streaming conversion methods.
 */
public class StreamingMetadataCreator extends MetadataCreator {
    private static final Logger logger = Logger.getLogger(StreamingMetadataCreator.class.getName());
    private final Map<String, Value> mapOfBlanks = new HashMap<>();
    /**
     * Cache for fast column lookup by composite key (name + titles + propertyUrl + lang + datatype).
     * Maps composite key to the matching Column, avoiding O(n²) linear search.
     */
    private final Map<String, Column> columnCache = new HashMap<>();
    
    /**
     * Cache for titles by predicate IRI to avoid repeated dereferencing.
     * Maps predicate IRI to its fetched title/label.
     */
    private final Map<String, String> predicateTitlesCache = new HashMap<>();
    
    /**
     * The Blank node registered to config.
     */
    boolean blankNodeRegisteredToConfig;
    private int blankNodeCounter = 0;
    /**
     * The application configuration.
     */
    protected AppConfig config;
    /**
     * The File name to read.
     */
    protected String fileNameToRead;
    /**
     * The Table schema.
     */
    protected TableSchema tableSchema;
    /**
     * The File number.
     */
    int fileNumber = 0;
    /**
     * The Line counter.
     */
    int lineCounter = 0;

    /**
     * Instantiates a new Streaming metadata creator.
     * @deprecated Use {@link #StreamingMetadataCreator(AppConfig)} instead
     */
    @Deprecated
    public StreamingMetadataCreator() {
        this(null);
    }

    /**
     * Instantiates a new Streaming metadata creator with AppConfig.
     * @param config the application configuration
     */
    public StreamingMetadataCreator(AppConfig config) {
        super(config);  // Pass config to parent MetadataCreator
        this.config = config;
        // Use the resolved input file name which includes path adjustments (like ../ prefix)
        String fileNameFromConfig = config != null ? config.getInputFileName() : null;
        /*
        //URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
        //File file = new File("temp.csv");
        try {
            file = new File(location.toURI().getPath());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String jarDirectory = file.getParentFile().getName();
*/
        // For streaming, we need the actual file to read, not the library-relative path
        // If it's a URL, extract local name; otherwise use the path as-is
        // For relative paths, they are relative to user's working directory
        if (isUrl(fileNameFromConfig)) {
            this.fileNameToRead = iri(fileNameFromConfig).getLocalName();
        } else {
            // Use the file path as provided - it's either absolute or relative to user's current directory
            this.fileNameToRead = fileNameFromConfig;
        }
        //"../"
    }

    /**
     * Get the AppConfig instance.
     * @return the application configuration
     */
    public AppConfig getConfig() {
        return config;
    }


    /**
     * Parse triple from line string [ ].
     *
     * @param line the line
     * @return the string [ ]
     * @throws InvalidObjectException the invalid object exception
     */
    public static String[] parseTripleFromLine(String line) throws InvalidObjectException {
        // Updated regex to handle URIs, literals, and blank nodes
        String regex = "^(<[^>]*>|_:\\w+)\\s+<([^>]*)>\\s+(\".*?\"(?:@\\w+|\\^\\^<[^>]+>)?|<[^>]*>|_:\\w+)\\s+\\.$";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(line);

        if (matcher.matches()) {
            String subject = matcher.group(1);
            String predicate = matcher.group(2);
            String object = matcher.group(3);

            // Save into String array

            return new String[]{subject, predicate, object};
        } else {
            throw new InvalidObjectException("Invalid N-Triple line: " + line);
        }

    }

    /**
     * Replace blank nodes with iri statement.
     *
     * @param st   the st
     * @param line the line
     * @return the statement
     */
    public Statement replaceBlankNodesWithIRI(Statement st, String line) {
        Resource subject;
        Value object;
        ValueFactory vf = SimpleValueFactory.getInstance();
        String[] triple = {"", "", ""};
        if (st.getSubject().isBNode() || st.getObject().isBNode()) {
            try {
                triple = parseTripleFromLine(line);
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }
        }

        if (st.getObject().isBNode()) {
            if (mapOfBlanks.get(triple[2]) != null) {
                object = mapOfBlanks.get(triple[2]);
            } else {
                if (!blankNodeRegisteredToConfig) {
                    blankNodeRegisteredToConfig = true;
                    this.config.setConversionHasBlankNodes(true);
                }
                IRI v = vf.createIRI("https://blank_Nodes_IRI.org/" + blankNodeCounter);

                mapOfBlanks.put(triple[2], v);
                object = mapOfBlanks.get(triple[2]);
                blankNodeCounter++;
            }
        } else {
            object = st.getObject();
        }
        if (st.getSubject().isBNode()) {
            if (mapOfBlanks.get(triple[0]) != null) {
                subject = (IRI) mapOfBlanks.get(triple[0]);
            } else {
                if (!blankNodeRegisteredToConfig) {
                    this.config.setConversionHasBlankNodes(true);
                    blankNodeRegisteredToConfig = true;
                }
                IRI v = vf.createIRI("https://blank_Nodes_IRI.org/" + blankNodeCounter);
                blankNodeCounter++;
                mapOfBlanks.put(triple[0], v);
                subject = (IRI) mapOfBlanks.get(triple[0]);
            }
        } else {
            subject = st.getSubject();
        }

        return vf.createStatement(subject, st.getPredicate(), object);
    }

    /**
     * Process n triple line statement.
     *
     * @param line the line
     * @return the statement
     */
    static Statement processNTripleLine(String line) {
        AtomicReference<Statement> statementRef = new AtomicReference<>();
        try {
            // Create an RDFParser instance
            RDFParser parser = Rio.createParser(RDFFormat.NTRIPLES);

            // Set a custom RDFHandler to process the parsed statements
            parser.setRDFHandler(new AbstractRDFHandler() {
                @Override
                public void handleStatement(Statement st) {
                    // Custom processing logic for each statement
                    statementRef.set(st);
                }
            });

            // Parse the single line
            parser.parse(new StringReader(line), "");
        } catch (Exception e) {
            System.err.println("Error parsing line: " + line);
            logger.log(Level.SEVERE, "There was an exception while processing a line in streaming metadata creator.");
        }
        return statementRef.get();
    }

    /**
     * Process line into triple that certainly does not contain BNodes
     *
     * @param line the line
     * @return the triple
     */
    public static Triple processLineIntoTripleIRIsOnly(String line) {
        Statement statement = processNTripleLine(line);
        return new Triple((IRI) statement.getSubject(), statement.getPredicate(), statement.getObject());
    }

    /**
     * Process line into triple triple.
     *
     * @param line the line
     * @return the triple
     */
    public Triple processLineIntoTriple(String line) {
        Statement statement = processNTripleLine(line);
        Statement statementWithIRIs = replaceBlankNodesWithIRI(statement, line);
        return new Triple((IRI) statementWithIRIs.getSubject(), statementWithIRIs.getPredicate(), statementWithIRIs.getObject());
    }

    /**
     * Repair metadata and make it jsonld.
     *
     * @param metadata the metadata
     */
    public void repairMetadataAndMakeItJsonld(Metadata metadata) {
        makeMetadataNameUnique(metadata);
        if (!config.getMultipleTables()) {
            // Consolidate first, then add properties to the consolidated metadata
            Metadata consolidatedMetadata = consolidateMetadataAndCSVs(metadata);
            // Copy consolidated tables back to original metadata
            metadata.getTables().clear();
            metadata.getTables().addAll(consolidatedMetadata.getTables());
        }
        
        // Optimize metadata and CSV files for better readability
        // This analyzes the CSV to determine optimal valueUrl patterns
        for (Table table : metadata.getTables()) {
            File csvFile = new File(table.getUrl());
            if (csvFile.exists()) {
                optimizeMetadataAndCSV(metadata, csvFile);
                break; // Only process once since metadata is shared
            }
        }
        
        // Add properties after optimization
        metadata.getTables().forEach(table -> {
            table.getTableSchema().addRowTitles();
            table.getTableSchema().setPrimaryKey("Subject");
            table.addTransformations(config);
        });
        metadata.jsonldMetadata();
    }
    
    /**
     * Data structure to hold analysis results for a column.
     */
    private static class ColumnAnalysis {
        boolean hasEmpty = false;
        Set<String> uniquePrefixes = new HashSet<>();
        String commonPrefix = null;
        
        /**
         * Determine if this column should use a prefix pattern.
         * Rules:
         * 1. If there's an empty/null value → use simple pattern {+columnName}
         * 2. If there are multiple prefixes → use simple pattern {+columnName}
         * 3. If all rows have the same prefix → use prefix pattern
         */
        boolean shouldUsePrefix() {
            return !hasEmpty && uniquePrefixes.size() == 1 && commonPrefix != null;
        }
    }
    
    /**
     * Optimizes metadata and CSV for better readability after initial conversion.
     * Analyzes the CSV to determine optimal valueUrl patterns and adjusts CSV values accordingly.
     * 
     * Rules for IRI columns (those with valueUrl):
     * 1. If a column has empty/null values → use {+columnName}, restore full URIs in CSV
     * 2. If a column has multiple IRI prefixes → use {+columnName}, restore full URIs in CSV
     * 3. If all rows share the same prefix → keep prefix pattern, keep shortened values in CSV
     * 
     * Literal columns (no valueUrl) are not affected.
     * 
     * @param metadata the metadata to optimize
     * @param csvFile the CSV file to analyze and possibly rewrite
     */
    protected void optimizeMetadataAndCSV(Metadata metadata, File csvFile) {
        try {
            // Step 1: Analyze columns in the CSV
            Map<String, ColumnAnalysis> analysis = analyzeColumnPrefixes(metadata, csvFile);
            
            // Step 2: Update metadata and restore prefixes in CSV where needed
            boolean csvNeedsRewrite = updateMetadataAndRestorePrefixes(metadata, analysis, csvFile);
            
            if (csvNeedsRewrite) {
                logger.info("Metadata and CSV optimization completed for " + csvFile.getName());
            } else {
                logger.fine("No CSV modifications needed - all columns can use prefix patterns");
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to optimize metadata and CSV: " + e.getMessage(), e);
            // Don't fail the whole conversion, just log the warning
        }
    }
    
    /**
     * Analyzes each IRI column (those with valueUrl) in the CSV to determine:
     * - Whether it has empty values
     * - What unique IRI prefixes appear
     * - What the common prefix is (if only one exists)
     * 
     * @param metadata the metadata with column definitions
     * @param csvFile the CSV file to analyze
     * @return map of column name to analysis results
     */
    private Map<String, ColumnAnalysis> analyzeColumnPrefixes(Metadata metadata, File csvFile) throws IOException {
        Map<String, ColumnAnalysis> analysis = new HashMap<>();
        
        // Get column info from metadata - only process columns with valueUrl (IRI columns)
        List<String> columnNames = new ArrayList<>();
        Map<Integer, String> columnIndexToName = new HashMap<>();
        Map<Integer, String> columnIndexToCurrentValueUrl = new HashMap<>();
        
        for (Table table : metadata.getTables()) {
            List<Column> columns = table.getTableSchema().getColumns();
            for (int i = 0; i < columns.size(); i++) {
                Column column = columns.get(i);
                String colName = column.getName();
                columnNames.add(colName);
                
                // Only analyze columns that have valueUrl (IRI columns, not literals)
                if (column.getValueUrl() != null && !column.getValueUrl().isEmpty()) {
                    columnIndexToName.put(i, colName);
                    columnIndexToCurrentValueUrl.put(i, column.getValueUrl());
                    analysis.put(colName, new ColumnAnalysis());
                }
            }
        }
        
        if (analysis.isEmpty()) {
            return analysis; // No IRI columns to analyze
        }
        
        // Read CSV file line by line
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String headerLine = reader.readLine(); // Skip header
            if (headerLine == null) {
                return analysis; // Empty file
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> values = parseCsvLineSimple(line);
                
                // Analyze each IRI column value
                for (Map.Entry<Integer, String> entry : columnIndexToName.entrySet()) {
                    int colIndex = entry.getKey();
                    String colName = entry.getValue();
                    
                    if (colIndex >= values.size()) continue;
                    
                    String value = values.get(colIndex);
                    ColumnAnalysis colAnalysis = analysis.get(colName);
                    
                    if (colAnalysis == null) continue;
                    
                    // Check for empty
                    if (value == null || value.trim().isEmpty()) {
                        colAnalysis.hasEmpty = true;
                        continue;
                    }
                    
                    // Reconstruct full URI if the current valueUrl uses a prefix pattern
                    String currentValueUrl = columnIndexToCurrentValueUrl.get(colIndex);
                    String fullUri = value;
                    
                    if (currentValueUrl != null && currentValueUrl.contains("{+") && !currentValueUrl.startsWith("{+")) {
                        // Extract prefix from pattern like "http://example.org/category/{+CategoryName}"
                        String prefix = currentValueUrl.substring(0, currentValueUrl.indexOf("{+"));
                        fullUri = prefix + value;
                    }
                    
                    // Check if it's a URI and extract prefix
                    if (isUrl(fullUri)) {
                        String prefix = extractPrefix(fullUri);
                        if (prefix != null) {
                            colAnalysis.uniquePrefixes.add(prefix);
                            // Set common prefix if this is the first or matches existing
                            if (colAnalysis.commonPrefix == null) {
                                colAnalysis.commonPrefix = prefix;
                            } else if (!colAnalysis.commonPrefix.equals(prefix)) {
                                colAnalysis.commonPrefix = null; // Multiple prefixes found
                            }
                        }
                    }
                }
            }
        }
        
        return analysis;
    }
    
    /**
     * Simple CSV line parser that handles quoted fields.
     * Returns list of field values.
     */
    private List<String> parseCsvLineSimple(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                // Check for escaped quote ""
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        
        return result;
    }
    
    /**
     * Extracts the prefix from a URI.
     * For example: "http://example.org/person/John" → "http://example.org/person/"
     */
    private String extractPrefix(String uri) {
        int lastSlash = uri.lastIndexOf('/');
        int lastHash = uri.lastIndexOf('#');
        int splitPoint = Math.max(lastSlash, lastHash);
        
        if (splitPoint > 0 && splitPoint < uri.length() - 1) {
            return uri.substring(0, splitPoint + 1);
        }
        return null;
    }
    
    /**
     * Updates metadata with optimal valueUrl patterns based on analysis.
     * CRITICAL: When changing from prefix pattern to simple pattern, must restore full URIs in CSV!
     * 
     * @param metadata the metadata to update
     * @param analysis the column analysis results
     * @param csvFile the CSV file to potentially rewrite
     * @return true if CSV was rewritten, false otherwise
     */
    private boolean updateMetadataAndRestorePrefixes(Metadata metadata, Map<String, ColumnAnalysis> analysis, File csvFile) throws IOException {
        // Track which columns need their prefixes restored in CSV
        Map<Integer, String> columnsNeedingPrefixRestoration = new HashMap<>();
        List<String> columnNames = new ArrayList<>();
        
        for (Table table : metadata.getTables()) {
            List<Column> columns = table.getTableSchema().getColumns();
            for (int i = 0; i < columns.size(); i++) {
                Column column = columns.get(i);
                columnNames.add(column.getName());
                
                // Skip columns without valueUrl (literals) and Subject column
                if (column.getValueUrl() == null || "Subject".equals(column.getName())) {
                    continue;
                }
                
                String currentValueUrl = column.getValueUrl();
                ColumnAnalysis colAnalysis = analysis.get(column.getName());
                if (colAnalysis == null) continue;
                
                // Determine if this column currently uses a prefix pattern
                boolean currentlyHasPrefix = currentValueUrl.contains("{+") && !currentValueUrl.startsWith("{+");
                
                if (colAnalysis.shouldUsePrefix()) {
                    // Rule 3: Single prefix → use or keep prefix pattern
                    String newPattern = colAnalysis.commonPrefix + "{+" + column.getName() + "}";
                    if (!newPattern.equals(currentValueUrl)) {
                        column.setValueUrl(newPattern);
                        logger.fine("Column '" + column.getName() + "' using prefix pattern: " + colAnalysis.commonPrefix);
                    }
                } else {
                    // Rules 1 & 2: Empty values or multiple prefixes → use simple pattern
                    // If currently has prefix pattern, need to restore full URIs in CSV!
                    if (currentlyHasPrefix) {
                        String prefix = currentValueUrl.substring(0, currentValueUrl.indexOf("{+"));
                        columnsNeedingPrefixRestoration.put(i, prefix);
                        logger.fine("Column '" + column.getName() + "' needs prefix restoration: " + prefix);
                    }
                    
                    column.setValueUrl("{+" + column.getName() + "}");
                    
                    if (colAnalysis.hasEmpty) {
                        logger.fine("Column '" + column.getName() + "' has empty values, using simple pattern");
                    } else if (colAnalysis.uniquePrefixes.size() > 1) {
                        logger.fine("Column '" + column.getName() + "' has " + colAnalysis.uniquePrefixes.size() + " prefixes, using simple pattern");
                    }
                }
            }
        }
        
        // Rewrite CSV if any columns need prefix restoration
        if (!columnsNeedingPrefixRestoration.isEmpty()) {
            restorePrefixesInCSV(csvFile, columnsNeedingPrefixRestoration);
            return true;
        }
        
        return false;
    }
    
    /**
     * Rewrites the CSV file, restoring full URIs for columns that were changed from prefix patterns to simple patterns.
     * This prevents csv2rdf from creating malformed file:// URIs.
     * 
     * @param csvFile the CSV file to rewrite
     * @param columnPrefixes map of column index to prefix that needs to be restored
     */
    private void restorePrefixesInCSV(File csvFile, Map<Integer, String> columnPrefixes) throws IOException {
        File tempFile = new File(csvFile.getParent(), csvFile.getName() + ".tmp");
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile));
             java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(tempFile))) {
            
            // Copy header as-is
            String headerLine = reader.readLine();
            if (headerLine != null) {
                writer.println(headerLine);
            }
            
            // Process data lines - restore prefixes to values
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> values = parseCsvLineSimple(line);
                List<String> newValues = new ArrayList<>();
                
                for (int i = 0; i < values.size(); i++) {
                    String value = values.get(i);
                    String prefix = columnPrefixes.get(i);
                    
                    // If this column needs prefix restoration and value is not empty
                    if (prefix != null && value != null && !value.trim().isEmpty() && !value.startsWith("http://") && !value.startsWith("https://")) {
                        // Restore the full URI
                        newValues.add(prefix + value);
                    } else {
                        newValues.add(value);
                    }
                }
                
                // Write CSV line with proper escaping
                writer.println(formatCsvLine(newValues));
            }
        }
        
        // Replace original with temp file
        if (!csvFile.delete()) {
            logger.warning("Could not delete original CSV file");
        }
        if (!tempFile.renameTo(csvFile)) {
            logger.warning("Could not rename temp file to original");
        }
    }
    
    /**
     * Formats a list of values as a CSV line with proper quoting and escaping.
     */
    private String formatCsvLine(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(',');
            String value = values.get(i);
            if (value == null) value = "";
            
            // Quote if contains comma, quote, or newline
            if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
                sb.append('"');
                sb.append(value.replace("\"", "\"\""));
                sb.append('"');
            } else {
                sb.append(value);
            }
        }
        return sb.toString();
    }

    private void makeMetadataNameUnique(Metadata metadata) {
        ArrayList<Column> allColumns = new ArrayList<>();
        metadata.getTables().forEach(t -> allColumns.addAll(t.getTableSchema().getColumns()));
        TableSchema.makeColumnNamesUnique(allColumns);
    }

    /**
     * Create first column.
     */
    void createFirstColumn() {
        Column firstColumn = new Column();

        firstColumn.setName("Subject");
        firstColumn.setValueUrl("{+Subject}");

        firstColumn.setSuppressOutput(true);
        firstColumn.setTitles("Subject");

        tableSchema.getColumns().add(firstColumn);
    }

    /**
     * Process line.
     *
     * @param line the line
     */
    public void processLine(String line) {
        Statement statement = processNTripleLine(line);
        Statement statementWithIRIs = replaceBlankNodesWithIRI(statement, line);
        Triple triple = new Triple((IRI) statementWithIRIs.getSubject(), statementWithIRIs.getPredicate(), statementWithIRIs.getObject());
        addMetadataToTableSchema(triple);
        lineCounter++;
    }

    /**
     * Add metadata to table schema.
     *
     * @param triple the triple
     */
    void addMetadataToTableSchema(Triple triple) {
        // Debug: Print every 500 triples to track progress
        if (lineCounter == 0 || lineCounter % 500 == 0) {
            logger.info("[DEBUG] Processing triple #" + lineCounter);
        }
        
        long startTotal = System.nanoTime();
        
        Column newColumn = new Column(config);
        long afterColumnCreate = System.nanoTime();
        
        newColumn.createLangFromLiteral(triple.object);
        long afterLang = System.nanoTime();
        
        newColumn.createNameFromIRI(triple.predicate);
        long afterName = System.nanoTime();
        
        newColumn.setPropertyUrl(triple.predicate.stringValue());
        if (triple.object.isIRI()) {
            newColumn.setValueUrl(((IRI) triple.object).getNamespace() + "{+" + newColumn.getName() + "}");
        } else if (triple.object.isBNode()) {
            newColumn.setValueUrl("{+" + newColumn.getName() + "}");
        }
        long afterValueUrl = System.nanoTime();
        
        newColumn.createDatatypeFromValue(triple.object);
        long afterDatatype = System.nanoTime();
        
        newColumn.setAboutUrl(triple.subject.getNamespace() + "{+Subject}");
        long afterAboutUrl = System.nanoTime();
        
        // Check cache first before calling expensive createTitles()
        // Cache key includes predicate IRI + language tag (since title includes language)
        String predicateIri = triple.predicate.stringValue();
        String langTag = "";
        if (triple.object.isLiteral()) {
            Literal literal = (Literal) triple.object;
            java.util.Optional<String> languageTag = literal.getLanguage();
            if (languageTag.isPresent()) {
                langTag = languageTag.get();
            }
        }
        String cacheKey = predicateIri + "|" + langTag;
        
        String titles = predicateTitlesCache.get(cacheKey);
        if (titles == null) {
            // Cache miss - fetch and store
            titles = newColumn.createTitles(triple.predicate, triple.object);
            predicateTitlesCache.put(cacheKey, titles);
        }
        newColumn.setTitles(titles);
        long afterTitles = System.nanoTime();
        
        if (!thereIsMatchingColumnAlready(newColumn, triple)) {
            tableSchema.getColumns().add(newColumn);
            // Add to cache for future fast lookups
            String columnCacheKey = getColumnCacheKey(newColumn);
            columnCache.put(columnCacheKey, newColumn);
        }
        long afterMatching = System.nanoTime();
        
        // Log detailed timing every 500 triples to track performance trends
        if (lineCounter % 500 == 0 && lineCounter > 0) {
            long totalMicros = (afterMatching - startTotal) / 1000;
            long titlesMicros = (afterTitles - afterAboutUrl) / 1000;
            long matchingMicros = (afterMatching - afterTitles) / 1000;
            logger.info(String.format("[TIMING] Triple %d: TOTAL=%dus (Titles=%dus, Matching=%dus)", 
                lineCounter, totalMicros, titlesMicros, matchingMicros));
            
        }
    }

    /**
     * Consolidate metadata and cs vs metadata.
     *
     * @param oldmeta the old metadata
     * @return the metadata
     */
    protected Metadata consolidateMetadataAndCSVs(Metadata oldmeta) {
        MetadataConsolidator mc = new MetadataConsolidator(config);
        Metadata consolidatedMetadata = mc.consolidateMetadata(oldmeta, config);
        CSVConsolidator cc = new CSVConsolidator(config);
        cc.consolidateCSVs(oldmeta, consolidatedMetadata);
        return consolidatedMetadata;
    }

    /**
     * Generate a composite cache key for fast column lookup.
     * Key format: name|titles|propertyUrl|lang|datatype
     */
    private String getColumnCacheKey(Column column) {
        return (column.getName() != null ? column.getName().toLowerCase() : "") + "|" +
               (column.getTitles() != null ? column.getTitles().toLowerCase() : "") + "|" +
               (column.getPropertyUrl() != null ? column.getPropertyUrl().toLowerCase() : "") + "|" +
               (column.getLang() != null ? column.getLang().toLowerCase() : "") + "|" +
               (column.getDatatype() != null ? column.getDatatype().toLowerCase() : "");
    }

    /**
     * There is matching column already in the metadata table.
     *
     * @param newColumn the new column that we are trying to make
     * @param triple    the triple
     * @return the boolean
     */
    boolean thereIsMatchingColumnAlready(Column newColumn, Triple triple) {
        if (tableSchema.getColumns().isEmpty()) {
            return false;
        }
        
        // Fast path: Check cache first using composite key
        String cacheKey = getColumnCacheKey(newColumn);
        Column cachedColumn = columnCache.get(cacheKey);
        
        if (cachedColumn != null) {
            // Found exact match in cache - update aboutUrl/valueUrl if needed
            if (cachedColumn.getAboutUrl() != null && newColumn.getAboutUrl() != null && 
                !cachedColumn.getAboutUrl().equalsIgnoreCase(newColumn.getAboutUrl()) &&
                (cachedColumn.getAboutUrl().indexOf(triple.getSubject().getNamespace()) != 0 || 
                 cachedColumn.getAboutUrl().length() != newColumn.getAboutUrl().length())) {
                cachedColumn.setAboutUrl("{+Subject}");
            }
            if (cachedColumn.getValueUrl() != null && newColumn.getValueUrl() != null && 
                !cachedColumn.getValueUrl().equalsIgnoreCase(newColumn.getValueUrl()) && 
                (cachedColumn.getValueUrl().indexOf(triple.getSubject().getNamespace()) != 0 || 
                 cachedColumn.getValueUrl().length() != newColumn.getValueUrl().length())) {
                cachedColumn.setValueUrl("{+" + cachedColumn.getName() + "}");
            }
            return true;
        }
        
        // Slow path: Linear search (only happens on cache miss)
        for (Column col : tableSchema.getColumns()) {
            if (!col.getName().equalsIgnoreCase(newColumn.getName())) {
                continue;
            }
            if (!col.getTitles().equalsIgnoreCase(newColumn.getTitles())) {
                continue;
            }
            if (col.getPropertyUrl() != null && newColumn.getPropertyUrl() != null && !col.getPropertyUrl().equalsIgnoreCase(newColumn.getPropertyUrl())) {
                continue;
            }
            if (col.getLang() != null && newColumn.getLang() != null && !col.getLang().equalsIgnoreCase(newColumn.getLang())) {
                continue;
            }
            if (col.getDatatype() != null && newColumn.getDatatype() != null && !col.getDatatype().equalsIgnoreCase(newColumn.getDatatype())) {
                continue;
            }
            if (col.getAboutUrl() != null && newColumn.getAboutUrl() != null && !col.getAboutUrl().equalsIgnoreCase(newColumn.getAboutUrl())
                    && (col.getAboutUrl().indexOf(triple.getSubject().getNamespace()) != 0 || col.getAboutUrl().length() != newColumn.getAboutUrl().length())) {
                // Adjust the metadata so that they are general as the namespaces are not matching

                col.setAboutUrl("{+Subject}");
            }
            if (col.getValueUrl() != null && newColumn.getValueUrl() != null && !col.getValueUrl().equalsIgnoreCase(newColumn.getValueUrl()) && (col.getValueUrl().indexOf(triple.getSubject().getNamespace()) != 0 || col.getValueUrl().length() != newColumn.getValueUrl().length())) {
                // Adjust the metadata so that they are general as the namespaces are not matching
                col.setValueUrl("{+" + col.getName() + "}");
            }
            
            // Add to cache for future lookups
            columnCache.put(cacheKey, col);
            return true;
        }
        return false;
    }

    /**
     * Create new metadata string.
     *
     * @return the string
     */
    @SuppressWarnings("unused")
    String createNewMetadata() {


        File f = new File(fileNameToRead);
        String newCSVname = f.getName() + 1 + ".csv";
        Table newTable = new Table(newCSVname);


        metadata.getTables().add(newTable);
        tableSchema = new TableSchema();
        tableSchema.setPrimaryKey("Subject");
        createFirstColumn();
        newTable.setTableSchema(tableSchema);
        return newCSVname;
    }
}
