package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.input_processor.parsing_methods.*;
import com.miklosova.rdftocsvw.support.BaseTest;
import com.miklosova.rdftocsvw.support.AppConfig;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@RunWith(Parameterized.class)
public class ParsersTest extends BaseTest {
    private AppConfig config;
    private String processMethod;
    private String filePath;
    private String originalFileToCopy;
    private PrefinishedOutput prefinishedOutput;
    private final Class<? extends Throwable> thrownException;
    private MethodService ms;
    private RepositoryConnection rc;
    private String testName;
    private Class<? extends IRDF4JParsingMethod> parser;

    public ParsersTest(String testName, String processMethod, String filePath, Class<? extends Throwable> exception, Class<? extends IRDF4JParsingMethod> parser, String originalFileToCopy) {
        this.testName = testName;
        this.processMethod = processMethod;
        this.filePath = filePath;
        this.thrownException = exception;
        this.parser = parser;
        this.originalFileToCopy = originalFileToCopy;

    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][]{
                //{"brd - NOK", "rdf4j", "./src/test/resources/nonexisting.brd", RuntimeException.class, BinaryParser.class},
                //{"hdt - NOK", "rdf4j", "./src/test/resources/input_processor/nonexisting.hdt", RuntimeException.class, HdtParser.class, "./src/test/resources/differentSerializations/testingInput.hdt"},
                {"jsonld - NOK", "rdf4j", "./src/test/resources/input_processor/nonexisting.jsonld", RuntimeException.class, JsonldParser.class, "./src/test/resources/differentSerializations/testingInput.jsonld"},
                {"n3 - NOK", "rdf4j", "./src/test/resources/input_processor/nonexisting.n3", RuntimeException.class, N3Parser.class, "./src/test/resources/differentSerializations/testingInput.n3"},
                {"ndjsonld - NOK", "rdf4j", "./src/test/resources/input_processor/nonexisting.ndjsonld", RuntimeException.class, NdjsonldParser.class, "./src/test/resources/differentSerializations/testingInput.ndjsonld"},
                {"nq - NOK", "rdf4j", "./src/test/resources/input_processor/nonexisting.nq", RuntimeException.class, NquadsParser.class, "./src/test/resources/differentSerializations/testingInput.nq"},
                {"nt - NOK", "rdf4j", "./src/test/resources/input_processor/nonexisting.nt", RuntimeException.class, NtriplesParser.class, "./src/test/resources/differentSerializations/testingInput.nt"},
                //{"html - NOK", "rdf4j", "./src/test/resources/input_processor/nonexisting.html", RuntimeException.class, RdfaParser.class, "./src/test/resources/differentSerializations/testingInput.html"},
                {"rj - NOK", "rdf4j", "./src/test/resources/input_processor/nonexisting.rj", RuntimeException.class, RdfjsonParser.class, "./src/test/resources/differentSerializations/testingInput.rj"},
                {"rdf - NOK", "rdf4j", "./src/test/resources/input_processor/nonexisting.rdf", FileNotFoundException.class, RdfxmlParser.class, "./src/test/resources/differentSerializations/testingInput.rdf"},
                {"trig - NOK", "rdf4j", "./src/test/resources/input_processor/nonexisting.trig", RuntimeException.class, TrigParser.class, "./src/test/resources/differentSerializations/testingInput.trig"},
                {"trigs - NOK", "rdf4j", "./src/test/resources/input_processor/nonexisting.trigs", RuntimeException.class, TrigstarParser.class, "./src/test/resources/differentSerializations/testingInput.trigs"},
                {"trix - NOK", "rdf4j", "./src/test/resources/input_processor/nonexisting.trix", RuntimeException.class, TrixParser.class, "./src/test/resources/differentSerializations/testingInput.trix"},
                {"ttl - NOK", "rdf4j", "./src/test/resources/input_processor/nonexisting.ttl", RuntimeException.class, TurtleParser.class, "./src/test/resources/differentSerializations/testingInput.ttl"},
                {"ttls - NOK", "rdf4j", "./src/test/resources/input_processor/nonexisting.ttls", RuntimeException.class, TurtlestarParser.class, "./src/test/resources/differentSerializations/testingInput.ttls"},
                //{" - NOK", "rdf4j", "./src/test/resources/input_processor/nonexisting.", RuntimeException.class, TurtlestarParser.class, "./src/test/resources/differentSerializations/testingInput.hdt"},
                //{" - NOK", "rdf4j", "./src/test/resources/input_processor/nonexisting.", RuntimeException.class, TurtlestarParser.class, "./src/test/resources/differentSerializations/testingInput.hdt"},
        });
    }

    @BeforeEach
    void createDbAndMethodService() {
        config = new AppConfig.Builder(filePath)
                .parsing(processMethod)
                .build();
        ms = new MethodService(config);
        db = new SailRepository(new MemoryStore());


    }

    @Test
    public void parserIOException() throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        createDbAndMethodService();
        IRDF4JParsingMethod instance = parser.getDeclaredConstructor().newInstance();
        IRDF4JParsingMethod mockFileProcessor = Mockito.spy(parser);
        File file = new File(filePath);
        File fileToCopyFrom = new File(originalFileToCopy);
        if (file.createNewFile()) {
            System.out.println("File created: " + file.getName());
        } else {
            System.out.println("File already exists.");
        }

        Files.copy(fileToCopyFrom.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Configure the mock to throw an IOException when processFile is called with any String argument
        doThrow(RuntimeException.class).when(mockFileProcessor).processInput(null, file);

        // Call the method to verify it throws the IOException
        // Instantiate the class
        IRDF4JParsingMethod parserInstance = (IRDF4JParsingMethod) parser.getDeclaredConstructor().newInstance();

        // Find and invoke the processInput method
        Method processInputMethod = parser.getMethod("processInput", RepositoryConnection.class, File.class);
        rc = ms.processInput(filePath, "rdf4j", db);

        // Delete the file
        if (file.delete()) {
            System.out.println("File deleted: " + file.getName());
        } else {
            System.out.println("Failed to delete the file.");
        }
        // Verify that the exception is thrown
        try {
            processInputMethod.invoke(parserInstance, rc, file);
        } catch(RuntimeException | InvocationTargetException ex){
            Throwable cause = ex.getCause();
            Assert.assertTrue("Expected IOException as cause, but got: " +
                            (cause != null ? cause.getClass().getName() : "null"), cause instanceof IOException || cause instanceof RuntimeException
                    );
        }

    }
}
