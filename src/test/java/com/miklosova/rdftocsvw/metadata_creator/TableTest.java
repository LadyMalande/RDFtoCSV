package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.RDFtoCSV;

import com.miklosova.rdftocsvw.convertor.Row;
import com.miklosova.rdftocsvw.support.BaseTest;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import java.util.ArrayList;
import org.junit.jupiter.params.provider.CsvSource;

import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class TableTest extends BaseTest {

    private Table table;

    @BeforeEach
    void setUp() {
        rdfToCSV = new RDFtoCSV(fileName);
        table = new Table();
        args = new String[]{"-f", "test.rdf", "-p", "rdf4j"};
        ConfigurationManager.loadSettingsFromInputToConfigFile(args);
        keys = new ArrayList<>();
        keys.add(iri("http://predicate1.cz"));
        keys.add(iri("http://predicate2.cz"));
        keys1 = new ArrayList<>();
        keys1.add(iri("http://predicate3.cz"));
        keys1.add(iri("http://predicate4.cz"));
        firstRow = new Row(iri("http://subject1.cz"), iri("http://predicate1.cz") , true);
        secondRow = new Row(iri("http://subject2.cz"), iri("http://predicate1.cz") , true);
        rows = new ArrayList<>();
        rows.add(firstRow);
        rows.add(secondRow);
        System.out.println("CONFIGURATION ROWNUMS " + ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.METADATA_ROWNUMS));
    }

    //BaseRock generated method id: ${testTableConstructorWithUrl}, hash: 206520DA09663338B6B1A5B79EBF7A57
    @Test
    void testTableConstructorWithUrl() {
        String url = "http://example.com";
        Table tableWithUrl = new Table(url);
        assertEquals(url, tableWithUrl.getUrl());
    }

    //BaseRock generated method id: ${testGetSetTransformations}, hash: 24EFEFA780BA12AF8EC1A2661C668C5F
    @Test
    void testGetSetTransformations() {
        List<Transformation> transformations = new ArrayList<>();
        transformations.add(new Transformation());
        table.setTransformations(transformations);
        assertEquals(transformations, table.getTransformations());
    }

    //BaseRock generated method id: ${testGetSetUrl}, hash: CAA4FB7CE2A04C837CF16BC72C1A2BB7
    @Test
    void testGetSetUrl() {
        String url = "http://example.com";
        table.setUrl(url);
        assertEquals(url, table.getUrl());
    }

    //BaseRock generated method id: ${testGetSetTableSchema}, hash: A69AFDB07FA757E0769A04507AE5E907
    @Test
    void testGetSetTableSchema() {
        TableSchema tableSchema = new TableSchema();
        table.setTableSchema(tableSchema);
        assertEquals(tableSchema, table.getTableSchema());
    }

    //BaseRock generated method id: ${testAddTableMetadata}, hash: BEF3E3B37D2F93B705AE9EB850CCCBD5
    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void testAddTableMetadata(String hasBlankNodes) throws Exception {
        //ArrayList<Value> keys = new ArrayList<>();
        //ArrayList<Row> rows = new ArrayList<>();
        if (Boolean.parseBoolean(hasBlankNodes)) {
            ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_BLANK_NODES, "true");
        }
    table.addTableMetadata(keys, rows);
    assertNotNull(table.getTableSchema());
    assertEquals(keys, table.getTableSchema().getKeys());
    assertEquals(rows, table.getTableSchema().getRows());
    if (Boolean.parseBoolean(hasBlankNodes)) {

        assertNotNull(table.getTransformations());
        assertEquals(1, table.getTransformations().size());
        Transformation transformation = table.getTransformations().get(0);
        assertEquals("https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/main/scripts/transformationForBlankNodesStreamed.js", transformation.getUrl());
        assertEquals("http://www.iana.org/assignments/media-types/application/javascript", transformation.getScriptFormat());
        assertEquals("http://www.iana.org/assignments/media-types/turtle", transformation.getTargetFormat());
        assertEquals("rdf", transformation.getSource());
        assertEquals("RDF format used as the output format in the transformation from CSV to RDF", transformation.getTitles());
    } else {
        assertNull(table.getTransformations());
    }

    }
}