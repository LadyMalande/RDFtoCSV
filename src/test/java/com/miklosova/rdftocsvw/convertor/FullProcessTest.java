package com.miklosova.rdftocsvw.convertor;

import com.miklosova.rdftocsvw.support.Main;
import com.miklosova.rdftocsvw.support.TestSupport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
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


    public FullProcessTest(String nameForTest, String filePath, String filePathForMetadata, String filePathForOutput, String filePathForImage, String filePathForTestRDFOutput, String methodName) {
        this.nameForTest = nameForTest;
        this.filePath = filePath;
        this.filePathForMetadata = filePathForMetadata;
        this.filePathForOutput = filePathForOutput;
        this.filePathForImage = filePathForImage;
        this.filePathForTestRDFOutput = filePathForTestRDFOutput;
        this.methodName = methodName;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][]{
                {"SplitQuerySmallDataset", "RDFtoCSV/src/test/resources/StreamingNTriples/streamingSample02.nt", "./src/test/resources/csv-metadata.json", "./src/test/resources/splitQueryTest", "./src/test/resources/StreamingNTriples/streamingSample02.nt", "./src/test/resources/splitQueryTestRDFOutput.ttl", "rdf4j"},
                //{ "", "", "", "", "", ""},
        });
    }

    @Test
    @Disabled
    public void originalIsSubsetOfResult() {
        String[] args = new String[]{"-f", filePath, "-p", "streaming"};
        Main.main(args);
        /*
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME, filePathForMetadata);
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_FILENAME, filePathForOutput);
        Repository db = new SailRepository(new MemoryStore());
        MethodService methodService = new MethodService();
        RepositoryConnection rc = null;
        try {
            rc = methodService.processInput(filePath, methodName, db);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assert (rc != null);
        // Convert the table to intermediate data for processing into metadata
        ConversionService cs = new ConversionService();
        PrefinishedOutput prefinishedOutput = cs.convertByQuery(rc, db);
        // Convert intermediate data into basic metadata
        MetadataService ms = new MetadataService();
        Metadata metadata = ms.createMetadata(prefinishedOutput);

        RowsAndKeys rnk = (RowsAndKeys) prefinishedOutput.getPrefinishedOutput();
        int i = 0;
        ArrayList<String> fileNamesCreated = new ArrayList<>();
        for (RowAndKey rowAndKey : rnk.getRowsAndKeys()) {
            String newFileName = filePathForOutput + i + ".csv";
            System.out.println("newFileName " + newFileName);
            FileWrite.saveCSVFileFromRows(newFileName, rowAndKey.getRows(), metadata);
            fileNamesCreated.add(newFileName);
            i++;
        }


         */
        //TestSupport.runToRDFConverter("fileNamesCreated.get(0)", filePathForMetadata, filePathForTestRDFOutput);
        //TestSupport.rubyRun(fileNamesCreated.get(0), filePathForMetadata, filePathForTestRDFOutput, "src/test/resources/script.rb");

        try {
            Assert.assertTrue(TestSupport.isRDFSubsetOfTerms(filePathForTestRDFOutput, filePathForImage));
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }

    }
}
