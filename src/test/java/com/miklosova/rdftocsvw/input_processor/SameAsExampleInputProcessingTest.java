package com.miklosova.rdftocsvw.input_processor;

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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.*;

@RunWith(Parameterized.class)
public class SameAsExampleInputProcessingTest {
    private Map<String, String> map;
    private String nameForTest;
    private String filePath;
    private String filePathForImage;
    final String inputProcessingMethod = "rdf4j";
    MethodService ms;
    Repository db;
    MethodService msForTurtle;
    Repository dbForTurtle;
    String queryString, queryStringForTest;
    Set<Value> valuesFromTurtle;
    Set<Value> valuesFromTest;

    private void prepareQuery(){
        SelectQuery selectQuery = Queries.SELECT();
        Variable o = SparqlBuilder.var("o"), s = SparqlBuilder.var("s");
        selectQuery.select(s).where(s.isA(o));

        queryString = selectQuery.getQueryString();
    }

    private void prepareConnectionAndResult(){
        valuesFromTurtle = new HashSet<>();
        msForTurtle = new MethodService();
        dbForTurtle = new SailRepository(new MemoryStore());
        try(RepositoryConnection rcForTurtle = msForTurtle.processInput(filePathForImage, inputProcessingMethod, dbForTurtle)){
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

    void setUp() {
        ms = new MethodService();
        db = new SailRepository(new MemoryStore());

        SelectQuery selectQuery = Queries.SELECT();
        Variable o = SparqlBuilder.var("o"), s = SparqlBuilder.var("s");
        selectQuery.select(s).where(s.isA(o));

        queryStringForTest = selectQuery.getQueryString();

        valuesFromTest = new HashSet<>();
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

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs(){
        return Arrays.asList(new Object[][]{
                { "TRIG-TURTLE", "src/test/resources/differentSerializations/testingInput.trig", "src/test/resources/differentSerializations/testingInput.ttl"},
                { "TURTLE-RDF", "src/test/resources/differentSerializations/testingInput.ttl", "src/test/resources/differentSerializations/testingInput.rdf"},
                { "NQ-NT", "src/test/resources/differentSerializations/testingInput.nq", "src/test/resources/differentSerializations/testingInput.nt"},
                { "TURTLE-NT", "src/test/resources/differentSerializations/testingInput.ttl", "src/test/resources/differentSerializations/testingInput.nt"},

                { "N3-TRIGS", "src/test/resources/differentSerializations/testingInput.n3", "src/test/resources/differentSerializations/testingInput.trigs"},
                { "NDJSONLD-TURTLESTAR", "src/test/resources/differentSerializations/testingInput.ndjsonld", "src/test/resources/differentSerializations/testingInput.ttls"},
                // Neither of formats have not been parsed successfully with rdf4j. Removing the test from parameters.
                //{ "BRF-HDT", "src/test/resources/differentSerializations/testingInput.brf", "src/test/resources/differentSerializations/testingInput.hdt"},
                // As of July 2024, .html parsing is not supported by rdf4j. Removing the test from parameters.
                //{ "TURTLE-HTML", "src/test/resources/differentSerializations/testingInput.ttl", "src/test/resources/differentSerializations/testingInput.html"},
                { "RJ-OWL", "src/test/resources/differentSerializations/testingInput.rj", "src/test/resources/differentSerializations/testingInput.owl"},
                { "JSONL-NDJSON", "src/test/resources/differentSerializations/testingInput.jsonl", "src/test/resources/differentSerializations/testingInput.ndjson"},
                { "JSONLD-RDFS", "src/test/resources/differentSerializations/testingInput.jsonld" ,"src/test/resources/differentSerializations/testingInput.rdfs" },
                { "TRIX-TTLS", "src/test/resources/differentSerializations/testingInput.trix", "src/test/resources/differentSerializations/testingInput.ttls"},
        });
    }

    public SameAsExampleInputProcessingTest(String nameForTest, String filePath, String filePathForImage){
        this.nameForTest = nameForTest;
        this.filePath = filePath;
        this.filePathForImage = filePathForImage;
    }

    @Test
    public void test() throws IOException {
        prepareQuery();
        prepareConnectionAndResult();
        setUp();


        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        reportCurrentMethodName(rc, this);

        Assert.assertNotNull(rc);

        setValuesFromTest(rc);

        Assert.assertEquals(valuesFromTest, valuesFromTurtle);
    }

}
