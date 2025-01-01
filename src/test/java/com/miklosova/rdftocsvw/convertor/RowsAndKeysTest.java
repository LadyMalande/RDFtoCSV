package com.miklosova.rdftocsvw.convertor;

import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.ParameterizedTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class RowsAndKeysTest {

    private RowsAndKeys rowsAndKeys;

    @BeforeEach
    void setUp() {
        rowsAndKeys = new RowsAndKeys();
    }

    //BaseRock generated method id: ${testConstructor}, hash: 75F75EB0F8F294CA7518163767304064
    @Test
    void testConstructor() {
        assertNotNull(rowsAndKeys.getRowsAndKeys());
        assertTrue(rowsAndKeys.getRowsAndKeys().isEmpty());
    }

    //BaseRock generated method id: ${testGetRowsAndKeys}, hash: D8E4E4230E452D5A1B6D3FD0A2808D62
    @Test
    void testGetRowsAndKeys() {
        ArrayList<RowAndKey> list = new ArrayList<>();
        list.add(new RowAndKey());
        rowsAndKeys.setRowsAndKeys(list);
        assertEquals(list, rowsAndKeys.getRowsAndKeys());
    }

    //BaseRock generated method id: ${testSetRowsAndKeys}, hash: ADDB16EF8861D007078B410CA18BBAA1
    @Test
    void testSetRowsAndKeys() {
        ArrayList<RowAndKey> list = new ArrayList<>();
        list.add(new RowAndKey());
        list.add(new RowAndKey());
        rowsAndKeys.setRowsAndKeys(list);
        assertEquals(2, rowsAndKeys.getRowsAndKeys().size());
    }

    //BaseRock generated method id: ${testToString}, hash: 80E9A48ED140095A8D5EAA660E7E68F8
    @Test
    void testToString() {
        assertEquals("Rows and keys for metadata creation", rowsAndKeys.toString());
    }

    //BaseRock generated method id: ${testRowsAndKeysFactory}, hash: 1AD57FB6F2AF4F579648A31EBFDD06DB
    @Test
    void testRowsAndKeysFactory() {
        RowsAndKeys.RowsAndKeysFactory factory = new RowsAndKeys.RowsAndKeysFactory();
        RowsAndKeys result = factory.factory();
        assertNotNull(result);
        assertTrue(result.getRowsAndKeys().isEmpty());
    }

    //BaseRock generated method id: ${testQueryMethods}, hash: 9234F5D342ABEA04418A6D88CDCE26D8
    @ParameterizedTest
    @CsvSource({ "BASIC_QUERY,basicQuery", "SPLIT_QUERY,splitQuery" })
    void testQueryMethods(QueryMethods method, String expectedValue) {
        assertEquals(expectedValue, method.getValue());
    }

    //BaseRock generated method id: ${testTypeOfValue}, hash: 83D43FD80136B2837B0C00838558E328
    @Test
    void testTypeOfValue() {
        assertNotNull(TypeOfValue.IRI);
        assertNotNull(TypeOfValue.BNODE);
        assertNotNull(TypeOfValue.LITERAL);
    }
}