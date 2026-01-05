package com.miklosova.rdftocsvw.w3c_tests;

import com.miklosova.rdftocsvw.converter.ConversionService;
import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.RDFtoCSV;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.MetadataService;
import com.miklosova.rdftocsvw.output_processor.FileWrite;
import com.miklosova.rdftocsvw.support.*;
import com.miklosova.rdftocsvw.support.AppConfig;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
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
    private AppConfig config;

    private String exception;

    public XMLBaseTests(String nameForTest, String expectedDatatype, String exception) {
        this.nameForTest = nameForTest;
        this.filePath = RESOURCES_PATH + nameForTest + ".rdf";
        this.filePathForMetadata = filePath + ".csv-metadata.json";
        this.filePathForOutput = RESOURCES_PATH + nameForTest + "TestOutput";
        this.expectedDatatype = expectedDatatype;
        this.exception = exception;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][]{
                {"test001", "", null},
                {"test002", "", null},
                {"test003", "", null},
                {"test004", "", null},
                // Marked OBSOLETE { "test005",  ""},
                {"test006", "", null},
                {"test007", "", null},
                {"test008", "", null},
                {"test009", "", null},
                {"test010", "", null},
                {"test011", "", null},
                // Marked WITHDRAWN { "test012",  ""},
                {"test013", "", null},
                {"test014", "", "RDFParseException"},
                // Status: NOT_APPROVED
                {"test015", "", null},
                // Status: NOT_APPROVED
                {"test016", "", null},
                {"test-001", "", "RuntimeException"},
                {"test-002", "", null},
                {"test-003", "", null},
                {"test-004", "", null},
                {"test-005", "", null},


                // { "Datatypes-boolean", "./src/test/resources/datatypes-boolean.ttl", "./src/test/resources/datatypes-boolean.csv-metadata.json", "./src/test/resources/testingInputOutput", "boolean"},
                //{"Datatypes-string2", "./src/test/resources/test001.rdf", "./src/test/resources/datatypes-string2.csv-metadata.json", "./src/test/resources/testingInputOutput", "" }
                //{ "", "", "", "", "", ""},
        });
    }

    @BeforeEach
    void createMetadata() {
        config = new AppConfig.Builder(filePath)
                .parsing(PROCESS_METHOD)
                .output(filePathForOutput)
                .build();
        rdfToCSV = new RDFtoCSV(config);
        config.setOutputFileName(filePathForOutput);
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
        ConversionService cs = new ConversionService(config);
        this.prefinishedOutput = cs.convertByQuery(rc, db);
        MetadataService ms = new MetadataService(config);
        Metadata metadata = ms.createMetadata(prefinishedOutput);
        this.testMetadata = metadata;
    }

    @Test
    public void csvFileIsCreated() throws IOException {
        if(exception == null) {
            createMetadata();

            RowsAndKeys rnk = (RowsAndKeys) prefinishedOutput.getPrefinishedOutput();
            int i = 0;
            ArrayList<String> fileNamesCreated = new ArrayList<>();
            String allFiles = config.getIntermediateFileNames();
            for (String filename : allFiles.split(",")) {
                //System.out.println("newFileName " + filename);
                FileWrite.saveCSVFileFromRows(filename, rnk.getRowsAndKeys().get(0).getRows(), this.testMetadata, config);
                Assert.assertFalse(TestSupport.isFileEmpty(filename));
            }
            JsonUtil.serializeAndWriteToFile(this.testMetadata, config);
            Assert.assertFalse(TestSupport.isFileEmpty(this.filePathForMetadata));
        } else {
            switch(exception){
                case "RDFParseException": Assertions.assertThrows(RDFParseException.class, () -> {
                    createMetadata();
                    RowsAndKeys rnk = (RowsAndKeys) prefinishedOutput.getPrefinishedOutput();});
                case "RuntimeException": Assertions.assertThrows(RuntimeException.class, () -> {
                    createMetadata();
                    RowsAndKeys rnk = (RowsAndKeys) prefinishedOutput.getPrefinishedOutput();});
            }

        }
    }
}
