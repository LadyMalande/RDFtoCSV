package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BigFileStreamingNTriplesMetadataCreatorTest extends BaseTest {

    @Mock
    private PrefinishedOutput<RowsAndKeys> mockData;

    private AppConfig config;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConstructorWithConfig() throws IOException {
        Path testFile = createSimpleNTriplesFile();
        AppConfig testConfig = createConfig(testFile, "output.csv");
        
        BigFileStreamingNTriplesMetadataCreator creator = 
            new BigFileStreamingNTriplesMetadataCreator(mockData, testConfig);
        assertNotNull(creator);
        assertNotNull(creator.metadata);
    }

    @Test
    void testConstructorWithNullConfig() {
        BigFileStreamingNTriplesMetadataCreator creator = 
            new BigFileStreamingNTriplesMetadataCreator(mockData, null);
        assertNotNull(creator);
        assertNotNull(creator.metadata);
    }

    @Test
    @SuppressWarnings("deprecation")
    void testDeprecatedConstructor() {
        BigFileStreamingNTriplesMetadataCreator creator = 
            new BigFileStreamingNTriplesMetadataCreator(mockData);
        assertNotNull(creator);
        assertNotNull(creator.metadata);
    }

    @Test
    void testImplementsInterface() throws IOException {
        Path testFile = createSimpleNTriplesFile();
        AppConfig testConfig = createConfig(testFile, "output.csv");
        
        BigFileStreamingNTriplesMetadataCreator creator = 
            new BigFileStreamingNTriplesMetadataCreator(mockData, testConfig);
        assertTrue(creator instanceof IMetadataCreator);
    }

    @Test
    void testExtendsStreamingMetadataCreator() throws IOException {
        Path testFile = createSimpleNTriplesFile();
        AppConfig testConfig = createConfig(testFile, "output.csv");
        
        BigFileStreamingNTriplesMetadataCreator creator = 
            new BigFileStreamingNTriplesMetadataCreator(mockData, testConfig);
        assertTrue(creator instanceof StreamingMetadataCreator);
    }

    @Test
    void testCounterInitialValue() throws IOException {
        Path testFile = createSimpleNTriplesFile();
        AppConfig testConfig = createConfig(testFile, "output.csv");
        
        BigFileStreamingNTriplesMetadataCreator creator = 
            new BigFileStreamingNTriplesMetadataCreator(mockData, testConfig);
        assertEquals(0, creator.counter);
    }
    
    @Test
    void testAddMetadataWithSmallFile() throws IOException {
        Path testFile = createSimpleNTriplesFile();
        AppConfig testConfig = createConfig(testFile, "output.csv");
        
        BigFileStreamingNTriplesMetadataCreator creator = 
            new BigFileStreamingNTriplesMetadataCreator(mockData, testConfig);
        
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertNotNull(result.getTables());
        assertFalse(result.getTables().isEmpty());
        assertEquals(1, result.getTables().size());
    }
    
    @Test
    void testAddMetadataWithEmptyFile() throws IOException {
        Path testFile = tempDir.resolve("empty.nt");
        Files.createFile(testFile);
        
        AppConfig testConfig = createConfig(testFile, "output.csv");
        
        BigFileStreamingNTriplesMetadataCreator creator = 
            new BigFileStreamingNTriplesMetadataCreator(mockData, testConfig);
        
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
    }
    
    @Test
    void testAddMetadataWithComments() throws IOException {
        Path testFile = tempDir.resolve("comments.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("# This is a comment\n");
            writer.write("\n");
            writer.write("<http://example.org/subject1> <http://example.org/predicate1> \"Object1\" .\n");
            writer.write("# Another comment\n");
            writer.write("<http://example.org/subject2> <http://example.org/predicate1> \"Object2\" .\n");
        }
        
        AppConfig testConfig = createConfig(testFile, "output.csv");
        
        BigFileStreamingNTriplesMetadataCreator creator = 
            new BigFileStreamingNTriplesMetadataCreator(mockData, testConfig);
        
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertTrue(creator.counter >= 2); // Should process the 2 valid triples
    }
    
    @Test
    void testMetadataNotNull() throws IOException {
        Path testFile = createSimpleNTriplesFile();
        AppConfig testConfig = createConfig(testFile, "output.csv");
        
        BigFileStreamingNTriplesMetadataCreator creator = 
            new BigFileStreamingNTriplesMetadataCreator(mockData, testConfig);
        assertNotNull(creator.metadata);
        assertNotNull(creator.metadata.getTables());
    }
    
    @Test
    void testOutputFileNameFromConfig() throws IOException {
        Path testFile = createSimpleNTriplesFile();
        AppConfig testConfig = createConfig(testFile, "custom_output.csv");
        
        BigFileStreamingNTriplesMetadataCreator creator = 
            new BigFileStreamingNTriplesMetadataCreator(mockData, testConfig);
        
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertEquals(1, result.getTables().size());
        assertTrue(result.getTables().get(0).getUrl().contains("custom_output"));
    }
    
    @Test
    void testMultiplePredicates() throws IOException {
        Path testFile = tempDir.resolve("multiple_predicates.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("<http://example.org/subject1> <http://example.org/name> \"Alice\" .\n");
            writer.write("<http://example.org/subject1> <http://example.org/age> \"30\" .\n");
            writer.write("<http://example.org/subject1> <http://example.org/email> \"alice@example.org\" .\n");
            writer.write("<http://example.org/subject2> <http://example.org/name> \"Bob\" .\n");
            writer.write("<http://example.org/subject2> <http://example.org/age> \"25\" .\n");
        }
        
        AppConfig testConfig = createConfig(testFile, "output.csv");
        
        BigFileStreamingNTriplesMetadataCreator creator = 
            new BigFileStreamingNTriplesMetadataCreator(mockData, testConfig);
        
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertTrue(creator.counter >= 5);
        assertFalse(result.getTables().isEmpty());
    }
    
    @Test
    void testBlankNodes() throws IOException {
        Path testFile = tempDir.resolve("blank_nodes.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("_:b1 <http://example.org/name> \"Anonymous\" .\n");
            writer.write("_:b1 <http://example.org/age> \"Unknown\" .\n");
            writer.write("<http://example.org/person1> <http://example.org/friend> _:b1 .\n");
        }
        
        AppConfig testConfig = createConfig(testFile, "output.csv");
        
        BigFileStreamingNTriplesMetadataCreator creator = 
            new BigFileStreamingNTriplesMetadataCreator(mockData, testConfig);
        
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertTrue(creator.counter >= 3);
    }
    
    @Test
    void testUnicodeCharacters() throws IOException {
        Path testFile = tempDir.resolve("unicode.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("<http://example.org/osoba1> <http://example.org/jméno> \"Petr Dvořák\" .\n");
            writer.write("<http://example.org/osoba1> <http://example.org/místo> \"Brno\" .\n");
            writer.write("<http://example.org/osoba2> <http://example.org/jméno> \"Größe\" .\n");
        }
        
        AppConfig testConfig = createConfig(testFile, "output.csv");
        
        BigFileStreamingNTriplesMetadataCreator creator = 
            new BigFileStreamingNTriplesMetadataCreator(mockData, testConfig);
        
        Metadata result = creator.addMetadata(mockData);
        
        assertNotNull(result);
        assertTrue(creator.counter >= 3);
    }
    
    // Helper methods
    
    private Path createSimpleNTriplesFile() throws IOException {
        Path testFile = tempDir.resolve("test.nt");
        try (BufferedWriter writer = Files.newBufferedWriter(testFile)) {
            writer.write("<http://example.org/subject1> <http://example.org/predicate1> \"Object1\" .\n");
            writer.write("<http://example.org/subject1> <http://example.org/predicate2> \"Object2\" .\n");
            writer.write("<http://example.org/subject2> <http://example.org/predicate1> \"Object3\" .\n");
        }
        return testFile;
    }
    
    private AppConfig createConfig(Path inputFile, String outputFileName) {
        Path outputPath = tempDir.resolve(outputFileName);
        AppConfig testConfig = new AppConfig.Builder(inputFile.toString())
                .parsing("bigFileStreaming")
                .output(outputPath.toString())
                .build();
        return testConfig;
    }
}