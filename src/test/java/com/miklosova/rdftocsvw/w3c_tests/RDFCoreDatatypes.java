package com.miklosova.rdftocsvw.w3c_tests;

import com.miklosova.rdftocsvw.BaseTest;
import com.miklosova.rdftocsvw.convertor.ConversionService;
import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.convertor.RowsAndKeys;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.MetadataService;
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
public class RDFCoreDatatypes extends BaseTest {
    private static final CharSequence EXCEPTION_MESSAGE = "OR 'NO TRIPLES FOUND'";
    private final String PROCESS_METHOD = "rdf4j";
    private String nameForTest;
    private String filePath;
    private String filePathForMetadata;
    private String filePathForOutput;
    private String expectedDatatype;
    private PrefinishedOutput prefinishedOutput;

    private String expectedException;

    private static final String RESOURCES_PATH = "./src/test/resources/RDFCoreDatatypes/";
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs(){
        return Arrays.asList(new Object[][]{

                { "empty",  "", new StringBuilder().append("s").toString()},
                /*{ "test001",  ""},
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

               */
                { "test006",  "", null},
                { "test007a",  "", null},
                { "test007b",  "", null},
                { "test008a",  "", null},
                { "test008b",  "", null},
                { "test009a",  "", null},
                { "test009b",  "", null},
                { "test010",  "", null},
                { "test011a",  "", null},
                { "test011b",  "", null},
        });
    }

    public RDFCoreDatatypes(String nameForTest, String expectedDatatype, String expectedException) {
        this.nameForTest = nameForTest;
        if(nameForTest.endsWith(".rdf")){
            this.filePath = RESOURCES_PATH + nameForTest;
        } else{
            this.filePath = RESOURCES_PATH + nameForTest + ".nt";
        }
        this.filePathForMetadata = RESOURCES_PATH + nameForTest + ".csv-metadata.json";
        this.filePathForOutput = RESOURCES_PATH + nameForTest + "TestOutput";
        this.expectedException = expectedException;
    }

    @BeforeEach
    void createPrefinishedOutputAndMetadata(){
        db = new SailRepository(new MemoryStore());
        this.prefinishedOutput = TestSupport.createPrefinishedOutput(this.filePath, this.filePathForMetadata, this.filePathForOutput, this.PROCESS_METHOD, this.db);
        this.testMetadata = TestSupport.createMetadata(this.prefinishedOutput);
    }
    @Test
    public void filesAreCreated() {
        if(expectedException != null){
            System.out.println("Expecting exception ");
            Assert.assertThrows(RuntimeException.class, this::createPrefinishedOutputAndMetadata);
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
