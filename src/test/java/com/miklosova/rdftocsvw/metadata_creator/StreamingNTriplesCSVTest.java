package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.*;
import com.miklosova.rdftocsvw.support.BaseTest;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.FileWrite;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@RunWith(Parameterized.class)
public class StreamingNTriplesCSVTest extends BaseTest {
    private final String PROCESS_METHOD = "rdf4j";
    private String nameForTest;
    private String filePath;
    private String filePathForMetadata;
    private String filePathForOutput;
    private String expectedFile;
    private String expectedDatatype;

    private PrefinishedOutput prefinishedOutput;
    private static final String RESOURCES_PATH = "./RDFtoCSV/src/test/resources/StreamingNTriples/";
    private static final String RESOURCES_PATH_OUTPUT = "./";
    private static final String RESOURCES_PATH_EXPECTATION = "./src/test/resources/StreamingNTriples/";
    private static String CUSTOM_CONFIG_FILENAME = "./src/test/resources/StreamingNTriples/app.config";
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs(){
        return Arrays.asList(new Object[][]{
                { "", "streamingSample08c", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},
                { "", "streamingSample03", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},
                { "", "streamingSample04", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},
                { "", "streamingSample05", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},
                { "", "streamingSample06", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},
                { "", "streamingSample07", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},
                { "", "streamingSample08", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},
                { "", "streamingSample08b", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},
                { "", "streamingSample09", "./src/test/resources/StreamingNTriples/streamingSample02Expectation.csv"},
//{ "", "", "", "", "", ""},
        });
    }

    public StreamingNTriplesCSVTest(String nameForTest, String filePath, String expectedFileName) {
        this.nameForTest = nameForTest;
        this.filePath = RESOURCES_PATH + filePath + ".nt";
        this.expectedFile = RESOURCES_PATH_EXPECTATION + filePath + "Expectation.csv";
        this.filePathForOutput = RESOURCES_PATH_OUTPUT  + filePath + ".nt" + "0.csv";

    }

    @BeforeAll
    void setConfigFile(){
        System.out.println("LoadConfig");
        ConfigurationManager.loadSettingsFromInputToConfigFile(new String[]{filePath, "streaming", "streaming", "true"});
    }

    @BeforeEach
    void createCSV(){
        System.out.println("LoadConfig");
        ConfigurationManager.loadSettingsFromInputToConfigFile(new String[]{filePath, "streaming", "streaming", "true"});

        RDFtoCSV rdFtoCSV = new RDFtoCSV(filePath);
        try {
            rdFtoCSV.getCSVTableAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void isGivenDatatype() {
        createCSV();

        try (CSVReader reader = new CSVReader(new FileReader(filePathForOutput)) ) {
            System.out.println("filePathForOutput = " + filePathForOutput);
            try(CSVReader reader2 = new CSVReader(new FileReader(expectedFile))) {
                String[] line;
                boolean isFirstLine = true;
                while ((line = reader.readNext()) != null) {
                    String[] line2 = reader2.readNext();
                    System.out.println(line);
                    System.out.println(line2);
                    Assert.assertArrayEquals(line, line2);

                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }

    }
}

