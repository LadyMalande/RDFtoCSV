package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.ConversionService;
import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.BaseTest;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DereferencerTest extends BaseTest {
    private static final String PROCESS_METHOD = "rdf4j";
    private static String filePath = "./src/test/resources/DereferenceTests/dereferenceTestInput.ttl";
    private static String filePathForMetadata = "./src/test/resources/DereferenceTests/dereferenceTestInput.csv-metadata.json";
    private static Metadata createdMetadata;
    private static Repository db;

    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private CloseableHttpResponse httpResponse;

    @Mock
    private StatusLine statusLine;

    // Test data provider for parameterized tests
    static Stream<TestData> testDataProvider() {
        return Stream.of(
                new TestData("Dereferencer-dcterms", "creator", "Creator"),
                new TestData("Dereferencer-dc", "publisher", "Publisher"),
                new TestData("Dereferencer-foaf", "givenName", "Given name"),
                new TestData("Dereferencer-vann", "usageNote", "Usage Note"),
                new TestData("Dereferencer-cc", "Distribution", "Distribution"),
                new TestData("Dereferencer-vs", "moreinfo", "moreinfo"),
                new TestData("Dereferencer-wot", "encrypter", "Encrypted by"),
                new TestData("Dereferencer-skos", "scopeNote", "scope note")
        );
    }

    // Helper class to hold test data
    static class TestData {
        final String nameForTest;
        final String expectedName;
        final String expectedTitle;

        TestData(String nameForTest, String expectedName, String expectedTitle) {
            this.nameForTest = nameForTest;
            this.expectedName = expectedName;
            this.expectedTitle = expectedTitle;
        }

        @Override
        public String toString() {
            return nameForTest;
        }
    }

    @BeforeAll
    static void createMetadata() {

        AppConfig config = new AppConfig.Builder(filePath).build();

        System.out.println("Override before each");
        ConfigurationManager.loadSettingsFromInputToConfigFile(new String[]{"-f", filePath});
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME, filePathForMetadata);

        db = new SailRepository(new MemoryStore());
        MethodService methodService = new MethodService(config);
        RepositoryConnection rc = null;
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
            config.setOutputFilePath("output.csv");
            config.setConversionMethod("basicQuery");
            config.setMetadataRowNums(false);
            config.setOutputMetadataFileName("../RDFtoCSV/src/test/resources/StreamingNTriples/testingInput.nt.csv-metadata.json");
            config.setInputFileName("dereferenceTestInput.ttl");

            mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_FILENAME)).thenReturn("output.csv");
            mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME)).thenReturn("../RDFtoCSV/src/test/resources/StreamingNTriples/testingInput.nt.csv-metadata.json");

            mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD)).thenReturn("basicQuery");

            mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INPUT_FILENAME)).thenReturn("dereferenceTestInput.ttl");

            mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.METADATA_ROWNUMS)).thenReturn("false");

            try {
                rc = methodService.processInput(filePath, PROCESS_METHOD, db);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            assert (rc != null);
            // Convert the table to intermediate data for processing into metadata
            ConversionService cs = new ConversionService(config);
            System.out.println("createMetadata @BeforeEach");
            PrefinishedOutput prefinishedOutput = cs.convertByQuery(rc, db);
            // Convert intermediate data into basic metadata
            MetadataService ms = new MetadataService(config);
            createdMetadata = ms.createMetadata(prefinishedOutput);
        }

    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testDataProvider")
    public void isGivenDatatype(TestData testData) {
        System.out.println("START isGivenDatatype for " + testData.nameForTest);
        JSONObject jsonObject = readJSONFile(filePathForMetadata);
        System.out.println(jsonObject.toJSONString());
        JSONArray tables = (JSONArray) jsonObject.get("tables");
        JSONObject table = (JSONObject) tables.get(0);
        System.out.println(table.toJSONString());
        JSONObject tableSchema = (JSONObject) table.get("tableSchema");
        System.out.println(tableSchema.toJSONString());
        JSONArray columns = (JSONArray) tableSchema.get("columns");
        System.out.println(columns.toJSONString());
        // Convert JSONArray to List<JSONObject>
        List<JSONObject> columnList = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            columnList.add((JSONObject) columns.get(i));
        }
        JSONObject testColumn = (JSONObject) columnList.stream().filter(column -> {
            System.out.println("column.get(\"name\")=" + column.get("name"));
            return column.get("name").equals(testData.expectedName);
        }).findAny().orElseThrow(() -> new AssertionError("Column not found: " + testData.expectedName));

        System.out.println(testColumn.toJSONString());
        assertEquals(testData.expectedTitle, testColumn.get("titles"));
    }
}
