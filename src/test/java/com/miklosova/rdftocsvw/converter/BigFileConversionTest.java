package com.miklosova.rdftocsvw.converter;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.miklosova.rdftocsvw.support.BaseTest;
import com.miklosova.rdftocsvw.support.AppConfig;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class BigFileConversionTest extends BaseTest {
    private String fileName = "/src/test/resources/CSVExactMatch/csvFileToTestSameCSV.csv";
    private String filePath;
    private static final String RESOURCES_PATH = "./";
    @Test
    void testreadingBigFilesToString() {
        try {
            this.filePath = RESOURCES_PATH + fileName;
            AppConfig config = new AppConfig.Builder(filePath)
                    .parsing("rdf4j")
                    .build();
            rdfToCSV = new RDFtoCSV(config);
            db = new SailRepository(new MemoryStore());
            String stringRead = String.join("\n", Files.readLines(new File(filePath), Charsets.UTF_8));
            assertNotNull(stringRead);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
