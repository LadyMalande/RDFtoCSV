package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.ConversionService;
import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.support.BaseTest;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import org.apache.http.message.BasicHeader;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
@ExtendWith(MockitoExtension.class)
public class DereferencerTest extends BaseTest {
    private static final String PROCESS_METHOD = "rdf4j";
    private static String filePath = "./src/test/resources/DereferenceTests/dereferenceTestInput.ttl";
    private static String filePathForMetadata = "./src/test/resources/DereferenceTests/dereferenceTestInput.csv-metadata.json";
    private static Metadata createdMetadata;
    private static Repository db;
    private String nameForTest;
    private String expectedTitle;
    private String expectedName;


    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private CloseableHttpResponse httpResponse;

    @Mock
    private StatusLine statusLine;

    public DereferencerTest(String nameForTest, String expectedName, String expectedTitle) {
        this.nameForTest = nameForTest;
        this.expectedName = expectedName;
        this.expectedTitle = expectedTitle;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][]{
                {"Dereferencer-dcterms", "creator", "Creator"},
                {"Dereferencer-dc", "publisher", "Publisher"},
                {"Dereferencer-foaf", "givenName", "Given name"},
                {"Dereferencer-vann", "usageNote", "Usage Note"},
                {"Dereferencer-cc", "Distribution", "Distribution"},
                {"Dereferencer-vs", "moreinfo", "moreinfo"},
                {"Dereferencer-wot", "encrypter", "Encrypted by"},
                {"Dereferencer-skos", "scopeNote", "scope note"},
        });
    }

    @BeforeAll
    static void createMetadata() {

        System.out.println("Override before each");
        ConfigurationManager.loadSettingsFromInputToConfigFile(new String[]{"-f", filePath});
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME, filePathForMetadata);
        db = new SailRepository(new MemoryStore());
        MethodService methodService = new MethodService();
        RepositoryConnection rc = null;
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
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
            ConversionService cs = new ConversionService();
            System.out.println("createMetadata @BeforeEach");
            PrefinishedOutput prefinishedOutput = cs.convertByQuery(rc, db);
            // Convert intermediate data into basic metadata
            MetadataService ms = new MetadataService();
            createdMetadata = ms.createMetadata(prefinishedOutput);
        }

    }

    @Test
    public void isGivenDatatype() {
        createMetadata();

        System.out.println("START isGivenDatatype");
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
            return column.get("name").equals(this.expectedName);
        }).findAny().get();

        System.out.println(testColumn.toJSONString());
        Assert.assertEquals(this.expectedTitle, testColumn.get("titles"));
    }
}
