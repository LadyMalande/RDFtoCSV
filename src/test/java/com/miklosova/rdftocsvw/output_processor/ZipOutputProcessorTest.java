package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.support.AppConfig;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Path;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import org.junit.jupiter.api.io.TempDir;
import com.miklosova.rdftocsvw.support.ConfigurationManager;

import static org.junit.jupiter.api.Assertions.*;

import org.mockito.MockedStatic;
import static org.mockito.Mockito.*;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class ZipOutputProcessorTest {

    private ZipOutputProcessor zipOutputProcessor;

    private PrefinishedOutput<?> mockPrefinishedOutput;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        zipOutputProcessor = new ZipOutputProcessor();
        mockPrefinishedOutput = mock(PrefinishedOutput.class);
    }

    @Disabled
    @Test
    void testProcessCSVToOutput() throws IOException {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
    mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES)).thenReturn("./src/test/resources/csvFileToTestSameCSVnq.csv,./src/test/resources/csvFileToTestSameCSV.csv");
    mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_ZIPFILE_NAME)).thenReturn("output.zip");
    mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME)).thenReturn("./src/test/resources/test-002.csv-metadata.json");
    FinalizedOutput<byte[]> result = zipOutputProcessor.processCSVToOutput(mockPrefinishedOutput);
    assertNotNull(result);
    assertTrue(result.getOutputData().length > 0);
}
    }
    @Disabled
    @Test
    void testZipMultipleFiles() throws IOException {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
            mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES)).thenReturn("./src/test/resources/csvFileToTestSameCSVnq.csv,./src/test/resources/csvFileToTestSameCSV.csv");
            mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_ZIPFILE_NAME)).thenReturn(tempDir.resolve("output.zip").toString());
            mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME)).thenReturn("./src/test/resources/test-002.csv-metadata.json");
            assertNull(zipOutputProcessor.zipMultipleFiles());
            File zipFile = tempDir.resolve("output.zip").toFile();
            assertTrue(zipFile.exists());
            assertTrue(zipFile.length() > 0);
        }
    }

    // AppConfig-based test methods

    @Test
    void testProcessCSVToOutputWithAppConfig() throws IOException {
        AppConfig config = new AppConfig.Builder("test.ttl")
                .output("./src/test/resources/test-002")
                .build();
        config.setIntermediateFileNames("./src/test/resources/test-002TestOutput0.csv,./src/test/resources/test-003TestOutput0.csv");
        //config.setOutputZipFileName("output.zip");

        ZipOutputProcessor processor = new ZipOutputProcessor(config);
        FinalizedOutput<byte[]> result = processor.processCSVToOutput(mockPrefinishedOutput);
        assertNotNull(result);
        assertTrue(result.getOutputData().length > 0);
    }

    @Test
    void testZipMultipleFilesWithAppConfig() throws IOException {
        String zipPath = tempDir.resolve("output.zip").toString();
        AppConfig config = new AppConfig.Builder("test.ttl")
                //.output("./src/test/resources/test-002")
                .build();
        config.setIntermediateFileNames("./src/test/resources/test-002TestOutput0.csv,./src/test/resources/test-003TestOutput0.csv");
        config.setOutputZipFileName(zipPath);

        ZipOutputProcessor processor = new ZipOutputProcessor(config);
        assertNull(processor.zipMultipleFiles());
        File zipFile = new File(zipPath);
        assertTrue(zipFile.exists());
        assertTrue(zipFile.length() > 0);
    }
}