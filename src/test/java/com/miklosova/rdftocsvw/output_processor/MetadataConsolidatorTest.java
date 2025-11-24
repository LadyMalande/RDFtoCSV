package com.miklosova.rdftocsvw.output_processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.RDFtoCSV;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.support.BaseTest;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.JsonUtil;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
@Disabled
public class MetadataConsolidatorTest extends BaseTest {
    private final String PROCESS_METHOD = "rdf4j";
    private String nameForTest;
    private String filePath;
    private String filePathForMetadata;
    private Metadata metadata;
    private String filePathForOutput;
    private String outputFile;
    private String expectedMetadataFile;
    private Metadata expectedMetadata;
    private PrefinishedOutput prefinishedOutput;

    public MetadataConsolidatorTest(String nameForTest, String filePath, String filePathForMetadata, String filePathForOutput, String outputFile, String expectedMetadataFile) {
        this.nameForTest = nameForTest;
        this.filePath = filePath;
        this.filePathForMetadata = filePathForMetadata;
        this.filePathForOutput = filePathForOutput;
        this.outputFile = outputFile;
        this.expectedMetadataFile = expectedMetadataFile;
        AppConfig config = new AppConfig.Builder(filePath)
                .parsing(PROCESS_METHOD)
                .output(filePathForOutput)
                .outputMetadata(filePathForMetadata)
                .build();
        rdfToCSV = new RDFtoCSV(config);
        db = new SailRepository(new MemoryStore());
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][]{
                //{"200", "./src/test/resources/testingInputForTwoEntities.ttl", "./dissesto_200_triples.nt.csv-metadata.json", "./src/test/resources/testingInputOutput", "consolidatedMetadata200.json", ""},
                {"2000", "./src/test/resources/testingInputForTwoEntities.ttl", "./src/test/resourcesdissesto_2k_triples.nt.csv-metadata.json", "./src/test/resources/testingInputOutput", "consolidatedMetadata2k.json", ""},
                //{"12k", "./src/test/resources/experimentstestingInputForTwoEntities.ttl", "./dissesto_2k_triples.nt.csv-metadata.json", "./src/test/resources/testingInputOutput", "consolidatedMetadata2k.json", ""},


                //{ "Datatypes-anyURI", "./src/test/resources/datatypes-anyURI.ttl", "./src/test/resources/datatypes-anyURI.csv-metadata.json", "./src/test/resources/testingInputOutput", "anyURI"},
                //{ "Datatypes-boolean", "./src/test/resources/datatypes-boolean.ttl", "./src/test/resources/datatypes-boolean.csv-metadata.json", "./src/test/resources/testingInputOutput", "boolean"},
                //{"Datatypes-string2", "./src/test/resources/test001.rdf", "./src/test/resources/datatypes-string2.csv-metadata.json", "./src/test/resources/testingInputOutput", "" }
                //{ "", "", "", "", "", ""},
        });
    }

    @BeforeEach
    void loadMetadata() {
        System.out.println("load metadata from file " + expectedMetadataFile);
        AppConfig config = new AppConfig.Builder(filePath)
                .parsing(PROCESS_METHOD)
                .output(filePathForOutput)
                .outputMetadata(filePathForMetadata)
                .build();
        MethodService methodService = new MethodService(config);
        RepositoryConnection rc = null;
        try {
            rc = methodService.processInput(filePath, PROCESS_METHOD, db);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assert (rc != null);
        this.expectedMetadata = metadata;
    }

    @Test
    public void isGivenDatatype() {
        logger.info("Starting test isGivenDatatype in MetadataConsolidatorTest.");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Metadata unconsolidatedMetadata = null;
        AppConfig config = new AppConfig.Builder(filePath)
                .parsing(PROCESS_METHOD)
                .output(filePathForOutput)
                .outputMetadata(filePathForMetadata)
                .build();
        try {
            unconsolidatedMetadata = objectMapper.readValue(new File(filePathForMetadata), Metadata.class);
            MetadataConsolidator mc = new MetadataConsolidator(config);
            Metadata consolidatedMetadata = mc.consolidateMetadata(unconsolidatedMetadata, config);
            JsonUtil.serializeAndWriteToFile(consolidatedMetadata, config);
            JSONObject jsonObject = readJSONFile(filePathForMetadata);
            System.out.println(jsonObject.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }


/*
        System.out.println("START isGivenDatatype");
        JSONObject jsonObject = readJSONFile(filePathForMetadata);
        JSONArray tables = (JSONArray) jsonObject.get("tables");
        JSONObject table = (JSONObject) tables.get(2);
        JSONObject tableSchema = (JSONObject) table.get("tableSchema");
        JSONArray columns = (JSONArray) tableSchema.get("columns");
        JSONObject testColumn = (JSONObject) columns.stream().filter(column -> ((JSONObject) column).get("name").equals("datatypeTest")).findAny().get();



        Assert.assertEquals(testColumn.get("datatype"), this.expectedDatatype);

 */
        File file = new File(filePathForMetadata);

        // Assert that the file exists
        Assert.assertTrue(file.exists());

        // Assert that the file is not empty
        Assert.assertTrue(file.length() > 0);
    }
}

