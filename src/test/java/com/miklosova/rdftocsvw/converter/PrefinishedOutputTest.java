package com.miklosova.rdftocsvw.converter;

import com.miklosova.rdftocsvw.converter.data_structure.IFactory;
import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.converter.data_structure.TypeOfValue;
import org.eclipse.rdf4j.model.Value;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class PrefinishedOutputTest {

    @Mock
    private IFactory<String> mockFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //BaseRock generated method id: ${testConstructorWithFactory}, hash: CE5A7633C726E9367C0439247B743C48
    @Test
    void testConstructorWithFactory() {
        when(mockFactory.factory()).thenReturn("TestOutput");
        PrefinishedOutput<String> output = new PrefinishedOutput<>(mockFactory.factory());
        assertEquals("TestOutput", output.getPrefinishedOutput());
        verify(mockFactory, times(1)).factory();
    }

    //BaseRock generated method id: ${testConstructorWithParameter}, hash: C352D217E42CEA26962081B98D3F9294
    @Test
    void testConstructorWithParameter() {
        String testOutput = "TestOutput";
        PrefinishedOutput<String> output = new PrefinishedOutput<>(testOutput);
        assertEquals(testOutput, output.getPrefinishedOutput());
    }

    //BaseRock generated method id: ${testGetPrefinishedOutput}, hash: 302F7E074B77AB499A52CB0149FA3152
    @Test
    void testGetPrefinishedOutput() {
        String testOutput = "TestOutput";
        PrefinishedOutput<String> output = new PrefinishedOutput<>(testOutput);
        assertEquals(testOutput, output.getPrefinishedOutput());
    }

    //BaseRock generated method id: ${testSetPrefinishedOutput}, hash: 84684261A11A01A7658A5CE21CDF1FCF
    @Test
    void testSetPrefinishedOutput() {
        PrefinishedOutput<String> output = new PrefinishedOutput<>("InitialOutput");
        String newOutput = "NewOutput";
        output.setPrefinishedOutput(newOutput);
        assertEquals(newOutput, output.getPrefinishedOutput());
    }

    //BaseRock generated method id: ${testWithQueryMethods}, hash: AB2603757B8C5A4929FA0E57FE2F430A
    @ParameterizedTest
    @EnumSource(QueryMethods.class)
    void testWithQueryMethods(QueryMethods method) {
        PrefinishedOutput<QueryMethods> output = new PrefinishedOutput<>(method);
        assertEquals(method, output.getPrefinishedOutput());
    }

    //BaseRock generated method id: ${testWithTypeOfValue}, hash: 748C610F925DF4A2CF927544BD5EEAF6
    @ParameterizedTest
    @EnumSource(TypeOfValue.class)
    void testWithTypeOfValue(TypeOfValue type) {
        PrefinishedOutput<TypeOfValue> output = new PrefinishedOutput<>(type);
        assertEquals(type, output.getPrefinishedOutput());
    }

    //BaseRock generated method id: ${testNullParameter}, hash: 5AB2CC841F278DF9452010C27CE52F19
    @Test
    void testNullParameter() {
        PrefinishedOutput<String> output = new PrefinishedOutput<>((String) null);
        assertNull(output.getPrefinishedOutput());
    }
}