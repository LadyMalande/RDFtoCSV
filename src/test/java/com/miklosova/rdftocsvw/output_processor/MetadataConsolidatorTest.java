package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.TableSchema;
import com.miklosova.rdftocsvw.support.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 parameterized tests for MetadataConsolidator.
 */
@DisplayName("MetadataConsolidator Tests")
class MetadataConsolidatorTest {

    private AppConfig testConfig;
    private MetadataConsolidator consolidator;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("metadata-consolidator-test");
        testConfig = new AppConfig.Builder("test-input.ttl")
                .parsing("rdf4j")
                .output(tempDir.resolve("output").toString())
                .build();
        consolidator = new MetadataConsolidator(testConfig);
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("Constructor should throw IllegalArgumentException when AppConfig is null")
    void constructor_shouldThrowException_whenConfigIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new MetadataConsolidator(null)
        );
        assertEquals("AppConfig cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Constructor should create instance with valid AppConfig")
    void constructor_shouldCreateInstance_whenConfigIsValid() {
        assertNotNull(consolidator);
    }

    // ========== getMatchingColumn Tests ==========

    @ParameterizedTest(name = "Should find matching column with propertyUrl: {0}")
    @CsvSource({
            "http://example.org/name, name, true",
            "http://example.org/age, age, true",
            "http://example.org/email, email, true"
    })
    @DisplayName("getMatchingColumn should find matching column by propertyUrl")
    void getMatchingColumn_shouldFindMatch_whenPropertyUrlMatches(String propertyUrl, String columnName, boolean shouldMatch) {
        // Arrange
        List<Table> tables = new ArrayList<>();
        
        // Create table 1
        Table table1 = new Table("table1.csv", testConfig);
        TableSchema schema1 = new TableSchema();
        Column col1 = new Column();
        col1.setName(columnName);
        col1.setPropertyUrl(propertyUrl);
        schema1.getColumns().add(col1);
        table1.setTableSchema(schema1);
        tables.add(table1);

        // Create table 2 (current table)
        Table currentTable = new Table("table2.csv", testConfig);
        TableSchema schema2 = new TableSchema();
        currentTable.setTableSchema(schema2);
        tables.add(currentTable);

        // Create column to check
        Column columnToCheck = new Column();
        columnToCheck.setName(columnName);
        columnToCheck.setPropertyUrl(propertyUrl);

        // Act
        Table result = MetadataConsolidator.getMatchingColumn(tables, currentTable, columnToCheck);

        // Assert
        if (shouldMatch) {
            assertNotNull(result);
            assertEquals(table1, result);
        }
    }

    @Test
    @DisplayName("getMatchingColumn should return null when no match found")
    void getMatchingColumn_shouldReturnNull_whenNoMatchFound() {
        // Arrange
        List<Table> tables = new ArrayList<>();
        Table currentTable = new Table("current.csv", testConfig);
        TableSchema schema = new TableSchema();
        currentTable.setTableSchema(schema);
        tables.add(currentTable);

        Column columnToCheck = new Column();
        columnToCheck.setName("nonexistent");
        columnToCheck.setPropertyUrl("http://example.org/nonexistent");

        // Act
        Table result = MetadataConsolidator.getMatchingColumn(tables, currentTable, columnToCheck);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("getMatchingColumn should exclude current table from search")
    void getMatchingColumn_shouldExcludeCurrentTable() {
        // Arrange
        List<Table> tables = new ArrayList<>();
        Table currentTable = new Table("current.csv", testConfig);
        TableSchema schema = new TableSchema();
        Column col = new Column();
        col.setName("testCol");
        col.setPropertyUrl("http://example.org/test");
        schema.getColumns().add(col);
        currentTable.setTableSchema(schema);
        tables.add(currentTable);

        Column columnToCheck = new Column();
        columnToCheck.setName("testCol");
        columnToCheck.setPropertyUrl("http://example.org/test");

        // Act
        Table result = MetadataConsolidator.getMatchingColumn(tables, currentTable, columnToCheck);

        // Assert
        assertNull(result, "Should not match column in the current table");
    }

    @Test
    @DisplayName("getMatchingColumn should skip subject columns")
    void getMatchingColumn_shouldSkipSubjectColumns() {
        // Arrange
        List<Table> tables = new ArrayList<>();
        
        Table table1 = new Table("table1.csv", testConfig);
        TableSchema schema1 = new TableSchema();
        Column subjectCol = new Column();
        subjectCol.setName("subject");
        subjectCol.setPropertyUrl("http://example.org/test");
        schema1.getColumns().add(subjectCol);
        table1.setTableSchema(schema1);
        tables.add(table1);

        Table currentTable = new Table("table2.csv", testConfig);
        TableSchema schema2 = new TableSchema();
        currentTable.setTableSchema(schema2);
        tables.add(currentTable);

        Column columnToCheck = new Column();
        columnToCheck.setName("testCol");
        columnToCheck.setPropertyUrl("http://example.org/test");

        // Act
        Table result = MetadataConsolidator.getMatchingColumn(tables, currentTable, columnToCheck);

        // Assert
        assertNull(result, "Should skip columns named 'subject'");
    }

    // ========== consolidateMetadata Tests ==========

    @Test
    @DisplayName("consolidateMetadata should create merged table")
    void consolidateMetadata_shouldCreateMergedTable() {
        // Arrange
        Metadata oldMetadata = new Metadata(testConfig);
        
        Table table1 = new Table("table1.csv", testConfig);
        TableSchema schema1 = new TableSchema();
        Column col1 = new Column();
        col1.setName("name");
        col1.setPropertyUrl("http://example.org/name");
        schema1.getColumns().add(col1);
        table1.setTableSchema(schema1);
        oldMetadata.getTables().add(table1);

        // Act
        Metadata result = consolidator.consolidateMetadata(oldMetadata, testConfig);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTables().size());
        assertTrue(result.getTables().get(0).getUrl().contains("_merged.csv"));
    }

    @Test
    @DisplayName("consolidateMetadata should merge columns from multiple tables")
    void consolidateMetadata_shouldMergeColumnsFromMultipleTables() {
        // Arrange
        Metadata oldMetadata = new Metadata(testConfig);
        
        // Table 1
        Table table1 = new Table("table1.csv", testConfig);
        TableSchema schema1 = new TableSchema();
        Column col1 = new Column();
        col1.setName("name");
        col1.setPropertyUrl("http://example.org/name");
        schema1.getColumns().add(col1);
        table1.setTableSchema(schema1);
        oldMetadata.getTables().add(table1);

        // Table 2
        Table table2 = new Table("table2.csv", testConfig);
        TableSchema schema2 = new TableSchema();
        Column col2 = new Column();
        col2.setName("age");
        col2.setPropertyUrl("http://example.org/age");
        schema2.getColumns().add(col2);
        table2.setTableSchema(schema2);
        oldMetadata.getTables().add(table2);

        // Act
        Metadata result = consolidator.consolidateMetadata(oldMetadata, testConfig);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTables().size());
        Table mergedTable = result.getTables().get(0);
        assertEquals(2, mergedTable.getTableSchema().getColumns().size());
    }

    @Test
    @DisplayName("consolidateMetadata should handle duplicate column names")
    void consolidateMetadata_shouldHandleDuplicateColumnNames() {
        // Arrange
        Metadata oldMetadata = new Metadata(testConfig);
        
        // Table 1
        Table table1 = new Table("table1.csv", testConfig);
        TableSchema schema1 = new TableSchema();
        Column col1 = new Column();
        col1.setName("value");
        col1.setPropertyUrl("http://example.org/value1");
        schema1.getColumns().add(col1);
        table1.setTableSchema(schema1);
        oldMetadata.getTables().add(table1);

        // Table 2 with same column name but different propertyUrl
        Table table2 = new Table("table2.csv", testConfig);
        TableSchema schema2 = new TableSchema();
        Column col2 = new Column();
        col2.setName("value");
        col2.setPropertyUrl("http://example.org/value2");
        schema2.getColumns().add(col2);
        table2.setTableSchema(schema2);
        oldMetadata.getTables().add(table2);

        // Act
        Metadata result = consolidator.consolidateMetadata(oldMetadata, testConfig);

        // Assert
        assertNotNull(result);
        Table mergedTable = result.getTables().get(0);
        assertEquals(2, mergedTable.getTableSchema().getColumns().size());
        
        // Check that duplicate names are renamed
        List<Column> columns = mergedTable.getTableSchema().getColumns();
        assertTrue(columns.stream().anyMatch(c -> c.getName().equals("value")));
        assertTrue(columns.stream().anyMatch(c -> c.getName().startsWith("value_")));
    }

    @Test
    @DisplayName("consolidateMetadata should not duplicate columns with same propertyUrl")
    void consolidateMetadata_shouldNotDuplicateColumnsByPropertyUrl() {
        // Arrange
        Metadata oldMetadata = new Metadata(testConfig);
        
        // Table 1
        Table table1 = new Table("table1.csv", testConfig);
        TableSchema schema1 = new TableSchema();
        Column col1 = new Column();
        col1.setName("name");
        col1.setPropertyUrl("http://example.org/name");
        schema1.getColumns().add(col1);
        table1.setTableSchema(schema1);
        oldMetadata.getTables().add(table1);

        // Table 2 with same column
        Table table2 = new Table("table2.csv", testConfig);
        TableSchema schema2 = new TableSchema();
        Column col2 = new Column();
        col2.setName("name");
        col2.setPropertyUrl("http://example.org/name");
        schema2.getColumns().add(col2);
        table2.setTableSchema(schema2);
        oldMetadata.getTables().add(table2);

        // Act
        Metadata result = consolidator.consolidateMetadata(oldMetadata, testConfig);

        // Assert
        assertNotNull(result);
        Table mergedTable = result.getTables().get(0);
        assertEquals(1, mergedTable.getTableSchema().getColumns().size(), 
                "Should not duplicate columns with same propertyUrl");
    }

    @ParameterizedTest
    @MethodSource("provideMetadataConsolidationScenarios")
    @DisplayName("consolidateMetadata with various scenarios")
    void consolidateMetadata_variousScenarios(String scenario, Metadata input, int expectedColumnCount) {
        // Act
        Metadata result = consolidator.consolidateMetadata(input, testConfig);

        // Assert
        assertNotNull(result, "Result should not be null for scenario: " + scenario);
        assertEquals(1, result.getTables().size(), "Should have 1 merged table for scenario: " + scenario);
        assertEquals(expectedColumnCount, result.getTables().get(0).getTableSchema().getColumns().size(),
                "Column count mismatch for scenario: " + scenario);
    }

    static Stream<Arguments> provideMetadataConsolidationScenarios() {
        AppConfig config = new AppConfig.Builder("test.ttl").build();
        
        // Scenario 1: Empty metadata
        Metadata emptyMetadata = new Metadata(config);
        
        // Scenario 2: Single table with one column
        Metadata singleTableMetadata = new Metadata(config);
        Table table1 = new Table("table1.csv", config);
        TableSchema schema1 = new TableSchema();
        Column col1 = new Column();
        col1.setName("col1");
        col1.setPropertyUrl("http://example.org/col1");
        schema1.getColumns().add(col1);
        table1.setTableSchema(schema1);
        singleTableMetadata.getTables().add(table1);
        
        // Scenario 3: Multiple tables with unique columns
        Metadata multiTableMetadata = new Metadata(config);
        for (int i = 0; i < 3; i++) {
            Table table = new Table("table" + i + ".csv", config);
            TableSchema schema = new TableSchema();
            Column col = new Column();
            col.setName("col" + i);
            col.setPropertyUrl("http://example.org/col" + i);
            schema.getColumns().add(col);
            table.setTableSchema(schema);
            multiTableMetadata.getTables().add(table);
        }

        return Stream.of(
                Arguments.of("Empty metadata", emptyMetadata, 0),
                Arguments.of("Single table with one column", singleTableMetadata, 1),
                Arguments.of("Multiple tables with unique columns", multiTableMetadata, 3)
        );
    }

    // ========== getFilePathForFileName Tests ==========

    @Test
    @DisplayName("getFilePathForFileName should throw exception when config is null")
    void getFilePathForFileName_shouldThrowException_whenConfigIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> MetadataConsolidator.getFilePathForFileName("test.csv", null)
        );
        assertEquals("AppConfig cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("getFilePathForFileName should return matching file path")
    void getFilePathForFileName_shouldReturnMatchingPath() {
        // Arrange
        AppConfig config = new AppConfig.Builder("test.ttl")
                .parsing("rdf4j")
                .build();
        config.setIntermediateFileNames("/path/to/file1.csv,/path/to/table.csv,/path/to/file2.csv");

        // Act
        String result = MetadataConsolidator.getFilePathForFileName("table.csv", config);

        // Assert
        assertNotNull(result);
        assertEquals("/path/to/table.csv", result);
    }

    @Test
    @DisplayName("getFilePathForFileName should return null when no match found")
    void getFilePathForFileName_shouldReturnNull_whenNoMatchFound() {
        // Arrange
        AppConfig config = new AppConfig.Builder("test.ttl")
                .parsing("rdf4j")
                .build();
        config.setIntermediateFileNames("/path/to/file1.csv,/path/to/file2.csv");

        // Act
        String result = MetadataConsolidator.getFilePathForFileName("nonexistent.csv", config);

        // Assert
        assertNull(result);
    }

    @ParameterizedTest
    @CsvSource({
            "table1.csv, /tmp/table1.csv",
            "data.csv, /home/user/data.csv",
            "output.csv, ./output/output.csv"
    })
    @DisplayName("getFilePathForFileName should match various file patterns")
    void getFilePathForFileName_shouldMatchVariousPatterns(String filename, String expectedPath) {
        // Arrange
        AppConfig config = new AppConfig.Builder("test.ttl")
                .parsing("rdf4j")
                .build();
        config.setIntermediateFileNames("/tmp/table1.csv,/home/user/data.csv,./output/output.csv");

        // Act
        String result = MetadataConsolidator.getFilePathForFileName(filename, config);

        // Assert
        assertEquals(expectedPath, result);
    }

    @Test
    @DisplayName("getFilePathForFileName should handle empty intermediate file names")
    void getFilePathForFileName_shouldHandleEmptyIntermediateFiles() {
        // Arrange
        AppConfig config = new AppConfig.Builder("test.ttl")
                .parsing("rdf4j")
                .build();
        config.setIntermediateFileNames("");

        // Act
        String result = MetadataConsolidator.getFilePathForFileName("table.csv", config);

        // Assert
        assertNull(result);
    }

    // ========== Integration Tests ==========

    @Test
    @DisplayName("Integration: consolidate metadata with columns having different languages")
    void integration_consolidateMetadataWithLanguageTags() {
        // Arrange
        Metadata oldMetadata = new Metadata(testConfig);
        
        Table table1 = new Table("table1.csv", testConfig);
        TableSchema schema1 = new TableSchema();
        Column col1 = new Column();
        col1.setName("label");
        col1.setPropertyUrl("http://example.org/label");
        col1.setLang("en");
        schema1.getColumns().add(col1);
        table1.setTableSchema(schema1);
        oldMetadata.getTables().add(table1);

        Table table2 = new Table("table2.csv", testConfig);
        TableSchema schema2 = new TableSchema();
        Column col2 = new Column();
        col2.setName("label");
        col2.setPropertyUrl("http://example.org/label");
        col2.setLang("de");
        schema2.getColumns().add(col2);
        table2.setTableSchema(schema2);
        oldMetadata.getTables().add(table2);

        // Act
        Metadata result = consolidator.consolidateMetadata(oldMetadata, testConfig);

        // Assert
        assertNotNull(result);
        Table mergedTable = result.getTables().get(0);
        assertEquals(2, mergedTable.getTableSchema().getColumns().size(), 
                "Should keep both language variants as separate columns");
    }

    @Test
    @DisplayName("Integration: consolidate metadata with suppressOutput columns")
    void integration_consolidateMetadataWithSuppressOutput() {
        // Arrange
        Metadata oldMetadata = new Metadata(testConfig);
        
        Table table1 = new Table("table1.csv", testConfig);
        TableSchema schema1 = new TableSchema();
        Column col1 = new Column();
        col1.setName("hidden");
        col1.setPropertyUrl("http://example.org/hidden");
        col1.setSuppressOutput(true);
        schema1.getColumns().add(col1);
        table1.setTableSchema(schema1);
        oldMetadata.getTables().add(table1);

        Table table2 = new Table("table2.csv", testConfig);
        TableSchema schema2 = new TableSchema();
        Column col2 = new Column();
        col2.setName("visible");
        col2.setPropertyUrl("http://example.org/visible");
        col2.setSuppressOutput(false);
        schema2.getColumns().add(col2);
        table2.setTableSchema(schema2);
        oldMetadata.getTables().add(table2);

        // Act
        Metadata result = consolidator.consolidateMetadata(oldMetadata, testConfig);

        // Assert
        assertNotNull(result);
        Table mergedTable = result.getTables().get(0);
        assertEquals(2, mergedTable.getTableSchema().getColumns().size());
    }
}