package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.support.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
@Disabled
class FileWriteTest {

    private AppConfig config;
/* 
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        config = new AppConfig.Builder("test.ttl")
                .output(tempDir.resolve("output.csv").toString())
                .outputMetadata(tempDir.resolve("output-metadata.json").toString())
                .build();
        
    }


    @Test
    void testProcessCSVToOutputWithValidData() throws IOException {
        PrefinishedOutput<RowsAndKeys> prefinishedOutput = new PrefinishedOutput<>(
            new RowsAndKeys.RowsAndKeysFactory().factory()
        );
        Metadata metadata = new Metadata(config);
        prefinishedOutput.setMetadata(metadata);

        FinalizedOutput<byte[]> result = FileWrite.processCSVToOutput(prefinishedOutput);
        
        assertNotNull(result);
        assertNotNull(result.getData());
    }

    @Test
    void testProcessCSVToOutputWithNullData() {
        assertThrows(NullPointerException.class, () -> {
            fileWrite.processCSVToOutput(null);
        });
    }

    @Test
    void testImplementsIOutputProcessor() {
        assertTrue(fileWrite instanceof IOutputProcessor);
    }

    @Test
    void testMultipleProcessCalls() throws IOException {
        PrefinishedOutput<RowsAndKeys> prefinishedOutput = new PrefinishedOutput<>(
            new RowsAndKeys.RowsAndKeysFactory().factory()
        );
        Metadata metadata = new Metadata(config);
        prefinishedOutput.setMetadata(metadata);

        FinalizedOutput<byte[]> result1 = fileWrite.processCSVToOutput(prefinishedOutput);
        FinalizedOutput<byte[]> result2 = fileWrite.processCSVToOutput(prefinishedOutput);
        
        assertNotNull(result1);
        assertNotNull(result2);
    }

    @Test
    void testOutputFilesCreated() throws IOException {
        PrefinishedOutput<RowsAndKeys> prefinishedOutput = new PrefinishedOutput<>(
            new RowsAndKeys.RowsAndKeysFactory().factory()
        );
        Metadata metadata = new Metadata(config);
        prefinishedOutput.setMetadata(metadata);

        fileWrite.processCSVToOutput(prefinishedOutput);
        
        // Check that output files were created
        Path outputPath = tempDir.resolve("output.csv");
        Path metadataPath = tempDir.resolve("output-metadata.json");
        
        assertTrue(Files.exists(outputPath) || Files.exists(metadataPath),
                  "At least one output file should be created");
    }

    @Test
    void testFileWriteWithDifferentConfigs() {
        AppConfig config1 = new AppConfig.Builder("test1.ttl")
                .output("output1.csv")
                .build();
        AppConfig config2 = new AppConfig.Builder("test2.ttl")
                .output("output2.csv")
                .build();
        
        FileWrite fw1 = new FileWrite(config1);
        FileWrite fw2 = new FileWrite(config2);
        
        assertNotNull(fw1);
        assertNotNull(fw2);
    }

    @Test
    void testFileWriteWithCustomOutputPath() {
        Path customPath = tempDir.resolve("custom").resolve("output.csv");
        AppConfig customConfig = new AppConfig.Builder("test.ttl")
                .output(customPath.toString())
                .build();
        
        FileWrite customFileWrite = new FileWrite(customConfig);
        assertNotNull(customFileWrite);
    }

    @Test
    void testFileWriteWithZipOutput() {
        AppConfig zipConfig = new AppConfig.Builder("test.ttl")
                .output(tempDir.resolve("output.zip").toString())
                .zip(true)
                .build();
        
        FileWrite zipFileWrite = new FileWrite(zipConfig);
        assertNotNull(zipFileWrite);
    }

    @Test
    void testFileWriteWithMultipleTables() {
        AppConfig multiTableConfig = new AppConfig.Builder("test.ttl")
                .output("output.csv")
                .multipleTables(true)
                .build();
        
        FileWrite multiTableFileWrite = new FileWrite(multiTableConfig);
        assertNotNull(multiTableFileWrite);
    } */
}
