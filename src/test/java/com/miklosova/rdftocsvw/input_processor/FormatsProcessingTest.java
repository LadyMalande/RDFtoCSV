package com.miklosova.rdftocsvw.input_processor;


import com.sun.source.doctree.SeeTree;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FormatsProcessingTest {

    String csvFromTtl;
    final String inputProcessingMethod = "rdf4j";
    MethodService ms;
    Repository db;
    MethodService msForTurtle;
    Repository dbForTurtle;
    String queryString, queryStringForTest;
    RepositoryConnection rcForTurtle;
    TupleQueryResult resultForTurtle;
    String filePathForTurtle = "src/test/resources/testingInput.ttl";
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
        String filePath = "src/test/resources/testingInput.ttl";
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
    void csvIsSameTrig() throws IOException {
        String filePath = "src/test/resources/testingInput.trig";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        reportCurrentMethodName(rc, this);

        Assert.assertNotNull(rc);

        setValuesFromTest(rc);

        Assert.assertEquals(valuesFromTest, valuesFromTurtle);

    }

    @Test
    void csvIsSameHTML() throws IOException {
        String filePath = "src/test/resources/testingInput.html";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        reportCurrentMethodName(rc, this);

        Assert.assertNotNull(rc);

        setValuesFromTest(rc);

        Assert.assertEquals(valuesFromTest, valuesFromTurtle);
    }

    @Test
    void csvIsSameJsonLD() throws IOException {
        String filePath = "src/test/resources/testingInput.jsonld";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        reportCurrentMethodName(rc, this);

        Assert.assertNotNull(rc);

        setValuesFromTest(rc);

        Assert.assertEquals(valuesFromTest, valuesFromTurtle);
    }

    @Test
    void csvIsSameNq() throws IOException {
        String filePath = "src/test/resources/testingInput.nq";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        reportCurrentMethodName(rc, this);

        Assert.assertNotNull(rc);

        setValuesFromTest(rc);

        Assert.assertEquals(valuesFromTest, valuesFromTurtle);
    }

    @Test
    void csvIsSameNt() throws IOException {
        String filePath = "src/test/resources/testingInput.nt";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        reportCurrentMethodName(rc, this);

        Assert.assertNotNull(rc);

        setValuesFromTest(rc);

        Assert.assertEquals(valuesFromTest, valuesFromTurtle);
    }

    @Test
    void csvIsSameRDF() throws IOException {
        String filePath = "src/test/resources/testingInput.rdf";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        reportCurrentMethodName(rc, this);

        Assert.assertNotNull(rc);

        setValuesFromTest(rc);

        Assert.assertEquals(valuesFromTest, valuesFromTurtle);
    }

    @Test
    void csvIsCreatedFromTurtle() throws IOException {
        String filePath = "src/test/resources/testingInput.ttl";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        reportCurrentMethodName(rc, this);

        Assert.assertNotNull(rc);

        setValuesFromTest(rc);

        Assert.assertEquals(valuesFromTest, valuesFromTurtle);
    }

    @Test
    void csvIsCreatedFromBrf() throws IOException {
        String filePath = "src/test/resources/testingInput.brf";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        reportCurrentMethodName(rc, this);

        Assert.assertNotNull(rc);

        setValuesFromTest(rc);

        Assert.assertEquals(valuesFromTest, valuesFromTurtle);
    }

    @Test
    void csvIsCreatedFromNdjsonld() throws IOException {
        String filePath = "src/test/resources/testingInput.ndjsonld";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    // HDT format writer/reader is not implemented in RDF4J due to licencing issues.
    @Test
    @Disabled
    void csvIsCreatedFromHdt() throws IOException {
        String filePath = "src/test/resources/testingInput.hdt";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromN3() throws IOException {
        String filePath = "src/test/resources/testingInput.n3";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromJsonl() throws IOException {
        String filePath = "src/test/resources/testingInput.jsonl";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromNdjson() throws IOException {
        String filePath = "src/test/resources/testingInput.ndjson";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromXhtml() throws IOException {
        String filePath = "src/test/resources/testingInput.xhtml";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromRj() throws IOException {
        String filePath = "src/test/resources/testingInput.rj";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromRdfs() throws IOException {
        String filePath = "src/test/resources/testingInput.rdfs";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromOwl() throws IOException {
        String filePath = "src/test/resources/testingInput.owl";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromXml() throws IOException {
        String filePath = "src/test/resources/testingInput.xml";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromTrigs() throws IOException {
        String filePath = "src/test/resources/testingInput.trigs";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromTrix() throws IOException {
        String filePath = "src/test/resources/testingInput.trix";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromTtls() throws IOException {
        String filePath = "src/test/resources/testingInput.ttls";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }


    @Test
    @Disabled
    void csvIsCreatedFromHTML() throws IOException {
        String filePath = "src/test/resources/typy-tříděného-odpadu.html";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromURI() throws IOException {
        String filePath = "https://gist.githubusercontent.com/kal/ee1260ceb462d8e0d5bb/raw/1364c2bb469af53323fdda508a6a579ea60af6e4/log_sample.ttl";
        System.out.println(filePath.toString());
        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }
}
