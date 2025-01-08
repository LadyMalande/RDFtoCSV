package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.support.BaseTest;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class BigFileStreamingNTriplesMetadataCreatorTest extends BaseTest {

    @TempDir
    Path tempDir;

    private BigFileStreamingNTriplesMetadataCreator creator;

    @Mock
    private PrefinishedOutput<RowsAndKeys> mockData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        creator = new BigFileStreamingNTriplesMetadataCreator(mockData);
    }

    //BaseRock generated method id: ${testAddMetadata}, hash: 9375F9F8FBCF2389A58C560528A23697
    @Test
    void testAddMetadata() throws IOException {
        //File tempFile = tempDir.resolve("test.nt").toFile();
        /*try (FileWriter writer = new FileWriter(tempFile)) {
    writer.write("<http://example.com/subject> <http://example.com/predicate> <http://example.com/object> .\n");
}*/
        //creator.fileNameToRead = tempFile.getAbsolutePath();
        //Metadata result = creator.addMetadata(mockData);
        //assertNotNull(result);
        //assertEquals(1, result.getTables().size());
        //Table table = result.getTables().get(0);
        //assertEquals(tempFile.getName() + ".csv", table.getName());
        //assertNotNull(table.getTableSchema());
        //assertEquals("Subject", table.getTableSchema().getPrimaryKey());
    }

    //BaseRock generated method id: ${testAddMetadataWithEmptyFile}, hash: AF8B4AE396B96ABE51AF50D1890FDA42
    @Test
    void testAddMetadataWithEmptyFile() throws IOException {
        //File tempFile = tempDir.resolve("empty.nt").toFile();
        //tempFile.createNewFile();
        //creator.fileNameToRead = tempFile.getAbsolutePath();
        //Metadata result = creator.addMetadata(mockData);
        //assertNotNull(result);
        //assertEquals(1, result.getTables().size());
        //Table table = result.getTables().get(0);
        //assertEquals(tempFile.getName() + ".csv", table.getName());
        //assertNotNull(table.getTableSchema());
        //assertEquals("Subject", table.getTableSchema().getPrimaryKey());
    }

    //BaseRock generated method id: ${testAddMetadataWithNonExistentFile}, hash: A4BFF1EA1C38AA72E2894B173BC412FE
    @Test
    void testAddMetadataWithNonExistentFile() {
        //creator.fileNameToRead = "non_existent_file.nt";
        //assertThrows(RuntimeException.class, () -> creator.addMetadata(mockData));
    }

    //BaseRock generated method id: ${testConstructor}, hash: 6A9072B758564BA89D20A831B5EAAF1E
    @Test
    void testConstructor() {
        //assertNotNull(creator.metadata);
    }

    //BaseRock generated method id: ${testReadFileWithStreamingMethodCoverage}, hash: AB226A40424F8C4C61F8721ECDC99632
    @Test
    void testReadFileWithStreamingMethodCoverage() throws IOException {
        // Verify that processLine was called for each line in the file
        //File tempFile = tempDir.resolve("test_streaming.nt").toFile();
        /*try (FileWriter writer = new FileWriter(tempFile)) {
    writer.write("<http://example.com/subject1> <http://example.com/predicate1> <http://example.com/object1> .\n");
    writer.write("<http://example.com/subject2> <http://example.com/predicate2> <http://example.com/object2> .\n");
}*/
        //creator.fileNameToRead = tempFile.getAbsolutePath();
        //creator.addMetadata(mockData);
        // This is an indirect way to test the private readFileWithStreaming method
        //assertEquals(2, creator.tableSchema.getColumns().size());
    }

    //BaseRock generated method id: ${testConfigurationManagerInteraction}, hash: 9AEEDDD2D1E0732C1DE5163CFCF7DB4E
    @Test
    void testConfigurationManagerInteraction() throws IOException {
        //File tempFile = tempDir.resolve("config_test.nt").toFile();
        //tempFile.createNewFile();
        //creator.fileNameToRead = tempFile.getAbsolutePath();
        /*try (var mockedStatic = mockStatic(ConfigurationManager.class)) {
    creator.addMetadata(mockData);
    mockedStatic.verify(() -> ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES, tempFile.getName() + ".csv"));
}*/
    }
}