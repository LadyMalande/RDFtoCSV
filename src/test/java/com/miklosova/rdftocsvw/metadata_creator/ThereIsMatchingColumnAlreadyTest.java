package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.TableSchema;
import com.miklosova.rdftocsvw.support.AppConfig;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test coverage for StreamingMetadataCreator.thereIsMatchingColumnAlready(Column, Triple)
 */
class ThereIsMatchingColumnAlreadyTest {

    private StreamingMetadataCreator metadataCreator;
    private AppConfig config;
    private ValueFactory vf;

    @BeforeEach
    void setUp() {
        config = new AppConfig.Builder("test.nt").build();
        metadataCreator = new StreamingMetadataCreator(config);
        metadataCreator.tableSchema = new TableSchema();
        metadataCreator.tableSchema.setColumns(new ArrayList<>());
        vf = SimpleValueFactory.getInstance();
    }

    // ==================== Basic Matching Tests ====================

    @Test
    void testThereIsMatchingColumnAlready_EmptyTableSchema() {
        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertFalse(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_ExactMatch() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_NameMismatch() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name2", "Title 1", "http://prop1", null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertFalse(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_TitleMismatch() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 2", "http://prop1", null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertFalse(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_PropertyUrlMismatch() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop2", null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop2", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertFalse(result);
    }

    // ==================== Case Insensitive Matching Tests ====================

    @Test
    void testThereIsMatchingColumnAlready_NameCaseInsensitive() {
        Column existingColumn = createColumn("Name1", "Title 1", "http://prop1", null, null, null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_TitleCaseInsensitive() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "TITLE 1", "http://prop1", null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_PropertyUrlCaseInsensitive() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "HTTP://PROP1", null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    // ==================== Language Tag Tests ====================

    @Test
    void testThereIsMatchingColumnAlready_SameLangTag() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", "en", null, null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", "en", null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_DifferentLangTag() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", "en", null, null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", "cs", null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertFalse(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_LangTagCaseInsensitive() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", "en", null, null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", "EN", null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_OneNullLangTag() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", "en", null, null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_BothNullLangTag() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    // ==================== Datatype Tests ====================

    @Test
    void testThereIsMatchingColumnAlready_SameDatatype() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, "xsd:string", null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, "xsd:string", null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_DifferentDatatype() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, "xsd:string", null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, "xsd:integer", null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "123");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertFalse(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_DatatypeCaseInsensitive() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, "xsd:string", null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, "XSD:STRING", null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_OneNullDatatype() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, "xsd:string", null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    // ==================== AboutUrl Tests ====================

    @Test
    void testThereIsMatchingColumnAlready_SameAboutUrl() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, 
                "http://example.org/{+Subject}", null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, null, 
                "http://example.org/{+Subject}", null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_DifferentAboutUrl_SetsToGeneric() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, 
                "http://example.org/subject1", null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, null, 
                "http://different.org/subject2", null);
        Triple triple = createTriple("http://different.org/subject2", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
        // Should be updated to generic {+Subject}
        assertEquals("{+Subject}", existingColumn.getAboutUrl());
    }

    @Test
    void testThereIsMatchingColumnAlready_AboutUrlCaseInsensitive() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, 
                "http://example.org/subject", null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, null, 
                "HTTP://EXAMPLE.ORG/SUBJECT", null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_OneNullAboutUrl() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, 
                "http://example.org/subject", null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_BothNullAboutUrl() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    // ==================== ValueUrl Tests ====================

    @Test
    void testThereIsMatchingColumnAlready_SameValueUrl() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, 
                "http://example.org/value");
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, 
                "http://example.org/value");
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_DifferentValueUrl_SetsToGeneric() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, 
                "http://example.org/value1");
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, 
                "http://different.org/value2");
        Triple triple = createTriple("http://different.org/subject", "http://prop1", "value2");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
        // Should be updated to generic {+name1}
        assertEquals("{+name1}", existingColumn.getValueUrl());
    }

    @Test
    void testThereIsMatchingColumnAlready_ValueUrlCaseInsensitive() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, 
                "http://example.org/value");
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, 
                "HTTP://EXAMPLE.ORG/VALUE");
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_OneNullValueUrl() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, 
                "http://example.org/value");
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_BothNullValueUrl() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    // ==================== Multiple Columns Tests ====================

    @Test
    void testThereIsMatchingColumnAlready_MultipleColumnsNoMatch() {
        Column col1 = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        Column col2 = createColumn("name2", "Title 2", "http://prop2", null, null, null, null);
        Column col3 = createColumn("name3", "Title 3", "http://prop3", null, null, null, null);
        metadataCreator.tableSchema.getColumns().add(col1);
        metadataCreator.tableSchema.getColumns().add(col2);
        metadataCreator.tableSchema.getColumns().add(col3);

        Column newColumn = createColumn("name4", "Title 4", "http://prop4", null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop4", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertFalse(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_MultipleColumnsMatchFirst() {
        Column col1 = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        Column col2 = createColumn("name2", "Title 2", "http://prop2", null, null, null, null);
        Column col3 = createColumn("name3", "Title 3", "http://prop3", null, null, null, null);
        metadataCreator.tableSchema.getColumns().add(col1);
        metadataCreator.tableSchema.getColumns().add(col2);
        metadataCreator.tableSchema.getColumns().add(col3);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_MultipleColumnsMatchLast() {
        Column col1 = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        Column col2 = createColumn("name2", "Title 2", "http://prop2", null, null, null, null);
        Column col3 = createColumn("name3", "Title 3", "http://prop3", null, null, null, null);
        metadataCreator.tableSchema.getColumns().add(col1);
        metadataCreator.tableSchema.getColumns().add(col2);
        metadataCreator.tableSchema.getColumns().add(col3);

        Column newColumn = createColumn("name3", "Title 3", "http://prop3", null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop3", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    // ==================== Complex Matching Tests ====================

    @Test
    void testThereIsMatchingColumnAlready_AllPropertiesMatch() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", "en", "xsd:string", 
                "http://example.org/{+Subject}", "http://example.org/{+name1}");
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", "http://prop1", "en", "xsd:string", 
                "http://example.org/{+Subject}", "http://example.org/{+name1}");
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_NullPropertyUrl() {
        Column existingColumn = createColumn("name1", "Title 1", null, null, null, null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", null, null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    @Test
    void testThereIsMatchingColumnAlready_OneNullPropertyUrl() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn = createColumn("name1", "Title 1", null, null, null, null, null);
        Triple triple = createTriple("http://example.org/subject", "http://prop1", "value");

        boolean result = metadataCreator.thereIsMatchingColumnAlready(newColumn, triple);

        assertTrue(result);
    }

    // ==================== Cache Tests ====================

    @Test
    void testThereIsMatchingColumnAlready_CacheHit() {
        Column existingColumn = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        metadataCreator.tableSchema.getColumns().add(existingColumn);

        Column newColumn1 = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        Triple triple1 = createTriple("http://example.org/subject1", "http://prop1", "value");

        // First call should populate cache
        boolean result1 = metadataCreator.thereIsMatchingColumnAlready(newColumn1, triple1);
        assertTrue(result1);

        // Second call with same column should hit cache
        Column newColumn2 = createColumn("name1", "Title 1", "http://prop1", null, null, null, null);
        Triple triple2 = createTriple("http://example.org/subject2", "http://prop1", "value");

        boolean result2 = metadataCreator.thereIsMatchingColumnAlready(newColumn2, triple2);
        assertTrue(result2);
    }

    // ==================== Helper Methods ====================

    /**
     * Helper to create a Column with specified properties
     */
    private Column createColumn(String name, String titles, String propertyUrl, 
                                String lang, String datatype, String aboutUrl, String valueUrl) {
        Column column = new Column();
        column.setName(name);
        column.setTitles(titles);
        column.setPropertyUrl(propertyUrl);
        column.setLang(lang);
        column.setDatatype(datatype);
        column.setAboutUrl(aboutUrl);
        column.setValueUrl(valueUrl);
        return column;
    }

    /**
     * Helper to create a Triple
     */
    private Triple createTriple(String subject, String predicate, String object) {
        return new Triple(
            vf.createIRI(subject),
            vf.createIRI(predicate),
            vf.createLiteral(object)
        );
    }
}
