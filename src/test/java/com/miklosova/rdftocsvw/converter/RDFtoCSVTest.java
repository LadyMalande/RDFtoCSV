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
 import com.miklosova.rdftocsvw.support.ConfigurationManager;

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

     private RDFtoCSV rdfToCSV;

     private Repository db;

     private String fileName = "/RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt";

     private String[] args;

     @BeforeEach
     void setUp() {
         MockitoAnnotations.openMocks(this);
         rdfToCSV = new RDFtoCSV(fileName);
         db = new SailRepository(new MemoryStore());

         args = new String[]{"-f", "test.rdf", "-p", "rdf4j"};
         ConfigurationManager.loadSettingsFromInputToConfigFile(args);
     }

//     //BaseRock generated method id: ${testConstructorWithFileName}, hash: B7210CFF75EC7ED5BF54503E1FCF8DA4
     @ParameterizedTest
     @CsvSource({ "test.rdf,../test.rdf,../test.rdf.csv-metadata.json", "http://example.com/test.rdf,test.rdf,http://example.com/test.rdf.csv-metadata.json" })
     void testConstructorWithFileName(String input, String expectedFilePath, String expectedMetadataFilename) {

         RDFtoCSV instance = new RDFtoCSV(input);

         assertEquals(expectedFilePath, instance.getFilePathForOutput());
         assertEquals(expectedMetadataFilename, instance.getMetadataFilename());
     }

     //BaseRock generated method id: ${testConstructorWithFileNameAndConfigMap}, hash: A477329819075261588DD3B31D3049F8
     @Test
     void testConstructorWithFileNameAndConfigMap() {
         Map<String, String> configMap = new HashMap<>();
         configMap.put("key", "value");
         RDFtoCSV instance = new RDFtoCSV("test.rdf", configMap);
         assertEquals("test.rdf", instance.getFilePathForOutput());
         assertEquals("test.rdf.csv-metadata.json", instance.getMetadataFilename());
     }

//     //BaseRock generated method id: ${testConvertToZip}, hash: C0D3BBC0F1BC9F595910824D8F65C432
//     @Test
//     void testConvertToZip() throws IOException {
//         PrefinishedOutput<RowsAndKeys> mockPrefinishedOutput = mock(PrefinishedOutput.class);
//         Metadata mockMetadata = mock(Metadata.class);
//         FinalizedOutput<byte[]> mockFinalizedOutput = mock(FinalizedOutput.class);
//         when(methodService.processInput(anyString(), anyString(), any(Repository.class))).thenReturn(repositoryConnection);
//         when(conversionService.convertByQuery(any(RepositoryConnection.class), any(Repository.class))).thenReturn(mockPrefinishedOutput);
//         when(metadataService.createMetadata(any(PrefinishedOutput.class))).thenReturn(mockMetadata);
//         when(zipOutputProcessor.processCSVToOutput(any(PrefinishedOutput.class))).thenReturn(mockFinalizedOutput);
//         FinalizedOutput<byte[]> result = rdfToCSV.convertToZip();
//         assertNotNull(result);
//         verify(methodService).processInput(anyString(), anyString(), any(Repository.class));
//         verify(conversionService).convertByQuery(any(RepositoryConnection.class), any(Repository.class));
//         verify(metadataService).createMetadata(any(PrefinishedOutput.class));
//         verify(zipOutputProcessor).processCSVToOutput(any(PrefinishedOutput.class));
//     }

//     //BaseRock generated method id: ${testGetTrivialCSVTableAsString}, hash: D2187AAFD85946C3D952808C3F276BBD
//     @Test
//     void testGetTrivialCSVTableAsString() throws IOException {
//         PrefinishedOutput<RowsAndKeys> mockPrefinishedOutput = mock(PrefinishedOutput.class);
//         RowsAndKeys mockRowsAndKeys = mock(RowsAndKeys.class);
//         RowAndKey mockRowAndKey = mock(RowAndKey.class);
//         ArrayList<RowAndKey> rowsAndKeys = new ArrayList<>();
//         rowsAndKeys.add(mockRowAndKey);
//         mockRowsAndKeys.setRowsAndKeys(rowsAndKeys);
//         when(methodService.processInput(anyString(), anyString(), any(Repository.class))).thenReturn(repositoryConnection);
//         when(conversionService.convertByQuery(any(RepositoryConnection.class), any(Repository.class))).thenReturn(mockPrefinishedOutput);
//         when(mockPrefinishedOutput.getPrefinishedOutput()).thenReturn(mockRowsAndKeys);
//         ArrayList<Value> mockKeys = new ArrayList<>();
//         mockKeys.add(iri("https://predicate1.com"));
//         mockKeys.add(iri("https://predicate2.com"));
//         ArrayList<Row> mockRows = new ArrayList<>();
//         Row mockRow = mock(Row.class);
//         when(mockRow.getId()).thenReturn(iri("https://subject.com"));
//         mockRows.add(mockRow);
//         when(mockRowAndKey.getKeys()).thenReturn(mockKeys);
//         when(mockRowAndKey.getRows()).thenReturn(mockRows);
//         ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.TABLES, "more");
//         ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_METHOD, "trivial");
//         String result = rdfToCSV.getTrivialCSVTableAsString();
//         assertNotNull(result);
//         assertTrue(result.contains("https://predicate1.com,https://predicate2.com"));
//         assertTrue(result.contains("https://subject.com"));
//     }

