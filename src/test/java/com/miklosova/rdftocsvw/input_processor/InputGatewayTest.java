package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.IRDF4JParsingMethod;
import com.miklosova.rdftocsvw.input_processor.parsing_methods.RdfxmlParser;
import com.miklosova.rdftocsvw.input_processor.parsing_methods.TurtleParser;
import com.miklosova.rdftocsvw.support.BaseTest;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class InputGatewayTest extends BaseTest {

    @Mock
    private IRDF4JParsingMethod parsingMethod;

    @Mock
    private RepositoryConnection connection;

    private InputGateway inputGateway;

    @BeforeEach
    void setUp() throws IOException {
        inputGateway = new InputGateway();
        inputGateway.setParsingMethod(parsingMethod);
        connection = rdfToCSV.createRepositoryConnection(db, "../RDFtoCSV/src/test/resources/differentSerializations/testingInput.rdf", "rdf4j");

    }

    //BaseRock generated method id: ${testSetParsingMethod}, hash: 074DD83CB1BC40ABA2D1D36A9EEA4D11
    @Test
    void testSetParsingMethod() {
        IRDF4JParsingMethod newParsingMethod = mock(IRDF4JParsingMethod.class);
        inputGateway.setParsingMethod(newParsingMethod);
        RepositoryConnection testConn = mock(RepositoryConnection.class);
        File testFile = new File("test.rdf");
        try {
            inputGateway.processInput(testConn, testFile);
        } catch (RDFParseException | IOException e) {
            fail("Exception should not be thrown");
        }
        verify(newParsingMethod).processInput(testConn, testFile);

    }

    //BaseRock generated method id: ${testProcessInput}, hash: B4D196C8E42B98008469CC60C160F488
    @Test
    void testProcessInput() throws RDFParseException, IOException {
        inputGateway.setParsingMethod(new RdfxmlParser());
        File fileToProcess = new File("../RDFtoCSV/src/test/resources/differentSerializations/testingInput.rdf");
        RepositoryConnection result = inputGateway.processInput(connection, fileToProcess);
        assertNotNull(result);
    }

    //BaseRock generated method id: ${testProcessInputThrowsRDFParseException}, hash: 16E6B916DB28558C486989ABCAEB333C
    @Test
    void testProcessInputThrowsRDFParseException() throws RDFParseException, IOException {
        File fileToProcess = new File("../RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt");
        parsingMethod = mock(TurtleParser.class);
        inputGateway.setParsingMethod(parsingMethod);
        when(parsingMethod.processInput(connection, fileToProcess)).thenThrow(new RDFParseException("Parse error"));
        assertThrows(RDFParseException.class, () -> inputGateway.processInput(connection, fileToProcess));
        verify(parsingMethod).processInput(connection, fileToProcess);
    }

    //BaseRock generated method id: ${testProcessInputThrowsIOException}, hash: 4DE01B2A6ECAF7E38A1079D1834F89DF
    @Test
    void testProcessInputThrowsIOException() throws RDFParseException, IOException {
        connection = mock(RepositoryConnection.class);

        File fileToProcess = new File("nonexistent.rdf");
        parsingMethod = mock(TurtleParser.class);
        inputGateway.setParsingMethod(parsingMethod);
        when(parsingMethod.processInput(connection, fileToProcess)).thenThrow(new RuntimeException("File not found"));
        assertThrows(RuntimeException.class, () -> inputGateway.processInput(connection, fileToProcess));
        verify(parsingMethod).processInput(connection, fileToProcess);
    }
}
