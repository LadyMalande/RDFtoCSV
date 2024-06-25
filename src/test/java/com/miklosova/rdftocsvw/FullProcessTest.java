package com.miklosova.rdftocsvw;

import com.miklosova.rdftocsvw.convertor.ConversionService;
import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.convertor.RowAndKey;
import com.miklosova.rdftocsvw.convertor.RowsAndKeys;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.MetadataService;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.FileWrite;
import com.miklosova.rdftocsvw.support.TestSupport;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class FullProcessTest {
    private String nameForTest;
    private String filePath;
    private String filePathForImage;

    private String filePathForOutput;
    private String filePathForMetadata;

    private String filePathForTestRDFOutput;
    private String methodName;



    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs(){
        return Arrays.asList(new Object[][]{
                { "SplitQuerySmallDataset", "./src/test/resources/testingInput.ttl", "./src/test/resources/csv-metadata.json", "./src/test/resources/splitQueryTest", "./src/test/resources/testingInput.ttl", "./src/test/resources/splitQueryTestRDFOutput.ttl", "rdf4j"},
                //{ "", "", "", "", "", ""},
        });
    }

    public FullProcessTest(String nameForTest, String filePath, String filePathForMetadata, String filePathForOutput, String filePathForImage, String filePathForTestRDFOutput, String methodName) {
        this.nameForTest = nameForTest;
        this.filePath = filePath;
        this.filePathForMetadata = filePathForMetadata;
        this.filePathForOutput = filePathForOutput;
        this.filePathForImage = filePathForImage;
        this.filePathForTestRDFOutput = filePathForTestRDFOutput;
        this.methodName = methodName;
    }
    @Test
    public void originalIsSubsetOfResult() {
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME, filePathForMetadata);
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INPUT_OUTPUT_FILENAME, filePathForOutput);
        Repository db = new SailRepository(new MemoryStore());
        MethodService methodService = new MethodService();
        RepositoryConnection rc = methodService.processInput(filePath, methodName, db);
        assert(rc != null);
        // Convert the table to intermediate data for processing into metadata
        ConversionService cs = new ConversionService();
        PrefinishedOutput prefinishedOutput = cs.convertByQuery(rc, db);
        // Convert intermediate data into basic metadata
        MetadataService ms = new MetadataService();
        Metadata metadata = ms.createMetadata(prefinishedOutput);

        RowsAndKeys rnk = (RowsAndKeys) prefinishedOutput.getPrefinishedOutput();
        int i = 0;
        ArrayList<String> fileNamesCreated = new ArrayList<>();
        for(RowAndKey rowAndKey : rnk.getRowsAndKeys()){
            String newFileName = filePathForOutput + i + ".csv";
            System.out.println("newFileName " + newFileName);
            FileWrite.saveCSVFileFromRows(newFileName, rowAndKey.getRows(), metadata);
            fileNamesCreated.add(newFileName);
            i++;
        }

        //TestSupport.runToRDFConverter(fileNamesCreated.get(0), filePathForMetadata, filePathForTestRDFOutput);
        TestSupport.rubyRun(fileNamesCreated.get(0), filePathForMetadata, filePathForTestRDFOutput, "src/test/resources/script.rb");

        //Assert.assertTrue(TestSupport.isRDFSubsetOfTerms(filePathForTestRDFOutput,  filePathForImage));

    }
}
