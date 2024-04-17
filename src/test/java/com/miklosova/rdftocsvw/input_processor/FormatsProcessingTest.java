package com.miklosova.rdftocsvw.input_processor;


import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.After;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FormatsProcessingTest {

    String csvFromTtl;
    final String inputProcessingMethod = "rdf4j";
    MethodService ms;
    Repository db;

    @BeforeEach
    void setUp() {
        String filePath = "src/test/resources/testingInput.ttl";
        String fileOutput = "src/test/resources/csvFileToTestSameCSV";

        ms = new MethodService();
        db = new SailRepository(new MemoryStore());
    }

    @After
    void tearDown() {
    }

    @Test
    void csvIsSameTrig() {
        String filePath = "src/test/resources/testingInput.trig";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsSameHTML() {
        String filePath = "src/test/resources/testingInput.html";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsSameJsonLD() {
        String filePath = "src/test/resources/testingInput.jsonld";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsSameNq() {
        String filePath = "src/test/resources/testingInput.nq";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsSameNt() {
        String filePath = "src/test/resources/testingInput.nt";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsSameRDF() {
        String filePath = "src/test/resources/testingInput.rdf";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromTurtle() {
        String filePath = "src/test/resources/testingInput.ttl";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    @Disabled
    void csvIsCreatedFromHTML() {
        String filePath = "src/test/resources/typy-tříděného-odpadu.html";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }
}
