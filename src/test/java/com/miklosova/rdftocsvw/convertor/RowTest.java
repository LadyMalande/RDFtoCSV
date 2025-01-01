package com.miklosova.rdftocsvw.convertor;

import com.miklosova.rdftocsvw.convertor.Row;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.CsvSource;
import org.eclipse.rdf4j.model.Literal;
import org.junit.jupiter.params.ParameterizedTest;
import static org.junit.jupiter.api.Assertions.*;
import org.eclipse.rdf4j.model.IRI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class RowTest {

    private SimpleValueFactory valueFactory;

    @BeforeEach
    void setUp() {
        valueFactory = SimpleValueFactory.getInstance();
    }

    //BaseRock generated method id: ${testConstructorWithThreeParameters}, hash: 93AFA1165E3AD80047CBD46B9FBD9DCF
    @Test
    void testConstructorWithThreeParameters() {
        IRI id = valueFactory.createIRI("http://example.com/id");
        IRI type = valueFactory.createIRI("http://example.com/type");
        boolean isRdfType = true;
        Row row = new Row(id, type, isRdfType);
        assertEquals(id, row.getId());
        assertEquals(type, row.getType());
        assertTrue(row.isRdfType);
        assertTrue(row.getColumns().isEmpty());
    }

    //BaseRock generated method id: ${testConstructorWithTwoParameters}, hash: 50CAEB1EA8DA618198A7CB40870ED4D2
    @Test
    void testConstructorWithTwoParameters() {
        IRI id = valueFactory.createIRI("http://example.com/id");
        boolean isRdfType = false;
        Row row = new Row(id, isRdfType);
        assertEquals(id, row.getId());
        assertNull(row.getType());
        assertFalse(row.isRdfType);
        assertTrue(row.getColumns().isEmpty());
    }

    //BaseRock generated method id: ${testSetAndGetId}, hash: AE1FB4B7D3F44423171386FC6008DB18
    @Test
    void testSetAndGetId() {
        Row row = new Row(null, false);
        IRI id = valueFactory.createIRI("http://example.com/newId");
        row.setId(id);
        assertEquals(id, row.getId());
    }

    //BaseRock generated method id: ${testSetAndGetType}, hash: FC78FBA296CB602AA81EBAE8659CFE0A
    @Test
    void testSetAndGetType() {
        Row row = new Row(null, false);
        IRI type = valueFactory.createIRI("http://example.com/newType");
        row.setType(type);
        assertEquals(type, row.getType());
    }

    //BaseRock generated method id: ${testSetAndGetColumns}, hash: 53C6632AA95B221F0B29632F61DD1461
    @Test
    void testSetAndGetColumns() {
        Row row = new Row(null, false);
        Map<Value, TypeIdAndValues> columns = new HashMap<>();
        IRI key = valueFactory.createIRI("http://example.com/key");
        TypeIdAndValues value = new TypeIdAndValues(null,TypeOfValue.IRI,new ArrayList<>());
        columns.put(key, value);
        row.setColumns(columns);
        assertEquals(columns, row.getColumns());
    }

    //BaseRock generated method id: ${testConstructorWithThreeParametersVariations}, hash: 1728ACAB771A909FAC9F0508CFC8651C
    @ParameterizedTest
    @CsvSource({ "http://example.com/id1, http://example.com/type1, true", "http://example.com/id2, http://example.com/type2, false" })
    void testConstructorWithThreeParametersVariations(String idString, String typeString, boolean isRdfType) {
        IRI id = valueFactory.createIRI(idString);
        IRI type = valueFactory.createIRI(typeString);
        Row row = new Row(id, type, isRdfType);
        assertEquals(id, row.getId());
        assertEquals(type, row.getType());
        assertEquals(isRdfType, row.isRdfType);
        assertTrue(row.getColumns().isEmpty());
    }

    //BaseRock generated method id: ${testConstructorWithTwoParametersVariations}, hash: 53BE8A1280F53B753855B7AAF2CDBED1
    @ParameterizedTest
    @CsvSource({ "http://example.com/id1, true", "http://example.com/id2, false" })
    void testConstructorWithTwoParametersVariations(String idString, boolean isRdfType) {
        IRI id = valueFactory.createIRI(idString);
        Row row = new Row(id, isRdfType);
        assertEquals(id, row.getId());
        assertNull(row.getType());
        assertEquals(isRdfType, row.isRdfType);
        assertTrue(row.getColumns().isEmpty());
    }

    //BaseRock generated method id: ${testSetAndGetIdWithLiteral}, hash: E8CB031F18EB56C6364A895B4C0280BD
    @Test
    void testSetAndGetIdWithLiteral() {
        Row row = new Row(null, false);
        Literal id = valueFactory.createLiteral("123");
        row.setId(id);
        assertEquals(id, row.getId());
    }

    //BaseRock generated method id: ${testSetAndGetTypeWithLiteral}, hash: 6A9F83FB25FFBDEF213A4EA0473E4931
    @Test
    void testSetAndGetTypeWithLiteral() {
        Row row = new Row(null, false);
        Literal type = valueFactory.createLiteral("TestType");
        row.setType(type);
        assertEquals(type, row.getType());
    }

    //BaseRock generated method id: ${testSetAndGetColumnsWithMultipleEntries}, hash: AB065E091E987B43A9EE7F3D1C1823BA
    @Test
    void testSetAndGetColumnsWithMultipleEntries() {
        Row row = new Row(null, false);
        Map<Value, TypeIdAndValues> columns = new HashMap<>();
        IRI key1 = valueFactory.createIRI("http://example.com/key1");
        IRI key2 = valueFactory.createIRI("http://example.com/key2");
        TypeIdAndValues value1 = new TypeIdAndValues(key1, TypeOfValue.IRI, new ArrayList<>());
        TypeIdAndValues value2 = new TypeIdAndValues(key2, TypeOfValue.IRI, new ArrayList<>());
        columns.put(key1, value1);
        columns.put(key2, value2);
        row.setColumns(columns);
        assertEquals(columns, row.getColumns());
        assertEquals(2, row.getColumns().size());
    }
}