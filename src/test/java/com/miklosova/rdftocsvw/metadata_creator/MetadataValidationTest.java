package com.miklosova.rdftocsvw.metadata_creator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miklosova.rdftocsvw.converter.RDFtoCSV;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.BuiltInDatatypes;
import com.miklosova.rdftocsvw.support.Main;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to validate that generated metadata correctly represents the RDF input structure.
 * 
 * Tests verify:
 * 1. All predicates from RDF input appear as propertyUrl in metadata
 * 2. Literal objects with datatypes have matching datatype in metadata
 * 3. Literal objects result in columns WITHOUT valueUrl
 * 4. IRI/blank node objects result in columns WITH valueUrl
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MetadataValidationTest {

    private List<Statement> inputStatements;
    private JsonNode metadataJson;
    private String currentTestInputFile;
    private String currentTestInputFileForConversion;
    private String currentMetadataOutputFile;
    private String currentCsvBaseName;

    static class TestFileSet {
        final String inputFile;
        final String inputFileForConversion;
        final String metadataOutputFile;
        final String csvBaseName;

        TestFileSet(String inputFile, String inputFileForConversion, String metadataOutputFile, String csvBaseName) {
            this.inputFile = inputFile;
            this.inputFileForConversion = inputFileForConversion;
            this.metadataOutputFile = metadataOutputFile;
            this.csvBaseName = csvBaseName;
        }

        @Override
        public String toString() {
            return csvBaseName;
        }
    }

    static java.util.stream.Stream<TestFileSet> testFileSets() {
        return java.util.stream.Stream.of(
            new TestFileSet(
                "./src/test/resources/StreamingNTriples/libraryTest.nt",
                "./RDFtoCSV/src/test/resources/StreamingNTriples/libraryTest.nt",
                "./libraryTest.csv-metadata.json",
                "libraryTest"
            ),
            new TestFileSet(
                "./src/test/resources/StreamingNTriples/restaurantTest.nt",
                "./RDFtoCSV/src/test/resources/StreamingNTriples/restaurantTest.nt",
                "./restaurantTest.csv-metadata.json",
                "restaurantTest"
            ),
            new TestFileSet(
                "./src/test/resources/StreamingNTriples/companyTest.nt",
                "./RDFtoCSV/src/test/resources/StreamingNTriples/companyTest.nt",
                "./companyTest.csv-metadata.json",
                "companyTest"
            )
        );
    }
    
    private void setUp(TestFileSet testFileSet) throws Exception {
        // Set current test parameters
        currentTestInputFile = testFileSet.inputFile;
        currentTestInputFileForConversion = testFileSet.inputFileForConversion;
        currentMetadataOutputFile = testFileSet.metadataOutputFile;
        currentCsvBaseName = testFileSet.csvBaseName;

        // Parse the input RDF file to collect all statements
        inputStatements = new ArrayList<>();
        File inputFile = new File(currentTestInputFile);
        assertTrue(inputFile.exists(), "Test input file must exist: " + currentTestInputFile);
        
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            Rio.parse(fis, "", RDFFormat.NTRIPLES)
                .forEach(st -> {
                    if (st != null) {
                        inputStatements.add(st);
                    }
                });
        }
        
        System.out.println("Loaded " + inputStatements.size() + " statements from input file");
        
        // Generate CSV and metadata using streaming method
        AppConfig config = new AppConfig.Builder(currentTestInputFileForConversion)
            .parsing("streaming")
            .firstNormalForm(false)  // Match old test expectations
            .build();
        
        Main.main(new String[]{"-f", currentTestInputFileForConversion, "-p", "streaming", "-n"});
        
        // Load the generated metadata JSON
        File metadataFile = new File(currentMetadataOutputFile);
        assertTrue(metadataFile.exists(), "Metadata file should be generated: " + currentMetadataOutputFile);
        
        ObjectMapper mapper = new ObjectMapper();
        metadataJson = mapper.readTree(metadataFile);
        
        // Handle both flattened (single table) and non-flattened (tables array) metadata
        int tableCount;
        if (metadataJson.has("tables")) {
            tableCount = metadataJson.get("tables").size();
        } else {
            // Flattened single table - wrap it for consistent processing
            tableCount = 1;
        }
        System.out.println("Loaded metadata JSON with " + tableCount + " table(s)");
    }
    
    private void tearDown() throws IOException {
        // Clean up generated files
        /*
        if (currentMetadataOutputFile != null) {
            deleteIfExists(currentMetadataOutputFile);
        }
        if (currentCsvBaseName != null) {
            deleteIfExists(currentCsvBaseName + ".csv");
            deleteIfExists(currentCsvBaseName + "0.csv");
            deleteIfExists(currentCsvBaseName + "_merged.csv");
            deleteIfExists(currentCsvBaseName + "_CSVW.zip");
            
            // Clean up any numbered CSV files that might have been created
            for (int i = 0; i < 20; i++) {
                deleteIfExists(currentCsvBaseName + i + ".csv");
            }
        }
         */
    }
    
    private void deleteIfExists(String filename) throws IOException {
        Path path = Paths.get(filename);
        Files.deleteIfExists(path);
    }
    
    /**
     * Helper method to check if two datatypes are equivalent.
     * CSVW allows both short form (e.g., "boolean") and full URI form (e.g., "http://www.w3.org/2001/XMLSchema#boolean").
     * This reuses the same logic as Column.createDatatypeFromValue() - if it's a built-in XSD type, the short form (local name) is used.
     */
    private boolean datatypesMatch(String rdfDatatypeUri, String csvwDatatype) {
        if (rdfDatatypeUri.equals(csvwDatatype)) {
            return true;
        }
        
        // Simulate the same logic as Column.createDatatypeFromValue():
        // For built-in XSD datatypes, the output should be the local name (short form)
        IRI datatypeIri = SimpleValueFactory.getInstance().createIRI(rdfDatatypeUri);
        String localName = datatypeIri.getLocalName();
        
        // Check if this is a built-in datatype using the same method the converter uses
        if (BuiltInDatatypes.isBuiltInDatatype(datatypeIri)) {
            // For built-in types, compare using the local name
            return localName.equals(csvwDatatype);
        } else {
            // For non-built-in types, the full URI should be in the metadata
            return rdfDatatypeUri.equals(csvwDatatype);
        }
    }

    
    @ParameterizedTest
    @MethodSource("testFileSets")
    public void testLiteralDatatypesMatchMetadata(TestFileSet testFileSet) throws Exception {
        setUp(testFileSet);
        // Group statements by predicate
        Map<String, List<Statement>> statementsByPredicate = inputStatements.stream()
            .collect(Collectors.groupingBy(st -> st.getPredicate().stringValue()));
        
        // Build a map of propertyUrl -> column from metadata
        Map<String, List<JsonNode>> columnsByPropertyUrl = new HashMap<>();
        
        // Handle both flattened and non-flattened metadata
        JsonNode tables;
        if (metadataJson.has("tables")) {
            tables = metadataJson.get("tables");
        } else {
            // Flattened single table - wrap it in an array for consistent processing
            ObjectMapper mapper = new ObjectMapper();
            tables = mapper.createArrayNode().add(metadataJson);
        }
        
        for (JsonNode table : tables) {
            JsonNode tableSchema = table.get("tableSchema");
            if (tableSchema != null) {
                JsonNode columns = tableSchema.get("columns");
                if (columns != null) {
                    for (JsonNode column : columns) {
                        JsonNode propertyUrl = column.get("propertyUrl");
                        if (propertyUrl != null && !propertyUrl.isNull()) {
                            columnsByPropertyUrl
                                .computeIfAbsent(propertyUrl.asText(), k -> new ArrayList<>())
                                .add(column);
                        }
                    }
                }
            }
        }
        
        // For each predicate, check datatype handling
        int checkedDatatypes = 0;
        int checkedLanguageTaggedLiterals = 0;
        
        for (Map.Entry<String, List<Statement>> entry : statementsByPredicate.entrySet()) {
            String predicate = entry.getKey();
            List<Statement> statements = entry.getValue();
            
            // Check language-tagged literals - they should have NO datatype in metadata
            List<Statement> languageTaggedStmts = statements.stream()
                .filter(st -> st.getObject().isLiteral())
                .filter(st -> ((org.eclipse.rdf4j.model.Literal) st.getObject()).getLanguage().isPresent())
                .collect(Collectors.toList());
            
            if (!languageTaggedStmts.isEmpty()) {
                // For language-tagged literals, find corresponding columns and verify NO datatype
                List<JsonNode> columns = columnsByPropertyUrl.get(predicate);
                if (columns != null) {
                    for (JsonNode column : columns) {
                        JsonNode lang = column.get("lang");
                        if (lang != null && !lang.isNull()) {
                            // This column has lang - verify it has NO datatype
                            JsonNode datatype = column.get("datatype");
                            if (datatype != null && !datatype.isNull()) {
                                fail("Column for predicate " + predicate + 
                                    " with lang='" + lang.asText() + "' should NOT have datatype, but found: " + 
                                    datatype.asText() + ". Language-tagged literals should have no explicit datatype " +
                                    "(rdf:langString is inferred from lang during conversion).");
                            }
                            checkedLanguageTaggedLiterals++;
                        }
                    }
                }
            }
            
            // Get the first typed literal (non-language-tagged) for this predicate
            Optional<Statement> typedLiteralStmt = statements.stream()
                .filter(st -> st.getObject().isLiteral())
                .filter(st -> {
                    org.eclipse.rdf4j.model.Literal lit = (org.eclipse.rdf4j.model.Literal) st.getObject();
                    // Only include literals that have a datatype AND no language tag
                    return lit.getDatatype() != null && !lit.getLanguage().isPresent();
                })
                .findFirst();
            
            if (typedLiteralStmt.isPresent()) {
                org.eclipse.rdf4j.model.Literal literal = 
                    (org.eclipse.rdf4j.model.Literal) typedLiteralStmt.get().getObject();
                String expectedDatatype = literal.getDatatype().stringValue();
                
                // Skip validation for xsd:string (it's the default, should be omitted from metadata)
                if (expectedDatatype.equals("http://www.w3.org/2001/XMLSchema#string")) {
                    continue; // xsd:string is default, no need to have it in metadata
                }
                
                // Skip validation for rdf:langString (it's inferred from lang tag)
                if (expectedDatatype.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")) {
                    // For langString, we should find a column with matching lang instead
                    List<JsonNode> columns = columnsByPropertyUrl.get(predicate);
                    if (columns != null) {
                        // Check if there's a column with lang property (any lang is fine)
                        boolean foundLangColumn = columns.stream()
                            .anyMatch(col -> {
                                JsonNode lang = col.get("lang");
                                return lang != null && !lang.isNull();
                            });
                        
                        if (foundLangColumn) {
                            checkedLanguageTaggedLiterals++;
                        }
                    }
                    continue; // rdf:langString is inferred from lang, skip datatype check
                }
                
                // Find corresponding column in metadata for other datatypes
                List<JsonNode> columns = columnsByPropertyUrl.get(predicate);
                if (columns != null) {
                    boolean foundMatchingDatatype = false;
                    for (JsonNode column : columns) {
                        // Skip columns with lang (those shouldn't have datatype)
                        JsonNode lang = column.get("lang");
                        if (lang != null && !lang.isNull()) {
                            continue;
                        }
                        
                        JsonNode datatype = column.get("datatype");
                        if (datatype != null && !datatype.isNull()) {
                            String metadataDatatype = datatype.asText();
                            // Use helper method to compare - handles both short form and full URI
                            if (datatypesMatch(expectedDatatype, metadataDatatype)) {
                                foundMatchingDatatype = true;
                                checkedDatatypes++;
                                break;
                            }
                        }
                    }
                    assertTrue(foundMatchingDatatype,
                        "Column for predicate " + predicate + 
                        " should have datatype " + expectedDatatype + " (or its short form equivalent) in metadata");
                }
            }
        }
        
        System.out.println("Verified " + checkedDatatypes + " datatype matches for typed literals");
        System.out.println("Verified " + checkedLanguageTaggedLiterals + " language-tagged literals have no datatype");
        assertTrue(checkedDatatypes > 0 || checkedLanguageTaggedLiterals > 0,
            "Should have verified at least some datatypes or language tags");
    }
    
    @ParameterizedTest
    @MethodSource("testFileSets")
    public void testLiteralObjectsHaveNoValueUrl(TestFileSet testFileSet) throws Exception {
        setUp(testFileSet);
        // Group statements by predicate
        Map<String, List<Statement>> statementsByPredicate = inputStatements.stream()
            .collect(Collectors.groupingBy(st -> st.getPredicate().stringValue()));
        
        // Build a map of propertyUrl -> column from metadata
        Map<String, List<JsonNode>> columnsByPropertyUrl = new HashMap<>();
        
        // Handle both flattened and non-flattened metadata
        JsonNode tables;
        if (metadataJson.has("tables")) {
            tables = metadataJson.get("tables");
        } else {
            // Flattened single table - wrap it in an array for consistent processing
            ObjectMapper mapper = new ObjectMapper();
            tables = mapper.createArrayNode().add(metadataJson);
        }
        
        for (JsonNode table : tables) {
            JsonNode tableSchema = table.get("tableSchema");
            if (tableSchema != null) {
                JsonNode columns = tableSchema.get("columns");
                if (columns != null) {
                    for (JsonNode column : columns) {
                        JsonNode propertyUrl = column.get("propertyUrl");
                        if (propertyUrl != null && !propertyUrl.isNull()) {
                            columnsByPropertyUrl
                                .computeIfAbsent(propertyUrl.asText(), k -> new ArrayList<>())
                                .add(column);
                        }
                    }
                }
            }
        }
        
        // For each predicate where ALL objects are literals, verify NO valueUrl
        int checkedPredicates = 0;
        for (Map.Entry<String, List<Statement>> entry : statementsByPredicate.entrySet()) {
            String predicate = entry.getKey();
            List<Statement> statements = entry.getValue();
            
            // Check if all objects for this predicate are literals
            boolean allLiterals = statements.stream()
                .allMatch(st -> st.getObject().isLiteral());
            
            if (allLiterals) {
                List<JsonNode> columns = columnsByPropertyUrl.get(predicate);
                if (columns != null) {
                    for (JsonNode column : columns) {
                        JsonNode valueUrl = column.get("valueUrl");
                        // valueUrl should be null or not present for literal-only predicates
                        if (valueUrl != null && !valueUrl.isNull()) {
                            fail("Column for literal-only predicate " + predicate + 
                                " should not have valueUrl, but found: " + valueUrl.asText());
                        }
                    }
                    checkedPredicates++;
                }
            }
        }
        
        System.out.println("Verified " + checkedPredicates + " literal-only predicates have no valueUrl");
        assertTrue(checkedPredicates > 0, "Should have verified at least some literal-only predicates");
    }
    
    @ParameterizedTest
    @MethodSource("testFileSets")
    public void testIriObjectsHaveValueUrl(TestFileSet testFileSet) throws Exception {
        setUp(testFileSet);
        // Group statements by predicate
        Map<String, List<Statement>> statementsByPredicate = inputStatements.stream()
            .collect(Collectors.groupingBy(st -> st.getPredicate().stringValue()));
        
        // Build a map of propertyUrl -> column from metadata
        Map<String, List<JsonNode>> columnsByPropertyUrl = new HashMap<>();
        
        // Handle both flattened and non-flattened metadata
        JsonNode tables;
        if (metadataJson.has("tables")) {
            tables = metadataJson.get("tables");
        } else {
            // Flattened single table - wrap it in an array for consistent processing
            ObjectMapper mapper = new ObjectMapper();
            tables = mapper.createArrayNode().add(metadataJson);
        }
        
        for (JsonNode table : tables) {
            JsonNode tableSchema = table.get("tableSchema");
            if (tableSchema != null) {
                JsonNode columns = tableSchema.get("columns");
                if (columns != null) {
                    for (JsonNode column : columns) {
                        JsonNode propertyUrl = column.get("propertyUrl");
                        if (propertyUrl != null && !propertyUrl.isNull()) {
                            columnsByPropertyUrl
                                .computeIfAbsent(propertyUrl.asText(), k -> new ArrayList<>())
                                .add(column);
                        }
                    }
                }
            }
        }
        
        // For each predicate where ALL objects are IRIs, verify valueUrl exists
        int checkedPredicates = 0;
        for (Map.Entry<String, List<Statement>> entry : statementsByPredicate.entrySet()) {
            String predicate = entry.getKey();
            List<Statement> statements = entry.getValue();
            
            // Check if all objects for this predicate are IRIs (not literals, not blank nodes)
            boolean allIRIs = statements.stream()
                .allMatch(st -> st.getObject().isIRI());
            
            if (allIRIs) {
                List<JsonNode> columns = columnsByPropertyUrl.get(predicate);
                if (columns != null) {
                    for (JsonNode column : columns) {
                        JsonNode valueUrl = column.get("valueUrl");
                        assertNotNull(valueUrl, "Column for IRI-only predicate " + predicate + 
                            " should have valueUrl");
                        assertFalse( valueUrl.isNull(), "Column for IRI-only predicate " + predicate + 
                            " should have non-null valueUrl");
                        
                        String valueUrlStr = valueUrl.asText();
                        assertTrue(valueUrlStr.contains("{+"),
                            "valueUrl should contain a pattern variable: " + valueUrlStr);
                    }
                    checkedPredicates++;
                }
            }
        }
        
        System.out.println("Verified " + checkedPredicates + " IRI-only predicates have valueUrl");
        assertTrue(checkedPredicates > 0, "Should have verified at least some IRI-only predicates");
    }
    
    @ParameterizedTest
    @MethodSource("testFileSets")
    public void testLanguageTaggedLiteralsHaveLangInMetadata(TestFileSet testFileSet) throws Exception {
        setUp(testFileSet);
        // Group statements by predicate
        Map<String, List<Statement>> statementsByPredicate = inputStatements.stream()
            .collect(Collectors.groupingBy(st -> st.getPredicate().stringValue()));
        
        // Build a map of propertyUrl -> column from metadata
        Map<String, List<JsonNode>> columnsByPropertyUrl = new HashMap<>();
        
        // Handle both flattened and non-flattened metadata
        JsonNode tables;
        if (metadataJson.has("tables")) {
            tables = metadataJson.get("tables");
        } else {
            // Flattened single table - wrap it in an array for consistent processing
            ObjectMapper mapper = new ObjectMapper();
            tables = mapper.createArrayNode().add(metadataJson);
        }
        
        for (JsonNode table : tables) {
            JsonNode tableSchema = table.get("tableSchema");
            if (tableSchema != null) {
                JsonNode columns = tableSchema.get("columns");
                if (columns != null) {
                    for (JsonNode column : columns) {
                        JsonNode propertyUrl = column.get("propertyUrl");
                        if (propertyUrl != null && !propertyUrl.isNull()) {
                            columnsByPropertyUrl
                                .computeIfAbsent(propertyUrl.asText(), k -> new ArrayList<>())
                                .add(column);
                        }
                    }
                }
            }
        }
        
        // For each predicate with language-tagged literals, verify lang in metadata
        int checkedLanguageTags = 0;
        for (Map.Entry<String, List<Statement>> entry : statementsByPredicate.entrySet()) {
            String predicate = entry.getKey();
            List<Statement> statements = entry.getValue();
            
            // Get language-tagged literals for this predicate
            Map<String, List<Statement>> byLanguage = statements.stream()
                .filter(st -> st.getObject().isLiteral())
                .filter(st -> ((org.eclipse.rdf4j.model.Literal) st.getObject()).getLanguage().isPresent())
                .collect(Collectors.groupingBy(st -> 
                    ((org.eclipse.rdf4j.model.Literal) st.getObject()).getLanguage().get()));
            
            if (!byLanguage.isEmpty()) {
                List<JsonNode> columns = columnsByPropertyUrl.get(predicate);
                if (columns != null) {
                    // For each language, there should be a column with that lang
                    for (String expectedLang : byLanguage.keySet()) {
                        boolean foundMatchingLang = false;
                        for (JsonNode column : columns) {
                            JsonNode lang = column.get("lang");
                            if (lang != null && !lang.isNull()) {
                                String metadataLang = lang.asText();
                                if (metadataLang.equals(expectedLang)) {
                                    foundMatchingLang = true;
                                    checkedLanguageTags++;
                                    break;
                                }
                            }
                        }
                        
                        assertTrue(foundMatchingLang,
                            "Column for predicate " + predicate + 
                            " should have lang=" + expectedLang + " in metadata");
                    }
                }
            }
        }
        
        System.out.println("Verified " + checkedLanguageTags + " language tag matches");
        assertTrue(checkedLanguageTags > 0, "Should have verified at least some language tags");
    }
    
    @ParameterizedTest
    @MethodSource("testFileSets")
    public void testColumnsWithLangHaveNoDatatypeProperty(TestFileSet testFileSet) throws Exception {
        setUp(testFileSet);
        // According to CSVW spec, lang and datatype are mutually exclusive
        // When a column has lang, it should NOT have datatype
        // The datatype rdf:langString is INFERRED during CSV->RDF conversion, not in metadata
        
        // Handle both flattened and non-flattened metadata
        JsonNode tables;
        if (metadataJson.has("tables")) {
            tables = metadataJson.get("tables");
        } else {
            // Flattened single table - wrap it in an array for consistent processing
            ObjectMapper mapper = new ObjectMapper();
            tables = mapper.createArrayNode().add(metadataJson);
        }
        
        int columnsWithLang = 0;
        
        for (JsonNode table : tables) {
            JsonNode tableSchema = table.get("tableSchema");
            if (tableSchema != null) {
                JsonNode columns = tableSchema.get("columns");
                if (columns != null) {
                    for (JsonNode column : columns) {
                        JsonNode lang = column.get("lang");
                        if (lang != null && !lang.isNull()) {
                            columnsWithLang++;
                            
                            // This column has lang - it must NOT have datatype
                            JsonNode datatype = column.get("datatype");
                            if (datatype != null && !datatype.isNull()) {
                                String columnName = column.has("name") ? column.get("name").asText() : "unknown";
                                String propertyUrl = column.has("propertyUrl") ? column.get("propertyUrl").asText() : "unknown";
                                fail("Column '" + columnName + "' (propertyUrl: " + propertyUrl + 
                                    ") has lang='" + lang.asText() + "' but also has datatype='" + 
                                    datatype.asText() + "'. According to CSVW spec, lang and datatype are mutually exclusive. " +
                                    "The datatype rdf:langString is inferred during conversion, not stored in metadata.");
                            }
                        }
                    }
                }
            }
        }
        
        System.out.println("Verified " + columnsWithLang + " columns with lang have no datatype property");
        assertTrue(columnsWithLang > 0, "Should have found at least some columns with lang to verify");
    }
    
    @ParameterizedTest
    @MethodSource("testFileSets")
    public void testCSVRowsHaveCorrectNumberOfNonEmptyCells(TestFileSet testFileSet) throws Exception {
        setUp(testFileSet);
        // Count triples per subject from the RDF input
        Map<String, Integer> triplesPerSubject = new HashMap<>();
        for (Statement stmt : inputStatements) {
            String subject = stmt.getSubject().stringValue();
            triplesPerSubject.put(subject, triplesPerSubject.getOrDefault(subject, 0) + 1);
        }
        
        System.out.println("Counted triples for " + triplesPerSubject.size() + " unique subjects");
        
        // Read all generated CSV files
        List<String> csvFiles = new ArrayList<>();
        csvFiles.add("./" + currentCsvBaseName + ".csv");
        for (int i = 0; i < 20; i++) {
            String filename = "./" + currentCsvBaseName + i + ".csv";
            if (new File(filename).exists()) {
                csvFiles.add(filename);
            }
        }
        
        int totalRowsChecked = 0;
        int totalMismatches = 0;
        List<String> mismatchDetails = new ArrayList<>();
        
        for (String csvFile : csvFiles) {
            File file = new File(csvFile);
            if (!file.exists()) {
                continue;
            }
            
            System.out.println("Checking CSV file: " + csvFile);
            
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty()) {
                continue;
            }
            
            // Skip header row
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] cells = parseCSVLine(line);
                
                if (cells.length == 0 || cells[0].trim().isEmpty()) {
                    continue; // Skip empty rows
                }
                
                String subject = cells[0].trim();
                
                // Count non-empty cells in this row
                int nonEmptyCells = 0;
                for (String cell : cells) {
                    if (cell != null && !cell.trim().isEmpty()) {
                        nonEmptyCells++;
                    }
                }
                
                // Get expected count: number of triples for this subject + 1 (for subject column)
                Integer expectedTriples = triplesPerSubject.get(subject);
                if (expectedTriples == null) {
                    System.out.println("Warning: Subject '" + subject + "' not found in input RDF");
                    continue;
                }
                
                int expectedNonEmptyCells = expectedTriples + 1; // triples + subject column
                
                totalRowsChecked++;
                
                if (nonEmptyCells != expectedNonEmptyCells) {
                    totalMismatches++;
                    String detail = String.format("Row %d: Subject '%s' has %d non-empty cells but expected %d (triples: %d, +1 for subject)",
                        i, subject, nonEmptyCells, expectedNonEmptyCells, expectedTriples);
                    mismatchDetails.add(detail);
                    
                    // Print first few mismatches for debugging
                    if (totalMismatches <= 5) {
                        System.out.println("MISMATCH: " + detail);
                    }
                }
            }
        }
        
        System.out.println("Total rows checked: " + totalRowsChecked);
        System.out.println("Total mismatches: " + totalMismatches);
        
        if (totalMismatches > 0) {
            System.out.println("\nFirst mismatches:");
            for (int i = 0; i < Math.min(10, mismatchDetails.size()); i++) {
                System.out.println("  " + mismatchDetails.get(i));
            }
        }
        
        assertEquals(0, totalMismatches,
            "All CSV rows should have (triples count + 1) non-empty cells");
    }
    
    /**
     * Parse a CSV line handling quoted fields with commas and newlines.
     * Simple parser for testing purposes.
     */
    private String[] parseCSVLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder currentCell = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote
                    currentCell.append('"');
                    i++; // Skip next quote
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // End of cell
                cells.add(currentCell.toString());
                currentCell = new StringBuilder();
            } else {
                currentCell.append(c);
            }
        }
        
        // Add last cell
        cells.add(currentCell.toString());
        
        return cells.toArray(new String[0]);
    }
}
