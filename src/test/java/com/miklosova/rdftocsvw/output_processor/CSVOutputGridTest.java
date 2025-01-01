package com.miklosova.rdftocsvw.output_processor;

import java.util.Arrays;
import org.eclipse.rdf4j.model.Value;
import java.io.PrintStream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class CSVOutputGridTest {

    private CSVOutputGrid csvOutputGrid;

    private SimpleValueFactory valueFactory;

    @BeforeEach
    void setUp() {
        csvOutputGrid = new CSVOutputGrid();
        valueFactory = SimpleValueFactory.getInstance();
    }

    //BaseRock generated method id: ${testConstructor}, hash: 68ADECE756AF9B012C57214083B96429
    @Test
    void testConstructor() {
        assertNotNull(csvOutputGrid.getCsvOutputBuffer());
        assertTrue(csvOutputGrid.getCsvOutputBuffer().isEmpty());
    }

    //BaseRock generated method id: ${testGetCsvOutputBuffer}, hash: C31DF37A96C6F6ADE0EE324D81F832CC
    @Test
    void testGetCsvOutputBuffer() {
        Map<IRI, Map<String, List<Value>>> buffer = csvOutputGrid.getCsvOutputBuffer();
        assertNotNull(buffer);
        assertTrue(buffer.isEmpty());
        IRI iri = valueFactory.createIRI("http://example.org/resource");
        Map<String, List<Value>> innerMap = new HashMap<>();
        innerMap.put("key", Arrays.asList(valueFactory.createLiteral("value")));
        buffer.put(iri, innerMap);
        assertEquals(1, csvOutputGrid.getCsvOutputBuffer().size());
        assertTrue(csvOutputGrid.getCsvOutputBuffer().containsKey(iri));
    }

    //BaseRock generated method id: ${testPrint}, hash: C2DB77C61E2C57A0974C8E58CEF78CCA
    @Test
    void testPrint() {
        IRI iri1 = valueFactory.createIRI("http://example.org/resource1");
        IRI iri2 = valueFactory.createIRI("http://example.org/resource2");
        Map<String, List<Value>> innerMap1 = new HashMap<>();
        innerMap1.put("key1", Arrays.asList(valueFactory.createLiteral("value1")));
        Map<String, List<Value>> innerMap2 = new HashMap<>();
        innerMap2.put("key2", Arrays.asList(valueFactory.createLiteral("value2")));
        innerMap2.put("key3", Arrays.asList(valueFactory.createLiteral("value3")));
        csvOutputGrid.getCsvOutputBuffer().put(iri1, innerMap1);
        csvOutputGrid.getCsvOutputBuffer().put(iri2, innerMap2);
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        csvOutputGrid.print();
        String expectedOutput = iri1 + ":  key1 \"value1\"\n" + iri2 + ":  key2 \"value2\" key3 \"value3\"\n";
        assertEquals(expectedOutput, outContent.toString());
        System.setOut(System.out);
    }

    //BaseRock generated method id: ${testPrintEmptyBuffer}, hash: DE72204985B9061AFBF06ED59DEAB660
    @Test
    void testPrintEmptyBuffer() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        csvOutputGrid.print();
        assertEquals("", outContent.toString());
        System.setOut(System.out);
    }
}
