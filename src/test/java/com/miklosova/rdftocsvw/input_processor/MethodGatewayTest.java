package com.miklosova.rdftocsvw.input_processor;

import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.BeforeEach;
import com.miklosova.rdftocsvw.input_processor.parsing_methods.IInputParsingMethod;
import org.eclipse.rdf4j.repository.Repository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
@Disabled
class MethodGatewayTest {

    private MethodGateway methodGateway;

    @Mock
    private IInputParsingMethod mockParsingMethod;

    @Mock
    private Repository mockRepository;

    @Mock
    private RepositoryConnection mockConnection;

    @BeforeEach
    void setUp() {
        methodGateway = new MethodGateway();
    }

    //BaseRock generated method id: ${testGetParsingMethod_WhenNotSet_ShouldReturnNull}, hash: D3A0C85E9E3172A59947D3D491EE2132
    @Test
    void testGetParsingMethod_WhenNotSet_ShouldReturnNull() {
        assertNull(methodGateway.getParsingMethod());
    }

    //BaseRock generated method id: ${testSetParsingMethod_WhenCalled_ShouldSetParsingMethod}, hash: A7C863F544128A9EC6DFD33674692DA0
    @Test
    void testSetParsingMethod_WhenCalled_ShouldSetParsingMethod() {
        methodGateway.setParsingMethod(mockParsingMethod);
        assertEquals(mockParsingMethod, methodGateway.getParsingMethod());
    }

    //BaseRock generated method id: ${testProcessInput_WhenCalled_ShouldDelegateToParsingMethod}, hash: 7B19F9AEEA42CC0FF8CF568011A7139E
    @Test
    void testProcessInput_WhenCalled_ShouldDelegateToParsingMethod() throws RDFParseException, IOException {
        File mockFile = mock(File.class);
        when(mockParsingMethod.processInput(mockFile, mockRepository)).thenReturn(mockConnection);
        methodGateway.setParsingMethod(mockParsingMethod);
        RepositoryConnection result = methodGateway.processInput(mockFile, mockRepository);
        assertEquals(mockConnection, result);
        verify(mockParsingMethod).processInput(mockFile, mockRepository);
    }

    //BaseRock generated method id: ${testProcessInput_WhenParsingMethodNotSet_ShouldThrowNullPointerException}, hash: 2F48FDABC162650FF1720CFF49D4E5F5
    @Test
    void testProcessInput_WhenParsingMethodNotSet_ShouldThrowNullPointerException() {
        File mockFile = mock(File.class);
        assertThrows(NullPointerException.class, () -> methodGateway.processInput(mockFile, mockRepository));
    }

    //BaseRock generated method id: ${testProcessInput_WhenParsingMethodThrowsRDFParseException_ShouldPropagateException}, hash: 37DB41052C1A6AF22CC2862B1D1C9CD5
    @Test
    void testProcessInput_WhenParsingMethodThrowsRDFParseException_ShouldPropagateException() throws RDFParseException, IOException {
        File mockFile = mock(File.class);
        when(mockParsingMethod.processInput(mockFile, mockRepository)).thenThrow(new RDFParseException("Parse error"));
        methodGateway.setParsingMethod(mockParsingMethod);
        assertThrows(RDFParseException.class, () -> methodGateway.processInput(mockFile, mockRepository));
    }

    //BaseRock generated method id: ${testProcessInput_WhenParsingMethodThrowsIOException_ShouldPropagateException}, hash: 446E8102F2CD7D9C0BDE204D5800D8F3
    @Test
    void testProcessInput_WhenParsingMethodThrowsIOException_ShouldPropagateException() throws RDFParseException, IOException {
        File mockFile = mock(File.class);
        when(mockParsingMethod.processInput(mockFile, mockRepository)).thenThrow(new IOException("IO error"));
        methodGateway.setParsingMethod(mockParsingMethod);
        assertThrows(IOException.class, () -> methodGateway.processInput(mockFile, mockRepository));
    }
}
