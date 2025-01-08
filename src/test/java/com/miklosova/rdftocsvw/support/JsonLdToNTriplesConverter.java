package com.miklosova.rdftocsvw.support;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.JSONLDSettings;
import org.junit.jupiter.api.*;

import java.io.*;
@Disabled
public class JsonLdToNTriplesConverter {

    final static String DIRECTORY_PATH = "src/test/resources/OriginalIsSubsetOfCSV/";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void transformJsonLdToNTriples() {
        // Input JSON-LD file and Output N-Triples file
        String inputFilePath = "events_Brno.jsonld";
        //String inputFilePath = "sample.jsonld";
        String outputFilePath = "events_Brno_converted.nt";
        //String outputFilePath = "sample_converted.nt";

        // Create a parser for JSON-LD
        RDFParser parser = Rio.createParser(RDFFormat.JSONLD);

        // Configure the parser: Disable SECURE_MODE
        parser.getParserConfig().set(JSONLDSettings.SECURE_MODE, false);

        // Create an RDFHandler to write output in N-Triples format
        try (InputStream inputStream = new FileInputStream(inputFilePath);
             OutputStream outputStream = new FileOutputStream(outputFilePath)) {

            RDFWriter writer = Rio.createWriter(RDFFormat.NTRIPLES, outputStream);

            // Explicitly call startRDF() before processing statements
            writer.startRDF();


            // Set the RDFHandler to process the triples
            parser.setRDFHandler(new AbstractRDFHandler() {
                @Override
                public void handleStatement(Statement st) {
                    // Write each parsed triple to the output file
                    writer.handleStatement(st);
                }
            });

            // Parse the input JSON-LD file with an explicit base URI
            parser.parse(inputStream, "http://example.org/base/");

            // Properly finalize the writer
            writer.endRDF();

            System.out.println("Successfully converted JSON-LD to N-Triples.");

        } catch (IOException | RDFParseException | RDFHandlerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void
    transformTrigToNTriples(){
        // Define input JSON-LD file and output N-Triples file
        File inputFile = new File("posudky_trig.trig");
        File outputFile = new File("posudky_rdf4j.nt");

        try (InputStream inputStream = new FileInputStream(inputFile);
             OutputStream outputStream = new FileOutputStream(outputFile)) {

            // Parse the JSON-LD file into an in-memory RDF model
            Model model = Rio.parse(inputStream, "", RDFFormat.TRIG);

            // Write the RDF model to N-Triples format
            Rio.write(model, outputStream, RDFFormat.NTRIPLES);

            System.out.println("Successfully converted JSON-LD to N-Triples format!");

        } catch (Exception e) {
            System.err.println("Error while processing JSON-LD: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Test
    void readRDF() {

    }
}
