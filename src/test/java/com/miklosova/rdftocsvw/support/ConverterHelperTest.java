package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.input_processor.MethodService;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.eclipse.rdf4j.rio.*;

import static com.miklosova.rdftocsvw.support.TestSupport.assertFilesEqual;
import static com.miklosova.rdftocsvw.support.TestSupport.isFile1ContainedInFile2;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class ConverterHelperTest {
    RepositoryConnection rc;
    MethodService ms;
    Repository db;
    @BeforeEach
    public void setUp() throws IOException {
        String filePath = "src/test/resources/differentSerializations/testingInput.ttl";
        AppConfig config = new AppConfig.Builder(filePath)
                .parsing("rdf4j")
                .build();
        ms = new MethodService(config);
        db = new SailRepository(new MemoryStore());

    }

    // Template test to check the resulting file against the expected file
    @ParameterizedTest
    @CsvSource({
            "beforeBNodeReplacement.nt, expectedFileAfterBNodesReplacement1.nt, afterBNodeReplacement.nt",
            "beforeBNodeReplacement.ttl, expectedFileAfterBNodesReplacement2.ttl, afterBNodeReplacement.ttl",
            "beforeBNodeReplacement.xml, expectedFileAfterBNodesReplacement3.xml, afterBNodeReplacement.xml"
    })
    void testChangeBNodesForIri(String inputFileName, String expectedFileName, String outputFileName) throws IOException {

        // Define paths to your input and expected files
        String fullInputFileName = "src/test/resources/support/" + inputFileName;
        Path inputFilePath = Paths.get("src/test/resources/support", inputFileName);
        Path expectedFilePath = Paths.get("src/test/resources/support", expectedFileName);
        rc = ms.processInput(fullInputFileName, "rdf4j", db);
        // Temporary output file for testing
        Path outputFilePath = Paths.get("src/test/resources/support", outputFileName);

        // Ensure the output file is clean before testing
        Files.deleteIfExists(outputFilePath);

        ConverterHelper helper = new ConverterHelper();
        helper.changeBNodesForIri(rc);

        // Serialize the repository to a file in the chosen format (e.g., Turtle)
        File outputFile = new File(outputFilePath.toString());
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            // Serialize the repository in the Turtle format
            RDFWriter writer;
            if(inputFileName.endsWith(".nt")){
                writer = Rio.createWriter(RDFFormat.NTRIPLES, outputStream);
            } else if(inputFileName.endsWith(".ttl")){
                writer = Rio.createWriter(RDFFormat.TURTLE, outputStream);
            }
            else if(inputFileName.endsWith(".xml")){
                writer = Rio.createWriter(RDFFormat.RDFXML, outputStream);
            } else {
                throw new UnsupportedOperationException();
            }

            rc.export(writer);
            System.out.println("Data serialized to " + outputFile.getAbsolutePath() + " in Turtle format.");
        }

        // Compare the contents of the output file with the expected file
        assertTrue(isFile1ContainedInFile2(expectedFilePath,outputFile.toPath()));
    }

    @ParameterizedTest
    @CsvSource({
            "beforeNoBNodeReplacement.nt, beforeNoBNodeReplacement.nt, afterNoBNodeReplacement.nt",
            "beforeNoBNodeReplacement.ttl, beforeNoBNodeReplacement.ttl, afterNoBNodeReplacement.ttl",
            "beforeNoBNodeReplacement.xml, beforeNoBNodeReplacement.xml, afterNoBNodeReplacement.xml"
    })
    void testChangeBNodesForIriWithNoBNodes(String inputFileName, String expectedFileName, String outputFileName) throws IOException {
        // Define paths to your input and expected files
        String fullInputFileName = "src/test/resources/support/" + inputFileName;
        Path inputFilePath = Paths.get("src/test/resources/support", inputFileName);
        Path expectedFilePath = Paths.get("src/test/resources/support", expectedFileName);
        rc = ms.processInput(fullInputFileName, "rdf4j", db);
        // Temporary output file for testing
        Path outputFilePath = Paths.get("src/test/resources/support", outputFileName);

        // Ensure the output file is clean before testing
        Files.deleteIfExists(outputFilePath);

        ConverterHelper helper = new ConverterHelper();
        helper.changeBNodesForIri(rc);

        // Serialize the repository to a file in the chosen format (e.g., Turtle)
        File outputFile = new File(outputFilePath.toString());
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            // Serialize the repository in the Turtle format
            RDFWriter writer;
            if(inputFileName.endsWith(".nt")){
                writer = Rio.createWriter(RDFFormat.NTRIPLES, outputStream);
            } else if(inputFileName.endsWith(".ttl")){
                writer = Rio.createWriter(RDFFormat.TURTLE, outputStream);
            }
            else if(inputFileName.endsWith(".xml")){
                writer = Rio.createWriter(RDFFormat.RDFXML, outputStream);
            } else {
                throw new UnsupportedOperationException();
            }

            rc.export(writer);
            System.out.println("Data serialized to " + outputFile.getAbsolutePath() + " in Turtle format.");
        }

        // Compare the contents of the output file with the expected file
        assertTrue(isFile1ContainedInFile2(outputFile.toPath(), expectedFilePath));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "beforeNoBNodeReplacement.ttl, http://www.w3.org/1999/02/22-rdf-syntax-ns#Graph, 1, http://example.org/myGraph",
            "beforeNoBNodeReplacement.xml, http://purl.org/dc/dcmitype/Text, 2, http://example.org/resource/subject01"
    })
    void testRootsThatHaveThisType(String inputFileName, String expectedTypeIRI, int expectedNumberOfRoots, String expectedFirstRootIRI) throws Exception {
        String fullInputFileName = "src/test/resources/support/" + inputFileName;
        rc = ms.processInput(fullInputFileName, "rdf4j", db);

        // Calling the method under test
        Set<Value> roots = ConverterHelper.rootsThatHaveThisType(rc, SimpleValueFactory.getInstance().createIRI(expectedTypeIRI), true);

        assertEquals(expectedNumberOfRoots, roots.size(), "Expected number of compliant roots: " + expectedNumberOfRoots);
        //assertEquals(expectedFirstRootIRI, roots.get(0).stringValue(), "The root should be 'root'.");
        assertEquals(expectedFirstRootIRI, roots.iterator().next().stringValue(), "The root should be 'root'.");
    }

    @Test
    void testRootsThatHaveThisTypeEmpty() throws Exception {
        String fullInputFileName = "src/test/resources/support/beforeNoBNodeReplacement.ttl";
        rc = ms.processInput(fullInputFileName, "rdf4j", db);

        // Calling the method under test
        Set<Value> roots = ConverterHelper.rootsThatHaveThisType(rc, SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/dcmitype/Picture"), true);

        assertTrue(roots.isEmpty(), "Expected no compliant roots.");
    }

    @Test
    void testRootHasThisTypeTrue() {
        Value value = SimpleValueFactory.getInstance().createLiteral("example");
        ConverterHelper helper = new ConverterHelper();
        Set<Value> h = new HashSet<>(Collections.singletonList(value));
        // List containing the value
        boolean result = ConverterHelper.rootHasThisType(h, value);

        assertTrue(result, "The value should be found in the list.");
    }

    @Test
    void testRootHasThisTypeFalse() {
        Value value = SimpleValueFactory.getInstance().createLiteral("example");
        Value anotherValue = SimpleValueFactory.getInstance().createLiteral("different");
        ConverterHelper helper = new ConverterHelper();
        Set<Value> h = new HashSet<>(Collections.singletonList(anotherValue));
        // List not containing the value
        boolean result = ConverterHelper.rootHasThisType(h, value);

        assertFalse(result, "The value should not be found in the list.");
    }

}
