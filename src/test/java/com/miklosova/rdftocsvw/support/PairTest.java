package com.miklosova.rdftocsvw.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PairTest {

    @Test
    void testConstructorAndGetters() {
        // Test with String types
        Pair<String, Integer> pair = new Pair<>("hello", 42);
        assertEquals("hello", pair.getFirst());
        assertEquals(42, pair.getSecond());
    }

    @Test
    void testConstructorWithNullValues() {
        // Test with null values
        Pair<String, String> pairWithNulls = new Pair<>(null, null);
        assertNull(pairWithNulls.getFirst());
        assertNull(pairWithNulls.getSecond());
    }

    @Test
    void testConstructorWithMixedNulls() {
        // Test with one null value
        Pair<String, Integer> pair1 = new Pair<>("test", null);
        assertEquals("test", pair1.getFirst());
        assertNull(pair1.getSecond());

        Pair<String, Integer> pair2 = new Pair<>(null, 123);
        assertNull(pair2.getFirst());
        assertEquals(123, pair2.getSecond());
    }

    @Test
    void testEqualsWithSameObject() {
        Pair<String, Integer> pair = new Pair<>("test", 1);
        assertEquals(pair, pair);
    }

    @Test
    void testEqualsWithEqualPairs() {
        Pair<String, Integer> pair1 = new Pair<>("test", 1);
        Pair<String, Integer> pair2 = new Pair<>("test", 1);
        assertEquals(pair1, pair2);
    }

    @Test
    void testEqualsWithDifferentFirstValue() {
        Pair<String, Integer> pair1 = new Pair<>("test1", 1);
        Pair<String, Integer> pair2 = new Pair<>("test2", 1);
        assertNotEquals(pair1, pair2);
    }

    @Test
    void testEqualsWithDifferentSecondValue() {
        Pair<String, Integer> pair1 = new Pair<>("test", 1);
        Pair<String, Integer> pair2 = new Pair<>("test", 2);
        assertNotEquals(pair1, pair2);
    }

    @Test
    void testEqualsWithNull() {
        Pair<String, Integer> pair = new Pair<>("test", 1);
        assertNotEquals(null, pair);
    }

    @Test
    void testEqualsWithDifferentClass() {
        Pair<String, Integer> pair = new Pair<>("test", 1);
        assertNotEquals("not a pair", pair);
    }

    @Test
    void testEqualsWithNullValues() {
        Pair<String, Integer> pair1 = new Pair<>(null, null);
        Pair<String, Integer> pair2 = new Pair<>(null, null);
        assertEquals(pair1, pair2);
    }

    @Test
    void testHashCodeConsistency() {
        Pair<String, Integer> pair = new Pair<>("test", 42);
        int hash1 = pair.hashCode();
        int hash2 = pair.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    void testHashCodeEqualPairs() {
        Pair<String, Integer> pair1 = new Pair<>("test", 42);
        Pair<String, Integer> pair2 = new Pair<>("test", 42);
        assertEquals(pair1.hashCode(), pair2.hashCode());
    }

    @Test
    void testHashCodeWithNulls() {
        Pair<String, Integer> pair1 = new Pair<>(null, null);
        Pair<String, Integer> pair2 = new Pair<>(null, null);
        assertEquals(pair1.hashCode(), pair2.hashCode());
    }

    @Test
    void testToString() {
        Pair<String, Integer> pair = new Pair<>("hello", 42);
        assertEquals("(hello, 42)", pair.toString());
    }

    @Test
    void testToStringWithNulls() {
        Pair<String, Integer> pair = new Pair<>(null, null);
        assertEquals("(null, null)", pair.toString());
    }

    @Test
    void testToStringWithMixedNulls() {
        Pair<String, Integer> pair1 = new Pair<>("test", null);
        assertEquals("(test, null)", pair1.toString());

        Pair<String, Integer> pair2 = new Pair<>(null, 123);
        assertEquals("(null, 123)", pair2.toString());
    }

    @Test
    void testWithDifferentTypes() {
        // Test with different generic types
        Pair<Integer, String> intStringPair = new Pair<>(100, "value");
        assertEquals(100, intStringPair.getFirst());
        assertEquals("value", intStringPair.getSecond());

        Pair<Double, Boolean> doubleBoolPair = new Pair<>(3.14, true);
        assertEquals(3.14, doubleBoolPair.getFirst());
        assertTrue(doubleBoolPair.getSecond());
    }
}
