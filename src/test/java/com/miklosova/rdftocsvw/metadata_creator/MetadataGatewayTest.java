package com.miklosova.rdftocsvw.metadata_creator;

 import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
 import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
 import org.junit.jupiter.api.BeforeEach;
 import static org.junit.jupiter.api.Assertions.assertEquals;

 import org.junit.jupiter.api.Disabled;
 import org.junit.jupiter.api.Test;
 import static org.junit.jupiter.api.Assertions.assertThrows;
 import org.mockito.Mock;
 import static org.mockito.Mockito.*;
 import static org.mockito.ArgumentMatchers.any;
 import static org.mockito.ArgumentMatchers.eq;

class MetadataGatewayTest {

     @Mock
     private IMetadataCreator metadataCreator;

     @Mock
     private PrefinishedOutput prefinishedOutput;

     private MetadataGateway metadataGateway;

     @BeforeEach
     void setUp() {
         metadataGateway = new MetadataGateway();
         metadataGateway.setMetadataCreator(metadataCreator);
     }

     //BaseRock generated method id: ${testSetMetadataCreator}, hash: B38C2F7FF5BC08C29423C4E30C162A2B
     @Test
     @Disabled
     void testSetMetadataCreator() {
         IMetadataCreator newMetadataCreator = mock(IMetadataCreator.class);
         metadataGateway.setMetadataCreator(newMetadataCreator);
         Metadata expectedMetadata = new Metadata();
         when(newMetadataCreator.addMetadata(any(PrefinishedOutput.class))).thenReturn(expectedMetadata);
         Metadata result = metadataGateway.processInput(prefinishedOutput);
         assertEquals(expectedMetadata, result);
         verify(newMetadataCreator).addMetadata(prefinishedOutput);
     }

     //BaseRock generated method id: ${testProcessInput}, hash: 7C5EE57E86014DBA98DAC67E919F5545
     @Test
     @Disabled
     void testProcessInput() {
         Metadata expectedMetadata = new Metadata();
         metadataCreator = mock(SplitFilesMetadataCreator.class);

         when(metadataCreator.addMetadata(prefinishedOutput)).thenReturn(expectedMetadata);
         Metadata result = metadataGateway.processInput(prefinishedOutput);
         assertEquals(expectedMetadata, result);
         verify(metadataCreator).addMetadata(prefinishedOutput);
     }

     //BaseRock generated method id: ${testProcessInputWithNullInput}, hash: 3B085824B682D6231CF8BFD579AE86B3
     @Test
     void testProcessInputWithNullInput() {
         assertThrows(NullPointerException.class, () -> metadataGateway.processInput(null));
     }

     //BaseRock generated method id: ${testProcessInputWithNullMetadataCreator}, hash: 89A2AF05642A003418D6670D9B3EF138
     @Test
     void testProcessInputWithNullMetadataCreator() {
         metadataGateway.setMetadataCreator(null);
         assertThrows(NullPointerException.class, () -> metadataGateway.processInput(prefinishedOutput));
     }
}