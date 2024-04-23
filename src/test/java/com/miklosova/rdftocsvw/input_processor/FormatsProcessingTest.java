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
    void csvIsCreatedFromBrf() {
        String filePath = "src/test/resources/testingInput.brf";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromNdjsonld() {
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
    void csvIsCreatedFromHdt() {
        String filePath = "src/test/resources/testingInput.hdt";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromN3() {
        String filePath = "src/test/resources/testingInput.n3";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromJsonl() {
        String filePath = "src/test/resources/testingInput.jsonl";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromNdjson() {
        String filePath = "src/test/resources/testingInput.ndjson";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromXhtml() {
        String filePath = "src/test/resources/testingInput.xhtml";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromRj() {
        String filePath = "src/test/resources/testingInput.rj";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromRdfs() {
        String filePath = "src/test/resources/testingInput.rdfs";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromOwl() {
        String filePath = "src/test/resources/testingInput.owl";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromXml() {
        String filePath = "src/test/resources/testingInput.xml";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromTrigs() {
        String filePath = "src/test/resources/testingInput.trigs";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromTrix() {
        String filePath = "src/test/resources/testingInput.trix";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }

    @Test
    void csvIsCreatedFromTtls() {
        String filePath = "src/test/resources/testingInput.ttls";

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

    @Test
    void csvIsCreatedFromURI() {
        String filePath = "https://gist.githubusercontent.com/kal/ee1260ceb462d8e0d5bb/raw/1364c2bb469af53323fdda508a6a579ea60af6e4/log_sample.ttl";
        System.out.println(filePath.toString());
        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        System.out.println("Reporting from tests " + methodName);
        System.out.println(rc != null);
        Assert.assertNotNull(rc);
    }
}
