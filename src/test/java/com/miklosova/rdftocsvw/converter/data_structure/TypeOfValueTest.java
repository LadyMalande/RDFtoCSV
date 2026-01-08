package com.miklosova.rdftocsvw.converter.data_structure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypeOfValueTest {

    private TypeOfValue typeOfValue;

 /*    @BeforeEach
    void setUp() {
        typeOfValue = new TypeOfValue();
    }

    @Test
    void testConstructor() {
        assertNotNull(typeOfValue);
    }

    @Test
    void testSetAndGetType() {
        String testType = "http://www.w3.org/2001/XMLSchema#string";
        typeOfValue.setType(testType);
        assertEquals(testType, typeOfValue.getType());
    }

    @Test
    void testSetAndGetValue() {
        String testValue = "Test Value";
        typeOfValue.setValue(testValue);
        assertEquals(testValue, typeOfValue.getValue());
    }

    @Test
    void testSetAndGetMultipleValues() {
        typeOfValue.setType("http://www.w3.org/2001/XMLSchema#integer");
        typeOfValue.setValue("42");
        
        assertEquals("http://www.w3.org/2001/XMLSchema#integer", typeOfValue.getType());
        assertEquals("42", typeOfValue.getValue());
    }

    @Test
    void testSetNullType() {
        typeOfValue.setType(null);
        assertNull(typeOfValue.getType());
    }

    @Test
    void testSetNullValue() {
        typeOfValue.setValue(null);
        assertNull(typeOfValue.getValue());
    }

    @Test
    void testSetEmptyType() {
        typeOfValue.setType("");
        assertEquals("", typeOfValue.getType());
    }

    @Test
    void testSetEmptyValue() {
        typeOfValue.setValue("");
        assertEquals("", typeOfValue.getValue());
    }

    @Test
    void testMultipleInstances() {
        TypeOfValue tov1 = new TypeOfValue();
        TypeOfValue tov2 = new TypeOfValue();
        
        tov1.setType("type1");
        tov1.setValue("value1");
        
        tov2.setType("type2");
        tov2.setValue("value2");
        
        assertEquals("type1", tov1.getType());
        assertEquals("value1", tov1.getValue());
        assertEquals("type2", tov2.getType());
        assertEquals("value2", tov2.getValue());
    }

    @Test
    void testOverwriteType() {
        typeOfValue.setType("initialType");
        assertEquals("initialType", typeOfValue.getType());
        
        typeOfValue.setType("newType");
        assertEquals("newType", typeOfValue.getType());
    }

    @Test
    void testOverwriteValue() {
        typeOfValue.setValue("initialValue");
        assertEquals("initialValue", typeOfValue.getValue());
        
        typeOfValue.setValue("newValue");
        assertEquals("newValue", typeOfValue.getValue());
    }

    @Test
    void testTypeWithSpecialCharacters() {
        String specialType = "http://example.org/type#special-type_123";
        typeOfValue.setType(specialType);
        assertEquals(specialType, typeOfValue.getType());
    }

    @Test
    void testValueWithSpecialCharacters() {
        String specialValue = "Value with spaces, commas, and \"quotes\"";
        typeOfValue.setValue(specialValue);
        assertEquals(specialValue, typeOfValue.getValue());
    }

    @Test
    void testValueWithUnicode() {
        String unicodeValue = "Příliš žluťoučký kůň";
        typeOfValue.setValue(unicodeValue);
        assertEquals(unicodeValue, typeOfValue.getValue());
    }

    @Test
    void testLongType() {
        String longType = "http://very.long.domain.name.example.org/ontology/2024/version/1.0#veryLongTypeNameForTesting";
        typeOfValue.setType(longType);
        assertEquals(longType, typeOfValue.getType());
    }

    @Test
    void testLongValue() {
        String longValue = "This is a very long value string that contains multiple sentences. " +
                          "It could be used to test how the TypeOfValue class handles long text. " +
                          "This is useful for ensuring proper memory handling and string storage.";
        typeOfValue.setValue(longValue);
        assertEquals(longValue, typeOfValue.getValue());
    } */
}
