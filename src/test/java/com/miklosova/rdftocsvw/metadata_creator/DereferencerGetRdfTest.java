package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.RDFtoCSV;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.models.DereferencerTestParameters;
import com.miklosova.rdftocsvw.support.BaseTest;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.poiji.bind.Poiji;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.jena.shared.JenaException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

    @ParameterizedTest
    @MethodSource("dataSource")
    void testFetchLabelFromTurtle(DereferencerTestParameters line) throws IOException {
        // Mock HTTP response with Turtle content
        String turtleContent = """
                @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
                @prefix ex: <http://example.org/> .

                ex:Resource rdfs:label "Test Resource"@en .""";

        String label = Dereferencer.fetchLabel(line.getUrl());
        logger.info("line.getUrl(): " + line.getUrl());
        logger.info("label: " +  label);
        if(line.getPredicateName().equalsIgnoreCase("null")){
            Assertions.assertNull(label);
        } else {
            assertEquals(line.getPredicateName(), label);
        }
        //   add to dataset?              DBpedia 	http://dbpedia.org/resource/Paris	 moreinfo	 moreinfo
    }

    @Test
    void testFetchLabelFromRDFXML() throws IOException {

        //String label1 = Dereferencer.fetchLabel("http://purl.org/dc/elements/1.1/publisher");
        String label2 = Dereferencer.fetchLabel("http://purl.org/dc/terms/creator");
        System.out.println(label2);
        //logger.log(Level.INFO, label1);
        logger.log(Level.INFO, label2);

        assertEquals("Creator", label2);

    }

    @Test
    void testFetchLabelWhenUnknownURIWithoutFragment() throws JenaException {

        assertThrows(IOException.class, () -> Dereferencer.fetchLabel("http://example.org/NotFound"));
    }

    @Test
    void testFetchLabelWhenUnknownURIWithFragment() {

        assertThrows(IOException.class, () -> Dereferencer.fetchLabel("http://example.org/NotFound#unavailable"));
    }

    @Test
    void createPrefinishedOutputAndMetadata() {
        rdfToCSV = new RDFtoCSV(filePath);
        db = new SailRepository(new MemoryStore());
        args = new String[]{"-f", filePath, "-p", "rdf4j"};
        ConfigurationManager.loadSettingsFromInputToConfigFile(args);
        try {
            rdfToCSV.convertToZip();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

