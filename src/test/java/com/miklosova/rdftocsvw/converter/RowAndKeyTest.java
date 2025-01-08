package com.miklosova.rdftocsvw.converter;

import com.miklosova.rdftocsvw.converter.data_structure.Row;
import com.miklosova.rdftocsvw.converter.data_structure.RowAndKey;
import org.eclipse.rdf4j.model.Value;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.util.stream.Stream;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.provider.Arguments;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class RowAndKeyTest {

    private RowAndKey rowAndKey;

    private SimpleValueFactory valueFactory;

    @BeforeEach
    void setUp() {
        rowAndKey = new RowAndKey();
        valueFactory = SimpleValueFactory.getInstance();
    }

    //BaseRock generated method id: ${testDefaultConstructor}, hash: 170202ADD36F384FD964EE90BCFBF769
    @Test
    void testDefaultConstructor() {
        assertNotNull(rowAndKey.getKeys());
        assertNotNull(rowAndKey.getRows());
        assertTrue(rowAndKey.getKeys().isEmpty());
        assertTrue(rowAndKey.getRows().isEmpty());
    }

    //BaseRock generated method id: ${testParameterizedConstructor}, hash: 29DFD9B25A9A53E5336302300FF7B2DA
    @Test
    void testParameterizedConstructor() {
        ArrayList<Value> keys = new ArrayList<>();
        keys.add(valueFactory.createIRI("http://example.org/key1"));
        ArrayList<Row> rows = new ArrayList<>();
        rows.add(new Row(null, null, false));
        RowAndKey customRowAndKey = new RowAndKey(keys, rows);
        assertEquals(keys, customRowAndKey.getKeys());
        assertEquals(rows, customRowAndKey.getRows());
    }

    //BaseRock generated method id: ${testSetAndGetKeys}, hash: 763B79CC756E05CD6A3201C93E021E65
    @Test
    void testSetAndGetKeys() {
        ArrayList<Value> keys = new ArrayList<>();
        keys.add(valueFactory.createIRI("http://example.org/key1"));
        keys.add(valueFactory.createIRI("http://example.org/key2"));
        rowAndKey.setKeys(keys);
        assertEquals(keys, rowAndKey.getKeys());
    }

    //BaseRock generated method id: ${testSetAndGetRows}, hash: 9CD6898166A6CBE26CE8727E1ADF5A41
    @Test
    void testSetAndGetRows() {
        ArrayList<Row> rows = new ArrayList<>();
        rows.add(new Row(null, null, false));
        rows.add(new Row(null, null, false));
        rowAndKey.setRows(rows);
        assertEquals(rows, rowAndKey.getRows());
    }

    //BaseRock generated method id: ${testAddingDifferentValueTypesToKeys}, hash: D45CB200CE9788A1727E6F11B06B8DB3
    @ParameterizedTest
    @MethodSource("provideValuesForKeys")
    void testAddingDifferentValueTypesToKeys(Value value) {
        rowAndKey.getKeys().add(value);
        assertTrue(rowAndKey.getKeys().contains(value));
    }

    private static Stream<Arguments> provideValuesForKeys() {
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        return Stream.of(Arguments.of(vf.createIRI("http://example.org/iri")), Arguments.of(vf.createBNode()), Arguments.of(vf.createLiteral("test literal")));
    }

    //BaseRock generated method id: ${testRowAndKeyFactory}, hash: DB0AC87C963D4158C9B2DC21C1237469
    @Test
    void testRowAndKeyFactory() {
        RowAndKey.RowAndKeyFactory factory = new RowAndKey.RowAndKeyFactory();
        RowAndKey newRowAndKey = factory.factory();
        assertNotNull(newRowAndKey);
        assertTrue(newRowAndKey.getKeys().isEmpty());
        assertTrue(newRowAndKey.getRows().isEmpty());
    }

    //BaseRock generated method id: ${testKeysAndRowsIndependence}, hash: 0DA9D7247E0D6A2FCFE1F21EFE1BE5C5
    @Test
    void testKeysAndRowsIndependence() {
        ArrayList<Value> keys1 = new ArrayList<>();
        keys1.add(valueFactory.createIRI("http://example.org/key1"));
        ArrayList<Row> rows1 = new ArrayList<>();
        rows1.add(new Row(null, null, false));
        RowAndKey rowAndKey1 = new RowAndKey(keys1, rows1);
        ArrayList<Value> keys2 = new ArrayList<>();
        keys2.add(valueFactory.createIRI("http://example.org/key2"));
        ArrayList<Row> rows2 = new ArrayList<>();
        rows2.add(new Row(null, null, false));
        RowAndKey rowAndKey2 = new RowAndKey(keys2, rows2);
        assertNotEquals(rowAndKey1.getKeys(), rowAndKey2.getKeys());
        assertNotEquals(rowAndKey1.getRows(), rowAndKey2.getRows());
    }

    //BaseRock generated method id: ${testEmptyKeysAndRows}, hash: B479B57C0DAED938C5A63680CDE0801D
    @Test
    void testEmptyKeysAndRows() {
        assertTrue(rowAndKey.getKeys().isEmpty());
        assertTrue(rowAndKey.getRows().isEmpty());
        rowAndKey.setKeys(new ArrayList<>());
        rowAndKey.setRows(new ArrayList<>());
        assertTrue(rowAndKey.getKeys().isEmpty());
        assertTrue(rowAndKey.getRows().isEmpty());
    }
}