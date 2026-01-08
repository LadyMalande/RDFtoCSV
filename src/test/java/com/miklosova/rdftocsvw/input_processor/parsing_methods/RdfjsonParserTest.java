package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RdfjsonParserTest {

    private RdfjsonParser rdfjsonParser;
    private Repository repository;
    private RepositoryConnection connection;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        repository = new SailRepository(new MemoryStore());
        repository.init();
        connection = repository.getConnection();
        rdfjsonParser = new RdfjsonParser();
    }

    @Test
    void testConstructor() {
        assertNotNull(rdfjsonParser);
    }

    @Test
    void testImplementsInterface() {
        assertTrue(rdfjsonParser instanceof IRDF4JParsingMethod);
    }

    @Test
    void testProcessInputWithValidRdfJsonFile() throws IOException {
        Path rdfJsonFile = tempDir.resolve("test.rj");
        String rdfJsonContent = """
            {
              "http://example.org/subject1": {
                "http://example.org/predicate1": [
                  { "type": "literal", "value": "object1" }
                ]
              }
            }
            """;
        Files.writeString(rdfJsonFile, rdfJsonContent);

        RepositoryConnection result = rdfjsonParser.processInput(connection, rdfJsonFile.toFile());
        
        assertNotNull(result);
        assertTrue(result.isOpen());
    }

    @Test
    void testProcessInputWithEmptyFile() throws IOException {
        Path emptyFile = tempDir.resolve("empty.rj");
        Files.writeString(emptyFile, "{}");

        RepositoryConnection result = rdfjsonParser.processInput(connection, emptyFile.toFile());
        
        assertNotNull(result);
    }

    @Test
    void testProcessInputWithNonExistentFile() {
        File nonExistent = new File(tempDir.toFile(), "nonexistent.rj");
        
        assertThrows(RuntimeException.class, () -> {
            rdfjsonParser.processInput(connection, nonExistent);
        });
    }

    @Test
    void testProcessInputWithMultipleTriples() throws IOException {
        Path rdfJsonFile = tempDir.resolve("multiple.rj");
        String content = """
            {
              "http://example.org/subject1": {
                "http://example.org/predicate1": [
                  { "type": "literal", "value": "object1" }
                ],
                "http://example.org/predicate2": [
                  { "type": "literal", "value": "object2" }
                ]
              }
            }
            """;
        Files.writeString(rdfJsonFile, content);

        RepositoryConnection result = rdfjsonParser.processInput(connection, rdfJsonFile.toFile());
        
        assertNotNull(result);
    }

    @Test
    void testProcessInputWithIRIObject() throws IOException {
        Path rdfJsonFile = tempDir.resolve("iri.rj");
        String content = """
            {
              "http://example.org/subject1": {
                "http://example.org/knows": [
                  { "type": "uri", "value": "http://example.org/subject2" }
                ]
              }
            }
            """;
        Files.writeString(rdfJsonFile, content);

        RepositoryConnection result = rdfjsonParser.processInput(connection, rdfJsonFile.toFile());
        
        assertNotNull(result);
    }

    @Test
    void testProcessInputWithLanguageTag() throws IOException {
        Path rdfJsonFile = tempDir.resolve("lang.rj");
        String content = """
            {
              "http://example.org/subject1": {
                "http://example.org/label": [
                  { "type": "literal", "value": "Hello", "lang": "en" },
                  { "type": "literal", "value": "Bonjour", "lang": "fr" }
                ]
              }
            }
            """;
        Files.writeString(rdfJsonFile, content);

        RepositoryConnection result = rdfjsonParser.processInput(connection, rdfJsonFile.toFile());
        
        assertNotNull(result);
    }

    @Test
    void testProcessInputWithDatatype() throws IOException {
        Path rdfJsonFile = tempDir.resolve("datatype.rj");
        String content = """
            {
              "http://example.org/subject1": {
                "http://example.org/age": [
                  { "type": "literal", "value": "25", "datatype": "http://www.w3.org/2001/XMLSchema#integer" }
                ]
              }
            }
            """;
        Files.writeString(rdfJsonFile, content);

        RepositoryConnection result = rdfjsonParser.processInput(connection, rdfJsonFile.toFile());
        
        assertNotNull(result);
    }
}
