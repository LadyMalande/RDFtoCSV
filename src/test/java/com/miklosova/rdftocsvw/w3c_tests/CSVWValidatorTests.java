package com.miklosova.rdftocsvw.w3c_tests;

import com.miklosova.rdftocsvw.BaseTest;
import com.miklosova.rdftocsvw.convertor.ConversionService;
import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.convertor.RDFtoCSV;
import com.miklosova.rdftocsvw.convertor.RowsAndKeys;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.MetadataService;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.FileWrite;
import com.miklosova.rdftocsvw.support.TestSupport;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFParseException;
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
import java.util.List;

@RunWith(Parameterized.class)
public class CSVWValidatorTests extends BaseTest {
    private static final CharSequence EXCEPTION_MESSAGE = "OR 'NO TRIPLES FOUND'";
    private final String READ_METHOD = "rdf4j";
    private String nameForTest;
    private String filePath;
    private String filePathForMetadata;
    private String filePathForOutput;
    private String expectedDatatype;
    private PrefinishedOutput prefinishedOutput;

    // There is 307 tests on the W3C test page, here we need to add +1 because of the file naming system
    private static final Integer NUMBER_OF_W3C_TESTS = 308;

    private String expectedException;

    private RepositoryConnection repositoryConnection;

    private static final List<Integer> NOT_DEFINED =  Arrays.asList(new Integer[]{2, 3, 4,11,12,14,15,16,17,18,19,20,21,22,24,25,26,50,51,52,53,54,55,56,57,58,64,94,96,145,239,240,241,249,250,254,255,256,257,258,262,265});
    private static final String RESOURCES_PATH = "./src/test/resources/CSVWRDFTests/";


    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs(){
        Collection<Object[]> conf = new ArrayList<>();
        for(int i = 1; i < 2; i++)
        {
            Object[] array = new Object[3];
            String extra = "";
            if(i < 10){
                extra = "00";
            } else if(i > 9 && i < 100){
                extra = "0";
            }
            array[0] = "test" + extra + i + ".ttl";
            conf.add(array);
        }
        return conf;

    }

    public CSVWValidatorTests(String nameForTest, String expectedDatatype, String expectedException) {
        this.nameForTest = nameForTest;
        this.filePath = RESOURCES_PATH + nameForTest;
        this.filePathForMetadata = RESOURCES_PATH + nameForTest + ".csv-metadata.json";
        this.filePathForOutput = RESOURCES_PATH + nameForTest + "TestOutput";
        this.expectedException = expectedException;
    }

    @BeforeEach
    void createPrefinishedOutputAndMetadata(){
        db = new SailRepository(new MemoryStore());
        RDFtoCSV rdFtoCSV = new RDFtoCSV(this.filePath);
        rdFtoCSV.configure();
        try {
            repositoryConnection = rdFtoCSV.createRepositoryConnection(db, this.filePath, READ_METHOD);
            this.prefinishedOutput = rdFtoCSV.convertData(repositoryConnection, db);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //this.prefinishedOutput = TestSupport.createPrefinishedOutput(this.filePath, this.filePathForMetadata, this.filePathForOutput, this.PROCESS_METHOD, this.db, new String[]{this.filePath});
        this.testMetadata = rdFtoCSV.createMetadata(this.prefinishedOutput);
    }
    @Test
    public void filesAreCreated() {
        if(expectedException != null){
            System.out.println("Expecting exception ");
            switch(expectedException){
                case "RuntimeException" : Assert.assertThrows(RuntimeException.class, this::createPrefinishedOutputAndMetadata);
                    break;
                case "RDFParseException" : Assert.assertThrows(RDFParseException.class, this::createPrefinishedOutputAndMetadata);
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
