package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.BaseTest;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.TestSupport;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
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
    private AppConfig config;
    private Class<? extends Throwable> thrownException;
    private MethodService methodService;
    private RepositoryConnection rc;
    private String testName;

    private String parsingMethod;

    private String expectedMessage;
    Class<? extends Throwable> exceptionForProcessMethod;

    Class<? extends Throwable> exceptionDuringMethodServiceRun;

    public MethodServiceTest(String testName, String processMethod, String filePath, Class<? extends Throwable> exception,
                             Class<? extends Throwable> exceptionForProcessMethod, Class<? extends Throwable> exceptionDuringMethodServiceRun, String parsingMethod, String expectedMessage) {
        this.testName = testName;
        this.processMethod = processMethod;
        this.filePath = filePath;
        this.thrownException = exception;
        this.parsingMethod = parsingMethod;
        this.exceptionForProcessMethod = exceptionForProcessMethod;
        this.expectedMessage = expectedMessage;
        this.exceptionDuringMethodServiceRun = exceptionDuringMethodServiceRun;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][]{
                {"rdf4j - OK", "rdf4j", "../RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt", null, null, null, "com.miklosova.rdftocsvw.input_processor.parsing_methods.RDF4JMethod", ""},
                {"RuntimeException - File does not exist", "rdf4j", "./src/test/resources/nonexistingFile.ttl", RuntimeException.class, RuntimeException.class, RuntimeException.class, "com.miklosova.rdftocsvw.input_processor.parsing_methods.RDF4JMethod", ""},
                {"IllegalArgumentException - unknown readMethod", "unknownMethod", "../RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt", IllegalArgumentException.class, IllegalArgumentException.class, NullPointerException.class, null, "Invalid parsing method: unknownMethod. Valid values are: rdf4j, streaming, bigfilestreaming" },
                {"IOException from read URL or file - mocked", "rdf4j", "../RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt", IOException.class, null, null,"com.miklosova.rdftocsvw.input_processor.parsing_methods.RDF4JMethod", ""},
                {"streaming - OK", "streaming", "../RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt", null, null, null,"com.miklosova.rdftocsvw.input_processor.streaming_methods.StreamingMethod", ""},
                {"bigFileStreaming - OK", "bigFileStreaming", "../RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt", null, null, null,"com.miklosova.rdftocsvw.input_processor.streaming_methods.StreamingMethod", ""},
        });
    }

    @Before
    public void createDbAndMethodService() {
        try {
            config = new AppConfig.Builder(filePath)
                    .parsing(processMethod)
                    .output(filePath)
                    .outputMetadata(filePath + ".csv-metadata.json")
                    .build();
            db = new SailRepository(new MemoryStore());
            methodService = new MethodService(config);
            System.out.println("Before test done ");
        } catch (IllegalArgumentException e) {
            // If an IllegalArgumentException is expected, check its message
            if (thrownException == IllegalArgumentException.class) {
                assertEquals(thrownException.getSimpleName(), e.getClass().getSimpleName());
                // Optionally check the message if you have a specific expected message
                assertEquals(expectedMessage, e.getMessage());
            } else {
                throw e;
            }
        }
    }

    @Test
    public void isGivenDatatype() {
        // createDbAndMethodService() is already called by @Before, don't call it again
        
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

            Assert.assertThrows(exceptionDuringMethodServiceRun, () -> {
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
        String methodChoice = "default";
        MethodService methodService = new MethodService(config);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            methodService.processInput("./RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt", "default", db);
        });

        // Verify exception message
        assertEquals("Invalid reading method: " + methodChoice, exception.getMessage());
    }
    @Test
    public void processMethodChoiceTest(){
        String methodChoice = "default";
        MethodService methodService = new MethodService(config);
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
                assertEquals("Invalid reading method: " + methodChoice, exception.getMessage());
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
