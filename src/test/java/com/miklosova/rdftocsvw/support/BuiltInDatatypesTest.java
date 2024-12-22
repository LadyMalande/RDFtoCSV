package com.miklosova.rdftocsvw.support;
import org.junit.jupiter.api.Test;

import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.junit.jupiter.api.Assertions.*;

class BuiltInDatatypesTest {

    @Test
    void testIsBuiltInDatatypeWithValidBuiltInDatatypes() {
        assertTrue(BuiltInDatatypes.isBuiltInDatatype(iri("http://www.w3.org/2001/XMLSchema#string")));
        assertTrue(BuiltInDatatypes.isBuiltInDatatype(iri("http://www.w3.org/2001/XMLSchema#decimal")));
        assertTrue(BuiltInDatatypes.isBuiltInDatatype(iri("http://www.w3.org/2001/XMLSchema#integer")));
        assertTrue(BuiltInDatatypes.isBuiltInDatatype(iri("http://www.w3.org/2001/XMLSchema#float")));
        assertTrue(BuiltInDatatypes.isBuiltInDatatype(iri("http://www.w3.org/2001/XMLSchema#double")));
        assertTrue(BuiltInDatatypes.isBuiltInDatatype(iri("http://www.w3.org/2001/XMLSchema#boolean")));
        assertTrue(BuiltInDatatypes.isBuiltInDatatype(iri("http://www.w3.org/2001/XMLSchema#dateTime")));
        assertTrue(BuiltInDatatypes.isBuiltInDatatype(iri("http://www.w3.org/2001/XMLSchema#date")));
        assertTrue(BuiltInDatatypes.isBuiltInDatatype(iri("http://www.w3.org/2001/XMLSchema#time")));
    }

    @Test
    void testIsBuiltInDatatypeWithInvalidDatatype() {
        assertFalse(BuiltInDatatypes.isBuiltInDatatype(iri("http://example.org/customDatatype")));
        assertFalse(BuiltInDatatypes.isBuiltInDatatype(iri("http://www.w3.org/2001/XMLSchema#unknown")));
    }

    @Test
    void testIsBuiltInDatatypeWithNull() {
        assertFalse(BuiltInDatatypes.isBuiltInDatatype(null));
    }
}
