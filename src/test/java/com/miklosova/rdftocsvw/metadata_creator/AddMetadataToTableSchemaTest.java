package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.TableSchema;
import com.miklosova.rdftocsvw.support.AppConfig;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test coverage for StreamingMetadataCreator.addMetadataToTableSchema(Triple)
 */
class AddMetadataToTableSchemaTest {

    private StreamingMetadataCreator metadataCreator;
    private AppConfig config;
    private ValueFactory vf;

    @BeforeEach
    void setUp() {
        config = new AppConfig.Builder("test.nt").build();
        metadataCreator = new StreamingMetadataCreator(config);
        metadataCreator.tableSchema = new TableSchema();
        vf = SimpleValueFactory.getInstance();
    }

    // ==================== Basic Functionality Tests ====================

    @Test
    void testAddMetadataToTableSchema_SimpleLiteral() {
        IRI subject = vf.createIRI("http://example.org/subject1");
        IRI predicate = vf.createIRI("http://example.org/name");
        Literal object = vf.createLiteral("John Doe");
        Triple triple = new Triple(subject, predicate, object);

        metadataCreator.tableSchema = new TableSchema();
        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        assertEquals(1, metadataCreator.tableSchema.getColumns().size());
        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertEquals("name", column.getName());
        assertEquals("http://example.org/name", column.getPropertyUrl());
        assertEquals("http://example.org/{+Subject}", column.getAboutUrl());
        assertNotNull(column.getTitles());
    }

    @Test
    void testAddMetadataToTableSchema_IRIObject() {
        IRI subject = vf.createIRI("http://example.org/person1");
        IRI predicate = vf.createIRI("http://example.org/knows");
        IRI object = vf.createIRI("http://example.org/person2");
        Triple triple = new Triple(subject, predicate, object);


        metadataCreator.tableSchema = new TableSchema(); metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        assertEquals(1, metadataCreator.tableSchema.getColumns().size());
        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertEquals("knows", column.getName());
        assertEquals("http://example.org/knows", column.getPropertyUrl());
        assertEquals("http://example.org/{+knows}", column.getValueUrl());
        assertEquals("http://example.org/{+Subject}", column.getAboutUrl());
    }

    @Test
    void testAddMetadataToTableSchema_BNodeObject() {
        IRI subject = vf.createIRI("http://example.org/subject1");
        IRI predicate = vf.createIRI("http://example.org/reference");
        BNode object = vf.createBNode("node1");
        Triple triple = new Triple(subject, predicate, object);

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        assertEquals(1, metadataCreator.tableSchema.getColumns().size());
        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertEquals("reference", column.getName());
        assertEquals("{+reference}", column.getValueUrl());
    }

    @Test
    void testAddMetadataToTableSchema_LiteralWithLanguageTag() {
        IRI subject = vf.createIRI("http://example.org/subject1");
        IRI predicate = vf.createIRI("http://example.org/label");
        Literal object = vf.createLiteral("Label", "en");
        Triple triple = new Triple(subject, predicate, object);

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        assertEquals(1, metadataCreator.tableSchema.getColumns().size());
        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertEquals("label_en", column.getName());
        assertEquals("en", column.getLang());
    }

    @Test
    void testAddMetadataToTableSchema_TypedLiteral() {
        IRI subject = vf.createIRI("http://example.org/subject1");
        IRI predicate = vf.createIRI("http://example.org/age");
        Literal object = vf.createLiteral(25);
        Triple triple = new Triple(subject, predicate, object);

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        assertEquals(1, metadataCreator.tableSchema.getColumns().size());
        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertEquals("age", column.getName());
        assertNotNull(column.getDatatype());
    }

    // ==================== Multiple Triples Tests ====================

