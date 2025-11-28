package com.miklosova.rdftocsvw.w3c_tests;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.RDFtoCSV;
import com.miklosova.rdftocsvw.support.BaseTest;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.TestSupport;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

@RunWith(Parameterized.class)
public class CSVWRDFTests extends BaseTest {
    private static final CharSequence EXCEPTION_MESSAGE = "OR 'NO TRIPLES FOUND'";
    // There is 307 tests on the W3C test page, here we need to add +1 because of the file naming system
    private static final Integer NUMBER_OF_W3C_TESTS = 308;
    private static final List<Integer> NOT_DEFINED = Arrays.asList(new Integer[]{2, 3, 4, 11, 12, 14, 15, 16, 17, 18, 19, 20, 21, 22, 24, 25, 26, 30, 34, 50, 51, 52, 53, 54, 55, 56, 57, 58, 64, 94, 96, 97, 100, 101, 145, 235, 236, 239, 240, 241, 249, 250, 254, 255, 256, 257, 258, 262, 265});
    private static final List<Integer> NO_FILE = Arrays.asList(new Integer[]{74, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 98, 103, 104, 108, 128, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 146, 199, 200, 201, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 243, 244, 251, 252, 253, 259, 260, 261, 263, 267, 271, 272, 274});
    private static final String RESOURCES_PATH = "./src/test/resources/CSVWRDFTests/";
    private final String READ_METHOD = "rdf4j";
    private String nameForTest;
    private String filePath;
    private String filePathForMetadata;
    private String filePathForOutput;
    private String expectedDatatype;
    private PrefinishedOutput prefinishedOutput;
    private AppConfig config;
    private String expectedException;
    private RepositoryConnection repositoryConnection;


    public CSVWRDFTests(String nameForTest, String expectedDatatype, String expectedException) {
        this.nameForTest = nameForTest;
        this.filePath = RESOURCES_PATH + nameForTest;
        this.filePathForMetadata = RESOURCES_PATH + nameForTest + ".csv-metadata.json";
        // Remove .ttl extension from output path to avoid duplication
        String baseNameWithoutExtension = nameForTest.substring(0, nameForTest.lastIndexOf('.'));
        this.filePathForOutput = RESOURCES_PATH + nameForTest;// + baseNameWithoutExtension;
        this.expectedException = expectedException;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs() {
        Collection<Object[]> conf = new ArrayList<>();
        for (int i = 1; i < 249; i++) {
        //for (int i = 39; i < 40; i++) {

                //308; i++) {
            Object[] array = new Object[3];
            String extra = "";
            if (i < 10) {
                extra = "00";
            } else if (i > 9 && i < 100) {
                extra = "0";
            }
            array[0] = "test" + extra + i + ".ttl";
            conf.add(array);
        }
        return conf;
        /*
        return Arrays.asList(new Object[][]{

                //{ "empty",  "", new StringBuilder().append("RuntimeException").toString()},
                { "test001",  ""},
              { "test001.rdf",  ""},
              { "test002",  ""},
              { "test002.rdf",  ""},
              { "test002b",  ""},
              { "test003a",  ""},
              { "test003b",  ""},
              { "test004a",  ""},
              { "test004b",  ""},
              { "test004c",  ""},
              { "test005a",  ""},
              { "test005b",  ""},


                { "test006",  "", null},
                { "test007a",  "",  new StringBuilder().append("RDFParseException").toString()},
                { "test007b",  "", new StringBuilder().append("RDFParseException").toString()},
                { "test008a",  "", null},
                { "test008b",  "", null},
                { "test009a",  "", null},
                { "test009b",  "", null},
                { "test010",  "", null},
                { "test011a",  "", null},
                { "test011b",  "", null},
        });
*/
    }

    @Before
    public void createPrefinishedOutputAndMetadata() {
        // Skip setup for tests that should be skipped
        int testNum = Integer.parseInt(nameForTest.substring(4, 7));
        Assume.assumeFalse("Skipping test because there is no file defined for this test", NO_FILE.contains(testNum));
        Assume.assumeFalse("Skipping test because the test is not defined", NOT_DEFINED.contains(testNum));
        
        // Skip if expecting exception - setup will be called in test method
        if (expectedException != null) {
            return;
        }
        
        config = new AppConfig.Builder(this.filePath)
                .parsing(READ_METHOD)
                .output(this.filePathForOutput)
                .outputMetadata(this.filePathForMetadata)
                .build();
        rdfToCSV = new RDFtoCSV(config);
        db = new SailRepository(new MemoryStore());
        try {
            repositoryConnection = rdfToCSV.createRepositoryConnection(db, this.filePath, READ_METHOD);
            this.prefinishedOutput = rdfToCSV.convertData(repositoryConnection, db);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.testMetadata = rdfToCSV.createMetadata(this.prefinishedOutput);
    }

    @Test
    public void filesAreCreated() {
        if (expectedException != null) {
            System.out.println("Expecting exception ");
            switch (expectedException) {
                case "RuntimeException":
                    Assert.assertThrows(RuntimeException.class, this::createPrefinishedOutputAndMetadata);
                    break;
                case "RDFParseException":
                    Assert.assertThrows(RDFParseException.class, this::createPrefinishedOutputAndMetadata);
            }
            //Assert.assertTrue(exception.getMessage().contains(EXCEPTION_MESSAGE));
        } else {
            //System.out.println("Integer.getInteger(nameForTest.substring(4,7))) " + Integer.parseInt(nameForTest.substring(4,7))+ " nameForTest.substring(4,7)) " + nameForTest.substring(4,7));
            Assume.assumeFalse("Skipping test because there is no file defined for this test", (NO_FILE.contains(Integer.parseInt(nameForTest.substring(4,7)))));
            Assume.assumeFalse("Skipping test because the test is not defined", (NOT_DEFINED.contains(Integer.parseInt(nameForTest.substring(4,7)))));
            createPrefinishedOutputAndMetadata();

            TestSupport.writeToFile(this.prefinishedOutput, this.testMetadata);

            String allFiles = config.getIntermediateFileNames();
            for (String filename : allFiles.split(",")) {
                logger.log(Level.INFO, filename + " should not be empty for this test to pass.");
                Assert.assertFalse(TestSupport.isFileEmpty(filename));
            }
            logger.log(Level.INFO, this.filePathForMetadata + " metadata should not be empty for this test to pass.");
            Assert.assertFalse(TestSupport.isFileEmpty(this.filePathForMetadata));
        }
    }
}

