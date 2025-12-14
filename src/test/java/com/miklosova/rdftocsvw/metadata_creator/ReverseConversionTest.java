package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.Main;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReverseConversionTest {

    static class TestFileSet {
        final String inputFile;
        final String inputFileForConversion;
        final String metadataOutputFile;
        final String csvBaseName;
        final String reconvertedRdfFile;
        final String csv2rdfJarPath;

        TestFileSet(String inputFile, String inputFileForConversion, String metadataOutputFile, 
                    String csvBaseName, String reconvertedRdfFile, String csv2rdfJarPath) {
            this.inputFile = inputFile;
            this.inputFileForConversion = inputFileForConversion;
            this.metadataOutputFile = metadataOutputFile;
            this.csvBaseName = csvBaseName;
            this.reconvertedRdfFile = reconvertedRdfFile;
            this.csv2rdfJarPath = csv2rdfJarPath;
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
            new TestFileSet(
                "./src/test/resources/StreamingNTriples/restaurantTest.nt",
                "./RDFtoCSV/src/test/resources/StreamingNTriples/restaurantTest.nt",
                "./restaurantTest.csv-metadata.json",
                "restaurantTest",
                "./restaurantTest-reconverted.ttl",
                csv2rdfPath
            ),
            new TestFileSet(
                "./src/test/resources/StreamingNTriples/companyTest.nt",
                "./RDFtoCSV/src/test/resources/StreamingNTriples/companyTest.nt",
                "./companyTest.csv-metadata.json",
                "companyTest",
                "./companyTest-reconverted.ttl",
                csv2rdfPath
            ),
            new TestFileSet(
                "./src/test/resources/StreamingNTriples/literalListTest.nt",
                "./RDFtoCSV/src/test/resources/StreamingNTriples/literalListTest.nt",
                "./literalListTest.csv-metadata.json",
                "literalListTest",
                "./literalListTest-reconverted.ttl",
                csv2rdfPath
            )
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
        AppConfig config = new AppConfig.Builder(testFileSet.inputFileForConversion)
            .parsing("streaming")
            .build();
        Main.main(new String[]{"-f", testFileSet.inputFileForConversion, "-p", "streaming"});
        
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
        
        System.out.println("Converting CSV+Metadata back to RDF using csv2rdf...");
        convertCsvToRdfUsingCsv2rdf(
            testFileSet.csv2rdfJarPath,
            testFileSet.metadataOutputFile,
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
     * Convert CSV+Metadata to RDF using the csv2rdf tool
     */
    private void convertCsvToRdfUsingCsv2rdf(String csv2rdfJarPath, String metadataPath, String outputPath) 
            throws IOException, InterruptedException {
        
        File outputFile = new File(outputPath);
        if (outputFile.exists()) {
            outputFile.delete();
        }
        
        // Run csv2rdf: java -jar csv2rdf.jar -u metadata.json -o output.ttl -m minimal
        // -m minimal: only translate what is given in the metadata, no extra triples
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
     * Compare two RDF graphs, accounting for blank node renaming
     */
    private void compareRdfGraphs(List<Statement> original, List<Statement> reconverted) {
        // Create normalized representations that ignore blank node identifiers
        Set<NormalizedTriple> originalNormalized = normalizeStatements(original);
        Set<NormalizedTriple> reconvertedNormalized = normalizeStatements(reconverted);
        
        System.out.println("Original normalized triples: " + originalNormalized.size());
        System.out.println("Reconverted normalized triples: " + reconvertedNormalized.size());
        
        // Find missing triples (from original not in reconverted)
        Set<NormalizedTriple> missing = new HashSet<>(originalNormalized);
        missing.removeAll(reconvertedNormalized);
        
        // Find extra triples (in reconverted but not in original)
        Set<NormalizedTriple> extra = new HashSet<>(reconvertedNormalized);
        extra.removeAll(originalNormalized);
        
        // Report differences
        if (!missing.isEmpty()) {
            System.out.println("\n⚠ Missing triples (present in original but not in reconverted):");
            missing.stream().limit(10).forEach(t -> System.out.println("  - " + t));
            if (missing.size() > 10) {
                System.out.println("  ... and " + (missing.size() - 10) + " more");
            }
        }
        
        if (!extra.isEmpty()) {
            System.out.println("\n⚠ Extra triples (present in reconverted but not in original):");
            extra.stream().limit(10).forEach(t -> System.out.println("  + " + t));
            if (extra.size() > 10) {
                System.out.println("  ... and " + (extra.size() - 10) + " more");
            }
        }
        
        // The reconverted graph should contain at least all the original triples
        // (it might have a few extra metadata triples, but all data should be there)
        assertTrue(missing.isEmpty(), 
            "Reconverted RDF is missing " + missing.size() + " triples from the original");
        
        // Allow some extra triples (CSVW might add metadata), but warn if too many
        if (extra.size() > originalNormalized.size() * 0.1) { // More than 10% extra
            System.out.println("⚠ Warning: Reconverted RDF has significantly more triples than original");
        }
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
