package com.miklosova.rdftocsvw.converter;

 import static org.eclipse.rdf4j.model.util.Values.iri;

 import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
 import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
 import com.miklosova.rdftocsvw.input_processor.MethodService;
 import org.eclipse.rdf4j.repository.RepositoryConnection;
 import static org.mockito.ArgumentMatchers.any;

 import org.junit.jupiter.api.Disabled;
 import org.junit.jupiter.api.Test;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.eclipse.rdf4j.repository.sail.SailRepository;
 import static org.mockito.ArgumentMatchers.anyString;
 import com.miklosova.rdftocsvw.metadata_creator.MetadataService;
 import org.junit.jupiter.params.provider.CsvSource;
 import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
 import static org.mockito.Mockito.*;
 import org.eclipse.rdf4j.sail.memory.MemoryStore;
 import java.io.IOException;
 import java.util.HashMap;

 import org.junit.jupiter.api.BeforeEach;
 import java.util.Map;
 import org.eclipse.rdf4j.repository.Repository;
 import org.junit.jupiter.params.ParameterizedTest;
import com.miklosova.rdftocsvw.support.AppConfig;

 import static org.junit.jupiter.api.Assertions.*;
 import com.miklosova.rdftocsvw.output_processor.ZipOutputProcessor;

 import static org.mockito.ArgumentMatchers.any;
 import static org.mockito.ArgumentMatchers.eq;

class RDFtoCSVTest {

     @Mock
     private MethodService methodService;

     @Mock
     private ConversionService conversionService;

     @Mock
     private MetadataService metadataService;

     @Mock
     private ZipOutputProcessor zipOutputProcessor;

     @Mock
     private Repository repository;

    @Mock
    private RepositoryConnection repositoryConnection;

    private Repository db;
    private String fileName = "/RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt";
    private AppConfig config;
    private RDFtoCSV rdfToCSV;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        config = new AppConfig.Builder(fileName)
                .parsing("rdf4j")
                .build();
        rdfToCSV = new RDFtoCSV(config);
        db = new SailRepository(new MemoryStore());
    }

//     //BaseRock generated method id: ${testConstructorWithFileName}, hash: B7210CFF75EC7ED5BF54503E1FCF8DA4
     @ParameterizedTest
     @CsvSource({ "test.rdf,test,test.csv-metadata.json", "http://example.com/test.rdf,test.rdf,test.rdf.csv-metadata.json" })
     void testConstructorWithFileName(String input, String expectedFilePath, String expectedMetadataFilename) {

        AppConfig testConfig = new AppConfig.Builder(input)
            .parsing("rdf4j")
            .build();
        RDFtoCSV instance = new RDFtoCSV(testConfig);
        assertEquals(expectedFilePath, instance.getFilePathForOutput());
        assertEquals(expectedMetadataFilename, instance.getMetadataFilename());
     }

     //BaseRock generated method id: ${testConstructorWithFileNameAndConfigMap}, hash: A477329819075261588DD3B31D3049F8
     @Test
     void testConstructorWithFileNameAndConfigMap() {
        AppConfig testConfig = new AppConfig.Builder("test.rdf")
            .parsing("rdf4j")
            .build();
        RDFtoCSV instance = new RDFtoCSV(testConfig);
        // Output files are created in current directory (no "../" prefix)
        assertEquals("test", instance.getFilePathForOutput());
        assertEquals("test.csv-metadata.json", instance.getMetadataFilename());
     }

     @Test
     @Disabled
     void testGetCSVTableAsString() throws IOException {
        PrefinishedOutput<RowsAndKeys> mockPrefinishedOutput = mock(PrefinishedOutput.class);
        Metadata mockMetadata = mock(Metadata.class);
        when(methodService.processInput(anyString(), anyString(), any(Repository.class))).thenReturn(repositoryConnection);
        when(conversionService.convertByQuery(any(RepositoryConnection.class), any(Repository.class))).thenReturn(mockPrefinishedOutput);
        when(metadataService.createMetadata(any(PrefinishedOutput.class))).thenReturn(mockMetadata);
        String result = rdfToCSV.getCSVTableAsString();
        //System.out.println("rdfToCSV.getCSVTableAsString() \n" + result);
        assertNotNull(result);
     }

}