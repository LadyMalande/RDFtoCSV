package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.BaseTest;
import com.miklosova.rdftocsvw.convertor.ConversionService;
import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.convertor.RowsAndKeys;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.MetadataService;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.FileWrite;
import com.miklosova.rdftocsvw.support.TestSupport;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs(){
        return Arrays.asList(new Object[][]{
                { "rdf4j - OK", "rdf4j", "./src/test/resources/testingInput.ttl", null},
                { "RuntimeException - File does not exist","rdf4j", "./src/test/resources/nonexistingFile.ttl", RuntimeException.class},
                { "IllegalArgumentException - unknown readMethod","unknownMethod", "./src/test/resources/testingInputForTwoEntities.ttl", IllegalArgumentException.class},
                { "IOException from read URL or file - mocked","rdf4j", "jdbc:oracle:thin:@localhost:1521:orcll", IOException.class}
        });
    }

    public MethodServiceTest(String testName, String processMethod, String filePath, Class<? extends Throwable> exception) {
        this.testName = testName;
        this.processMethod = processMethod;
        this.filePath = filePath;
        this.thrownException = exception;
    }

    @BeforeEach
    void createDbAndMethodService(){
        ConfigurationManager.loadSettingsFromInputToConfigFile(new String[]{filePath});
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.READ_METHOD, processMethod);
        db = new SailRepository(new MemoryStore());
        methodService = new MethodService();



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
        if(thrownException == null){
            try {
                rc = methodService.processInput(filePath, processMethod, db);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Assert.assertNotNull(rc);
        } else if(thrownException == IllegalArgumentException.class || thrownException == RuntimeException.class){

                Assert.assertThrows(thrownException, () -> {
                    rc = methodService.processInput(filePath, processMethod, db);
                });

        } else{
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
}