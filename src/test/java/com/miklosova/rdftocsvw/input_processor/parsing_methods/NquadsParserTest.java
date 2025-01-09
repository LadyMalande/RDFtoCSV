package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import java.io.FileInputStream;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class NquadsParserTest {

    @Mock
    private RepositoryConnection mockConnection;

    @Mock
    private File mockFile;

    private NquadsParser nquadsParser;

    @BeforeEach
    void setUp() {
        nquadsParser = new NquadsParser();
        mockFile = mock(File.class);
        mockConnection = mock(RepositoryConnection.class);
    }

    //BaseRock generated method id: ${processInput_SuccessfulParsing}, hash: A580351A8723153FEBD292EA435CA00A
    @Test
    void processInput_SuccessfulParsing() throws IOException {
        FileInputStream mockFileInputStream = mock(FileInputStream.class);
        try (var mockedConstructor = mockConstruction(FileInputStream.class, (mock, context) -> {
            when(mock.read()).thenReturn(-1);
        })) {
            RepositoryConnection result = nquadsParser.processInput(mockConnection, mockFile);
            verify(mockConnection).add(any(InputStream.class), eq(""), eq(RDFFormat.NQUADS));
            assertEquals(mockConnection, result);
        }
    }

    //BaseRock generated method id: ${processInput_IOExceptionThrown}, hash: 8B76A6593C5D5AFEF82444F413639969
    @Test
    @Disabled
    void processInput_IOExceptionThrown() throws IOException {
        when(new FileInputStream(mockFile)).thenThrow(new IOException("Test IO Exception"));
        assertThrows(RuntimeException.class, () -> nquadsParser.processInput(mockConnection, mockFile));
    }
}
