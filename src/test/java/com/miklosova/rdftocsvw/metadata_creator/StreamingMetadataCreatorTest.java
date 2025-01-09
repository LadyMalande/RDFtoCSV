package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.RDFtoCSV;

import static org.eclipse.rdf4j.model.util.Values.iri;
import java.io.InvalidObjectException;

import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.TableSchema;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.mockito.Mock;
import com.miklosova.rdftocsvw.output_processor.CSVConsolidator;

import static org.mockito.Mockito.*;

import com.miklosova.rdftocsvw.output_processor.MetadataConsolidator;
import org.eclipse.rdf4j.model.ValueFactory;
import org.junit.jupiter.api.BeforeEach;
import com.miklosova.rdftocsvw.support.Main;
import org.eclipse.rdf4j.model.IRI;
import java.net.URISyntaxException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class StreamingMetadataCreatorTest {

    private StreamingMetadataCreator streamingMetadataCreator;

    @Mock
    private ConfigurationManager configurationManager;

    @Mock
    private Main main;
    private String[] args;
    private RDFtoCSV rdfToCSV;
    private Repository db;
    private String fileName = "/RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt";

    @BeforeEach
    void setUp() {
        rdfToCSV = new RDFtoCSV(fileName);
        db = new SailRepository(new MemoryStore());
        args = new String[]{"-f", "test.rdf", "-p", "rdf4j"};
        ConfigurationManager.loadSettingsFromInputToConfigFile(args);
        streamingMetadataCreator = new StreamingMetadataCreator();
    }

    //BaseRock generated method id: ${testConstructor}, hash: 6FDCBC966DF1DF6A210F58234369527E
    @Test
    void testConstructor() throws URISyntaxException, MalformedURLException {
        String fileNameFromConfig = "test.nt";
        URL mockLocation = new URL("file:/path/to/jar");
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INPUT_FILENAME, fileNameFromConfig);
        File mockFile = mock(File.class);
        StreamingMetadataCreator creator = new StreamingMetadataCreator();
        assertEquals(fileNameFromConfig, creator.fileNameToRead);
    }

    //BaseRock generated method id: ${testParseTripleFromLine}, hash: 1AC8AA394083B403C655382202202228
    @Test
    void testParseTripleFromLine() throws InvalidObjectException {
        String line = "<http://example.com/subject> <http://example.com/predicate> \"object\" .";
        String[] result = StreamingMetadataCreator.parseTripleFromLine(line);
        assertArrayEquals(new String[] { "<http://example.com/subject>", "http://example.com/predicate", "\"object\"" }, result);
    }

    //BaseRock generated method id: ${testParseTripleFromLineInvalid}, hash: 5C335080E0218162513F87660AA0034E
    @Test
    void testParseTripleFromLineInvalid() {
        String line = "Invalid line";
        assertThrows(InvalidObjectException.class, () -> StreamingMetadataCreator.parseTripleFromLine(line));
    }

    //BaseRock generated method id: ${testReplaceBlankNodesWithIRI}, hash: 4211C1CC0FD913B05E09A30ADB533594
    @Test
    void testReplaceBlankNodesWithIRI() {
        ValueFactory vf = SimpleValueFactory.getInstance();
        Statement st = vf.createStatement(vf.createBNode("node1"), vf.createIRI("http://example.com/predicate"), vf.createBNode("node2"));
        String line = "_:node1 <http://example.com/predicate> _:node2 .";
        Statement result = StreamingMetadataCreator.replaceBlankNodesWithIRI(st, line);
        assertTrue(result.getSubject().isIRI());
        assertTrue(result.getObject().isIRI());
        assertEquals("http://example.com/predicate", result.getPredicate().stringValue());
    }

    //BaseRock generated method id: ${testProcessNTripleLine}, hash: 7EEB2711E377D25B6A23482885D58309
    @Test
    void testProcessNTripleLine() {
        String line = "<http://example.com/subject> <http://example.com/predicate> \"object\" .";
        Statement result = StreamingMetadataCreator.processNTripleLine(line);
        assertNotNull(result);
        assertEquals("http://example.com/subject", result.getSubject().stringValue());
        assertEquals("http://example.com/predicate", result.getPredicate().stringValue());
        assertEquals("object", result.getObject().stringValue());
    }

    //BaseRock generated method id: ${testRepairMetadataAndMakeItJsonld}, hash: 22A5403987C6544A03814008CD8CB653
    @Test
    void testRepairMetadataAndMakeItJsonld() {
        Metadata metadata = mock(Metadata.class);
        Table table = new Table("test.csv");
        TableSchema schema = new TableSchema();
        schema.setColumns(new ArrayList<>());
        table.setTableSchema(schema);
        metadata.getTables().add(table);
        File f = new File("test.csv");
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES,f.getAbsolutePath());
        streamingMetadataCreator.repairMetadataAndMakeItJsonld(metadata);
        //verify(metadata).jsonldMetadata();
    }

    //BaseRock generated method id: ${testCreateFirstColumn}, hash: 387B95E50804AEDF318F78EC64030EC8
    @Test
    void testCreateFirstColumn() {
        streamingMetadataCreator.tableSchema = new TableSchema();
        streamingMetadataCreator.tableSchema.setColumns(new ArrayList<>());
        streamingMetadataCreator.createFirstColumn();
        assertEquals(1, streamingMetadataCreator.tableSchema.getColumns().size());
        Column firstColumn = streamingMetadataCreator.tableSchema.getColumns().get(0);
        assertEquals("Subject", firstColumn.getName());
        assertEquals("{+Subject}", firstColumn.getValueUrl());
        assertTrue(firstColumn.getSuppressOutput());
        assertEquals("Subject", firstColumn.getTitles());
    }

    //BaseRock generated method id: ${testProcessLine}, hash: E42C02BB8F787708EC13BAF2D83F7874
    @Test
    void testProcessLine() {
        streamingMetadataCreator.tableSchema = new TableSchema();
        streamingMetadataCreator.tableSchema.setColumns(new ArrayList<>());
        String line = "<http://example.com/subject> <http://example.com/predicate> \"object\" .";
        streamingMetadataCreator.processLine(line);
        assertEquals(1, streamingMetadataCreator.tableSchema.getColumns().size());
        Column column = streamingMetadataCreator.tableSchema.getColumns().get(0);
        assertEquals("predicate", column.getName());
        assertEquals("http://example.com/predicate", column.getPropertyUrl());
    }

    //BaseRock generated method id: ${testProcessLineIntoTripleIRIsOnly}, hash: 661284CFB980C0D4CA18D02F20DCE5E0
    @Test
    void testProcessLineIntoTripleIRIsOnly() {
        String line = "<http://example.com/subject> <http://example.com/predicate> <http://example.com/object> .";
        Triple result = StreamingMetadataCreator.processLineIntoTripleIRIsOnly(line);
        assertNotNull(result);
        assertTrue(result.subject instanceof IRI);
        assertTrue(result.predicate instanceof IRI);
        assertTrue(result.object instanceof IRI);
    }

    //BaseRock generated method id: ${testProcessLineIntoTriple}, hash: 903BB0360C107DE923E115FB261270E3
    @Test
    void testProcessLineIntoTriple() {
        String line = "<http://example.com/subject> <http://example.com/predicate> \"object\" .";
        Triple result = StreamingMetadataCreator.processLineIntoTriple(line);
        assertNotNull(result);
        assertTrue(result.subject instanceof IRI);
        assertTrue(result.predicate instanceof IRI);
        assertFalse(result.object instanceof IRI);
    }

    //BaseRock generated method id: ${testAddMetadataToTableSchema}, hash: 820A46F89A9205BDBF408500200529B6
    @Test
    void testAddMetadataToTableSchema() {
        streamingMetadataCreator.tableSchema = new TableSchema();
        streamingMetadataCreator.tableSchema.setColumns(new ArrayList<>());
        ValueFactory vf = SimpleValueFactory.getInstance();
        Triple triple = new Triple(vf.createIRI("http://example.com/subject"), vf.createIRI("http://example.com/predicate"), vf.createLiteral("object"));
        streamingMetadataCreator.addMetadataToTableSchema(triple);
        assertEquals(1, streamingMetadataCreator.tableSchema.getColumns().size());
        Column column = streamingMetadataCreator.tableSchema.getColumns().get(0);
        assertEquals("predicate", column.getName());
        assertEquals("http://example.com/predicate", column.getPropertyUrl());
        assertEquals("http://example.com/{+Subject}", column.getAboutUrl());
    }

    //BaseRock generated method id: ${testConsolidateMetadataAndCSVs}, hash: 0AF72C6FFFCF0798214ADFF89B345A06
    @Test
    void testConsolidateMetadataAndCSVs() {
        Metadata oldMetadata = new Metadata();
        MetadataConsolidator mockMc = mock(MetadataConsolidator.class);
        CSVConsolidator mockCc = mock(CSVConsolidator.class);
        Metadata consolidatedMetadata = new Metadata();
        when(mockMc.consolidateMetadata(oldMetadata)).thenReturn(consolidatedMetadata);
        Metadata result = streamingMetadataCreator.consolidateMetadataAndCSVs(oldMetadata);
        assertNotNull(result);
        verify(mockMc, times(0)).consolidateMetadata(oldMetadata);
        verify(mockCc, times(0)).consolidateCSVs(oldMetadata, consolidatedMetadata);
    }

    //BaseRock generated method id: ${testThereIsMatchingColumnAlready}, hash: A472AF27797780A32D589E95AEE6A334
    @Test
    void testThereIsMatchingColumnAlready() {
        streamingMetadataCreator.tableSchema = new TableSchema();
        streamingMetadataCreator.tableSchema.setColumns(new ArrayList<>());
        Column existingColumn = new Column();
        existingColumn.setName("testColumn");
        existingColumn.setTitles("Test Column");
        existingColumn.setPropertyUrl("http://example.com/test");
        existingColumn.setAboutUrl("http://example.com/{+Subject}");
        streamingMetadataCreator.tableSchema.getColumns().add(existingColumn);
        Column newColumn = new Column();
        newColumn.setName("testColumn");
        newColumn.setTitles("Test Column");
        newColumn.setPropertyUrl("http://example.com/test");
        newColumn.setAboutUrl("http://example.com/{+Subject}");
        ValueFactory vf = SimpleValueFactory.getInstance();
        Triple triple = new Triple(vf.createIRI("http://example.com/subject"), vf.createIRI("http://example.com/test"), vf.createLiteral("test value"));
        boolean result = streamingMetadataCreator.thereIsMatchingColumnAlready(newColumn, triple);
        assertTrue(result);
    }

    //BaseRock generated method id: ${testCreateNewMetadata}, hash: 616FDE5AF88251921BC2B0C4AA4253C5
    @Test
    void testCreateNewMetadata() {
        streamingMetadataCreator.metadata = new Metadata();
        Table table = new Table();
        streamingMetadataCreator.metadata.getTables().add(table);
        streamingMetadataCreator.fileNameToRead = "test.nt";
        String result = streamingMetadataCreator.createNewMetadata();
        assertEquals("test.nt1.csv", result);
        assertEquals(2, streamingMetadataCreator.metadata.getTables().size());
        assertNotNull(streamingMetadataCreator.tableSchema);
        assertEquals("Subject", streamingMetadataCreator.tableSchema.getPrimaryKey());
        assertEquals(1, streamingMetadataCreator.tableSchema.getColumns().size());
    }
}