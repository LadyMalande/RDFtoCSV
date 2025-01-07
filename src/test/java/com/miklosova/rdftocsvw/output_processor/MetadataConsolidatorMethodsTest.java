package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.converter.RDFtoCSV;

import java.util.Arrays;

import com.miklosova.rdftocsvw.support.BaseTest;

import java.util.List;

import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.BeforeEach;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.TableSchema;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import org.jruby.ir.Tuple;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import java.util.ArrayList;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class MetadataConsolidatorMethodsTest extends BaseTest {

    private MetadataConsolidator metadataConsolidator;

    @BeforeEach
    void setUp() {
        metadataConsolidator = new MetadataConsolidator();
        rdfToCSV = new RDFtoCSV(fileName);
        db = new SailRepository(new MemoryStore());
    }

    @Test
    void testGetMatchingColumn() {
        Table table1 = new Table("table1");
        Table table2 = new Table("table2");
        Table table3 = new Table("table3");
        Column column1 = new Column();
        column1.setName("column1");
        column1.setPropertyUrl("http://example.com/property1");
        Column column4 = new Column();
        column4.setName("column4");
        column4.setPropertyUrl("http://example.com/property4");
        Column column2 = new Column();
        column2.setName("column2");
        column2.setPropertyUrl("http://example.com/property2");
        Column column3 = new Column();
        column3.setName("subject");
        column3.setPropertyUrl("http://example.com/subject");
        TableSchema schema1 = new TableSchema();
        schema1.setColumns(Arrays.asList(column1, column3, column4));
        table1.setTableSchema(schema1);
        TableSchema schema2 = new TableSchema();
        schema2.setColumns(Arrays.asList(column2, column3));
        table2.setTableSchema(schema2);
        TableSchema schema3 = new TableSchema();
        schema3.setColumns(Arrays.asList(column1, column2, column3));
        table3.setTableSchema(schema3);
        List<Table> tables = Arrays.asList( table1, table2, table3);
        assertNull(MetadataConsolidator.getMatchingColumn(tables, table1, column4));
        assertEquals(table3, MetadataConsolidator.getMatchingColumn(tables, table1, column1));
        assertNull(MetadataConsolidator.getMatchingColumn(tables, table1, column3));
    }

    //BaseRock generated method id: ${testConsolidateMetadata}, hash: 6684495C5C998D5F50E91795FDDDB91B
    @Test
    void testConsolidateMetadata() {
        Metadata oldMetadata = new Metadata();
        Table table1 = new Table("table1.csv");
        Table table2 = new Table("table2.csv");
        Column column1 = new Column();
        column1.setName("column1");
        column1.setPropertyUrl("http://example.com/property1");
        Column column2 = new Column();
        column2.setName("column2");
        column2.setPropertyUrl("http://example.com/property2");
        Column column3 = new Column();
        column3.setName("column1");
        column3.setPropertyUrl("http://example.com/property3");
        TableSchema schema1 = new TableSchema();
        schema1.setColumns(Arrays.asList(column1, column2));
        table1.setTableSchema(schema1);
        TableSchema schema2 = new TableSchema();
        schema2.setColumns(Arrays.asList(column2, column3));
        table2.setTableSchema(schema2);
        oldMetadata.getTables().add(table1);
        oldMetadata.getTables().add(table2);
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
    mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_FILENAME)).thenReturn("output.csv");
            mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME)).thenReturn("../RDFtoCSV/src/test/resources/StreamingNTriples/testingInput.nt.csv-metadata.json");

            Metadata newMetadata = metadataConsolidator.consolidateMetadata(oldMetadata);
    assertNotNull(newMetadata);
    assertEquals(1, newMetadata.getTables().size());
    Table consolidatedTable = newMetadata.getTables().get(0);
    assertEquals("output.csv_merged.csv", consolidatedTable.getUrl());
    assertEquals(3, consolidatedTable.getTableSchema().getColumns().size());
    List<String> columnNames = new ArrayList<>();
    for (Column column : consolidatedTable.getTableSchema().getColumns()) {
        columnNames.add(column.getName());
    }
    assertTrue(columnNames.contains("column1"));
    assertTrue(columnNames.contains("column2"));
    assertTrue(columnNames.contains("column1_1"));
}
    }

    //BaseRock generated method id: ${testGetFilePathForFileName}, hash: 39EF3D411B874EA5AA76861DADF028E8
    @ParameterizedTest
    @CsvSource({ "file1.csv,/path/to/file1.csv,/path/to/file1.csv", "file2.csv,/path/to/file2.csv,/path/to/file2.csv", "nonexistent.csv,/path/to/file1.csv;/path/to/file2.csv,null" })
    void testGetFilePathForFileName(String url, String intermediateFiles, String expectedResult) {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
            mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES)).thenReturn(intermediateFiles);
            String result = MetadataConsolidator.getFilePathForFileName(url);
            if ("null".equals(expectedResult)) {
                assertNull(result);
            } else {
                assertEquals(expectedResult, result);
            }
        }
    }

    //BaseRock generated method id: ${testFirstColumnHasLinksToAnotherColumn}, hash: 6B1B15C99031FCE14B72E4BB1FA212AD
    @Test
    @Disabled
    void testFirstColumnHasLinksToAnotherColumn() throws Exception {
        Metadata oldMetadata = new Metadata();
        Table table1 = new Table("table1.csv");
        Table table2 = new Table("table2.csv");
        Column subject = new Column();
        subject.setName("subject");
        TableSchema schema1 = new TableSchema();
        schema1.setColumns(Arrays.asList(subject));
        table1.setTableSchema(schema1);
        TableSchema schema2 = new TableSchema();
        schema2.setColumns(Arrays.asList(subject));
        table2.setTableSchema(schema2);
        oldMetadata.getTables().add(table1);
        oldMetadata.getTables().add(table2);
        String csvContent1 = "subject,column1\nvalue1,value2\nvalue3,value4";
        String csvContent2 = "subject,column2\nvalue2,somedata\nvalue4,moredata";
        Tuple<String, String> result = metadataConsolidator.firstColumnHasLinksToAnotherColumn(oldMetadata, subject, table1);
        assertNotNull(result);
        assertEquals("table2.csv", result.a);
        assertEquals("subject", result.b);


    }
}