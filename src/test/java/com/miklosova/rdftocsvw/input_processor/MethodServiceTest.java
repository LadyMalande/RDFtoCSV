package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.support.BaseTest;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.TestSupport;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RunWith(Parameterized.class)
public class MethodServiceTest extends BaseTest {
    private String processMethod;
    private String filePath;
    private PrefinishedOutput prefinishedOutput;
    private Class<? extends Throwable> thrownException;
    private MethodService methodService;
    private RepositoryConnection rc;
    private String testName;

    private String parsingMethod;
    Class<? extends Throwable> exceptionForProcessMethod;

    public MethodServiceTest(String testName, String processMethod, String filePath, Class<? extends Throwable> exception,
                             Class<? extends Throwable> exceptionForProcessMethod, String parsingMethod) {
        this.testName = testName;
        this.processMethod = processMethod;
        this.filePath = filePath;
        this.thrownException = exception;
        this.parsingMethod = parsingMethod;
        this.exceptionForProcessMethod = exceptionForProcessMethod;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][]{
                {"rdf4j - OK", "rdf4j", "../RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt", null, null, "com.miklosova.rdftocsvw.input_processor.parsing_methods.RDF4JMethod"},
                {"RuntimeException - File does not exist", "rdf4j", "./src/test/resources/nonexistingFile.ttl", RuntimeException.class, RuntimeException.class, "com.miklosova.rdftocsvw.input_processor.parsing_methods.RDF4JMethod"},
                {"IllegalArgumentException - unknown readMethod", "unknownMethod", "../RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt", IllegalArgumentException.class, IllegalArgumentException.class, null },
                {"IOException from read URL or file - mocked", "rdf4j", "../RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt", IOException.class, null, "com.miklosova.rdftocsvw.input_processor.parsing_methods.RDF4JMethod"},
                {"streaming - OK", "streaming", "../RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt", null, null, "com.miklosova.rdftocsvw.input_processor.streaming_methods.StreamingMethod"},
                {"bigFileStreaming - OK", "bigFileStreaming", "../RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt", null, null, "com.miklosova.rdftocsvw.input_processor.streaming_methods.StreamingMethod"},
        });
    }

    @Before
    public void createDbAndMethodService() {
        ConfigurationManager.loadSettingsFromInputToConfigFile(new String[]{"-f",filePath, "-p",processMethod});
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.READ_METHOD, processMethod);
        db = new SailRepository(new MemoryStore());
        methodService = new MethodService();
        System.out.println("Before test done ");


    }

    @Test
    public void isGivenDatatype() {
        createDbAndMethodService();

        try {
            Model m = TestSupport.parseInputByRio(filePath);
            TestSupport.createSerialization(null, null, m);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (thrownException == null) {
            try {
                rc = methodService.processInput(filePath, processMethod, db);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if(processMethod.equalsIgnoreCase("streaming") || processMethod.equalsIgnoreCase("bigFileStreaming")){
                Assert.assertNull(rc);
            } else {
                Assert.assertNotNull(rc);
            }

        } else if (thrownException == IllegalArgumentException.class || thrownException == RuntimeException.class) {

            Assert.assertThrows(thrownException, () -> {
                rc = methodService.processInput(filePath, processMethod, db);
            });

        } else {
            // Mock URL and HttpURLConnection
            URL url = mock(URL.class);
            HttpURLConnection connection = mock(HttpURLConnection.class);

            // When openConnection is called on the URL mock, return the HttpURLConnection mock
            try {
                when(url.openConnection()).thenReturn(connection);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Simulate an UnknownHostException when connect is called on the HttpURLConnection mock
            try {
                doThrow(new UnknownHostException("Host not found")).when(connection).connect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Verify that the exception is thrown
            Assert.assertThrows(thrownException, () -> {
                // Code that opens the connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect(); // This should throw the UnknownHostException
            });
        }

    }
    @Test
    public void processMethodChoiceTestThrowsException(){
        MethodService methodService = new MethodService();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            methodService.processInput("./RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt", "default", db);
        });

        // Verify exception message
        assertEquals("Invalid reading method", exception.getMessage());
    }
    @Test
    public void processMethodChoiceTest(){
        MethodService methodService = new MethodService();
        try {
            if(exceptionForProcessMethod == null) {
                methodService.processInput(filePath, processMethod, db);
                // Access the private field
                Field field = MethodService.class.getDeclaredField("methodGateway");
                field.setAccessible(true);

                // Get the value of the private field
                MethodGateway fieldValue = (MethodGateway) field.get(methodService);
                Class<?> parshinmethodClass = Class.forName(parsingMethod);
                // Assert the value
                assertEquals(parshinmethodClass, fieldValue.getParsingMethod().getClass(), "The parsing method is not an instance of the expected class.");
            } else {
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                    methodService.processInput("./RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt", "default", db);
                });

                // Verify exception message
                assertEquals("Invalid reading method", exception.getMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
