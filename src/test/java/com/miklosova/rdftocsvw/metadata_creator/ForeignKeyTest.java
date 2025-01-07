package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.ForeignKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.ParameterizedTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class ForeignKeyTest {

    //BaseRock generated method id: ${testConstructorAndGetters}, hash: EEF398D4444D361F5FFDEB802A1B7E3E
    @Test
    void testConstructorAndGetters() {
        //String columnReference = "column1";
        //Reference reference = mock(Reference.class);
        //ForeignKey foreignKey = new ForeignKey(columnReference, reference);
        //assertEquals(columnReference, foreignKey.getColumnReference());
        //assertEquals(reference, foreignKey.getReference());
    }

    //BaseRock generated method id: ${testConstructorAndGettersWithVariousInputs}, hash: 5A4B2D76847B79582842F5F539EAFB12
    @ParameterizedTest
    @CsvSource({ "column1, reference1", "column2, reference2", "'', ''", "'null', 'null'" })
    void testConstructorAndGettersWithVariousInputs(String columnReference, String referenceName) {
        //Reference reference = mock(Reference.class);
        //when(reference.toString()).thenReturn(referenceName);
        //ForeignKey foreignKey = new ForeignKey(columnReference, reference);
        //assertEquals(columnReference, foreignKey.getColumnReference());
        //assertEquals(reference, foreignKey.getReference());
        //assertEquals(referenceName, foreignKey.getReference().toString());
    }

    //BaseRock generated method id: ${testConstructorWithNullValues}, hash: 373352787652C24CED30113F82336E38
    @Test
    void testConstructorWithNullValues() {
        assertDoesNotThrow(() -> new ForeignKey(null, null));
        ForeignKey foreignKey = new ForeignKey(null, null);
        assertNull(foreignKey.getColumnReference());
        assertNull(foreignKey.getReference());
    }
}