package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import java.io.FileInputStream;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class RdfxmlParserTest {

    private RdfxmlParser rdfxmlParser;

    @Mock
    private RepositoryConnection mockConnection;

    @Mock
    private File mockFile;

    @BeforeEach
    void setUp() {
        rdfxmlParser = new RdfxmlParser();
        mockFile = mock(File.class);
        mockConnection = mock(RepositoryConnection.class);
    }

    //BaseRock generated method id: ${processInput_validFile_success}, hash: 932B23AD8C380AE648F128670047EA94
    @Test
    void processInput_validFile_success() throws IOException {
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.isFile()).thenReturn(true);
        try (InputStream inputStream = new FileInputStream("./src/test/resources/test-001.rdf")) {
            when(mockFile.getPath()).thenReturn("./src/test/resources/test-001.rdf");
            doNothing().when(mockConnection).add(any(InputStream.class), eq(""), eq(RDFFormat.RDFXML));
            RepositoryConnection result = rdfxmlParser.processInput(mockConnection, mockFile);
            assertNotNull(result);
            assertEquals(mockConnection, result);
            verify(mockConnection).add(any(InputStream.class), eq(""), eq(RDFFormat.RDFXML));
        }
    }

    //BaseRock generated method id: ${processInput_fileNotFound_throwsRuntimeException}, hash: 28D21F95FB895C4D7F1DA838912EB478
    @Test
    void processInput_fileNotFound_throwsRuntimeException() {
        when(mockFile.exists()).thenReturn(false);
        assertThrows(RuntimeException.class, () -> rdfxmlParser.processInput(mockConnection, mockFile));
    }

    //BaseRock generated method id: ${processInput_ioExceptionOccurs_throwsRuntimeException}, hash: AD228B43B6409A9336017A163B36505A
    @Test
    void processInput_ioExceptionOccurs_throwsRuntimeException() throws IOException {
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.isFile()).thenReturn(true);
        doThrow(new IOException("Test IO Exception")).when(mockConnection).add(any(InputStream.class), eq(""), eq(RDFFormat.RDFXML));
        assertThrows(RuntimeException.class, () -> rdfxmlParser.processInput(mockConnection, mockFile));
    }
}
