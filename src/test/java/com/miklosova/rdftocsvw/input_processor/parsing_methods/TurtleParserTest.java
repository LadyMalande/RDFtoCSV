package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.BeforeEach;
import java.io.FileWriter;
import java.nio.file.Path;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.io.File;
import org.mockito.Mock;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.rdf4j.rio.RDFFormat;
import static org.mockito.Mockito.*;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class TurtleParserTest {

    @Mock
    private RepositoryConnection mockConnection;

    @TempDir
    Path tempDir;

    private TurtleParser turtleParser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        turtleParser = new TurtleParser();
        mockConnection = mock(RepositoryConnection.class);
    }

    //BaseRock generated method id: ${testProcessInputSuccessful}, hash: C8B959BB6C26F42274BA643D6E8216A2
    @Test
    @Disabled
    void testProcessInputSuccessful() throws Exception {
        File testFile = createTempFile("test.ttl", "<subject> <predicate> <object> .");
        RepositoryConnection result = turtleParser.processInput(mockConnection, testFile);
        verify(mockConnection).add(any(java.io.InputStream.class), eq(""), eq(RDFFormat.TURTLE));
        assertEquals(mockConnection, result);
    }

    //BaseRock generated method id: ${testProcessInputWithRDFParseException}, hash: 10E4D1E54D41ED791599B142961BFAC5
    @Test
    @Disabled
    void testProcessInputWithRDFParseException() throws Exception {
        File testFile = createTempFile("test.ttl", "<subject> <predicate> <object> .");
        doThrow(RDFParseException.class).doNothing().when(mockConnection).add(any(java.io.InputStream.class), eq(""), eq(RDFFormat.TURTLE));
        RepositoryConnection result = turtleParser.processInput(mockConnection, testFile);
        verify(mockConnection, times(2)).add(any(java.io.InputStream.class), eq(""), eq(RDFFormat.TURTLE));
        assertEquals(mockConnection, result);
    }

    //BaseRock generated method id: ${testProcessInputWithIOException}, hash: 37DA087F88CD1BD987A1607062E8B01D
    @Test
    void testProcessInputWithIOException() throws Exception {
        File nonExistentFile = new File(tempDir.toFile(), "non_existent.ttl");
        assertThrows(RuntimeException.class, () -> turtleParser.processInput(mockConnection, nonExistentFile));
    }

    //BaseRock generated method id: ${testProcessInputWithFileNotFoundException}, hash: 4DDFAA2C2B400A08ABD51D8FDD5A70D3
    @Test
    void testProcessInputWithFileNotFoundException() throws Exception {
        File testFile = createTempFile("test.ttl", "<subject> <predicate> <object> .");
        doThrow(RDFParseException.class).when(mockConnection).add(any(java.io.InputStream.class), eq(""), eq(RDFFormat.TURTLE));
        // Delete the file to cause FileNotFoundException
        testFile.delete();
        assertThrows(RuntimeException.class, () -> turtleParser.processInput(mockConnection, testFile));
    }

    private File createTempFile(String fileName, String content) throws IOException {
        File file = new File(tempDir.toFile(), fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }
}
