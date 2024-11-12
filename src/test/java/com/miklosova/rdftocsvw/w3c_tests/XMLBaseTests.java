package com.miklosova.rdftocsvw.w3c_tests;

import com.miklosova.rdftocsvw.convertor.ConversionService;
import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.convertor.RowsAndKeys;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.MetadataService;
import com.miklosova.rdftocsvw.support.BaseTest;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.FileWrite;
import com.miklosova.rdftocsvw.support.TestSupport;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
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
public class XMLBaseTests extends BaseTest {
    private static final String RESOURCES_PATH = "./src/test/resources/XMLBaseTests/";
    private final String PROCESS_METHOD = "rdf4j";
    private String nameForTest;
    private String filePath;
    private String filePathForMetadata;
    private String filePathForOutput;
    private String expectedDatatype;
    private PrefinishedOutput prefinishedOutput;

    public XMLBaseTests(String nameForTest, String expectedDatatype) {
        this.nameForTest = nameForTest;
        this.filePath = RESOURCES_PATH + nameForTest + ".rdf";
        this.filePathForMetadata = RESOURCES_PATH + nameForTest + ".csv-metadata.json";
        this.filePathForOutput = RESOURCES_PATH + nameForTest + "TestOutput";
        this.expectedDatatype = expectedDatatype;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][]{
                {"test001", ""},
                {"test002", ""},
                {"test003", ""},
                {"test004", ""},
                // Marked OBSOLETE { "test005",  ""},
                {"test006", ""},
                {"test007", ""},
                {"test008", ""},
                {"test009", ""},
                {"test010", ""},
                {"test011", ""},
                // Marked WITHDRAWN { "test012",  ""},
                {"test013", ""},
                {"test014", ""},
                // Status: NOT_APPROVED
                {"test015", ""},
                // Status: NOT_APPROVED
                {"test016", ""},
                {"test-001", ""},
                {"test-002", ""},
                {"test-003", ""},
                {"test-004", ""},
                {"test-005", ""},


                // { "Datatypes-boolean", "./src/test/resources/datatypes-boolean.ttl", "./src/test/resources/datatypes-boolean.csv-metadata.json", "./src/test/resources/testingInputOutput", "boolean"},
                //{"Datatypes-string2", "./src/test/resources/test001.rdf", "./src/test/resources/datatypes-string2.csv-metadata.json", "./src/test/resources/testingInputOutput", "" }
                //{ "", "", "", "", "", ""},
        });
    }

    @BeforeEach
    void createMetadata() {
        System.out.println("Override before each");
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME, filePathForMetadata);
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_FILENAME, filePathForOutput);
        db = new SailRepository(new MemoryStore());
        MethodService methodService = new MethodService();
        RepositoryConnection rc = null;
        try {
            rc = methodService.processInput(filePath, PROCESS_METHOD, db);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assert (rc != null);
        // Convert the table to intermediate data for processing into metadata
        ConversionService cs = new ConversionService();
        System.out.println("createMetadata @BeforeEach");
        this.prefinishedOutput = cs.convertByQuery(rc, db);
        // Convert intermediate data into basic metadata
        MetadataService ms = new MetadataService();
        Metadata metadata = ms.createMetadata(prefinishedOutput);

        this.testMetadata = metadata;
    }

    @Test
    public void csvFileIsCreated() throws IOException {
        createMetadata();

        RowsAndKeys rnk = (RowsAndKeys) prefinishedOutput.getPrefinishedOutput();
        int i = 0;
        ArrayList<String> fileNamesCreated = new ArrayList<>();
        String allFiles = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES);

        for (String filename : allFiles.split(",")) {
            System.out.println("newFileName " + filename);
            FileWrite.saveCSVFileFromRows(filename, rnk.getRowsAndKeys().get(0).getRows(), this.testMetadata);
            Assert.assertFalse(TestSupport.isFileEmpty(filename));
        }


        Assert.assertFalse(TestSupport.isFileEmpty(this.filePathForMetadata));
    }
}
