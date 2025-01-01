package com.miklosova.rdftocsvw.support;

 import com.miklosova.rdftocsvw.convertor.RDFtoCSV;
 import com.miklosova.rdftocsvw.convertor.Row;
 import com.miklosova.rdftocsvw.metadata_creator.Table;
 import com.miklosova.rdftocsvw.metadata_creator.TableSchema;
 import com.fasterxml.jackson.databind.node.ObjectNode;

 import org.eclipse.rdf4j.model.Value;
 import org.eclipse.rdf4j.repository.Repository;
 import org.eclipse.rdf4j.repository.sail.SailRepository;
 import org.eclipse.rdf4j.sail.memory.MemoryStore;
 import org.junit.jupiter.api.BeforeEach;
 import java.nio.file.Path;

 import org.junit.jupiter.api.Test;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import org.junit.jupiter.api.io.TempDir;
 import com.fasterxml.jackson.databind.ObjectMapper;

 import static org.eclipse.rdf4j.model.util.Values.iri;
 import static org.junit.jupiter.api.Assertions.*;
 import org.mockito.Mockito;
 import org.mockito.MockedStatic;
 import static org.mockito.Mockito.*;
 import com.miklosova.rdftocsvw.metadata_creator.Metadata;
 import java.io.IOException;
 import java.util.ArrayList;

 import static org.mockito.ArgumentMatchers.any;
 import static org.mockito.ArgumentMatchers.eq;

class JsonUtilTest {

     private ObjectMapper mockMapper;

     private ConfigurationManager mockConfigManager;

     private FileWrite mockFileWrite;
    Metadata metadata;
    RDFtoCSV rdfToCSV;
    Repository db;
    String[] args;

    private String fileName = "/RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt";

     @BeforeEach
     void setUp() {
         rdfToCSV = new RDFtoCSV(fileName);
         db = new SailRepository(new MemoryStore());
         args = new String[]{"-f", "test.rdf", "-p", "rdf4j"};
         ConfigurationManager.loadSettingsFromInputToConfigFile(args);

         metadata = new Metadata();
         Table table = new Table("test.csv");

         ArrayList<Value> keys = new ArrayList<>();
         keys.add(iri("http://predicate1.cz"));
         keys.add(iri("http://predicate2.cz"));
         ArrayList<Value> keys1 = new ArrayList<>();
         keys1.add(iri("http://predicate3.cz"));
         keys1.add(iri("http://predicate4.cz"));
         Row firstRow = new Row(iri("http://subject1.cz"), iri("http://predicate1.cz") , true);
         Row secondRow = new Row(iri("http://subject2.cz"), iri("http://predicate2.cz") , true);
         ArrayList<Row> rows = new ArrayList<>();
         rows.add(firstRow);
         rows.add(secondRow);
         TableSchema tableSchema = new TableSchema(keys,rows);
         tableSchema.addTableSchemaMetadata();
         table.setTableSchema(tableSchema);
         mockMapper = mock(ObjectMapper.class);
         mockConfigManager = mock(ConfigurationManager.class);
         mockFileWrite = mock(FileWrite.class);
     }

     //BaseRock generated method id: ${testSerializeWithContext}, hash: 91C327EBBC373742EE2FFE5D5D896E88
     @Test
     void testSerializeWithContext() throws JsonProcessingException {
         Object testObj = new Object();
         String expectedResult = "{\"@context\":\"http://www.w3.org/ns/csvw\",\"@type\":\"TableGroup\",\"tables\":[]}";
         ObjectNode mockResultNode = mock(ObjectNode.class);
         try (MockedStatic<JsonUtil> mockedJsonUtil = Mockito.mockStatic(JsonUtil.class);
             MockedStatic<ConfigurationManager> mockedConfigManager = Mockito.mockStatic(ConfigurationManager.class)) {
             mockedJsonUtil.when(() -> JsonUtil.serializeWithContext(any())).thenCallRealMethod();
             //when(mockMapper.valueToTree(testObj)).thenReturn(mockJsonObject);
             when(mockMapper.createObjectNode()).thenReturn(mockResultNode);
             ObjectNode result = JsonUtil.serializeWithContext(metadata);
             //verify(mockResultNode).put("@context", "http://www.w3.org/ns/csvw");
             //verify(mockResultNode).setAll(mockJsonObject);
             assertEquals(expectedResult.toString(), result.toString());
         }
     }

     //BaseRock generated method id: ${testWriteJsonToFile}, hash: 26CEE7FA8A28A9F507832B2016C16D58
     @Test
     void testWriteJsonToFile(@TempDir Path tempDir) throws IOException {
         String testFileName = "test.json";
         Path testFilePath = tempDir.resolve(testFileName);
         ObjectNode mockResultNode = mock(ObjectNode.class);
         try (MockedStatic<ConfigurationManager> mockedConfigManager = Mockito.mockStatic(ConfigurationManager.class);
             MockedStatic<FileWrite> mockedFileWrite = Mockito.mockStatic(FileWrite.class)) {
             mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME)).thenReturn(testFilePath.toString());
             JsonUtil.writeJsonToFile(mockResultNode);
             mockedFileWrite.verify(() -> FileWrite.deleteFile(testFilePath.toString()));
             //verify(mockMapper.writerWithDefaultPrettyPrinter()).writeValue(eq(new File(testFilePath.toString())), eq(mockResultNode));
         }
     }

     //BaseRock generated method id: ${testSerializeAndWriteToFile}, hash: 4DFD147373C182D29AB47420F6A4C4C8
     @Test
     void testSerializeAndWriteToFile() throws JsonProcessingException {
         String expectedJson = "{\"@context\":\"http://www.w3.org/ns/csvw\",\"@type\":\"TableGroup\",\"tables\":[]}";

             String result = JsonUtil.serializeAndWriteToFile(metadata);
             assertEquals(expectedJson, result);

     }

     //BaseRock generated method id: ${testSerializeAndReturnPrettyString}, hash: 2E159A0FBA306B8BBC341C0D153C77FD
     @Test
     void testSerializeAndReturnPrettyString() throws JsonProcessingException {
         /*
         Object testObj = new Object();
         ObjectNode mockResultNode = mock(ObjectNode.class);
         String expectedJson = "{\"test\":\"json\"}";
         try (MockedStatic<JsonUtil> mockedJsonUtil = Mockito.mockStatic(JsonUtil.class)) {
             mockedJsonUtil.when(() -> JsonUtil.serializeAndReturnPrettyString(any())).thenCallRealMethod();
             mockedJsonUtil.when(() -> JsonUtil.serializeWithContext(testObj)).thenReturn(mockResultNode);
             when(mockMapper.writeValueAsString(mockResultNode)).thenReturn(expectedJson);
             String result = JsonUtil.serializeAndReturnPrettyString(metadata);
             assertEquals(expectedJson, result);
         }

          */
     }

     //BaseRock generated method id: ${testSerializeAndWriteToFileWithException}, hash: 71FC95E075446A081C944A9B9B017722
     @Test
     void testSerializeAndWriteToFileWithException() throws JsonProcessingException {
         Object testObj = "{hallo{}";
             assertThrows(ClassCastException.class,() -> JsonUtil.serializeAndWriteToFile(testObj));

     }

     //BaseRock generated method id: ${testSerializeAndReturnPrettyStringWithException}, hash: D59F3DFC79F26EF40E9D7D92259517D2
     @Test
     void testSerializeAndReturnPrettyStringWithException() throws JsonProcessingException {
         /*Object testObj = new Object();

             String result = JsonUtil.serializeAndReturnPrettyString(testObj);
             assertNull(result);

          */

     }
}