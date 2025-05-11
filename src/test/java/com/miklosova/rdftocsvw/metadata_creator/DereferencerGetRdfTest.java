package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.ConversionService;
import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.support.BaseTest;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import org.apache.http.message.BasicHeader;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DereferencerGetRdfTest extends BaseTest {
    private static final String PROCESS_METHOD = "rdf4j";
    private static String filePath = "./src/test/resources/DereferenceTests/dereferenceTestInput.ttl";
    private static String filePathForMetadata = "./src/test/resources/DereferenceTests/dereferenceTestInput.csv-metadata.json";
    private static Metadata createdMetadata;
    private static Repository db;
    private String nameForTest;
    private String expectedTitle;
    private String expectedName;


    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private CloseableHttpResponse httpResponse;

    @Mock
    private StatusLine statusLine;

    @Test
    void testFetchLabelFromTurtle() throws IOException {
        // Mock HTTP response with Turtle content
        String turtleContent = "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
                "@prefix ex: <http://example.org/> .\n" +
                "\n" +
                "ex:Resource rdfs:label \"Test Resource\"@en .";

        when(httpClient.execute(any())).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);

        HttpEntity entity = new StringEntity(turtleContent);
        when(httpResponse.getEntity()).thenReturn(entity);
        when(httpResponse.getEntity().getContentType()).thenReturn(
                new BasicHeader("Content-Type", "text/turtle")
        );

        String label = Dereferencer.fetchLabel("http://example.org/Resource");
        assertEquals("Test Resource", label);
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
    void testFetchLabelWhenHttpError() throws IOException {
/*
        when(httpClient.execute(any())).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(404);
*/

        assertThrows(IOException.class, () -> {
            Dereferencer.fetchLabel("http://example.org/NotFound");
        });
    }
}

