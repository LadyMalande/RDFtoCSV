package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Path;

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

    //BaseRock generated method id: ${testProcessCSVToOutput}, hash: C4BAF1E5E7917BF3CD58D127C0A42CDF
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
}