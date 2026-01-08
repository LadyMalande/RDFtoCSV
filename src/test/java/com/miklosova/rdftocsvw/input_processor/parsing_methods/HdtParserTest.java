package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
class HdtParserTest {

    private Repository repository;
    private RepositoryConnection connection;
    private HdtParser parser;

    @BeforeEach
    void setUp() {
        repository = new SailRepository(new MemoryStore());
        repository.init();
        connection = repository.getConnection();
        parser = new HdtParser();
    }

    @AfterEach
    void tearDown() {
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
        if (repository != null && repository.isInitialized()) {
            repository.shutDown();
        }
    }

    @Test
    void testParserIsNotNull() {
        assertNotNull(parser);
    }

    @Test
    void testParserImplementsInterface() {
        assertTrue(parser instanceof IRDF4JParsingMethod);
    }

    @Test
    void testProcessInputWithNonExistentFile() {
        File nonExistentFile = new File("nonexistent_file_that_does_not_exist.hdt");
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            parser.processInput(connection, nonExistentFile);
        });
        
        assertNotNull(exception);
    }

    @Test
    void testProcessInputWithNullConnection() {
        File testFile = new File("test.hdt");
        
        assertThrows(Exception.class, () -> {
            parser.processInput(null, testFile);
        });
    }
}
