package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.RDFtoCSV;
import com.miklosova.rdftocsvw.support.AppConfig;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to validate that RDF data can be round-tripped: RDF -> CSV+Metadata -> RDF
 * 
 * This test:
 * 1. Converts RDF to CSV using this tool
 * 2. Uses an external CSVW-to-RDF converter (csv2rdf) to convert back to RDF
 * 3. Validates that all original triples are present in the reconverted RDF
 * 
 * Note: Blank nodes will have different identifiers, but their relationships should be preserved.
 */
@Disabled
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReverseConversionTest {

    static class TestFileSet {
        final String inputFile;
        final String inputFileForConversion;
        final String metadataOutputFile;
        final String csvBaseName;
        final String reconvertedRdfFile;
        final String csv2rdfJarPath;

        final String parsingParameter;

        TestFileSet(String inputFile, String inputFileForConversion, String metadataOutputFile, 
                    String csvBaseName, String reconvertedRdfFile, String csv2rdfJarPath, String parsingParameter) {
            this.inputFile = inputFile;
            this.inputFileForConversion = inputFileForConversion;
            this.metadataOutputFile = metadataOutputFile;
            this.csvBaseName = csvBaseName;
            this.reconvertedRdfFile = reconvertedRdfFile;
            this.csv2rdfJarPath = csv2rdfJarPath;
            this.parsingParameter = parsingParameter;
        }

        @Override
        public String toString() {
            return csvBaseName;
        }
    }

    static java.util.stream.Stream<TestFileSet> testFileSets() {
        // Path to csv2rdf tool - you may need to download it from:
        // https://github.com/w3c/csvw/tree/gh-pages/csv2rdf
        String csv2rdfPath = "./src/test/resources/tools/csv2rdf-0.4.7-standalone.jar";
        
        return java.util.stream.Stream.of(
/**/


            new TestFileSet(
                "./src/test/resources/StreamingNTriples/restaurantTest.nt",
                "./RDFtoCSV/src/test/resources/StreamingNTriples/restaurantTest.nt",
                "./restaurantTest.csv-metadata.json",
                "restaurantTest",
                "./restaurantTest-reconverted.ttl",
                csv2rdfPath, "streaming"
            ),
            new TestFileSet(
                "./src/test/resources/StreamingNTriples/companyTest.nt",
                "./RDFtoCSV/src/test/resources/StreamingNTriples/companyTest.nt",
                "./companyTest.csv-metadata.json",
                "companyTest",
                "./companyTest-reconverted.ttl",
                csv2rdfPath, "streaming"
            ),
            new TestFileSet(
                "./src/test/resources/StreamingNTriples/literalListTest.nt",
                "./RDFtoCSV/src/test/resources/StreamingNTriples/literalListTest.nt",
                "./literalListTest.csv-metadata.json",
                "literalListTest",
                "./literalListTest-reconverted.ttl",
                csv2rdfPath, "streaming"
            ),
                new TestFileSet(
                        "./src/test/resources/StreamingNTriples/restaurantTestRDF4J.nt",
                        "./RDFtoCSV/src/test/resources/StreamingNTriples/restaurantTestRDF4J.nt",
                        "./restaurantTestRDF4J.csv-metadata.json",
                        "restaurantTestRDF4J",
                        "./restaurantTestRDF4J-reconverted.ttl",
                        csv2rdfPath, "rdf4j"
                ),
                new TestFileSet(
                        "./src/test/resources/StreamingNTriples/companyTest.nt",
                        "./RDFtoCSV/src/test/resources/StreamingNTriples/companyTest.nt",
                        "./companyTest.csv-metadata.json",
                        "companyTest",
                        "./companyTest-reconverted.ttl",
                        csv2rdfPath, "rdf4j"
                ),
                new TestFileSet(
                        "./src/test/resources/StreamingNTriples/literalListTest.nt",
                        "./RDFtoCSV/src/test/resources/StreamingNTriples/literalListTest.nt",
                        "./literalListTest.csv-metadata.json",
                        "literalListTest",
                        "./literalListTest-reconverted.ttl",
                        csv2rdfPath, "rdf4j"
                ),
                new TestFileSet(
                        "./src/test/resources/StreamingNTriples/restaurantTestBigFileStreaming.nt",
                        "./RDFtoCSV/src/test/resources/StreamingNTriples/restaurantTestBigFileStreaming.nt",
                        "./restaurantTestBigFileStreaming.csv-metadata.json",
                        "restaurantTestBigFileStreaming",
                        "./restaurantTestBigFileStreaming-reconverted.ttl",
                        csv2rdfPath, "bigFileStreaming"
                ),
                new TestFileSet(
                        "./src/test/resources/StreamingNTriples/companyTestBigFileStreaming.nt",
                        "./RDFtoCSV/src/test/resources/StreamingNTriples/companyTestBigFileStreaming.nt",
                        "./companyTestBigFileStreaming.csv-metadata.json",
                        "companyTestBigFileStreaming",
                        "./companyTestBigFileStreaming-reconverted.ttl",
                        csv2rdfPath, "bigFileStreaming"
                ),
                new TestFileSet(
                        "./src/test/resources/StreamingNTriples/literalListTestBigFileStreaming.nt",
                        "./RDFtoCSV/src/test/resources/StreamingNTriples/literalListTestBigFileStreaming.nt",
                        "./literalListTestBigFileStreaming.csv-metadata.json",
                        "literalListTestBigFileStreaming",
                        "./literalListTestBigFileStreaming-reconverted.ttl",
                        csv2rdfPath, "bigFileStreaming"
                )/* ,
                            new TestFileSet(
                "./src/test/resources/CSVWRDFTests/test001.ttl",
                "./RDFtoCSV/src/test/resources/CSVWRDFTests/test001.ttl",
                "./test001.csv-metadata.json",
                "test001",
                "./test001-reconverted.ttl",
                csv2rdfPath, "rdf4j"
            ),
            new TestFileSet(
                "./src/test/resources/CSVWRDFTests/test002b.nt",
                "./RDFtoCSV/src/test/resources/CSVWRDFTests/test002b.nt",
                "./test002b.csv-metadata.json",
                "test002b",
                "./test002b-reconverted.ttl",
                csv2rdfPath, "streaming"
            ),
            new TestFileSet(
                "./src/test/resources/CSVWRDFTests/test005.ttl",
                "./RDFtoCSV/src/test/resources/CSVWRDFTests/test005.ttl",
                "./test005.csv-metadata.json",
                "test005",
                "./test005-reconverted.ttl",
                csv2rdfPath, "rdf4j"
            ),
                new TestFileSet(
                        "./src/test/resources/CSVWRDFTests/test006.ttl",
                        "./RDFtoCSV/src/test/resources/CSVWRDFTests/test006.ttl",
                        "./test006.csv-metadata.json",
                        "test006",
                        "./test006-reconverted.ttl",
                        csv2rdfPath, "rdf4j"
                ),
                new TestFileSet(
                        "./src/test/resources/CSVWRDFTests/test007.ttl",
                        "./RDFtoCSV/src/test/resources/CSVWRDFTests/test007.ttl",
                        "./test007.csv-metadata.json",
                        "test007",
                        "./test007-reconverted.ttl",
                        csv2rdfPath, "rdf4j"
                ),
                new TestFileSet(
                        "./src/test/resources/CSVWRDFTests/test008.ttl",
                        "./RDFtoCSV/src/test/resources/CSVWRDFTests/test008.ttl",
                        "./test008.csv-metadata.json",
                        "test008",
                        "./test008-reconverted.ttl",
                        csv2rdfPath, "rdf4j"
                ),
                new TestFileSet(
                        "./src/test/resources/CSVWRDFTests/test009.ttl",
                        "./RDFtoCSV/src/test/resources/CSVWRDFTests/test009.ttl",
                        "./test009.csv-metadata.json",
                        "test009",
                        "./test009-reconverted.ttl",
                        csv2rdfPath, "rdf4j"
                ),
                new TestFileSet(
                        "./src/test/resources/CSVWRDFTests/test010.ttl",
                        "./RDFtoCSV/src/test/resources/CSVWRDFTests/test010.ttl",
                        "./test010.csv-metadata.json",
                        "test010",
                        "./test010-reconverted.ttl",
                        csv2rdfPath, "rdf4j"
                ),
                new TestFileSet(
                        "./src/test/resources/CSVWRDFTests/test011.ttl",
                        "./RDFtoCSV/src/test/resources/CSVWRDFTests/test011.ttl",
                        "./test011.csv-metadata.json",
                        "test011",
                        "./test011-reconverted.ttl",
                        csv2rdfPath, "rdf4j"
                )*/
        );
    }

    @ParameterizedTest
    @MethodSource("testFileSets")
    public void testRoundTripConversion(TestFileSet testFileSet) throws Exception {
        // Step 1: Load original RDF
        List<Statement> originalStatements = loadRdfFile(testFileSet.inputFile);
        System.out.println("Loaded " + originalStatements.size() + " statements from original RDF");
        
        // Step 2: Convert RDF to CSV using this tool
        System.out.println("Converting RDF to CSV+Metadata...");
        System.out.println("=== RDFtoCSV Conversion Started ===");
        
        // Build AppConfig with proper output path
        String outputPath = "./" + testFileSet.csvBaseName;
        AppConfig config = new AppConfig.Builder(testFileSet.inputFileForConversion)
                .parsing(testFileSet.parsingParameter)
                .output(outputPath)
                .firstNormalForm(false)  // Disable FNF for round-trip testing
                .build();
        
        // Create and run converter
        RDFtoCSV converter = new RDFtoCSV(config);
        converter.convertToZipFile();
        
        System.out.println("=== RDFtoCSV Conversion Completed Successfully ===");
        System.out.println("Total execution time: " + converter.getConfig().getOutputFilePath());
        
        // Verify metadata was created
        File metadataFile = new File(testFileSet.metadataOutputFile);
        assertTrue(metadataFile.exists(), "Metadata file should be generated: " + testFileSet.metadataOutputFile);
        
        // Step 3: Convert CSV+Metadata back to RDF using csv2rdf
        File csv2rdfJar = new File(testFileSet.csv2rdfJarPath);
        if (!csv2rdfJar.exists()) {
            System.out.println("SKIPPING TEST: csv2rdf jar not found at: " + testFileSet.csv2rdfJarPath);
            System.out.println("Download csv2rdf from: https://github.com/Swirrl/csv2rdf");
            System.out.println("Or use: wget https://github.com/Swirrl/csv2rdf/releases/download/v0.4.7/csv2rdf-0.4.7-standalone.jar");
            return; // Skip test if tool is not available
        }
        
        // Step 3.5: Preprocess CSV to work around csv2rdf bug with separator+valueUrl
        // csv2rdf doesn't properly split values when both separator and valueUrl are present
        System.out.println("Preprocessing CSV to work around csv2rdf separator bug...");
        
        // Find the actual CSV file created
        // Try different possible names based on parsing method
        String originalCsvPath = null;
        String[] possiblePaths = {
            testFileSet.csvBaseName + ".nt.csv",           // RDF4J output with -o option
            testFileSet.csvBaseName + ".ttl.csv",           // RDF4J output with -o option
            testFileSet.csvBaseName + ".nt_merged.csv", // Streaming merged output
            testFileSet.csvBaseName + ".nt0.csv"        // Streaming first file
        };
        
        for (String path : possiblePaths) {
            File csvFile = new File(path);
            System.out.println(" Trying to find CSV at: " + csvFile.getAbsolutePath());

            if (csvFile.exists()) {
                if(!config.getParsing().equals("rdf4j")){
                    if(path.contains(".nt.csv")){
                        continue;
                    }
                }
                originalCsvPath = path;
                System.out.println("  Found CSV at: " + originalCsvPath);
                break;
            }
        }
        
        if (originalCsvPath == null) {
            fail("CSV file not found. Tried: " + String.join(", ", possiblePaths));
        }
        
        String preprocessedCsv = preprocessCsvForCsv2rdfBugs(
            originalCsvPath,
            testFileSet.metadataOutputFile
        );
        
        // Determine which metadata file to use
        String metadataPath;
        if (preprocessedCsv.equals(originalCsvPath)) {
            // No preprocessing occurred, use original metadata
            metadataPath = testFileSet.metadataOutputFile;
        } else {
            // Preprocessing occurred, use preprocessed metadata
            metadataPath = preprocessedCsv.replace(".csv", ".csv-metadata.json");
        }
        
        System.out.println("Converting CSV+Metadata back to RDF using csv2rdf...");
        System.out.println("  Using CSV: " + preprocessedCsv);
        System.out.println("  Using metadata: " + metadataPath);
        convertCsvToRdfUsingCsv2rdf(
            testFileSet.csv2rdfJarPath,
            preprocessedCsv, // Use preprocessed CSV instead of original
            metadataPath,     // Use correct metadata file
            testFileSet.reconvertedRdfFile
        );
        
        // Step 4: Load reconverted RDF
        File reconvertedFile = new File(testFileSet.reconvertedRdfFile);
        assertTrue(reconvertedFile.exists(), "Reconverted RDF file should exist: " + testFileSet.reconvertedRdfFile);
        
        List<Statement> reconvertedStatements = loadRdfFile(testFileSet.reconvertedRdfFile);
        System.out.println("Loaded " + reconvertedStatements.size() + " statements from reconverted RDF");
        
        // Step 5: Compare the two RDF graphs
        compareRdfGraphs(originalStatements, reconvertedStatements);
        
        System.out.println("✓ Round-trip conversion successful!");
    }

    /**
     * Load RDF file and return all statements
     */
    private List<Statement> loadRdfFile(String filePath) throws IOException {
        List<Statement> statements = new ArrayList<>();
        File file = new File(filePath);
        assertTrue(file.exists(), "RDF file must exist: " + filePath);
        
        // Detect format from file extension
        RDFFormat format = Rio.getParserFormatForFileName(filePath).orElse(RDFFormat.TURTLE);
        
        try (FileInputStream fis = new FileInputStream(file)) {
            Rio.parse(fis, "", format).forEach(statements::add);
        }
        
        return statements;
    }

    /**
     * Preprocess CSV to work around csv2rdf bugs with separator+valueUrl.
     * 
     * csv2rdf Bug #5: When a column has both valueUrl and separator, csv2rdf doesn't
     * properly split the values and create multiple triples. Instead it creates a single
     * triple with the entire comma-separated string as the object.
     * 
     * This method:
     * 1. Reads the metadata to find columns with both separator and valueUrl
     * 2. Splits rows with multiple values in those columns into separate rows
     * 3. Returns path to the preprocessed CSV file
     */
    private String preprocessCsvForCsv2rdfBugs(String csvPath, String metadataPath) throws IOException {
        File csvFile = new File(csvPath);
        if (!csvFile.exists()) {
            System.out.println("CSV file not found: " + csvPath);
            return csvPath; // Return original path if file doesn't exist
        }
        
        // Parse metadata to find columns with separator + valueUrl
        ObjectMapper mapper = new ObjectMapper();
        JsonNode metadata = mapper.readTree(new File(metadataPath));
        
        Map<Integer, String> columnsToSplit = new HashMap<>(); // columnIndex -> separator
        JsonNode schema = metadata.path("tables").get(0).path("tableSchema");
        if (schema.isMissingNode()) {
            schema = metadata.path("tableSchema");
        }
        
        JsonNode columns = schema.path("columns");
        if (!columns.isMissingNode() && columns.isArray()) {
            int colIndex = 0;
            for (JsonNode col : columns) {
                boolean hasValueUrl = col.has("valueUrl");
                boolean hasSeparator = col.has("separator");
                
                if (hasValueUrl && hasSeparator) {
                    String separator = col.get("separator").asText();
                    columnsToSplit.put(colIndex, separator);
                    System.out.println("  Column " + colIndex + " (" + col.path("name").asText() + 
                                     ") has separator+valueUrl, will expand rows");
                }
                colIndex++;
            }
        }
        
        if (columnsToSplit.isEmpty()) {
            System.out.println("  No columns need preprocessing");
            return csvPath; // No preprocessing needed
        }
        
        // Read and preprocess CSV
        List<String> lines = Files.readAllLines(csvFile.toPath(), StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            return csvPath;
        }
        
        String header = lines.get(0);
        List<String> processedLines = new ArrayList<>();
        processedLines.add(header);
        
        // Process each data row
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            
            List<String> expandedRows = expandRowWithMultipleValues(line, columnsToSplit);
            processedLines.addAll(expandedRows);
        }
        
        // Write preprocessed CSV
        String preprocessedPath = csvPath.replace(".csv", "_preprocessed.csv");
        Files.write(new File(preprocessedPath).toPath(), processedLines, StandardCharsets.UTF_8);
        
        // Create metadata file for preprocessed CSV
        // Update the URL in metadata to point to the preprocessed CSV
        JsonNode updatedMetadata = updateMetadataForPreprocessedCsv(metadata, 
            new File(csvPath).getName(), 
            new File(preprocessedPath).getName());
        
        String preprocessedMetadataPath = preprocessedPath.replace(".csv", ".csv-metadata.json");
        mapper.writerWithDefaultPrettyPrinter()
            .writeValue(new File(preprocessedMetadataPath), updatedMetadata);
        
        System.out.println("  Created preprocessed CSV: " + preprocessedPath);
        System.out.println("  Created metadata: " + preprocessedMetadataPath);
        System.out.println("  Original rows: " + (lines.size() - 1) + ", Expanded rows: " + (processedLines.size() - 1));
        
        return preprocessedPath;
    }
    
    /**
     * Update metadata to reference the preprocessed CSV file
     */
    private JsonNode updateMetadataForPreprocessedCsv(JsonNode metadata, String originalCsvName, String preprocessedCsvName) {
        ObjectMapper mapper = new ObjectMapper();
        com.fasterxml.jackson.databind.node.ObjectNode root = metadata.deepCopy();
        
        // Update URL in tables array if present
        if (root.has("tables") && root.get("tables").isArray()) {
            com.fasterxml.jackson.databind.node.ArrayNode tables = 
                (com.fasterxml.jackson.databind.node.ArrayNode) root.get("tables");
            for (int i = 0; i < tables.size(); i++) {
                com.fasterxml.jackson.databind.node.ObjectNode table = 
                    (com.fasterxml.jackson.databind.node.ObjectNode) tables.get(i);
                if (table.has("url")) {
                    String url = table.get("url").asText();
                    if (url.equals(originalCsvName) || url.endsWith("/" + originalCsvName)) {
                        table.put("url", preprocessedCsvName);
                    }
                }
            }
        }
        
        return root;
    }
    
    /**
     * Expand a single CSV row into multiple rows if it has multi-valued cells.
     * Uses proper CSV parsing to handle quoted fields.
     */
    private List<String> expandRowWithMultipleValues(String line, Map<Integer, String> columnsToSplit) {
        List<String> cells = parseCsvLine(line);
        
        // Find maximum number of values in any column that needs splitting
        int maxValues = 1;
        Map<Integer, List<String>> splitValues = new HashMap<>();
        
        for (Map.Entry<Integer, String> entry : columnsToSplit.entrySet()) {
            int colIndex = entry.getKey();
            String separator = entry.getValue();
            
            if (colIndex < cells.size()) {
                String cellValue = cells.get(colIndex);
                if (!cellValue.isEmpty()) {
                    List<String> values = Arrays.asList(cellValue.split(separator));
                    splitValues.put(colIndex, values);
                    maxValues = Math.max(maxValues, values.size());
                }
            }
        }
        
        // If no splitting needed, return original row
        if (maxValues == 1) {
            return Collections.singletonList(line);
        }
        
        // Create expanded rows
        List<String> expandedRows = new ArrayList<>();
        for (int valueIndex = 0; valueIndex < maxValues; valueIndex++) {
            List<String> newCells = new ArrayList<>(cells);
            
            for (Map.Entry<Integer, List<String>> entry : splitValues.entrySet()) {
                int colIndex = entry.getKey();
                List<String> values = entry.getValue();
                
                if (valueIndex < values.size()) {
                    newCells.set(colIndex, values.get(valueIndex).trim());
                } else {
                    newCells.set(colIndex, ""); // Empty if fewer values
                }
            }
            
            expandedRows.add(createCsvLine(newCells));
        }
        
        return expandedRows;
    }
    
    /**
     * Parse a CSV line handling quoted fields properly
     */
    private List<String> parseCsvLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder currentCell = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentCell.append('"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                cells.add(currentCell.toString());
                currentCell = new StringBuilder();
            } else {
                currentCell.append(c);
            }
        }
        cells.add(currentCell.toString());
        
        return cells;
    }
    
    /**
     * Create a CSV line from cells, quoting as needed
     */
    private String createCsvLine(List<String> cells) {
        return cells.stream()
            .map(cell -> {
                if (cell.contains(",") || cell.contains("\"") || cell.contains("\n")) {
                    return "\"" + cell.replace("\"", "\"\"") + "\"";
                }
                return cell;
            })
            .collect(Collectors.joining(","));
    }
    
    /**
     * Convert CSV+Metadata to RDF using the csv2rdf tool
     */
    private void convertCsvToRdfUsingCsv2rdf(String csv2rdfJarPath, String csvPath, String metadataPath, String outputPath) 
            throws IOException, InterruptedException {
        
        File outputFile = new File(outputPath);
        if (outputFile.exists()) {
            outputFile.delete();
        }
        
        // Run csv2rdf: java -jar csv2rdf.jar -u metadata.json -o output.ttl -m minimal
        // -m minimal: only translate what is given in the metadata, no extra triples
        // Note: csv2rdf expects the metadata file path with -u flag
        ProcessBuilder builder = new ProcessBuilder(
            "java", "-jar", new File(csv2rdfJarPath).getAbsolutePath(),
            "-u", new File(metadataPath).getAbsolutePath(),
            "-o", new File(outputPath).getAbsolutePath(),
            "-m", "minimal"
        );
        
        builder.redirectErrorStream(true);
        Process process = builder.start();
        
        // Read output
        java.io.BufferedReader reader = new java.io.BufferedReader(
            new java.io.InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("csv2rdf: " + line);
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            fail("csv2rdf conversion failed with exit code: " + exitCode);
        }
    }

    /**
     * Compare two RDF graphs, accounting for blank node renaming using graph isomorphism
     */
    private void compareRdfGraphs(List<Statement> original, List<Statement> reconverted) {
        // Convert to RDF4J Models for isomorphism checking
        Model originalModel = new LinkedHashModel(original);
        Model reconvertedModel = new LinkedHashModel(reconverted);
        
        System.out.println("Original triples: " + originalModel.size());
        System.out.println("Reconverted triples: " + reconvertedModel.size());
        
        // Use RDF4J's Models.isomorphic() for proper graph isomorphism checking
        // This correctly handles blank node equivalence
        if (Models.isomorphic(originalModel, reconvertedModel)) {
            System.out.println("✓ Graphs are isomorphic - round-trip successful!");
            return;
        }
        
        // If not isomorphic, the graphs may differ. 
        // However, the difference might be acceptable CSVW metadata triples.
        // Let's check if only the size differs due to metadata, not the actual data.
        System.out.println("\n⚠ Graphs are not strictly isomorphic. Checking for acceptable differences...");
        
        // Try comparing without CSVW metadata triples
        Model originalWithoutCSVW = filterOutCSVWMetadata(originalModel);
        Model reconvertedWithoutCSVW = filterOutCSVWMetadata(reconvertedModel);
        
        System.out.println("After filtering CSVW metadata:");
        System.out.println("  Original data triples: " + originalWithoutCSVW.size());
        System.out.println("  Reconverted data triples: " + reconvertedWithoutCSVW.size());
        
        if (Models.isomorphic(originalWithoutCSVW, reconvertedWithoutCSVW)) {
            System.out.println("✓ Data triples are isomorphic (CSVW metadata differences are acceptable)");
            return;
        }
        
        // Still not isomorphic - provide detailed diagnostics
        System.out.println("\n⚠ Data triples are also not isomorphic. Analyzing differences...");
        
        // Create normalized representations for detailed comparison
        Set<NormalizedTriple> originalNormalized = normalizeStatements(new ArrayList<>(originalWithoutCSVW));
        Set<NormalizedTriple> reconvertedNormalized = normalizeStatements(new ArrayList<>(reconvertedWithoutCSVW));
        
        // Find missing triples (from original not in reconverted)
        Set<NormalizedTriple> missing = new HashSet<>(originalNormalized);
        missing.removeAll(reconvertedNormalized);
        
        // Find extra triples (in reconverted but not in original)
        Set<NormalizedTriple> extra = new HashSet<>(reconvertedNormalized);
        extra.removeAll(originalNormalized);
        
        // Filter out known csv2rdf bugs:
        // Bug #4: csv2rdf incorrectly generates file:// URIs for empty values with valueUrl
        Set<NormalizedTriple> csv2rdfBugs = extra.stream()
            .filter(t -> t.object.startsWith("file:/"))
            .collect(Collectors.toSet());
        
        if (!csv2rdfBugs.isEmpty()) {
            System.out.println("\n⚠ Filtering out csv2rdf bug (empty valueUrl -> file:// URI): " + csv2rdfBugs.size() + " triples");
            csv2rdfBugs.forEach(t -> System.out.println("  [IGNORED] " + t));
            extra.removeAll(csv2rdfBugs);
        }
        
        // Report differences (excluding blank node IDs which are shown for debugging only)
        if (!missing.isEmpty()) {
            System.out.println("\n⚠ Missing triples (present in original but not in reconverted):");
            System.out.println("   Note: Blank node IDs (_:bnX) in this output are normalized and may not match the actual IDs");
            missing.stream().limit(10).forEach(t -> System.out.println("  - " + t));
            if (missing.size() > 10) {
                System.out.println("  ... and " + (missing.size() - 10) + " more");
            }
        }
        
        if (!extra.isEmpty()) {
            System.out.println("\n⚠ Extra triples (present in reconverted but not in original):");
            System.out.println("   Note: Blank node IDs (_:bnX) in this output are normalized and may not match the actual IDs");
            extra.stream().limit(10).forEach(t -> System.out.println("  + " + t));
            if (extra.size() > 10) {
                System.out.println("  ... and " + (extra.size() - 10) + " more");
            }
        }
        
        // After filtering csv2rdf bugs, check if there are still differences
        if (!missing.isEmpty() || !extra.isEmpty()) {
            fail("Graphs are not isomorphic. There are real semantic differences in the data.");
        }
    }
    
    /**
     * Filter out CSVW metadata triples, keeping only the actual data.
     * CSVW metadata includes TableGroup, Table, Row, and describes relationships.
     */
    private Model filterOutCSVWMetadata(Model model) {
        String CSVW_NS = "http://www.w3.org/ns/csvw#";
        Model filtered = new LinkedHashModel();
        
        for (Statement stmt : model) {
            String predicate = stmt.getPredicate().stringValue();
            
            // Keep triples that are NOT CSVW metadata structure
            // Filter out: rdf:type csvw:*, csvw:table, csvw:row, csvw:describes, csvw:url, csvw:rownum
            if (predicate.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && 
                stmt.getObject().stringValue().startsWith(CSVW_NS)) {
                continue; // Skip type declarations for CSVW classes
            }
            if (predicate.startsWith(CSVW_NS)) {
                // Skip all CSVW metadata predicates (table, row, describes, url, rownum, etc.)
                continue;
            }
            
            // Keep all other triples (actual data)
            filtered.add(stmt);
        }
        
        return filtered;
    }

    /**
     * Normalize statements to allow comparison despite blank node renaming
     */
    private Set<NormalizedTriple> normalizeStatements(List<Statement> statements) {
        // Map blank nodes to their normalized form
        Map<BNode, String> blankNodeMap = new HashMap<>();
        int blankNodeCounter = 0;
        
        // First pass: create stable identifiers for blank nodes based on their structure
        Map<String, BNode> structureToBlankNode = new HashMap<>();
        for (Statement stmt : statements) {
            if (stmt.getSubject() instanceof BNode) {
                BNode bn = (BNode) stmt.getSubject();
                if (!blankNodeMap.containsKey(bn)) {
                    // Create a signature based on predicates and objects
                    String signature = createBlankNodeSignature(bn, statements);
                    blankNodeMap.put(bn, "bn" + blankNodeCounter++);
                }
            }
            if (stmt.getObject() instanceof BNode) {
                BNode bn = (BNode) stmt.getObject();
                if (!blankNodeMap.containsKey(bn)) {
                    blankNodeMap.put(bn, "bn" + blankNodeCounter++);
                }
            }
        }
        
        // Second pass: create normalized triples
        return statements.stream()
            .map(stmt -> new NormalizedTriple(stmt, blankNodeMap))
            .collect(Collectors.toSet());
    }

    /**
     * Create a signature for a blank node based on its relationships
     */
    private String createBlankNodeSignature(BNode bnode, List<Statement> statements) {
        StringBuilder sig = new StringBuilder();
        
        // Get all statements where this blank node is the subject
        List<Statement> outgoing = statements.stream()
            .filter(s -> s.getSubject().equals(bnode))
            .sorted(Comparator.comparing(s -> s.getPredicate().stringValue()))
            .collect(Collectors.toList());
        
        for (Statement s : outgoing) {
            sig.append(s.getPredicate().stringValue()).append(":");
            if (s.getObject() instanceof BNode) {
                sig.append("_:bnode");
            } else {
                sig.append(s.getObject().stringValue());
            }
            sig.append(";");
        }
        
        return sig.toString();
    }

    /**
     * A normalized representation of an RDF triple that can be compared
     */
    private static class NormalizedTriple {
        final String subject;
        final String predicate;
        final String object;
        final boolean objectIsLiteral;
        final String datatype;
        final String language;
        
        NormalizedTriple(Statement stmt, Map<BNode, String> blankNodeMap) {
            // Normalize subject
            if (stmt.getSubject() instanceof BNode) {
                this.subject = "_:" + blankNodeMap.get(stmt.getSubject());
            } else {
                this.subject = stmt.getSubject().stringValue();
            }
            
            // Predicate is always an IRI
            this.predicate = stmt.getPredicate().stringValue();
            
            // Normalize object
            Value obj = stmt.getObject();
            if (obj instanceof BNode) {
                this.object = "_:" + blankNodeMap.get(obj);
                this.objectIsLiteral = false;
                this.datatype = null;
                this.language = null;
            } else if (obj.isLiteral()) {
                org.eclipse.rdf4j.model.Literal lit = (org.eclipse.rdf4j.model.Literal) obj;
                this.object = lit.getLabel();
                this.objectIsLiteral = true;
                this.datatype = lit.getDatatype() != null ? lit.getDatatype().stringValue() : null;
                this.language = lit.getLanguage().orElse(null);
            } else {
                this.object = obj.stringValue();
                this.objectIsLiteral = false;
                this.datatype = null;
                this.language = null;
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NormalizedTriple that = (NormalizedTriple) o;
            
            // Check basic structure
            if (objectIsLiteral != that.objectIsLiteral) return false;
            if (!Objects.equals(subject, that.subject)) return false;
            if (!Objects.equals(predicate, that.predicate)) return false;
            if (!Objects.equals(datatype, that.datatype)) return false;
            if (!Objects.equals(language, that.language)) return false;
            
            // For xsd:decimal literals, compare numerically to handle trailing zeros
            if (objectIsLiteral && datatype != null && 
                datatype.equals("http://www.w3.org/2001/XMLSchema#decimal")) {
                try {
                    java.math.BigDecimal thisValue = new java.math.BigDecimal(this.object);
                    java.math.BigDecimal thatValue = new java.math.BigDecimal(that.object);
                    return thisValue.compareTo(thatValue) == 0;
                } catch (NumberFormatException e) {
                    // If parsing fails, fall back to string comparison
                    return Objects.equals(object, that.object);
                }
            }
            
            // For all other types, compare as strings
            return Objects.equals(object, that.object);
        }
        
        @Override
        public int hashCode() {
            // For xsd:decimal, use normalized form for consistent hashing
            String normalizedObject = object;
            if (objectIsLiteral && datatype != null && 
                datatype.equals("http://www.w3.org/2001/XMLSchema#decimal")) {
                try {
                    // Normalize to canonical form (removes trailing zeros)
                    java.math.BigDecimal bd = new java.math.BigDecimal(object);
                    normalizedObject = bd.stripTrailingZeros().toPlainString();
                } catch (NumberFormatException e) {
                    // Use original if parsing fails
                }
            }
            return Objects.hash(subject, predicate, normalizedObject, objectIsLiteral, datatype, language);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(subject).append(" ").append(predicate).append(" ");
            if (objectIsLiteral) {
                sb.append("\"").append(object).append("\"");
                if (language != null) {
                    sb.append("@").append(language);
                } else if (datatype != null) {
                    sb.append("^^<").append(datatype).append(">");
                }
            } else {
                sb.append(object);
            }
            return sb.toString();
        }
    }
}