//     //BaseRock generated method id: ${testGetCSVTableAsString}, hash: 380317AA4334EFDB6FAFD2EE1F3D02B1
     @Test
     @Disabled
     void testGetCSVTableAsString() throws IOException {
         PrefinishedOutput<RowsAndKeys> mockPrefinishedOutput = mock(PrefinishedOutput.class);
         Metadata mockMetadata = mock(Metadata.class);
         when(methodService.processInput(anyString(), anyString(), any(Repository.class))).thenReturn(repositoryConnection);
         when(conversionService.convertByQuery(any(RepositoryConnection.class), any(Repository.class))).thenReturn(mockPrefinishedOutput);
         when(metadataService.createMetadata(any(PrefinishedOutput.class))).thenReturn(mockMetadata);
         String result = rdfToCSV.getCSVTableAsString();
         System.out.println("rdfToCSV.getCSVTableAsString() \n" + result);
         assertNotNull(result);
     }

//     //BaseRock generated method id: ${testGetMetadataAsString}, hash: 1A37C87700BB7F8E979BDCBF593D280C
//     @Test
//     void testGetMetadataAsString() throws IOException {
//         Metadata mockMetadata = mock(Metadata.class);
//         when(methodService.processInput(anyString(), anyString(), any(Repository.class))).thenReturn(repositoryConnection);
//         when(conversionService.convertByQuery(any(RepositoryConnection.class), any(Repository.class))).thenReturn(mock(PrefinishedOutput.class));
//         when(metadataService.createMetadata(any(PrefinishedOutput.class))).thenReturn(mockMetadata);
//         String result = rdfToCSV.getMetadataAsString();
//         assertNotNull(result);
//     }

//     //BaseRock generated method id: ${testGetCSVTableAsFile}, hash: C911C145599231FCC40659A1980AECE3
//     @Test
//     void testGetCSVTableAsFile() throws IOException {
//         PrefinishedOutput<RowsAndKeys> mockPrefinishedOutput = mock(PrefinishedOutput.class);
//         Metadata mockMetadata = mock(Metadata.class);
//         when(methodService.processInput(anyString(), anyString(), any(Repository.class))).thenReturn(repositoryConnection);
//         when(conversionService.convertByQuery(any(RepositoryConnection.class), any(Repository.class))).thenReturn(mockPrefinishedOutput);
//         when(metadataService.createMetadata(any(PrefinishedOutput.class))).thenReturn(mockMetadata);
//         FinalizedOutput<byte[]> result = rdfToCSV.getCSVTableAsFile();
//         assertNotNull(result);
//     }

//     //BaseRock generated method id: ${testGetMetadataAsFile}, hash: E508DE6EF6106749B68ACCFC65F29256
//     @Test
//     void testGetMetadataAsFile() throws IOException {
//         Metadata mockMetadata = mock(Metadata.class);
//         when(methodService.processInput(anyString(), anyString(), any(Repository.class))).thenReturn(repositoryConnection);
//         when(conversionService.convertByQuery(any(RepositoryConnection.class), any(Repository.class))).thenReturn(mock(PrefinishedOutput.class));
//         when(metadataService.createMetadata(any(PrefinishedOutput.class))).thenReturn(mockMetadata);
//         FinalizedOutput<byte[]> result = rdfToCSV.getMetadataAsFile();
//         assertNotNull(result);
//     }

//     //BaseRock generated method id: ${testCreateMetadata}, hash: AD7AEA383D6ADDE78B2CF8BDE12A663B
//     @Test
//     void testCreateMetadata() throws IOException {
//         PrefinishedOutput<RowsAndKeys> mockPrefinishedOutput = mock(PrefinishedOutput.class);
//         Metadata mockMetadata = mock(Metadata.class);
//         when(methodService.processInput(anyString(), anyString(), any(Repository.class))).thenReturn(repositoryConnection);
//         when(conversionService.convertByQuery(any(RepositoryConnection.class), any(Repository.class))).thenReturn(mockPrefinishedOutput);
//         when(metadataService.createMetadata(any(PrefinishedOutput.class))).thenReturn(mockMetadata);
//         Metadata result = rdfToCSV.createMetadata(mockPrefinishedOutput);
//         assertNotNull(result);
//         verify(metadataService).createMetadata(mockPrefinishedOutput);
//     }

//     //BaseRock generated method id: ${testConvertData}, hash: 4BB8BFBC750EA370628FC65F7BD70F4E
//     @Test
//     void testConvertData() {
//         PrefinishedOutput<RowsAndKeys> mockPrefinishedOutput = mock(PrefinishedOutput.class);
//         when(conversionService.convertByQuery(any(RepositoryConnection.class), any(Repository.class))).thenReturn(mockPrefinishedOutput);
//         PrefinishedOutput<?> result = rdfToCSV.convertData(repositoryConnection, repository);
//         assertNotNull(result);
//         verify(conversionService).convertByQuery(repositoryConnection, repository);
//     }

//     //BaseRock generated method id: ${testParseInput}, hash: 5358D5D5D5C238E244F5458C8947786C
//     @Test
//     void testParseInput() throws IOException {
//         when(methodService.processInput(anyString(), anyString(), any(Repository.class))).thenReturn(repositoryConnection);
//         rdfToCSV.parseInput();
//         verify(methodService).processInput(anyString(), anyString(), any(Repository.class));
//     }

//     //BaseRock generated method id: ${testCreateRepositoryConnection}, hash: ADC989DEF403BA49E58AA64E4B663D5D
//     @Test
//     void testCreateRepositoryConnection() throws IOException {
//         RepositoryConnection result = rdfToCSV.createRepositoryConnection(db, "." + (fileName.substring(9)), "rdf4j");
//         assertNotNull(result);
//     }
}