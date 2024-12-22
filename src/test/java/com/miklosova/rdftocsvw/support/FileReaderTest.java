package com.miklosova.rdftocsvw.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class FileReaderTest {

    final static String DIRECTORY_PATH = "src/test/resources/OriginalIsSubsetOfCSV/";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void transformSplitQueryFile() {
        try {
            // -m minimal is for less verbose translation - to translate only what is given in the metadata, no extra triples like row numbers unless specifically mentioned in metadata

            File pathToExecutable = new File(DIRECTORY_PATH + "csv2rdf-0.4.7-standalone.jar");
            File pathToOutput = new File(DIRECTORY_PATH + "RDFoutput.ttl");
            System.out.println(pathToOutput.getAbsolutePath());
            File pathToMetadata = new File(DIRECTORY_PATH + "csv-metadata.json");
            //pathToExecutable.getAbsolutePath()
            ProcessBuilder builder = new ProcessBuilder("java", "-jar", pathToExecutable.getAbsolutePath(), "-u", pathToMetadata.getAbsolutePath(), "-o", pathToOutput.getAbsolutePath(), "-m", "minimal");
            builder.directory(new File("src/test/resources").getAbsoluteFile()); // this is where you set the root folder for the executable to run with
            builder.redirectErrorStream(true);
            Process process = builder.start();

            System.out.println("command line error " + process.errorReader().readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File f = new File(DIRECTORY_PATH + "RDFoutput.ttl");
        System.out.println(f.getAbsolutePath());
        Assertions.assertTrue(f.isFile());

    }


    @Test
    void readRDF() {

    }
}