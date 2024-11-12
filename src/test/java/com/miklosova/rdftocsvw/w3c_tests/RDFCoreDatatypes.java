package com.miklosova.rdftocsvw.w3c_tests;

import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.support.BaseTest;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.TestSupport;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class RDFCoreDatatypes extends BaseTest {
    private static final CharSequence EXCEPTION_MESSAGE = "OR 'NO TRIPLES FOUND'";
    private static final String RESOURCES_PATH = "./src/test/resources/RDFCoreDatatypes/";
    private final String PROCESS_METHOD = "rdf4j";
    private String nameForTest;
    private String filePath;
    private String filePathForMetadata;
    private String filePathForOutput;
    private String expectedDatatype;
    private PrefinishedOutput prefinishedOutput;
    private String expectedException;

    public RDFCoreDatatypes(String nameForTest, String expectedDatatype, String expectedException) {
        this.nameForTest = nameForTest;
        if (nameForTest.endsWith(".rdf")) {
            this.filePath = RESOURCES_PATH + nameForTest;
        } else {
            this.filePath = RESOURCES_PATH + nameForTest + ".nt";
        }
        this.filePathForMetadata = RESOURCES_PATH + nameForTest + ".csv-metadata.json";
        this.filePathForOutput = RESOURCES_PATH + nameForTest + "TestOutput";
        this.expectedException = expectedException;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][]{

                //{ "empty",  "", new StringBuilder().append("RuntimeException").toString()},
                /*
                 { "test001",  "", null},
                { "test001.rdf",  "", null},
                { "test002",  "", null},
                { "test002.rdf",  "", null},

                 */
                {"test002b", "", null},
                {"test003a", "", null},
                {"test003b", "", null},
                {"test004a", "", null},
                {"test004b", "", null},
                {"test004c", "", null},
                {"test005a", "", null},
                {"test005b", "", null},


                {"test006", "", null},
                {"test007a", "", new StringBuilder().append("RDFParseException").toString()},
                {"test007b", "", new StringBuilder().append("RDFParseException").toString()},
                {"test008a", "", null},
                {"test008b", "", null},
                {"test009a", "", null},
                {"test009b", "", null},
                {"test010", "", null},
                {"test011a", "", null},
                {"test011b", "", null},
        });
    }

    @BeforeEach
    void createPrefinishedOutputAndMetadata() {
        db = new SailRepository(new MemoryStore());
        this.prefinishedOutput = TestSupport.createPrefinishedOutput(this.filePath, this.filePathForMetadata, this.filePathForOutput, this.PROCESS_METHOD, this.db, new String[]{this.filePath}
        );
        this.testMetadata = TestSupport.createMetadata(this.prefinishedOutput);
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
            createPrefinishedOutputAndMetadata();

            TestSupport.writeToFile(this.prefinishedOutput, this.testMetadata);

            String allFiles = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES);
            for (String filename : allFiles.split(",")) {
                Assert.assertFalse(TestSupport.isFileEmpty(filename));
            }

            Assert.assertFalse(TestSupport.isFileEmpty(this.filePathForMetadata));
        }
    }
}
