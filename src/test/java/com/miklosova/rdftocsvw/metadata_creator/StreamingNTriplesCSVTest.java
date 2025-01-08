package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.support.BaseTest;
import com.miklosova.rdftocsvw.support.Main;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class StreamingNTriplesCSVTest extends BaseTest {
    private static final String RESOURCES_PATH = "./RDFtoCSV/src/test/resources/StreamingNTriples/";
    private static final String RESOURCES_PATH_OUTPUT = "./";
    private static final String RESOURCES_PATH_EXPECTATION = "./src/test/resources/StreamingNTriples/";
    private static String CUSTOM_CONFIG_FILENAME = "./src/test/resources/StreamingNTriples/app.config";
    private final String PROCESS_METHOD = "rdf4j";
    private String nameForTest;
    private String filePath;
    private String filePathForMetadata;
    private String filePathForOutput;
    private String expectedFile;
    private String expectedDatatype;
    private PrefinishedOutput<?> prefinishedOutput;

    public StreamingNTriplesCSVTest(String nameForTest, String filePath, String expectedFileName) {
        this.nameForTest = nameForTest;
        this.filePath = RESOURCES_PATH + filePath + ".nt";
        this.expectedFile = RESOURCES_PATH_EXPECTATION + filePath + "Expectation.csv";
        this.filePathForOutput = RESOURCES_PATH_OUTPUT + filePath + ".nt" + "0.csv";

    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][]{
                /*
                {"", "streamingSample03", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},
                {"", "streamingSample04", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},
                {"", "streamingSample05", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},
                {"", "streamingSample06", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},
                {"", "streamingSample07", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},
                {"", "streamingSample08", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},
                {"", "streamingSample08b", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},
                {"", "streamingSample09", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},

                 */
                //{"", "organizační-struktura", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},


                {"", "testingInput", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},
//{ "", "", "", "", "", ""},
        });
    }

    void createCSV() {
        System.out.println("LoadConfig");
        Main.main(new String[]{"-f", filePath, "-t", "-p", "streaming"});
        /*
        ConfigurationManager.loadSettingsFromInputToConfigFile(new String[]{filePath, "streaming", "streaming", "true"});

        RDFtoCSV rdFtoCSV = new RDFtoCSV(filePath);
        try {
            rdFtoCSV.getCSVTableAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }

         */
    }

    @Test
    public void streamingCSVOutputAsExpected() {
        createCSV();

        try (CSVReader reader = new CSVReader(new FileReader(filePathForOutput))) {
            System.out.println("filePathForOutput = " + filePathForOutput);
            System.out.println("expectedFile = " + expectedFile);
            try (CSVReader reader2 = new CSVReader(new FileReader(expectedFile))) {
                String[] line;
                boolean isFirstLine = true;
                while ((line = reader.readNext()) != null) {
                    String[] line2 = reader2.readNext();
                    System.out.println(Arrays.toString(line));
                    System.out.println(Arrays.toString(line2));
                    Assert.assertArrayEquals(line, line2);

                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }

    }
}

