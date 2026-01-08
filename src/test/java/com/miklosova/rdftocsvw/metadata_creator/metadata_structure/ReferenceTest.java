package com.miklosova.rdftocsvw.metadata_creator.metadata_structure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReferenceTest {

    @Test
    void testConstructorCreatesInstance() {
        Reference reference = new Reference("users.csv", "userId");
        assertNotNull(reference);
    }

    @Test
    void testConstructorWithNullResource() {
        Reference reference = new Reference(null, "userId");
        assertNotNull(reference);
    }

    @Test
    void testConstructorWithNullColumnReference() {
        Reference reference = new Reference("users.csv", null);
        assertNotNull(reference);
    }

    @Test
    void testConstructorWithBothNulls() {
        Reference reference = new Reference(null, null);
        assertNotNull(reference);
    }

    @Test
    void testConstructorWithEmptyStrings() {
        Reference reference = new Reference("", "");
        assertNotNull(reference);
    }

    @Test
    void testMultipleInstances() {
        Reference reference1 = new Reference("users.csv", "userId");
        Reference reference2 = new Reference("orders.csv", "orderId");
        Reference reference3 = new Reference("products.csv", "productId");
        
        assertNotNull(reference1);
        assertNotNull(reference2);
        assertNotNull(reference3);
    }
}
