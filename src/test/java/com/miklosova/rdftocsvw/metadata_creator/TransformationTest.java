package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Transformation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.ParameterizedTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class TransformationTest {

    //BaseRock generated method id: ${testDefaultConstructor}, hash: 0C650508D37C1FD7B4EF035A0663F65C
    @Test
    void testDefaultConstructor() {
        Transformation transformation = new Transformation();
        assertEquals("https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/main/scripts/transformationForBlankNodesStreamed.js", transformation.getUrl());
        assertEquals("http://www.iana.org/assignments/media-types/application/javascript", transformation.getScriptFormat());
        assertEquals("http://www.iana.org/assignments/media-types/turtle", transformation.getTargetFormat());
        assertEquals("rdf", transformation.getSource());
        assertEquals("RDF format used as the output format in the transformation from CSV to RDF", transformation.getTitles());
    }

    //BaseRock generated method id: ${testParameterizedConstructor}, hash: 2448D55E6F171AA49877D61D5149E92E
    @ParameterizedTest
    @CsvSource({ "http://example.com, application/json, text/csv, csv, CSV to JSON", "http://test.org, text/plain, application/xml, xml, XML Transformation" })
    void testParameterizedConstructor(String url, String scriptFormat, String targetFormat, String source, String titles) {
        Transformation transformation = new Transformation(url, scriptFormat, targetFormat, source, titles);
        assertEquals(url, transformation.getUrl());
        assertEquals(scriptFormat, transformation.getScriptFormat());
        assertEquals(targetFormat, transformation.getTargetFormat());
        assertEquals(source, transformation.getSource());
        assertEquals(titles, transformation.getTitles());
    }

    //BaseRock generated method id: ${testGetters}, hash: FD694156090506E1F830024C4BD20D8D
    @Test
    void testGetters() {
        Transformation transformation = new Transformation("http://example.com", "application/json", "text/csv", "csv", "CSV to JSON");
        assertEquals("http://example.com", transformation.getUrl());
        assertEquals("application/json", transformation.getScriptFormat());
        assertEquals("text/csv", transformation.getTargetFormat());
        assertEquals("csv", transformation.getSource());
        assertEquals("CSV to JSON", transformation.getTitles());
    }
}
