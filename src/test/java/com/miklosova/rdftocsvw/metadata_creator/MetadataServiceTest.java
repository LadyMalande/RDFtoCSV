package com.miklosova.rdftocsvw.metadata_creator;

 import com.miklosova.rdftocsvw.converter.RDFtoCSV;
 import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
 import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
 import com.miklosova.rdftocsvw.support.BaseTest;
 import org.eclipse.rdf4j.repository.sail.SailRepository;
 import org.eclipse.rdf4j.sail.memory.MemoryStore;
 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.Disabled;
 import org.junit.jupiter.api.Test;
 import org.junit.jupiter.params.ParameterizedTest;
 import org.mockito.Mock;
 import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
 import org.mockito.MockedStatic;
 import org.mockito.MockitoAnnotations;
 import com.miklosova.rdftocsvw.output_processor.FileWrite;
 import org.mockito.InjectMocks;
 import com.miklosova.rdftocsvw.support.ConfigurationManager;
 import org.junit.jupiter.params.provider.CsvSource;
 import static org.junit.jupiter.api.Assertions.*;
 import static org.mockito.Mockito.*;
 import static org.mockito.ArgumentMatchers.any;
 import static org.mockito.ArgumentMatchers.eq;

class MetadataServiceTest extends BaseTest {

     @InjectMocks
     private MetadataService metadataService;

     @Mock
     private MetadataGateway metadataGateway;


     @Mock
     private PrefinishedOutput<RowsAndKeys> mockData;
     @Mock
     private ConfigurationManager configurationManager;

     @Mock
     private FileWrite fileWrite;

     @BeforeEach
     void setUp() throws Exception {
         MockitoAnnotations.openMocks(this);
         rdfToCSV = new RDFtoCSV(fileName);
         db = new SailRepository(new MemoryStore());
         args = new String[]{"-f", "test.rdf", "-p", "rdf4j"};
         ConfigurationManager.loadSettingsFromInputToConfigFile(args);
     }

     //BaseRock generated method id: ${testCreateMetadata}, hash: 904D6779257AF2F67F62E92D51DCF1FC
     @Test
     @Disabled
     void testCreateMetadata() {
         PrefinishedOutput<RowsAndKeys> po = mock(PrefinishedOutput.class);
         mockData = (PrefinishedOutput<RowsAndKeys>) mock(PrefinishedOutput.class);

         when(mockData.getPrefinishedOutput()).thenReturn(new RowsAndKeys());
         Metadata expectedMetadata = mock(Metadata.class);
         when(metadataGateway.processInput(po)).thenReturn(expectedMetadata);
         Metadata result = metadataService.createMetadata(mockData);
         assertEquals(expectedMetadata, result);
         verify(metadataGateway).processInput(po);
     }

     //BaseRock generated method id: ${testProcessMetadataCreation}, hash: 37E7A3D2632D7E6308040075C96E507C
     @ParameterizedTest
     @Disabled
     @CsvSource({ "basicQuery, .rdf, BasicQueryMetadataCreator", "splitQuery, .rdf, SplitFilesMetadataCreator", "bigFileStreaming, .nt, BigFileStreamingNTriplesMetadataCreator", "streaming, .nt, StreamingNTriplesMetadataCreator" })
     void testProcessMetadataCreation(String conversionChoice, String fileExtension, String expectedCreatorClass) throws Exception {
         PrefinishedOutput<RowsAndKeys> data = mock(PrefinishedOutput.class);
         when(data.getPrefinishedOutput()).thenReturn(mock(RowsAndKeys.class));
         try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class);
             MockedStatic<FileWrite> mockedFileWrite = mockStatic(FileWrite.class)) {
             mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD)).thenReturn(conversionChoice);
             mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INPUT_FILENAME)).thenReturn("test" + fileExtension);
             mockedFileWrite.when(() -> FileWrite.getFileExtension(anyString())).thenReturn(fileExtension);
             metadataService.createMetadata(data);
             verify(metadataGateway).setMetadataCreator(any());
         }
     }

     //BaseRock generated method id: ${testProcessMetadataCreationWithInvalidConversionMethod}, hash: E561AFE69C96720B86145D1B2E1AEDAB
     @Test
     @Disabled
     void testProcessMetadataCreationWithInvalidConversionMethod() {
         PrefinishedOutput<RowsAndKeys> data = mock(PrefinishedOutput.class);
         try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
             mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD)).thenReturn("invalidMethod");
             assertThrows(IllegalArgumentException.class, () -> metadataService.createMetadata(data));
         }
     }

     //BaseRock generated method id: ${testProcessMetadataCreationWithInvalidDataForBasicQuery}, hash: 2467C0BB521124A176C68BD7C6777E72
     @Test
     @Disabled
     void testProcessMetadataCreationWithInvalidDataForBasicQuery() {
         PrefinishedOutput<RowsAndKeys> data = mock(PrefinishedOutput.class);
         when(data.getPrefinishedOutput()).thenReturn(null);
         try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
             mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD)).thenReturn("basicQuery");
             assertThrows(IllegalArgumentException.class, () -> metadataService.createMetadata(data));
         }
     }

     //BaseRock generated method id: ${testProcessMetadataCreationWithInvalidDataForSplitQuery}, hash: AA9C58E6FDD8193309EB3366637FBC27
     @Test
     @Disabled
     void testProcessMetadataCreationWithInvalidDataForSplitQuery() {
         PrefinishedOutput<RowsAndKeys> po = mock(PrefinishedOutput.class);
         mockData = (PrefinishedOutput<RowsAndKeys>) mock(PrefinishedOutput.class);
         when(mockData.getPrefinishedOutput()).thenReturn(new RowsAndKeys());
         PrefinishedOutput<RowsAndKeys> data = mock(PrefinishedOutput.class);
         when(data.getPrefinishedOutput()).thenReturn(null);
       assertThrows(IllegalArgumentException.class, () -> metadataService.createMetadata(mockData));

     }

     //BaseRock generated method id: ${testProcessMetadataCreationWithInvalidFileExtensionForBigFileStreaming}, hash: B25AA9DDA076DAA230FFCBCA0FFED640
     @Test
     void testProcessMetadataCreationWithInvalidFileExtensionForBigFileStreaming() {
         PrefinishedOutput<RowsAndKeys> data = mock(PrefinishedOutput.class);
         try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class);
             MockedStatic<FileWrite> mockedFileWrite = mockStatic(FileWrite.class)) {
             mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD)).thenReturn("bigFileStreaming");
             mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INPUT_FILENAME)).thenReturn("test.rdf");
             mockedFileWrite.when(() -> FileWrite.getFileExtension(anyString())).thenReturn(".rdf");
             assertThrows(IllegalArgumentException.class, () -> metadataService.createMetadata(data));
         }
     }

     //BaseRock generated method id: ${testProcessMetadataCreationWithInvalidFileExtensionForStreaming}, hash: 664B500A9A8534537E0C28CB87D45FB5
     @Test
     void testProcessMetadataCreationWithInvalidFileExtensionForStreaming() {
         PrefinishedOutput<RowsAndKeys> data = mock(PrefinishedOutput.class);
         try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class);
             MockedStatic<FileWrite> mockedFileWrite = mockStatic(FileWrite.class)) {
             mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD)).thenReturn("streaming");
             mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INPUT_FILENAME)).thenReturn("test.rdf");
             mockedFileWrite.when(() -> FileWrite.getFileExtension(anyString())).thenReturn(".rdf");
             assertThrows(IllegalArgumentException.class, () -> metadataService.createMetadata(data));
         }
     }
}