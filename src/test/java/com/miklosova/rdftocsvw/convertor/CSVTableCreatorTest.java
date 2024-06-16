package com.miklosova.rdftocsvw.convertor;

import org.junit.After;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class CSVTableCreatorTest {

    String csvFromTtl;

    @BeforeEach
    void setUp() {
        String filePath = "src/test/resources/testingInput.ttl";
        String fileOutput = "src/test/resources/csvFileToTestSameCSV";

        CSVTableCreator tc = new CSVTableCreator(";", fileOutput, filePath);
        csvFromTtl = tc.getCSVTableAsString();
    }

    @After
    public void tearDown() {
    }

    @Test
    void csvIsSameTrig() {
        String filePath = "src/test/resources/testingInput.trig";
        String fileOutput = "src/test/resources/csvFileToTestSameCSVTrig";

        CSVTableCreator tc = new CSVTableCreator(";", fileOutput, filePath);
        String csvFromTrig = tc.getCSVTableAsString();

        System.out.println("Reporting from tests csvFromTtl ");
        System.out.println(csvFromTtl.toString());
        System.out.println("Reporting from tests csvFromTrig ");
        System.out.println(csvFromTrig.toString());
        Assert.assertTrue(csvFromTtl.equals(csvFromTrig));
    }

    @Test
    @Disabled
    void csvIsSameHTML() {
        String filePath = "src/test/resources/testingInput.html";
        String fileOutput = "src/test/resources/csvFileToTestSameCSVhtml";

        CSVTableCreator tc = new CSVTableCreator(";", fileOutput, filePath);
        String csvFromhtml = tc.getCSVTableAsString();


        Assert.assertTrue(csvFromTtl.equals(csvFromhtml));
    }

    @Test
    @Disabled
    void csvIsSameJsonLD() {
        String filePath = "src/test/resources/testingInput.jsonld";
        String fileOutput = "src/test/resources/csvFileToTestSameCSVjsonld";

        CSVTableCreator tc = new CSVTableCreator(";", fileOutput, filePath);
        String csvFromJsonld = tc.getCSVTableAsString();


        Assert.assertTrue(csvFromTtl.equals(csvFromJsonld));
    }

    @Test
    void csvIsSameNq() {
        String filePath = "src/test/resources/testingInput.nq";
        String fileOutput = "src/test/resources/csvFileToTestSameCSVnq";

        CSVTableCreator tc = new CSVTableCreator(";", fileOutput, filePath);
        String csvFromNq = tc.getCSVTableAsString();


        Assert.assertTrue(csvFromTtl.equals(csvFromNq));
    }

    @Test
    void csvIsSameNt() {
        String filePath = "src/test/resources/testingInput.nt";
        String fileOutput = "src/test/resources/csvFileToTestSameCSVnt";

        CSVTableCreator tc = new CSVTableCreator(";", fileOutput, filePath);
        String csvFromnt = tc.getCSVTableAsString();


        Assert.assertTrue(csvFromTtl.equals(csvFromnt));
    }

    @Test
    void csvIsSameRDF() {
        String filePath = "src/test/resources/testingInput.rdf";
        String fileOutput = "src/test/resources/csvFileToTestSameCSVrdf";

        CSVTableCreator tc = new CSVTableCreator(";", fileOutput, filePath);
        String csvFromrdf = tc.getCSVTableAsString();


        Assert.assertTrue(csvFromTtl.equals(csvFromrdf));
    }

    @Test
    void csvIsCreatedFromTurtle() {
        String filePath = "src/test/resources/testingInput.ttl";
        String fileOutput = "src/test/resources/csvFileToTestSameCSVttl";

        CSVTableCreator tc = new CSVTableCreator(";", fileOutput, filePath);
        String csvFromRdf = tc.getCSVTableAsString();


        Assert.assertNotNull(csvFromRdf);
    }

    @Test
    @Disabled
    void csvIsCreatedFromHTML() {
        String filePath = "src/test/resources/typy-tříděného-odpadu.html";
        String fileOutput = "src/test/resources/csvFileToTestSameCSVHTML2";

        CSVTableCreator tc = new CSVTableCreator(";", fileOutput, filePath);
        String csvFromRdf = tc.getCSVTableAsString();


        Assert.assertNotNull(csvFromRdf);
    }
/*
    @Test
    void csvIsCreatedFrom() {
        String filePath = "src/test/resources/testingInput";
        String fileOutput = "src/test/resources/csvFileToTestSameCSV";

        CSVTableCreator tc = new CSVTableCreator(";", fileOutput, filePath);
        String csvFromRdf = tc.getCSVTableAsString();


        Assert.assertNotNull(csvFromRdf);
    }


    addLoader(RDFFormat.RDFXML,"rdf"); DONE
        addLoader(RDFFormat.RDFXML,"rdfs");
        addLoader(RDFFormat.RDFXML,"owl");
        addLoader(RDFFormat.RDFXML,"xml");
        addLoader(RDFFormat.NTRIPLES,"nt");DONE
        addLoader(RDFFormat.TURTLE,"ttl"); DONE
        addLoader(RDFFormat.TURTLESTAR,"ttls");
        addLoader(RDFFormat.N3,"n3");
        addLoader(RDFFormat.TRIX,"xml");
        addLoader(RDFFormat.TRIX,"trix");
        addLoader(RDFFormat.TRIG,"trig"); DONE
        addLoader(RDFFormat.TRIGSTAR,"trigs");
        addLoader(RDFFormat.BINARY,"brf");
        addLoader(RDFFormat.NQUADS,"nq"); DONE
        addLoader(RDFFormat.JSONLD,"jsonld");
        addLoader(RDFFormat.NDJSONLD,"ndjsonld");
        addLoader(RDFFormat.NDJSONLD,"jsonl");
        addLoader(RDFFormat.NDJSONLD,"ndjson");
        addLoader(RDFFormat.RDFJSON,"rj");
        addLoader(RDFFormat.RDFA,"xhtml");
        addLoader(RDFFormat.RDFA,"html");
        addLoader(RDFFormat.HDT,"hdt");
     */
}
