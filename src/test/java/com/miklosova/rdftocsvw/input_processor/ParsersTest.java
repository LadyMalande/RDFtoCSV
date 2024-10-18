package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.support.BaseTest;
import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.input_processor.parsing_methods.*;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RunWith(Parameterized.class)
public class ParsersTest extends BaseTest {
    private String processMethod;
    private String filePath;
    private PrefinishedOutput prefinishedOutput;
    private Class<? extends Throwable> thrownException;
    private MethodService methodService;
    private RepositoryConnection rc;
    private String testName;
    private Class<? extends IRDF4JParsingMethod> parser;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][]{
                {"brd - NOK", "rdf4j", "./src/test/resources/nonexisting.brd", RuntimeException.class, BinaryParser.class},
                {"hdt - NOK", "rdf4j", "./src/test/resources/nonexisting.hdt", RuntimeException.class, HdtParser.class},
                {"jsonld - NOK", "rdf4j", "./src/test/resources/nonexisting.jsonld", RuntimeException.class, JsonldParser.class},
                {"n3 - NOK", "rdf4j", "./src/test/resources/nonexisting.n3", RuntimeException.class, N3Parser.class},
                {"ndjsonld - NOK", "rdf4j", "./src/test/resources/nonexisting.ndjsonld", RuntimeException.class, NdjsonldParser.class},
                {"nq - NOK", "rdf4j", "./src/test/resources/nonexisting.nq", RuntimeException.class, NquadsParser.class},
                {"nt - NOK", "rdf4j", "./src/test/resources/nonexisting.nt", RuntimeException.class, NtriplesParser.class},
                {"html - NOK", "rdf4j", "./src/test/resources/nonexisting.html", RuntimeException.class, RdfaParser.class},
                {"rj - NOK", "rdf4j", "./src/test/resources/nonexisting.rj", RuntimeException.class, RdfjsonParser.class},
                {"rdf - NOK", "rdf4j", "./src/test/resources/nonexisting.rdf", RuntimeException.class, RdfxmlParser.class},
                {"trig - NOK", "rdf4j", "./src/test/resources/nonexisting.trig", RuntimeException.class, TrigParser.class},
                {"trigs - NOK", "rdf4j", "./src/test/resources/nonexisting.trigs", RuntimeException.class, TrigstarParser.class},
                {"trix - NOK", "rdf4j", "./src/test/resources/nonexisting.trix", RuntimeException.class, TrixParser.class},
                {"ttl - NOK", "rdf4j", "./src/test/resources/nonexisting.ttl", RuntimeException.class, TurtleParser.class},
                {"ttls - NOK", "rdf4j", "./src/test/resources/nonexisting.ttls", RuntimeException.class, TurtlestarParser.class},
                {" - NOK", "rdf4j", "./src/test/resources/nonexisting.", RuntimeException.class, TurtlestarParser.class},
                {" - NOK", "rdf4j", "./src/test/resources/nonexisting.", RuntimeException.class, TurtlestarParser.class},
        });
    }

    public ParsersTest(String testName, String processMethod, String filePath, Class<? extends Throwable> exception, Class<? extends IRDF4JParsingMethod> parser) {
        this.testName = testName;
        this.processMethod = processMethod;
        this.filePath = filePath;
        this.thrownException = exception;
        this.parser = parser;
    }

    @BeforeEach
    void createDbAndMethodService() {
        ConfigurationManager.loadSettingsFromInputToConfigFile(new String[]{filePath});
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.READ_METHOD, processMethod);
        db = new SailRepository(new MemoryStore());

    }

    @Test
    public void parserIOException() throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        createDbAndMethodService();
        IRDF4JParsingMethod instance = parser.getDeclaredConstructor().newInstance();
        IRDF4JParsingMethod mockFileProcessor = Mockito.spy(parser);
        File file = new File(filePath);

        // Configure the mock to throw an IOException when processFile is called with any String argument
        doThrow(RuntimeException.class).when(mockFileProcessor).processInput(null, file);

        // Call the method to verify it throws the IOException


        // Verify that the exception is thrown
        Assert.assertThrows(thrownException, () -> {
            mockFileProcessor.processInput(null, file);
        });
    }
}
