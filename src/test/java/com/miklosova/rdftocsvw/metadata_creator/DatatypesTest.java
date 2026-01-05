package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.ConversionService;
import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.BaseTest;
import com.miklosova.rdftocsvw.output_processor.FileWrite;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class DatatypesTest extends BaseTest {
    private final String PROCESS_METHOD = "rdf4j";
    private String nameForTest;
    private String filePath;
    private String filePathForMetadata;
    private String filePathForOutput;
    private String expectedDatatype;
    private PrefinishedOutput prefinishedOutput;

    public DatatypesTest(String nameForTest, String filePath, String filePathForMetadata, String filePathForOutput, String expectedDatatype) {
        this.nameForTest = nameForTest;
        this.filePath = filePath;
        this.filePathForMetadata = filePathForMetadata;
        this.filePathForOutput = filePathForOutput;
        this.expectedDatatype = expectedDatatype;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][]{
                {"Datatypes-integer", "./src/test/resources/testingInputForTwoEntities.ttl", "./src/test/resources/testingInput.csv-metadata.json", "./src/test/resources/testingInputOutput", "integer"},
                //{ "Datatypes-anyURI", "./src/test/resources/datatypes-anyURI.ttl", "./src/test/resources/datatypes-anyURI.csv-metadata.json", "./src/test/resources/testingInputOutput", "anyURI"},
                //{ "Datatypes-boolean", "./src/test/resources/datatypes-boolean.ttl", "./src/test/resources/datatypes-boolean.csv-metadata.json", "./src/test/resources/testingInputOutput", "boolean"},
                //{"Datatypes-string2", "./src/test/resources/test001.rdf", "./src/test/resources/datatypes-string2.csv-metadata.json", "./src/test/resources/testingInputOutput", "" }
                //{ "", "", "", "", "", ""},
        });
    }

    @BeforeEach
    void createMetadata() {
        System.out.println("Override before each");
        config = new AppConfig.Builder(filePath).build();
        config.setOutputMetadataFileName(filePathForMetadata);
        db = new SailRepository(new MemoryStore());
        MethodService methodService = new MethodService(config);
        RepositoryConnection rc = null;
        try {
            rc = methodService.processInput(filePath, PROCESS_METHOD, db);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assert (rc != null);
        // Convert the table to intermediate data for processing into metadata
        ConversionService cs = new ConversionService(config);
        System.out.println("createMetadata @BeforeEach");
        this.prefinishedOutput = cs.convertByQuery(rc, db);
        // Convert intermediate data into basic metadata
        MetadataService ms = new MetadataService(config);
        Metadata metadata = ms.createMetadata(prefinishedOutput);

        this.testMetadata = metadata;
    }

    @Test
    public void isGivenDatatype() {
        //logger.info("Starting test isGivenDatatype.");
        createMetadata();

        RowsAndKeys rnk = (RowsAndKeys) prefinishedOutput.getPrefinishedOutput();
        int i = 0;
        ArrayList<String> fileNamesCreated = new ArrayList<>();
        String allFiles = config.getIntermediateFileNames();
        for (String filename : allFiles.split(",")) {
            String newFileName = filePathForOutput + i + ".csv";
            //System.out.println("newFileName " + filename);
            FileWrite.saveCSVFileFromRows(filename, rnk.getRowsAndKeys().get(0).getRows(), this.testMetadata, config);
        }

        System.out.println("START isGivenDatatype");
        JSONObject jsonObject = readJSONFile(filePathForMetadata);
        JSONArray tables = (JSONArray) jsonObject.get("tables");
        JSONObject table = (JSONObject) tables.get(0);
        JSONObject tableSchema = (JSONObject) table.get("tableSchema");
        JSONArray columns = (JSONArray) tableSchema.get("columns");
        JSONObject testColumn = (JSONObject) columns.stream().filter(column -> ((JSONObject) column).get("name").equals("datatypeTest")).findAny().get();

        Assert.assertEquals(testColumn.get("datatype"), this.expectedDatatype);
    }
}
