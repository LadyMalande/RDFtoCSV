package com.miklosova.rdftocsvw.metadata_creator;

import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.miklosova.rdftocsvw.converter.RDFtoCSV;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.models.DereferencerTestParameters;
import com.miklosova.rdftocsvw.models.FilesParameters;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.BaseTest;
import com.poiji.bind.Poiji;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.jena.shared.JenaException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class DereferencerGetRdfTest extends BaseTest {
    private static final String PROCESS_METHOD = "rdf4j";
    private static String filePath = "./RDFtoCSV/src/test/resources/DereferenceTests/dereferenceTestInput.ttl";
    private static String filePathForMetadata = "./src/test/resources/DereferenceTests/dereferenceTestInput.csv-metadata.json";
    private static Metadata createdMetadata;
    private static Repository db;
    private String nameForTest;
    private String expectedTitle;
    private String expectedName;
    private String url;

    private AppConfig config;


    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private CloseableHttpResponse httpResponse;

    @Mock
    private StatusLine statusLine;

    @Mock
    private HttpEntity entity;

/*    public DereferencerGetRdfTest(String nameForTest, String url, String expectedName, String expectedTitle) {
        this.nameForTest = nameForTest;
        this.url = url;
        this.expectedName = expectedName;
        this.expectedTitle = expectedTitle;
    }*/

    public static Stream<DereferencerTestParameters> dataSource(){

        File file = new File("src/test/resources/poiji_datasets/test_dataset.xlsx");
        // Read the Excel file into a list of Person objects
        return Poiji.fromExcel(file, DereferencerTestParameters.class).stream();

    }

    public static Stream<FilesParameters> filesSource(){

        File file = new File("src/test/resources/poiji_datasets/test_dataset.xlsx");
        // Read the Excel file into a list of Person objects
        return Poiji.fromExcel(file, FilesParameters.class).stream();

    }

    @ParameterizedTest
    @MethodSource("dataSource")
    void testFetchLabelFromTurtle(DereferencerTestParameters line) throws IOException {
        Dereferencer dereferencer = new Dereferencer(line.getIri(), new AppConfig.Builder("test.ttl").build());

        try {
            String label = dereferencer.fetchLabel(line.getIri());
            logger.info("line.getUrl(): " + line.getIri());
            logger.info("label: " + label);
            if (line.getPredicateName().equalsIgnoreCase("null")) {
                Assertions.assertNull(label);
            } else {
                assertEquals(line.getPredicateName(), label);
            }
        } catch(ExecutionException ex){
            logger.log(Level.SEVERE, ex.getMessage());
        } catch(CacheLoader.InvalidCacheLoadException exNull){
            assertTrue(line.getPredicateName().equals("null"));
        }
    }

    @Test
    void testFetchLabelFromRDFXML() throws IOException, ExecutionException {
        Dereferencer dereferencer = new Dereferencer("http://purl.org/dc/terms/creator", new AppConfig.Builder("test.ttl").build());

        //String label1 = Dereferencer.fetchLabel("http://purl.org/dc/elements/1.1/publisher");
        String label2 = dereferencer.fetchLabel("http://purl.org/dc/terms/creator");
        System.out.println(label2);
        //logger.log(Level.INFO, label1);
        logger.log(Level.INFO, label2);

        assertEquals("Creator", label2);

    }

    @Test
    void testCacheIsFaster() throws IOException, ExecutionException {
        Dereferencer dereferencer = new Dereferencer("http://example.org/NotFound#unavailable", new AppConfig.Builder("test.ttl").build());

        long startTime1 = System.nanoTime();
        String label1 = dereferencer.fetchLabel("http://purl.org/dc/terms/creator");
        long endTime1 = System.nanoTime();


        long startTime2 = System.nanoTime();
        String label2 = dereferencer.fetchLabel("http://purl.org/dc/terms/creator");
        long endTime2 = System.nanoTime();

        long startTime3 = System.nanoTime();
        String label3 = dereferencer.fetchLabel("http://purl.org/dc/terms/creator");
        long endTime3 = System.nanoTime();

        long firstDereferencerCallInMilis = endTime1 - startTime1;
        long secondDereferencerCallInMilis =  endTime2 - startTime2;
        long thirdDereferencerCallInMilis =  endTime3 - startTime3;

        logger.log(Level.INFO, label1 +": " + firstDereferencerCallInMilis + " ns");
        logger.log(Level.INFO, label2 +": " + secondDereferencerCallInMilis + " ns");
        logger.log(Level.INFO, label3 +": " + thirdDereferencerCallInMilis + " ns");

        assertTrue(firstDereferencerCallInMilis > secondDereferencerCallInMilis);
    }

    @Test
    void testSubsequentHttpClientIsFaster() throws IOException, ExecutionException {
        Dereferencer dereferencer = new Dereferencer("http://example.org/NotFound#unavailable", new AppConfig.Builder("test.ttl").build());
        long startTime1 = System.nanoTime();
        String label1 = dereferencer.fetchLabel("http://purl.org/dc/terms/creator");
        long endTime1 = System.nanoTime();


        long startTime2 = System.nanoTime();
        String label2 = dereferencer.fetchLabel("http://purl.org/vocab/vann/preferredNamespaceUri");
        long endTime2 = System.nanoTime();

        long startTime3 = System.nanoTime();
        String label3 = dereferencer.fetchLabel("http://xmlns.com/wot/0.1/pubkeyAddress");
        long endTime3 = System.nanoTime();

        long firstDereferencerCallInMilis = endTime1 - startTime1;
        long secondDereferencerCallInMilis =  endTime2 - startTime2;
        long thirdDereferencerCallInMilis =  endTime3 - startTime3;

        logger.log(Level.INFO, label1 +": " + firstDereferencerCallInMilis + " ns" + " = " + (firstDereferencerCallInMilis/1000000) + " ms");
        logger.log(Level.INFO, label2 +": " + secondDereferencerCallInMilis + " ns" + " = " + (secondDereferencerCallInMilis/1000000) + " ms");
        logger.log(Level.INFO, label3 +": " + thirdDereferencerCallInMilis + " ns" + " = " + (thirdDereferencerCallInMilis/1000000) + " ms");

        assertTrue(firstDereferencerCallInMilis > secondDereferencerCallInMilis);
    }

    @Test
    void testFetchLabelWhenUnknownURIWithoutFragment() throws JenaException, IOException, ExecutionException {
        Dereferencer dereferencer = new Dereferencer("http://example.org/NotFound", new AppConfig.Builder("test.ttl").build());
        String label = dereferencer.fetchLabel("http://example.org/NotFound");
        assertEquals("NotFound", label);
       /* assertThrows(IOException.class, () -> {String label = Dereferencer.fetchLabel("http://example.org/NotFound");
            logger.info("label fetched = " + label);
        });*/
    }

    @Test
    void testFetchLabelWhenUnknownURIWithFragment() throws IOException, ExecutionException {
        Dereferencer dereferencer = new Dereferencer("http://example.org/NotFound#unavailable", new AppConfig.Builder("test.ttl").build());
        String label = dereferencer.fetchLabel("http://example.org/NotFound#unavailable");
        assertEquals("unavailable", label);
        /*assertThrows(UncheckedExecutionException.class, () -> { String label = Dereferencer.fetchLabel("http://example.org/NotFound#unavailable");
            logger.info("label fetched = " + label);} );*/
    }

    @ParameterizedTest
    @MethodSource("filesSource")
    void createPrefinishedOutputAndMetadata(FilesParameters line) throws IOException {
        rdfToCSV = new RDFtoCSV( new AppConfig.Builder(line.getFilePath()).parsing("rdf4j").output(line.getOutputPath()).build());
        db = new SailRepository(new MemoryStore());
        try {
            rdfToCSV.convertToZip();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

