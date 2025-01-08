package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.metadata_creator.Triple;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.eclipse.rdf4j.model.IRI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class TripleTest {

    private SimpleValueFactory valueFactory;

    @BeforeEach
    void setUp() {
        valueFactory = SimpleValueFactory.getInstance();
    }

    //BaseRock generated method id: ${testTripleConstructorAndGetters}, hash: 5041C2F17A5207180C68FC601CB1E81A
    @Test
    void testTripleConstructorAndGetters() {
        IRI subject = valueFactory.createIRI("http://example.com/subject");
        IRI predicate = valueFactory.createIRI("http://example.com/predicate");
        Value object = valueFactory.createLiteral("Test Object");
        Triple triple = new Triple(subject, predicate, object);
        assertNotNull(triple);
        assertEquals(subject, triple.getSubject());
        assertEquals(predicate, triple.getPredicate());
        assertEquals(object, triple.getObject());
    }

    //BaseRock generated method id: ${testTripleWithDifferentValues}, hash: 8527A06975682831CB6C2FEE7DD9F9CC
    @ParameterizedTest
    @CsvSource({ "http://example.com/subject1, http://example.com/predicate1, 'Literal Object'", "http://example.com/subject2, http://example.com/predicate2, http://example.com/object" })
    void testTripleWithDifferentValues(String subjectString, String predicateString, String objectString) {
        IRI subject = valueFactory.createIRI(subjectString);
        IRI predicate = valueFactory.createIRI(predicateString);
        Value object = objectString.startsWith("http") ? valueFactory.createIRI(objectString) : valueFactory.createLiteral(objectString);
        Triple triple = new Triple(subject, predicate, object);
        assertEquals(subject, triple.getSubject());
        assertEquals(predicate, triple.getPredicate());
        assertEquals(object, triple.getObject());
    }
}
