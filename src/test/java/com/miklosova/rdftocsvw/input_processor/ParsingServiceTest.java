package com.miklosova.rdftocsvw.input_processor;

 import org.eclipse.rdf4j.repository.RepositoryConnection;
 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.Disabled;
 import org.junit.jupiter.api.Test;

 import java.io.File;

 import org.mockito.Mock;
 import org.eclipse.rdf4j.rio.RDFParseException;
 import org.mockito.MockitoAnnotations;

 import static org.junit.jupiter.api.Assertions.*;

 import java.io.IOException;

 import static org.mockito.ArgumentMatchers.any;
 import static org.mockito.ArgumentMatchers.eq;

class ParsingServiceTest {

     @Mock
     private RepositoryConnection mockConnection;

     @Mock
     private InputGateway mockInputGateway;

     private ParsingService parsingService;

     @BeforeEach
     void setUp() {
         MockitoAnnotations.openMocks(this);
         parsingService = new ParsingService();
     }

     @Test
     void testProcessInput() throws RDFParseException, IOException {
         File mockFile = new File("../RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt");
         RepositoryConnection result = parsingService.processInput(mockConnection, mockFile);
         assertNotNull(result);
         assertEquals(mockConnection, result);
     }

     //BaseRock generated method id: ${testProcessExtensionInvalidExtension}, hash: B60A9FBBCC70DB0FFE92951936F8B294
     @Test
     void testProcessExtensionInvalidExtension() {
         ParsingService service = new ParsingService();
         assertThrows(IllegalArgumentException.class, () -> {
             service.processInput(mockConnection, new File("test.invalid"));
         });
     }


     @Test
     void testProcessURI() throws IOException {
         ParsingService service = new ParsingService();
         assertThrows(NoSuchMethodException.class, () -> {
             service.getClass().getDeclaredMethod("processURI", String.class).invoke(service, "http://invalid.url");
         });
     }
}