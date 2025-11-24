package com.miklosova.rdftocsvw.metadata_creator;

 import com.miklosova.rdftocsvw.converter.RDFtoCSV;
 import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
 import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
 import com.miklosova.rdftocsvw.support.AppConfig;
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
     private FileWrite fileWrite;

     @BeforeEach
     void setUp() throws Exception {
         MockitoAnnotations.openMocks(this);
         rdfToCSV = new RDFtoCSV(config);
         db = new SailRepository(new MemoryStore());
     }

     //BaseRock generated method id: ${testProcessMetadataCreationWithInvalidFileExtensionForBigFileStreaming}, hash: B25AA9DDA076DAA230FFCBCA0FFED640
    @Test
    void testProcessMetadataCreationWithInvalidFileExtensionForBigFileStreaming() {
        PrefinishedOutput<RowsAndKeys> data = mock(PrefinishedOutput.class);
        // Setup AppConfig with required values
        AppConfig appConfig = new AppConfig
        .Builder("test.rdf")
                .parsing("bigFileStreaming")
                .build();
        metadataService = new MetadataService(appConfig);
        try (MockedStatic<FileWrite> mockedFileWrite = mockStatic(FileWrite.class)) {
            mockedFileWrite.when(() -> FileWrite.getFileExtension(anyString())).thenReturn(".rdf");
            assertThrows(IllegalArgumentException.class, () -> metadataService.createMetadata(data));
        }
    }
}