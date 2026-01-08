package com.miklosova.rdftocsvw.converter;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.Row;
import com.miklosova.rdftocsvw.converter.data_structure.RowAndKey;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.*;
import com.miklosova.rdftocsvw.output_processor.FinalizedOutput;
import com.miklosova.rdftocsvw.support.AppConfig;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test coverage for:
 * - writeToString(PrefinishedOutput, Metadata)
 * - getCSVTableAsFile()
 * - writeToStringTrivial(PrefinishedOutput)
 * - getCSVTableAsString()
 */
class RDFtoCSVMethodsTest {

    private Path testDir;

    private AppConfig config;
    private RDFtoCSV rdfToCSV;
    private Repository db;
    private ValueFactory vf;

    @BeforeEach
    void setUp() throws Exception {
        // Create test directory in target folder (gets cleaned by Maven)
        testDir = Paths.get("target", "test-output", "RDFtoCSVMethodsTest-" + System.currentTimeMillis());
        Files.createDirectories(testDir);

        vf = SimpleValueFactory.getInstance();
        db = new SailRepository(new MemoryStore());
        db.init();

        // Create a default config
        config = new AppConfig.Builder(testDir.resolve("test.nt").toString()).build();
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);
    }

    /**
     * Helper method to initialize the db field in an RDFtoCSV instance
     */
    private void initializeDbField(RDFtoCSV instance) throws Exception {
        Field dbField = RDFtoCSV.class.getDeclaredField("db");
        dbField.setAccessible(true);
        dbField.set(instance, db);
    }

    @AfterEach
    void tearDown() {
        // Shutdown the repository to release file locks
        if (db != null && db.isInitialized()) {
            try {
                db.shutDown();
            } catch (Exception e) {
                // Ignore shutdown errors in cleanup
            }
        }

        // Clean up test directory - best effort, files may remain locked on Windows
        if (testDir != null && Files.exists(testDir)) {
            try {
                Thread.sleep(100); // Brief delay to allow file handles to close
                Files.walk(testDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // Ignore - file may be locked, Maven will clean target folder
                        }
                    });
            } catch (Exception e) {
                // Ignore cleanup errors - Maven will clean target folder
            }
        }
    }

    // ==================== writeToStringTrivial Tests ====================

    @Test
    void testWriteToStringTrivial_WithRowAndKey() throws Exception {
        // Create test data
        ArrayList<Value> keys = new ArrayList<>();
        keys.add(vf.createIRI("http://example.org/name"));
        keys.add(vf.createIRI("http://example.org/age"));

        ArrayList<Row> rows = new ArrayList<>();
        Row row1 = new Row(vf.createIRI("http://example.org/person1"), null, false);
        Row row2 = new Row(vf.createIRI("http://example.org/person2"), null, false);
        rows.add(row1);
        rows.add(row2);

        RowAndKey rnk = new RowAndKey(keys, rows);
        PrefinishedOutput<RowAndKey> po = new PrefinishedOutput<>(rnk);

        // Use reflection to call private method
        String result = invokeWriteToStringTrivial(rdfToCSV, po);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("John") || result.contains("name"));
    }

    @Test
    void testWriteToStringTrivial_WithRowsAndKeys() throws Exception {
        // Create test data with RowsAndKeys
        ArrayList<Value> keys = new ArrayList<>();
        keys.add(vf.createIRI("http://example.org/prop1"));

        ArrayList<Row> rows = new ArrayList<>();
        Row row = new Row(vf.createIRI("http://example.org/s1"), null, false);
        rows.add(row);

        RowAndKey rnk = new RowAndKey(keys, rows);
        RowsAndKeys rnks = new RowsAndKeys();
        rnks.getRowsAndKeys().add(rnk);

        PrefinishedOutput<RowsAndKeys> po = new PrefinishedOutput<>(rnks);

        config = new AppConfig.Builder(testDir.resolve("test.nt").toString()).build();
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);

        String result = invokeWriteToStringTrivial(rdfToCSV, po);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testWriteToStringTrivial_EmptyRows() throws Exception {
        ArrayList<Value> keys = new ArrayList<>();
        keys.add(vf.createIRI("http://example.org/prop"));

        ArrayList<Row> rows = new ArrayList<>();

        RowAndKey rnk = new RowAndKey(keys, rows);
        PrefinishedOutput<RowAndKey> po = new PrefinishedOutput<>(rnk);

        config = new AppConfig.Builder(testDir.resolve("test.nt").toString()).build();
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);

        String result = invokeWriteToStringTrivial(rdfToCSV, po);

        assertNotNull(result);
        // Should at least have header
    }

    @Test
    void testWriteToStringTrivial_MultipleColumns() throws Exception {
        ArrayList<Value> keys = new ArrayList<>();
        keys.add(vf.createIRI("http://example.org/name"));
        keys.add(vf.createIRI("http://example.org/age"));
        keys.add(vf.createIRI("http://example.org/email"));

        ArrayList<Row> rows = new ArrayList<>();
        Row row = new Row(vf.createIRI("http://example.org/alice"), null, false);
        rows.add(row);

        RowAndKey rnk = new RowAndKey(keys, rows);
        PrefinishedOutput<RowAndKey> po = new PrefinishedOutput<>(rnk);

        config = new AppConfig.Builder(testDir.resolve("test.nt").toString()).build();
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);

        String result = invokeWriteToStringTrivial(rdfToCSV, po);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testWriteToStringTrivial_InvalidType() throws Exception {
        // Create an invalid PrefinishedOutput with a String type
        PrefinishedOutput<String> po = new PrefinishedOutput<>("invalid");

        config = new AppConfig.Builder(testDir.resolve("test.nt").toString()).build();
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);

        assertThrows(Exception.class, () -> {
            invokeWriteToStringTrivial(rdfToCSV, po);
        });
    }

    // ==================== writeToString Tests ====================

    @Test
    void testWriteToString_WithValidData() throws Exception {
        // Create test data
        ArrayList<Value> keys = new ArrayList<>();
        keys.add(vf.createIRI("http://example.org/name"));

        ArrayList<Row> rows = new ArrayList<>();
        Row row = new Row(vf.createIRI("http://example.org/s1"), null, false);
        rows.add(row);

        RowAndKey rnk = new RowAndKey(keys, rows);
        RowsAndKeys rnks = new RowsAndKeys();
        rnks.getRowsAndKeys().add(rnk);

        PrefinishedOutput<RowsAndKeys> po = new PrefinishedOutput<>(rnks);

        File outputFile = testDir.resolve("output.nt").toFile();
        File csvFile = testDir.resolve("output.csv").toFile();
        config = new AppConfig.Builder(outputFile.getAbsolutePath())
                .output(csvFile.getAbsolutePath())
                .build();
        config.setIntermediateFileNames(csvFile.getAbsolutePath());
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);

        // Create metadata with matching table name
        Metadata metadata = createTestMetadataForFile(csvFile.getName());

        String result = invokeWriteToString(rdfToCSV, po, metadata);

        assertNotNull(result);
    }

    @Test
    void testWriteToString_WithNullPrefinishedOutput() throws Exception {
        Metadata metadata = createTestMetadata();

        File outputFile = testDir.resolve("output.nt").toFile();
        config = new AppConfig.Builder(outputFile.getAbsolutePath())
                .build();
        config.setConversionMethod("streaming");
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);

        String result = invokeWriteToString(rdfToCSV, null, metadata);

        assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    void testWriteToString_MultipleRowsAndKeys() throws Exception {
        // Create first RowAndKey
        ArrayList<Value> keys1 = new ArrayList<>();
        keys1.add(vf.createIRI("http://example.org/prop1"));

        ArrayList<Row> rows1 = new ArrayList<>();
        Row row1 = new Row(vf.createIRI("http://example.org/s1"), null, false);
        rows1.add(row1);

        RowAndKey rnk1 = new RowAndKey(keys1, rows1);

        // Create second RowAndKey
        ArrayList<Value> keys2 = new ArrayList<>();
        keys2.add(vf.createIRI("http://example.org/prop2"));

        ArrayList<Row> rows2 = new ArrayList<>();
        Row row2 = new Row(vf.createIRI("http://example.org/s2"), null, false);
        rows2.add(row2);

        RowAndKey rnk2 = new RowAndKey(keys2, rows2);

        RowsAndKeys rnks = new RowsAndKeys();
        rnks.getRowsAndKeys().add(rnk1);
        rnks.getRowsAndKeys().add(rnk2);

        PrefinishedOutput<RowsAndKeys> po = new PrefinishedOutput<>(rnks);

        File outputFile = testDir.resolve("output.nt").toFile();
        File csv1 = testDir.resolve("output0.csv").toFile();
        File csv2 = testDir.resolve("output1.csv").toFile();

        config = new AppConfig.Builder(outputFile.getAbsolutePath())
                .output(testDir.resolve("output.csv").toString())
                .build();
        config.setIntermediateFileNames(csv1.getAbsolutePath() + "," + csv2.getAbsolutePath());
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);

        // Create metadata with tables matching CSV file names
        Metadata metadata = createTestMetadataForMultipleFiles(csv1.getName(), csv2.getName());

        String result = invokeWriteToString(rdfToCSV, po, metadata);

        assertNotNull(result);
    }

    // ==================== getCSVTableAsString Tests ====================

    @Test
    void testGetCSVTableAsString_TrivialConversion() throws Exception {
        // Create a simple N-Triples file
        File inputFile = testDir.resolve("test.nt").toFile();
        String ntriples = "<http://example.org/s> <http://example.org/p> \"object\" .\n";
        Files.writeString(inputFile.toPath(), ntriples);

        config = new AppConfig.Builder(inputFile.getAbsolutePath())
                .build();
        config.setConversionMethod("trivial");
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);

        String result = rdfToCSV.getCSVTableAsString();

        assertNotNull(result);
    }

    @Test
    void testGetCSVTableAsString_WithValidInput() throws Exception {
        // Create a simple N-Triples file
        File inputFile = testDir.resolve("test.nt").toFile();
        String ntriples = "<http://example.org/subject1> <http://example.org/name> \"John\" .\n" +
                         "<http://example.org/subject1> <http://example.org/age> \"30\" .\n";
        Files.writeString(inputFile.toPath(), ntriples);

        config = new AppConfig.Builder(inputFile.getAbsolutePath())
                .parsing("rdf4j")
                .build();
        config.setConversionMethod("basicQuery");
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);

        String result = rdfToCSV.getCSVTableAsString();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetCSVTableAsString_EmptyFile() throws Exception {
        File inputFile = testDir.resolve("empty.nt").toFile();
        Files.writeString(inputFile.toPath(), "");

        config = new AppConfig.Builder(inputFile.getAbsolutePath())
                .parsing("rdf4j")
                .build();
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);

        // Empty files throw RuntimeException
        assertThrows(RuntimeException.class, () -> rdfToCSV.getCSVTableAsString());
    }

    @Test
    void testGetCSVTableAsString_MultipleTriples() throws Exception {
        File inputFile = testDir.resolve("multiple.nt").toFile();
        String ntriples = "<http://example.org/s1> <http://example.org/name> \"Alice\" .\n" +
                         "<http://example.org/s2> <http://example.org/name> \"Bob\" .\n" +
                         "<http://example.org/s1> <http://example.org/age> \"25\" .\n";
        Files.writeString(inputFile.toPath(), ntriples);

        config = new AppConfig.Builder(inputFile.getAbsolutePath())
                .parsing("rdf4j")
                .build();
        config.setConversionMethod("basicQuery");
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);

        String result = rdfToCSV.getCSVTableAsString();

        assertNotNull(result);
        assertTrue(result.contains("Alice") || result.contains("Bob"));
    }

    // ==================== getCSVTableAsFile Tests ====================

    @Test
    void testGetCSVTableAsFile_CreatesFile() throws Exception {
        File inputFile = testDir.resolve("test.nt").toFile();
        String ntriples = "<http://example.org/s> <http://example.org/p> \"value\" .\n";
        Files.writeString(inputFile.toPath(), ntriples);

        config = new AppConfig.Builder(inputFile.getAbsolutePath())
                .parsing("rdf4j")
                .output(testDir.resolve("output.csv").toString())
                .build();
        config.setConversionMethod("basicQuery");

        rdfToCSV = new RDFtoCSV(config);


        initializeDbField(rdfToCSV);

        FinalizedOutput<byte[]> result = rdfToCSV.getCSVTableAsFile();

        assertNotNull(result);
        assertNotNull(result.getOutputData());
        assertTrue(result.getOutputData().length > 0);
    }

    @Test
    void testGetCSVTableAsFile_ByteArrayContent() throws Exception {
        File inputFile = testDir.resolve("test.nt").toFile();
        String ntriples = "<http://example.org/person1> <http://example.org/name> \"John Doe\" .\n";
        Files.writeString(inputFile.toPath(), ntriples);

        config = new AppConfig.Builder(inputFile.getAbsolutePath())
                .parsing("rdf4j")
                .output(testDir.resolve("output.csv").toString())
                .build();
        config.setConversionMethod("basicQuery");
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);

        FinalizedOutput<byte[]> result = rdfToCSV.getCSVTableAsFile();

        assertNotNull(result.getOutputData());
        String content = new String(result.getOutputData());
        assertFalse(content.isEmpty());
    }

    @Test
    void testGetCSVTableAsFile_WithUrlFileName() throws Exception {
        // Test handling of URL-like filenames (without actual download)
        File inputFile = testDir.resolve("url_test.nt").toFile();
        String ntriples = "<http://example.org/s> <http://example.org/p> \"test\" .\n";
        Files.writeString(inputFile.toPath(), ntriples);

        config = new AppConfig.Builder(inputFile.getAbsolutePath())
                .parsing("rdf4j")
                .output(testDir.resolve("output.csv").toString())
                .build();
        config.setConversionMethod("basicQuery");
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);

        FinalizedOutput<byte[]> result = rdfToCSV.getCSVTableAsFile();

        assertNotNull(result);
        assertNotNull(result.getOutputData());
    }

    @Test
    void testGetCSVTableAsFile_LargeContent() throws Exception {
        File inputFile = testDir.resolve("large.nt").toFile();
        StringBuilder ntriples = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            ntriples.append("<http://example.org/s").append(i).append("> ")
                    .append("<http://example.org/p> ")
                    .append("\"value").append(i).append("\" .\n");
        }
        Files.writeString(inputFile.toPath(), ntriples.toString());

        config = new AppConfig.Builder(inputFile.getAbsolutePath())
                .parsing("rdf4j")
                .output(testDir.resolve("output.csv").toString())
                .build();
        config.setConversionMethod("basicQuery");
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);

        FinalizedOutput<byte[]> result = rdfToCSV.getCSVTableAsFile();

        assertNotNull(result);
        assertTrue(result.getOutputData().length > 100);
    }

    @Test
    void testGetCSVTableAsFile_FileExists() throws Exception {
        File inputFile = testDir.resolve("test.nt").toFile();
        String ntriples = "<http://example.org/s> <http://example.org/p> \"value\" .\n";
        Files.writeString(inputFile.toPath(), ntriples);

        config = new AppConfig.Builder(inputFile.getAbsolutePath())
                .parsing("rdf4j")
                .output(testDir.resolve("output.csv").toString())
                .build();
        config.setConversionMethod("basicQuery");
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);

        FinalizedOutput<byte[]> result = rdfToCSV.getCSVTableAsFile();

        // Check that result contains data
        assertNotNull(result);
        assertNotNull(result.getOutputData());
        assertTrue(result.getOutputData().length > 0);
    }

    // ==================== Edge Cases and Integration Tests ====================

    @Test
    void testGetCSVTableAsString_SpecialCharacters() throws Exception {
        File inputFile = testDir.resolve("special.nt").toFile();
        String ntriples = "<http://example.org/s> <http://example.org/p> \"Value with \\\"quotes\\\" and \\nnewlines\" .\n";
        Files.writeString(inputFile.toPath(), ntriples);

        config = new AppConfig.Builder(inputFile.getAbsolutePath())
                .parsing("rdf4j")
                .build();
        config.setConversionMethod("basicQuery");
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);

        String result = rdfToCSV.getCSVTableAsString();

        assertNotNull(result);
    }

    @Test
    void testGetCSVTableAsString_UnicodeCharacters() throws Exception {
        File inputFile = testDir.resolve("unicode.nt").toFile();
        String ntriples = "<http://example.org/s> <http://example.org/název> \"Český text\" .\n";
        java.nio.file.Files.writeString(inputFile.toPath(), ntriples, java.nio.charset.StandardCharsets.UTF_8);

        config = new AppConfig.Builder(inputFile.getAbsolutePath())
                .parsing("rdf4j")
                .build();
        config.setConversionMethod("basicQuery");
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);

        String result = rdfToCSV.getCSVTableAsString();
        System.out.println("Result content: " + result);
        assertNotNull(result);
        assertTrue(result.contains("Český") || result.contains("název"));
    }

    @Test
    void testWriteToString_WithSingleRowAndKey() throws Exception {
        ArrayList<Value> keys = new ArrayList<>();
        keys.add(vf.createIRI("http://example.org/single"));

        ArrayList<Row> rows = new ArrayList<>();
        Row row = new Row(vf.createIRI("http://example.org/s1"), null, false);
        rows.add(row);

        RowAndKey rnk = new RowAndKey(keys, rows);
        PrefinishedOutput<RowAndKey> po = new PrefinishedOutput<>(rnk);

        File outputFile = testDir.resolve("single.nt").toFile();
        File csvFile = testDir.resolve("single.csv").toFile();

        config = new AppConfig.Builder(outputFile.getAbsolutePath())
                .output(csvFile.getAbsolutePath())
                .build();
        config.setIntermediateFileNames(csvFile.getAbsolutePath());
        rdfToCSV = new RDFtoCSV(config);

        initializeDbField(rdfToCSV);

        // Create metadata with matching table name
        Metadata metadata = createTestMetadataForFile(csvFile.getName());

        String result = invokeWriteToString(rdfToCSV, po, metadata);

        assertNotNull(result);
    }

    // ==================== Helper Methods ====================

    /**
     * Helper method to invoke private writeToStringTrivial using reflection
     */
    private String invokeWriteToStringTrivial(RDFtoCSV rdfToCSV, PrefinishedOutput<?> po) throws Exception {
        var method = RDFtoCSV.class.getDeclaredMethod("writeToStringTrivial", PrefinishedOutput.class);
        method.setAccessible(true);
        return (String) method.invoke(rdfToCSV, po);
    }

    /**
     * Helper method to invoke private writeToString using reflection
     */
    private String invokeWriteToString(RDFtoCSV rdfToCSV, PrefinishedOutput<?> po, Metadata metadata) throws Exception {
        var method = RDFtoCSV.class.getDeclaredMethod("writeToString", PrefinishedOutput.class, Metadata.class);
        method.setAccessible(true);
        return (String) method.invoke(rdfToCSV, po, metadata);
    }

    /**
     * Helper method to create test metadata
     */
    private Metadata createTestMetadata() {
        Metadata metadata = new Metadata();
        Table table = new Table("test.csv");
        TableSchema schema = new TableSchema();

        Column column = new Column();
        column.setName("testColumn");
        column.setTitles("Test Column");

        schema.getColumns().add(column);
        table.setTableSchema(schema);
        metadata.getTables().add(table);

        return metadata;
    }

    /**
     * Helper method to create test metadata for a specific file
     */
    private Metadata createTestMetadataForFile(String fileName) {
        Metadata metadata = new Metadata();
        Table table = new Table(fileName);
        TableSchema schema = new TableSchema();

        Column column = new Column();
        column.setName("testColumn");
        column.setTitles("Test Column");

        schema.getColumns().add(column);
        table.setTableSchema(schema);
        metadata.getTables().add(table);

        return metadata;
    }

    /**
     * Helper method to create test metadata for multiple files
     */
    private Metadata createTestMetadataForMultipleFiles(String... fileNames) {
        Metadata metadata = new Metadata();
        for (String fileName : fileNames) {
            Table table = new Table(fileName);
            TableSchema schema = new TableSchema();

            Column column = new Column();
            column.setName("testColumn");
            column.setTitles("Test Column");

            schema.getColumns().add(column);
            table.setTableSchema(schema);
            metadata.getTables().add(table);
        }
        return metadata;
    }
}
