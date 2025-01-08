package com.miklosova.rdftocsvw.converter;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.miklosova.rdftocsvw.support.BaseTest;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BigFileConversionTest extends BaseTest {
    private String fileName = "soilc_2021.nt0.csv";
    private String filePath;
    private static final String RESOURCES_PATH = "./";
    @Test
    void testreadingBigFilesToString() {
        try {
            this.filePath = RESOURCES_PATH + fileName ;
            rdfToCSV = new RDFtoCSV(fileName);
            db = new SailRepository(new MemoryStore());
            args = new String[]{"-f", filePath, "-p", "rdf4j"};
            ConfigurationManager.loadSettingsFromInputToConfigFile(args);
            String stringRead = String.join("\n", Files.readLines(new File(filePath), Charsets.UTF_8) );
            System.out.println(stringRead.substring(0,1000));
            assertNotNull(stringRead);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
