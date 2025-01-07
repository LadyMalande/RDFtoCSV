package com.miklosova.rdftocsvw.output_processor;

import com.miklosova.rdftocsvw.converter.RDFtoCSV;
import com.miklosova.rdftocsvw.converter.data_structure.Row;

import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.TableSchema;
import com.miklosova.rdftocsvw.support.BaseTest;
import com.opencsv.CSVReader;

import static org.eclipse.rdf4j.model.util.Values.iri;

import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.Test;
import java.io.File;
import com.opencsv.CSVWriter;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileReader;
import static org.mockito.ArgumentMatchers.anyString;

import org.mockito.MockedStatic;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import java.io.FileWriter;
import java.nio.file.Path;

import com.miklosova.rdftocsvw.support.ConfigurationManager;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;
import com.opencsv.exceptions.CsvValidationException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class CSVConsolidatorTest extends BaseTest {

    private CSVConsolidator csvConsolidator;

    private Metadata oldMetadata;

    private Metadata newMetadata;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        csvConsolidator = new CSVConsolidator();
        oldMetadata = new Metadata();
        newMetadata = new Metadata();
        rdfToCSV = new RDFtoCSV(fileName);
        db = new SailRepository(new MemoryStore());
        args = new String[]{"-f", "test.rdf", "-p", "rdf4j"};
        ConfigurationManager.loadSettingsFromInputToConfigFile(args);
            keys = new ArrayList<>();
            keys.add(iri("http://predicate1.cz"));
            keys.add(iri("http://predicate2.cz"));
            firstRow = new Row(iri("http://subject1.cz"), iri("http://predicate1.cz"),true);
            secondRow = new Row(iri("http://subject2.cz"), iri("http://predicate2.cz") , false);
            rows = new ArrayList<>();
            rows.add(firstRow);
            rows.add(secondRow);
    }

    //BaseRock generated method id: ${testConsolidateCSVs}, hash: E8BB2DB378F9ED35DF1628A0294727C6
    @Test
    void testConsolidateCSVs() throws IOException {
        Table table = new Table();
        table.setUrl("test.csv");
        Column column1 = new Column();
        column1.setTitles("Column1");
        Column column2 = new Column();
        column2.setTitles("Column2");
        table.setTableSchema(new TableSchema());
        oldMetadata.getTables().add(table);

        File tempFile = tempDir.resolve("test.csv").toFile();

        ArrayList<String[]> lines = new ArrayList<>();
        String[] line = new String[2];
        line[0] = "Column1";
        line[1] = "Column2";
        String[] line2 = new String[2];
        line2[0] = "Value1";
        line2[1] = "Value2";
        lines.add(line);lines.add(line2);
        MetadataConsolidator mc = new MetadataConsolidator();
        newMetadata = mc.consolidateMetadata(oldMetadata);
        //File newTempFile = tempDir.resolve(newMetadata.getTables().get(0).getUrl()).toFile();
        System.out.println("newTempFile = " + newMetadata.getTables().get(0).getUrl());

        FileWrite.writeLinesToCSVFile(tempFile,lines,false);
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES,tempFile.getAbsolutePath());
        File newTempFile = new File(newMetadata.getTables().get(0).getUrl());
    csvConsolidator.consolidateCSVs(oldMetadata, newMetadata);
    assertTrue(tempFile.exists());
            assertTrue(newTempFile.exists());

    }

    //BaseRock generated method id: ${testCreateHeadersLineForCSV}, hash: 84D16D5A51B1DB093BBD0852E26F9322
    @Test
    void testCreateHeadersLineForCSV() throws IOException, CsvValidationException {
        Table table = new Table();
        Column column1 = new Column();
        column1.setTitles("Column1");
        Column column2 = new Column();
        column2.setTitles("Column2");
        table.setTableSchema(new TableSchema());
        table.getTableSchema().setColumns(Arrays.asList(column1, column2));

        File tempFile = tempDir.resolve("test.csv").toFile();
        newMetadata.getTables().add(table);
        csvConsolidator.createHeadersLineForCSV(newMetadata, tempFile);
        try (CSVReader reader = new CSVReader(new FileReader(tempFile))) {
    String[] header = reader.readNext();
    System.out.println(newMetadata.jsonldMetadata());

    assertArrayEquals(new String[] { "Column1", "Column2" }, header);
}
    }


    //BaseRock generated method id: ${testWriteToCSVFromOldMetadataToMerged}, hash: C957D8091F1E6E6CC16996431B6C5818
    @Test
    void testWriteToCSVFromOldMetadataToMerged() throws IOException, CsvValidationException {
        Table oldTable = new Table();
        oldTable.setUrl("old.csv");
        Column oldColumn1 = new Column();
        oldColumn1.setPropertyUrl("http://example.com/prop1");
        Column oldColumn2 = new Column();
        oldColumn2.setPropertyUrl("http://example.com/prop2");
        oldTable.setTableSchema(new TableSchema());
        oldTable.getTableSchema().setColumns(Arrays.asList(oldColumn1, oldColumn2));
        oldMetadata.getTables().add(oldTable);
        Table newTable = new Table();
        newTable.setUrl("new.csv");
        Column newColumn1 = new Column();
        newColumn1.setPropertyUrl("http://example.com/prop1");
        Column newColumn2 = new Column();
        newColumn2.setPropertyUrl("http://example.com/prop2");
        newTable.setTableSchema(new TableSchema());
        newTable.getTableSchema().setColumns(Arrays.asList(newColumn1, newColumn2));
        newMetadata.getTables().add(newTable);
        File oldFile = tempDir.resolve("old.csv").toFile();
        System.out.println("old.csv path = " + oldFile.getAbsolutePath());
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES,oldFile.getAbsolutePath());
        try (CSVWriter writer = new CSVWriter(new FileWriter(oldFile))) {
    writer.writeNext(new String[] { "Header1", "Header2" });
    writer.writeNext(new String[] { "Value1", "Value2" });
}
        File newFile = tempDir.resolve("new.csv").toFile();

    csvConsolidator.writeToCSVFromOldMetadataToMerged(oldMetadata, newMetadata, newFile);
    try (CSVReader reader = new CSVReader(new FileReader(newFile))) {
        String[] line = reader.readNext();
        assertArrayEquals(new String[] { "Value1", "Value2" }, line);
    }

    }

    //BaseRock generated method id: ${testIsMergeable}, hash: 08AA314B774CBD58DAEC8EC836207443
    @Test
    void testIsMergeable() {
        Column c1 = new Column();
        c1.setPropertyUrl("http://example.com/prop1");
        c1.setLang("en");
        Column c2 = new Column();
        c2.setPropertyUrl("http://example.com/prop1");
        c2.setLang("en");
        assertTrue(csvConsolidator.isMergeable(c1, c2));
        c2.setLang("fr");
        assertFalse(csvConsolidator.isMergeable(c1, c2));
        c2.setPropertyUrl("http://example.com/prop2");
        c2.setLang("en");
        assertFalse(csvConsolidator.isMergeable(c1, c2));
    }

    //BaseRock generated method id: ${testConsolidateCSVsWithIOException}, hash: 0A9F544484564AF054A10A4D988E49BE
    @Test
    void testConsolidateCSVsWithIOException() throws IOException {
        Table table = new Table();
        table.setUrl("test.csv");
        table.setTableSchema(new TableSchema());
        newMetadata.getTables().add(table);
        File tempFile = tempDir.resolve("test.csv").toFile();
        try (MockedStatic<MetadataConsolidator> mockedStatic = mockStatic(MetadataConsolidator.class)) {
    mockedStatic.when(() -> MetadataConsolidator.getFilePathForFileName(anyString())).thenReturn(tempFile.getAbsolutePath());
    tempFile.setReadOnly();
    assertDoesNotThrow(() -> csvConsolidator.consolidateCSVs(oldMetadata, newMetadata));
}
    }

    //BaseRock generated method id: ${testWriteToCSVFromOldMetadataToMergedWithCsvValidationException}, hash: D1302C22C01E585618DFF0AA1F9270A5
    @Test
    void testWriteToCSVFromOldMetadataToMergedWithCsvValidationException() throws IOException {
        Table oldTable = new Table();
        oldTable.setUrl("old.csv");
        oldMetadata.getTables().add(oldTable);
        Table newTable = new Table();
        newMetadata.getTables().add(newTable);
        File oldFile = tempDir.resolve("old.csv").toFile();
        File newFile = tempDir.resolve("new.csv").toFile();
        try (MockedStatic<MetadataConsolidator> mockedStatic = mockStatic(MetadataConsolidator.class)) {
    mockedStatic.when(() -> MetadataConsolidator.getFilePathForFileName(anyString())).thenReturn(oldFile.getAbsolutePath());
            try (CSVReader mockReader = mock(CSVReader.class)) {
                when(mockReader.readNext()).thenThrow(CsvValidationException.class);
            } catch (CsvValidationException e) {
                throw new RuntimeException(e);
            }
            assertThrows(RuntimeException.class, () -> csvConsolidator.writeToCSVFromOldMetadataToMerged(oldMetadata, newMetadata, newFile));
}
    }
}