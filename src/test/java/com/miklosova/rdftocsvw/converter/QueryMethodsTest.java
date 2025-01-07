package com.miklosova.rdftocsvw.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.provider.EnumSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class QueryMethodsTest {

    //BaseRock generated method id: ${testEnumValues}, hash: 81B017EA37E00B6702A212E6AD995672
    @Test
    void testEnumValues() {
        QueryMethods[] methods = QueryMethods.values();
        assertEquals(2, methods.length);
        assertArrayEquals(new QueryMethods[] { QueryMethods.BASIC_QUERY, QueryMethods.SPLIT_QUERY }, methods);
    }

    //BaseRock generated method id: ${testGetValue}, hash: E7DBF54CF81F4B08DDF743E16683E88C
    @ParameterizedTest
    @EnumSource(QueryMethods.class)
    void testGetValue(QueryMethods method) {
        assertNotNull(method.getValue());
        assertFalse(method.getValue().isEmpty());
    }

    //BaseRock generated method id: ${testBasicQueryValue}, hash: 82FD1AC8661EB1B8D44EF26D06557B90
    @Test
    void testBasicQueryValue() {
        assertEquals("basicQuery", QueryMethods.BASIC_QUERY.getValue());
    }

    //BaseRock generated method id: ${testSplitQueryValue}, hash: A997BB06550BF1C113F8D4F97E2A481F
    @Test
    void testSplitQueryValue() {
        assertEquals("splitQuery", QueryMethods.SPLIT_QUERY.getValue());
    }

    //BaseRock generated method id: ${testValueOfMethod}, hash: 27366C3953A6459A1C506E1D25B5695E
    @Test
    void testValueOfMethod() {
        assertEquals(QueryMethods.BASIC_QUERY, QueryMethods.valueOf("BASIC_QUERY"));
        assertEquals(QueryMethods.SPLIT_QUERY, QueryMethods.valueOf("SPLIT_QUERY"));
    }

    //BaseRock generated method id: ${testValueOfMethodThrowsException}, hash: 080BB1D56ED1399293C244427D09B8F2
    @Test
    void testValueOfMethodThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> QueryMethods.valueOf("INVALID_QUERY"));
    }
}