    @Test
    void testAddMetadataToTableSchema_MultipleDifferentPredicates() {
        Triple triple1 = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/name"),
            vf.createLiteral("John")
        );
        Triple triple2 = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/age"),
            vf.createLiteral(30)
        );
        Triple triple3 = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/email"),
            vf.createLiteral("john@example.com")
        );

        metadataCreator.addMetadataToTableSchema(triple1);
        metadataCreator.addMetadataToTableSchema(triple2);
        metadataCreator.addMetadataToTableSchema(triple3);

        assertEquals(3, metadataCreator.tableSchema.getColumns().size());
    }

    @Test
    void testAddMetadataToTableSchema_DuplicatePredicatesNotDuplicated() {
        Triple triple1 = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/name"),
            vf.createLiteral("John")
        );
        Triple triple2 = new Triple(
            vf.createIRI("http://example.org/subject2"),
            vf.createIRI("http://example.org/name"),
            vf.createLiteral("Jane")
        );

        metadataCreator.addMetadataToTableSchema(triple1);
        metadataCreator.addMetadataToTableSchema(triple2);

        // Should only have one column since the predicate is the same
        assertEquals(1, metadataCreator.tableSchema.getColumns().size());
        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertEquals("name", column.getName());
    }

    // ==================== Language Tag Tests ====================

    @Test
    void testAddMetadataToTableSchema_DifferentLanguageTags_CreatesSeparateColumns() {
        Triple triple1 = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/label"),
            vf.createLiteral("English Label", "en")
        );
        Triple triple2 = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/label"),
            vf.createLiteral("Czech Label", "cs")
        );

        metadataCreator.addMetadataToTableSchema(triple1);
        metadataCreator.addMetadataToTableSchema(triple2);

        // Different language tags should create separate columns
        assertEquals(2, metadataCreator.tableSchema.getColumns().size());
    }

    @Test
    void testAddMetadataToTableSchema_SameLanguageTag_NosDuplicate() {
        Triple triple1 = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/label"),
            vf.createLiteral("Label 1", "en")
        );
        Triple triple2 = new Triple(
            vf.createIRI("http://example.org/subject2"),
            vf.createIRI("http://example.org/label"),
            vf.createLiteral("Label 2", "en")
        );

        metadataCreator.addMetadataToTableSchema(triple1);
        metadataCreator.addMetadataToTableSchema(triple2);

        // Same predicate and language should not duplicate
        assertEquals(1, metadataCreator.tableSchema.getColumns().size());
        assertEquals("en", metadataCreator.tableSchema.getColumns().get(0).getLang());
    }

    @Test
    void testAddMetadataToTableSchema_NoLanguageTag() {
        Triple triple = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/description"),
            vf.createLiteral("A description")
        );

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        assertEquals(1, metadataCreator.tableSchema.getColumns().size());
        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertNull(column.getLang());
    }

    // ==================== Datatype Tests ====================

    @Test
    void testAddMetadataToTableSchema_IntegerDatatype() {
        Triple triple = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/count"),
            vf.createLiteral(42)
        );

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertNotNull(column.getDatatype());
        assertTrue(column.getDatatype().contains("integer") || column.getDatatype().contains("int"));
    }

    @Test
    void testAddMetadataToTableSchema_BooleanDatatype() {
        Triple triple = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/isActive"),
            vf.createLiteral(true)
        );

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertNotNull(column.getDatatype());
        assertTrue(column.getDatatype().contains("boolean"));
    }

    @Test
    void testAddMetadataToTableSchema_DecimalDatatype() {
        Triple triple = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/price"),
            vf.createLiteral(19.99)
        );

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertNotNull(column.getDatatype());
        assertTrue(column.getDatatype().contains("double") || column.getDatatype().contains("decimal"));
    }

    @Test
    void testAddMetadataToTableSchema_DateDatatype() {
        IRI xsdDate = vf.createIRI("http://www.w3.org/2001/XMLSchema#date");
        Triple triple = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/birthDate"),
            vf.createLiteral("2000-01-01", xsdDate)
        );

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertNotNull(column.getDatatype());
    }

    // ==================== About URL Tests ====================

    @Test
    void testAddMetadataToTableSchema_SetsAboutUrlCorrectly() {
        Triple triple = new Triple(
            vf.createIRI("http://example.org/people/subject1"),
            vf.createIRI("http://example.org/vocab/name"),
            vf.createLiteral("Test")
        );

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertEquals("http://example.org/people/{+Subject}", column.getAboutUrl());
    }

    @Test
    void testAddMetadataToTableSchema_DifferentSubjectNamespaces() {
        Triple triple1 = new Triple(
            vf.createIRI("http://example.org/people/subject1"),
            vf.createIRI("http://example.org/vocab/name"),
            vf.createLiteral("Test 1")
        );
        Triple triple2 = new Triple(
            vf.createIRI("http://other.org/items/subject2"),
            vf.createIRI("http://example.org/vocab/name"),
            vf.createLiteral("Test 2")
        );

        metadataCreator.addMetadataToTableSchema(triple1);
        metadataCreator.addMetadataToTableSchema(triple2);

        // Should update to generic {+Subject} when namespaces differ
        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertEquals("{+Subject}", column.getAboutUrl());
    }

    // ==================== Value URL Tests ====================

    @Test
    void testAddMetadataToTableSchema_IRIObject_SetsValueUrl() {
        Triple triple = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/knows"),
            vf.createIRI("http://example.org/other/person1")
        );

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertEquals("http://example.org/other/{+knows}", column.getValueUrl());
    }

    @Test
    void testAddMetadataToTableSchema_BNodeObject_SetsValueUrl() {
        Triple triple = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/ref"),
            vf.createBNode("node1")
        );

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertEquals("{+ref}", column.getValueUrl());
    }

    @Test
    void testAddMetadataToTableSchema_LiteralObject_NoValueUrl() {
        Triple triple = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/name"),
            vf.createLiteral("John")
        );

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertNull(column.getValueUrl());
    }

    // ==================== Property URL Tests ====================

    @Test
    void testAddMetadataToTableSchema_SetsPropertyUrlCorrectly() {
        Triple triple = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://schema.org/name"),
            vf.createLiteral("Test")
        );

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertEquals("http://schema.org/name", column.getPropertyUrl());
    }

    @Test
    void testAddMetadataToTableSchema_PreservesFullPredicateIRI() {
        Triple triple = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://www.w3.org/2000/01/rdf-schema#label"),
            vf.createLiteral("Test Label")
        );

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertEquals("http://www.w3.org/2000/01/rdf-schema#label", column.getPropertyUrl());
    }

    // ==================== Titles Cache Tests ====================

    @Test
    void testAddMetadataToTableSchema_TitlesCacheWorks() {
        Triple triple1 = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/name"),
            vf.createLiteral("First")
        );
        Triple triple2 = new Triple(
            vf.createIRI("http://example.org/subject2"),
            vf.createIRI("http://example.org/name"),
            vf.createLiteral("Second")
        );

        metadataCreator.addMetadataToTableSchema(triple1);
        metadataCreator.addMetadataToTableSchema(triple2);

        // Second call should use cached title
        assertEquals(1, metadataCreator.tableSchema.getColumns().size());
        assertNotNull(metadataCreator.tableSchema.getColumns().get(0).getTitles());
    }

    @Test
    void testAddMetadataToTableSchema_TitlesCacheKeyIncludesLanguage() {
        Triple triple1 = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/label"),
            vf.createLiteral("English", "en")
        );
        Triple triple2 = new Triple(
            vf.createIRI("http://example.org/subject2"),
            vf.createIRI("http://example.org/label"),
            vf.createLiteral("Czech", "cs")
        );

        metadataCreator.addMetadataToTableSchema(triple1);
        metadataCreator.addMetadataToTableSchema(triple2);

        // Different languages should create separate columns with different titles
        assertEquals(2, metadataCreator.tableSchema.getColumns().size());
    }

    // ==================== Column Cache Tests ====================

    @Test
    void testAddMetadataToTableSchema_ColumnCacheWorks() {
        Triple triple1 = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/name"),
            vf.createLiteral("First")
        );

        metadataCreator.addMetadataToTableSchema(triple1);
        assertEquals(1, metadataCreator.tableSchema.getColumns().size());

        // Add another triple with same predicate
        Triple triple2 = new Triple(
            vf.createIRI("http://example.org/subject2"),
            vf.createIRI("http://example.org/name"),
            vf.createLiteral("Second")
        );

        metadataCreator.addMetadataToTableSchema(triple2);
        
        // Should still have only one column (found via cache)
        assertEquals(1, metadataCreator.tableSchema.getColumns().size());
    }

    // ==================== Edge Cases ====================

    @Test
    void testAddMetadataToTableSchema_EmptyStringLiteral() {
        Triple triple = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/description"),
            vf.createLiteral("")
        );

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        assertEquals(1, metadataCreator.tableSchema.getColumns().size());
        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertEquals("description", column.getName());
    }

    @Test
    void testAddMetadataToTableSchema_VeryLongLiteral() {
        String longText = "A".repeat(10000);
        Triple triple = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/text"),
            vf.createLiteral(longText)
        );

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        assertEquals(1, metadataCreator.tableSchema.getColumns().size());
        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertEquals("text", column.getName());
    }

    @Test
    void testAddMetadataToTableSchema_UnicodeCharacters() {
        Triple triple = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/název"),
            vf.createLiteral("Český text")
        );

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        assertEquals(1, metadataCreator.tableSchema.getColumns().size());
        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertNotNull(column.getName());
    }

    @Test
    void testAddMetadataToTableSchema_SpecialCharactersInIRI() {
        Triple triple = new Triple(
            vf.createIRI("http://example.org/subject-1"),
            vf.createIRI("http://example.org/property_name"),
            vf.createLiteral("Value")
        );

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        assertEquals(1, metadataCreator.tableSchema.getColumns().size());
        assertNotNull(metadataCreator.tableSchema.getColumns().get(0).getName());
    }

    @Test
    void testAddMetadataToTableSchema_HashInPredicateIRI() {
        Triple triple = new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
            vf.createIRI("http://example.org/Person")
        );

        metadataCreator.tableSchema = new TableSchema(); metadataCreator.addMetadataToTableSchema(triple);

        assertEquals(1, metadataCreator.tableSchema.getColumns().size());
        Column column = metadataCreator.tableSchema.getColumns().get(0);
        assertEquals("type", column.getName());
    }

    // ==================== Integration Tests ====================

    @Test
    void testAddMetadataToTableSchema_ComplexScenario() {
        // Person 1 with multiple properties
        metadataCreator.addMetadataToTableSchema(new Triple(
            vf.createIRI("http://example.org/person1"),
            vf.createIRI("http://xmlns.com/foaf/0.1/name"),
            vf.createLiteral("John Doe")
        ));
        metadataCreator.addMetadataToTableSchema(new Triple(
            vf.createIRI("http://example.org/person1"),
            vf.createIRI("http://xmlns.com/foaf/0.1/age"),
            vf.createLiteral(30)
        ));
        metadataCreator.addMetadataToTableSchema(new Triple(
            vf.createIRI("http://example.org/person1"),
            vf.createIRI("http://xmlns.com/foaf/0.1/knows"),
            vf.createIRI("http://example.org/person2")
        ));

        // Person 2 with similar properties
        metadataCreator.addMetadataToTableSchema(new Triple(
            vf.createIRI("http://example.org/person2"),
            vf.createIRI("http://xmlns.com/foaf/0.1/name"),
            vf.createLiteral("Jane Smith")
        ));
        metadataCreator.addMetadataToTableSchema(new Triple(
            vf.createIRI("http://example.org/person2"),
            vf.createIRI("http://xmlns.com/foaf/0.1/age"),
            vf.createLiteral(25)
        ));

        // Should have 3 unique columns (name, age, knows)
        assertEquals(3, metadataCreator.tableSchema.getColumns().size());
    }

    @Test
    void testAddMetadataToTableSchema_MixedObjectTypes() {
        metadataCreator.addMetadataToTableSchema(new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/prop1"),
            vf.createLiteral("Literal value")
        ));
        metadataCreator.addMetadataToTableSchema(new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/prop2"),
            vf.createIRI("http://example.org/iri")
        ));
        metadataCreator.addMetadataToTableSchema(new Triple(
            vf.createIRI("http://example.org/subject1"),
            vf.createIRI("http://example.org/prop3"),
            vf.createBNode("node1")
        ));

        assertEquals(3, metadataCreator.tableSchema.getColumns().size());
    }

    @Test
    void testAddMetadataToTableSchema_PreservesColumnOrder() {
        metadataCreator.addMetadataToTableSchema(new Triple(
            vf.createIRI("http://example.org/s1"),
            vf.createIRI("http://example.org/alpha"),
            vf.createLiteral("A")
        ));
        metadataCreator.addMetadataToTableSchema(new Triple(
            vf.createIRI("http://example.org/s1"),
            vf.createIRI("http://example.org/beta"),
            vf.createLiteral("B")
        ));
        metadataCreator.addMetadataToTableSchema(new Triple(
            vf.createIRI("http://example.org/s1"),
            vf.createIRI("http://example.org/gamma"),
            vf.createLiteral("C")
        ));

        assertEquals(3, metadataCreator.tableSchema.getColumns().size());
        assertEquals("alpha", metadataCreator.tableSchema.getColumns().get(0).getName());
        assertEquals("beta", metadataCreator.tableSchema.getColumns().get(1).getName());
        assertEquals("gamma", metadataCreator.tableSchema.getColumns().get(2).getName());
    }
}
