package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.BeforeEach;
import org.eclipse.rdf4j.repository.Repository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import java.io.IOException;
import com.miklosova.rdftocsvw.input_processor.ParsingService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class RDF4JMethodTest {

    @Mock
    private Repository mockRepository;

    @Mock
    private RepositoryConnection mockConnection;

    @Mock
    private ParsingService mockParsingService;

    private RDF4JMethod rdf4JMethod;

    @BeforeEach
    void setUp() {
        rdf4JMethod = new RDF4JMethod();
        mockConnection = mock(RepositoryConnection.class);
        mockRepository = mock(Repository.class);
    }

    //BaseRock generated method id: ${processInput_SuccessfulParsing}, hash: 35A79BBBE593A9C73113ADF36FB7D767
    @Test
    void processInput_SuccessfulParsing() throws RDFParseException, IOException {
        File mockFile = mock(File.class);
        when(mockRepository.getConnection()).thenReturn(mockConnection);
        when(mockConnection.isEmpty()).thenReturn(false);
        try (var mockedConstructor = mockConstruction(ParsingService.class, (mock, context) -> when(mock.processInput(any(RepositoryConnection.class), any(File.class))).thenReturn(mockConnection))) {
            RepositoryConnection result = rdf4JMethod.processInput(mockFile, mockRepository);
            assertNotNull(result);
            assertEquals(mockConnection, result);
            verify(mockRepository).getConnection();
            verify(mockConnection).isEmpty();
        }
    }

    //BaseRock generated method id: ${processInput_EmptyConnection}, hash: ADB54636FDC4A53C69DE222DABF65D36
    @Test
    void processInput_EmptyConnection() throws RDFParseException, IOException {
        File mockFile = mock(File.class);
        when(mockRepository.getConnection()).thenReturn(mockConnection);
        when(mockConnection.isEmpty()).thenReturn(true);
        when(mockFile.getAbsolutePath()).thenReturn("/path/to/file.rdf");
        try (var mockedConstructor = mockConstruction(ParsingService.class, (mock, context) -> when(mock.processInput(any(RepositoryConnection.class), any(File.class))).thenReturn(mockConnection))) {
            assertThrows(RuntimeException.class, () -> rdf4JMethod.processInput(mockFile, mockRepository), "No loader registered for file type \".\" + /path/to/file.rdf + \"\" files OR 'NO TRIPLES FOUND'");
            verify(mockRepository).getConnection();
            verify(mockConnection).isEmpty();
            verify(mockFile).getAbsolutePath();
        }
    }

    //BaseRock generated method id: ${processInput_ThrowsRDFParseException}, hash: 264E7C2C3C8E175CBC38B43AE06FAFC2
    @Test
    void processInput_ThrowsRDFParseException() throws RDFParseException, IOException {
        File mockFile = mock(File.class);
        when(mockRepository.getConnection()).thenReturn(mockConnection);
        try (var mockedConstructor = mockConstruction(ParsingService.class, (mock, context) -> when(mock.processInput(any(RepositoryConnection.class), any(File.class))).thenThrow(new RDFParseException("Parsing error")))) {
            assertThrows(RDFParseException.class, () -> rdf4JMethod.processInput(mockFile, mockRepository));
            verify(mockRepository).getConnection();
        }
    }

    //BaseRock generated method id: ${processInput_ThrowsIOException}, hash: 145C4558457FB7AF09ABE92DE6DFC9C1
    @Test
    void processInput_ThrowsIOException() throws RDFParseException, IOException {
        File mockFile = mock(File.class);
        when(mockRepository.getConnection()).thenReturn(mockConnection);
        try (var mockedConstructor = mockConstruction(ParsingService.class, (mock, context) -> when(mock.processInput(any(RepositoryConnection.class), any(File.class))).thenThrow(new IOException("IO error")))) {
            assertThrows(IOException.class, () -> rdf4JMethod.processInput(mockFile, mockRepository));
            verify(mockRepository).getConnection();
        }
    }
}
