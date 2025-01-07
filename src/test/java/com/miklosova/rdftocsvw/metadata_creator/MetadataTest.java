package com.miklosova.rdftocsvw.metadata_creator;

// import com.miklosova.rdftocsvw.metadata_creator.metadata.Metadata;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.junit.jupiter.api.Test;
// import com.fasterxml.jackson.annotation.JsonAutoDetect;
// import java.io.File;
// import com.miklosova.rdftocsvw.support.JsonUtil;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import com.miklosova.rdftocsvw.convertor.TypeIdAndValues;
// import ioinformarics.oss.jackson.module.jsonld.JsonldModule;
// import static org.mockito.Mockito.*;
// import java.util.HashMap;
// import com.miklosova.rdftocsvw.convertor.Row;
// import org.eclipse.rdf4j.model.Value;
// import java.util.List;
// import org.junit.jupiter.api.BeforeEach;
// import java.util.Map;
// import com.fasterxml.jackson.annotation.PropertyAccessor;
// import org.junit.jupiter.params.ParameterizedTest;
// import com.fasterxml.jackson.databind.SerializationFeature;
// import org.eclipse.rdf4j.model.IRI;
// import java.util.stream.Stream;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;
// import java.util.ArrayList;
// import static org.junit.jupiter.api.Assertions.*;
// import org.junit.jupiter.params.provider.Arguments;
// import static org.hamcrest.Matchers.startsWith;
// import static org.hamcrest.Matchers.endsWith;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;

class MetadataTest {

//     private Metadata metadata;

//     @Mock
//     private JsonUtil jsonUtil;

//     @Mock
//     private ObjectMapper objectMapper;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//         metadata = new Metadata();
//         metadata.getTables().clear();
//     }

//     //BaseRock generated method id: ${testJsonldMetadata}, hash: EE0C2CCA497AA133D92C08FECBDB081B
//     @Test
//     void testJsonldMetadata() throws Exception {
//         String expectedJson = "{\"@context\":{},\"@type\":\"TableGroup\",\"tables\":[]}";
//         when(JsonUtil.serializeAndWriteToFile(any(Metadata.class))).thenReturn(expectedJson);
//         String result = metadata.jsonldMetadata();
//         assertEquals(expectedJson, result);
//         verify(JsonUtil.class, times(1)).serializeAndWriteToFile(any(Metadata.class));
//     }

//     //BaseRock generated method id: ${testAddMetadata}, hash: FF57FC690A66B64F6B2A9B2DF7817AE5
//     @Test
//     void testAddMetadata() {
//         String fileName = "test.csv";
//         ArrayList<Value> keys = new ArrayList<>();
//         ArrayList<Row> rows = new ArrayList<>();
//         metadata.addMetadata(fileName, keys, rows);
//         assertEquals(1, metadata.getTables().size());
//         assertEquals("test.csv", metadata.getTables().get(0).getUrl());
//     }

//     //BaseRock generated method id: ${testGetTables}, hash: F169C2F411908770EF139F66E9CCAFB7
//     @Test
//     void testGetTables() {
//         List<Table> tables = metadata.getTables();
//         assertNotNull(tables);
//         assertTrue(tables.isEmpty());
//     }

//     //BaseRock generated method id: ${testAddForeignKeys}, hash: C075FE5297AF798FC5F7E08F8E835BAA
//     @ParameterizedTest
//     @MethodSource("provideTestCasesForAddForeignKeys")
//     void testAddForeignKeys(ArrayList<ArrayList<Row>> allRows, int expectedForeignKeyCount) {
//         // Prepare test data
//         Table table1 = new Table("table1.csv");
//         Table table2 = new Table("table2.csv");
//         metadata.getTables().add(table1);
//         metadata.getTables().add(table2);
//         TableSchema schema1 = new TableSchema();
//         TableSchema schema2 = new TableSchema();
//         table1.setTableSchema(schema1);
//         table2.setTableSchema(schema2);
//         ArrayList<Column> columns1 = new ArrayList<>();
//         ArrayList<Column> columns2 = new ArrayList<>();
//         columns1.add(new Column("Column1", "http://example.com/Column1"));
//         columns2.add(new Column("Column2", "http://example.com/Column2"));
//         schema1.setColumns(columns1);
//         schema2.setColumns(columns2);
//         // Execute
//         metadata.addForeignKeys(allRows);
//         // Verify
//         int actualForeignKeyCount = 0;
//         for (Table table : metadata.getTables()) {
//             List<ForeignKey> foreignKeys = table.getTableSchema().getForeignKeys();
//             if (foreignKeys != null) {
//                 actualForeignKeyCount += foreignKeys.size();
//             }
//         }
//         assertEquals(expectedForeignKeyCount, actualForeignKeyCount);
//     }

//     private static Stream<Arguments> provideTestCasesForAddForeignKeys() {
//         return Stream.of(Arguments.of(new ArrayList<ArrayList<Row>>(), 0), Arguments.of(createTestRows(), 1));
//     }

//     private static ArrayList<ArrayList<Row>> createTestRows() {
//         ArrayList<ArrayList<Row>> allRows = new ArrayList<>();
//         ArrayList<Row> rows = new ArrayList<>();
//         Row row = new Row();
//         row.id = createMockIRI("http://example.com/id1");
//         row.type = createMockIRI("http://example.com/Column2");
//         Map<Value, TypeIdAndValues> columns = new HashMap<>();
//         columns.put(createMockIRI("http://example.com/Column1"), new TypeIdAndValues(createMockIRI("http://example.com/id1"), new ArrayList<>()));
//         row.columns = columns;
//         rows.add(row);
//         allRows.add(rows);
//         return allRows;
//     }

//     private static IRI createMockIRI(String iriString) {
//         IRI mockIRI = mock(IRI.class);
//         when(mockIRI.stringValue()).thenReturn(iriString);
//         when(mockIRI.getLocalName()).thenReturn(iriString.substring(iriString.lastIndexOf('/') + 1));
//         return mockIRI;
//     }
}