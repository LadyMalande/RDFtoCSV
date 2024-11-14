package com.miklosova.rdftocsvw.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class FileReaderTest {

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

            File pathToExecutable = new File("src/test/resources/csv2rdf-0.4.7-standalone.jar");
            //pathToExecutable.getAbsolutePath()
            ProcessBuilder builder = new ProcessBuilder("java", "-jar", pathToExecutable.getAbsolutePath(), " -t", "splitQueryTest0.csv", "-u", "csv-metadata.json", "-o", "RDFoutput.ttl", "-m", "minimal");
            builder.directory(new File("src/test/resources").getAbsoluteFile()); // this is where you set the root folder for the executable to run with
            builder.redirectErrorStream(true);
            Process process = builder.start();

            System.out.println("command line error " + process.errorReader().readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @Test
    void readRDF() {

    }
}