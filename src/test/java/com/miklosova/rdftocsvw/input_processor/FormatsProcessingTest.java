package com.miklosova.rdftocsvw.input_processor;


import com.miklosova.rdftocsvw.support.BaseTest;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.junit.After;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FormatsProcessingTest extends BaseTest {

    String csvFromTtl;
    final String inputProcessingMethod = "rdf4j";
    MethodService ms;
    Repository db;
    MethodService msForTurtle;
    Repository dbForTurtle;
    String queryString, queryStringForTest;
    RepositoryConnection rcForTurtle;
    TupleQueryResult resultForTurtle;
    String filePathForTurtle = "src/test/resources/differentSerializations/testingInput.ttl";
    Set<Value> valuesFromTurtle;
    Set<Value> valuesFromTest;

    @BeforeEach
    void prepareQuery(){
        SelectQuery selectQuery = Queries.SELECT();
        Variable o = SparqlBuilder.var("o"), s = SparqlBuilder.var("s"),
                s_in = SparqlBuilder.var("s_in"),p_in = SparqlBuilder.var("p_in");
        selectQuery.select(s).where(s.isA(o));

        queryString = selectQuery.getQueryString();
    }

    @BeforeEach
    void prepareConnectionAndResult(){
        valuesFromTurtle = new HashSet<>();
        msForTurtle = new MethodService();
        dbForTurtle = new SailRepository(new MemoryStore());
        try(RepositoryConnection rcForTurtle = msForTurtle.processInput(filePathForTurtle, inputProcessingMethod, dbForTurtle)){
            TupleQuery query = rcForTurtle.prepareTupleQuery(queryString);
            System.out.println("query.getDataset() @Before prepareConnectionAndResult " + query.getDataset());
            // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
            try (TupleQueryResult resultForTurtle = query.evaluate()) {
                for (BindingSet solution : resultForTurtle) {
                    System.out.println("?subject = " + solution.getValue("s") + " ?object = " + solution.getValue("o"));

                    valuesFromTurtle.add(solution.getValue("s"));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        String filePath = "src/test/resources/differentSerializations/testingInput.ttl";
        String fileOutput = "src/test/resources/csvFileToTestSameCSV";
        ms = new MethodService();
        db = new SailRepository(new MemoryStore());

        SelectQuery selectQuery = Queries.SELECT();
        Variable o = SparqlBuilder.var("o"), s = SparqlBuilder.var("s"),
                s_in = SparqlBuilder.var("s_in"),p_in = SparqlBuilder.var("p_in");
        selectQuery.select(s).where(s.isA(o));

        queryStringForTest = selectQuery.getQueryString();

        valuesFromTest = new HashSet<>();
    }

    @After
    public void tearDown() {
    }

    private void setValuesFromTest(RepositoryConnection rc){

        TupleQuery query = rc.prepareTupleQuery(queryStringForTest);
        try (TupleQueryResult result = query.evaluate()) {
            for (BindingSet solution : result) {
                System.out.println("?subject = " + solution.getValue("s") + " ?object = " + solution.getValue("o"));

                valuesFromTest.add(solution.getValue("s"));
            }
        }
    }

    private void reportCurrentMethodName(RepositoryConnection rc, Object o){
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
    }

    @Test
    void valuesInConnectionAreSameTrig() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.trig";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        reportCurrentMethodName(rc, this);

        Assert.assertNotNull(rc);

        setValuesFromTest(rc);

        Assert.assertEquals(valuesFromTest, valuesFromTurtle);

    }

    @Test
    void repositoryConnectionIsEstablishedForNquad() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.nq";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        reportCurrentMethodName(rc, this);

        Assert.assertNotNull(rc);

        setValuesFromTest(rc);

        Assert.assertEquals(valuesFromTest, valuesFromTurtle);
    }

    @Test
    void repositoryConnectionIsEstablishedForJsonLd() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.jsonld";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void repositoryConnectionIsEstablishedForNTriples() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.nt";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        reportCurrentMethodName(rc, this);

        Assert.assertNotNull(rc);

        setValuesFromTest(rc);

        Assert.assertEquals(valuesFromTest, valuesFromTurtle);
    }

    @Test
    void repositoryConnectionIsEstablishedForTurtle() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.ttl";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        reportCurrentMethodName(rc, this);

        Assert.assertNotNull(rc);

        setValuesFromTest(rc);

        Assert.assertEquals(valuesFromTest, valuesFromTurtle);
    }

    @Test
    void repositoryConnectionIsEstablishedForBrf() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.brf";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        reportCurrentMethodName(rc, this);

        Assert.assertNotNull(rc);

        setValuesFromTest(rc);

        Assert.assertEquals(valuesFromTest, valuesFromTurtle);
    }

    @Test
    void repositoryConnectionIsEstablishedForNdjsonld() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.ndjsonld";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    // HDT format writer/reader is not implemented in RDF4J due to licencing issues.
    @Disabled
    @Test
    void repositoryConnectionIsEstablishedForHdt() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.hdt";
/*
        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);

 */
        //Assert.assertNotNull(rc);
        // Was not able to test positive test for .hdt file format test.
        // So the default test case is expecting thrown exception.
        Assert.assertThrows(UnsupportedRDFormatException.class, () -> {
            ms.processInput(filePath, inputProcessingMethod, db);
        });
    }

    @Test
    void repositoryConnectionIsEstablishedForN3() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.n3";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void repositoryConnectionIsEstablishedForJsonl() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.jsonl";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void repositoryConnectionIsEstablishedForNdjson() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.ndjson";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void repositoryConnectionIsEstablishedForXhtml() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.xhtml";
        // As of 2nd July 2024, the parser for RDFa does not work in rdf4j according to https://github.com/eclipse-rdf4j/rdf4j/issues/512
        // Default test is therefore set to throw exception while parsing the file

        Assert.assertThrows(UnsupportedRDFormatException.class, () -> {
            ms.processInput(filePath, inputProcessingMethod, db);
        });
    }

    @Test
    void repositoryConnectionIsEstablishedForRj() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.rj";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void repositoryConnectionIsEstablishedForRdfs() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.rdfs";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void repositoryConnectionIsEstablishedForOwl() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.owl";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void repositoryConnectionIsEstablishedForXml() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.xml";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void repositoryConnectionIsEstablishedForTrigs() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.trigs";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void repositoryConnectionIsEstablishedForTrix() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.trix";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void repositoryConnectionIsEstablishedForTtls() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.ttls";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }


    @Test
    void repositoryConnectionIsEstablishedForHTML() throws IOException {
        String filePath = "src/test/resources/typy-tříděného-odpadu.html";

        // As of 2nd July 2024, the parser for RDFa does not work in rdf4j according to https://github.com/eclipse-rdf4j/rdf4j/issues/512
        // Default test is therefore set to throw exception while parsing the file

        Assert.assertThrows(UnsupportedRDFormatException.class, () -> {
            ms.processInput(filePath, inputProcessingMethod, db);
        });
    }

    @Test
    void repositoryConnectionIsEstablishedForURI() throws IOException {
        String filePath = "https://gist.githubusercontent.com/kal/ee1260ceb462d8e0d5bb/raw/1364c2bb469af53323fdda508a6a579ea60af6e4/log_sample.ttl";
        System.out.println(filePath.toString());
        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }
}
