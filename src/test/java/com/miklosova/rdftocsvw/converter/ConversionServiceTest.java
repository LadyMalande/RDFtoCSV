package com.miklosova.rdftocsvw.converter;

 import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
 import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
 import com.miklosova.rdftocsvw.support.AppConfig;
 import com.miklosova.rdftocsvw.support.BaseTest;
 import org.eclipse.rdf4j.repository.sail.SailRepository;
 import org.eclipse.rdf4j.sail.memory.MemoryStore;
 import org.junit.jupiter.api.Disabled;
 import org.mockito.MockitoAnnotations;
 import org.eclipse.rdf4j.repository.RepositoryConnection;
 import org.junit.jupiter.api.BeforeEach;
// ...existing code...
 import org.eclipse.rdf4j.repository.Repository;
 import org.junit.jupiter.api.Test;
 import org.junit.jupiter.params.provider.CsvSource;
 import org.junit.jupiter.params.ParameterizedTest;
 import static org.junit.jupiter.api.Assertions.*;
 import org.mockito.Mock;
 import static org.mockito.Mockito.*;
 import static org.mockito.ArgumentMatchers.any;
 import static org.mockito.ArgumentMatchers.eq;

class ConversionServiceTest extends BaseTest {

     private ConversionService conversionService;

     @Mock
     private RepositoryConnection mockRepositoryConnection;

     @Mock
     private Repository mockRepository;

     @Mock
     private ConversionGateway mockConversionGateway;

    // Removed ConfigurationManager mock

     @BeforeEach
     void setUp() {
         MockitoAnnotations.openMocks(this);
         // Use AppConfig for configuration
         AppConfig config = new AppConfig.Builder("/RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt")
                 .parsing("rdf4j")
                 .build();
         conversionService = new ConversionService(config);
         fileName = "/RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt";
         rdfToCSV = new RDFtoCSV(config);
         db = new SailRepository(new MemoryStore());
         args = new String[]{"-f", "test.rdf", "-p", "rdf4j"};
     }

     //BaseRock generated method id: ${testConvertByQueryWithNullRepositoryConnection}, hash: 1674FCE5A51BE2F537FAE74CF3ACEB52
     @Test
     void testConvertByQueryWithNullRepositoryConnection() {
         assertNull(conversionService.convertByQuery(null, mockRepository));
     }

     //BaseRock generated method id: ${testConvertByQueryWithValidInput}, hash: 1ABF22155D0C0E41BC32F9ADF54C92B0
     @Test
     @Disabled
     void testConvertByQueryWithValidInput() throws Exception {
         PrefinishedOutput<RowsAndKeys> expectedOutput = new PrefinishedOutput<>(new RowsAndKeys.RowsAndKeysFactory().factory());
         when(mockConversionGateway.processInput(mockRepositoryConnection)).thenReturn(expectedOutput);
         // Set conversion method via AppConfig
         AppConfig config = new AppConfig.Builder(fileName)
                 .parsing("rdf4j")
                 .build();
         config.setConversionMethod("basicQuery");
         ConversionService service = new ConversionService(config);
         PrefinishedOutput<RowsAndKeys> result = service.convertByQuery(mockRepositoryConnection, mockRepository);
         assertNotNull(result);
         assertEquals(expectedOutput, result);
     }

     //BaseRock generated method id: ${testProcessConversionTypeWithValidInput}, hash: 8B5B582BB01C80BA45A15AA8F420F329
     @ParameterizedTest
     @CsvSource({ "basicQuery, rdf4j", "trivial, rdf4j", "splitQuery, rdf4j", "streaming, streaming",
              "bigFileStreaming, bigFileStreaming" })
     void testProcessConversionTypeWithValidInput(String conversionChoice, String readingMethod) throws Exception {
         AppConfig config = new AppConfig.Builder(fileName)
                 .parsing(readingMethod)
                 .build();
         config.setConversionMethod(conversionChoice);
         ConversionService service = new ConversionService(config);
         conn = rdfToCSV.createRepositoryConnection(db, "./src/test/resources/differentSerializations/testingInput.nt", readingMethod.toLowerCase());
         if(conversionChoice.equalsIgnoreCase("streaming") || conversionChoice.toLowerCase().equalsIgnoreCase("bigFileStreaming")){
             assertNull(conn);
         } else {
             service.convertByQuery(conn, db);
             //System.out.println(service.getConversionGateway().getConversionMethod().toString());

             assertNotNull(service.getConversionGateway().getConversionMethod());
             switch (conversionChoice) {
                 case "basicQuery":
                 case "trivial":
                     assertTrue(service.getConversionGateway().getConversionMethod() instanceof BasicQueryConverter);
                     break;
                 case "splitQuery":
                     assertTrue(service.getConversionGateway().getConversionMethod() instanceof SplitFilesQueryConverter);
                     break;
             }
         }
     }

     //BaseRock generated method id: ${testProcessConversionTypeWithInvalidInput}, hash: 1D9713266D9D8E7955E88244475D5B46
     @Test
     void testProcessConversionTypeWithInvalidInput() throws Exception {
         assertThrows(IllegalArgumentException.class, () -> {
             AppConfig config = new AppConfig.Builder(fileName)
                     .parsing("invalidChoice")
                     .build();
             config.setConversionMethod("invalidChoice");
             ConversionService service = new ConversionService(config);
             rdfToCSV.createRepositoryConnection(db, fileName, "invalidChoice");
         });
     }

     // AppConfig-based test methods

     @Test
     void testConvertByQueryWithAppConfigBasicQuery() throws Exception {
         AppConfig config = new AppConfig.Builder("test.rdf")
                 .parsing("rdf4j")
                 .build();
         config.setConversionMethod("basicQuery");

         ConversionService service = new ConversionService(config);
         conn = rdfToCSV.createRepositoryConnection(db, "./src/test/resources/differentSerializations/testingInput.nt", "rdf4j");

         if (conn != null) {
             PrefinishedOutput<RowsAndKeys> result = service.convertByQuery(conn, db);
             assertNotNull(result);
             assertTrue(service.getConversionGateway().getConversionMethod() instanceof BasicQueryConverter);
         }
     }

     @Test
     void testConvertByQueryWithAppConfigSplitQuery() throws Exception {
         AppConfig config = new AppConfig.Builder("test.rdf")
                 .parsing("rdf4j")
                 .build();
         config.setConversionMethod("splitQuery");

         ConversionService service = new ConversionService(config);
         conn = rdfToCSV.createRepositoryConnection(db, "./src/test/resources/differentSerializations/testingInput.nt", "rdf4j");

         if (conn != null) {
             PrefinishedOutput<RowsAndKeys> result = service.convertByQuery(conn, db);
             assertNotNull(result);
             assertTrue(service.getConversionGateway().getConversionMethod() instanceof SplitFilesQueryConverter);
         }
     }

     @Test
     void testConvertByQueryWithAppConfigInvalidMethod() {
         AppConfig config = new AppConfig.Builder("test.rdf")
                 .parsing("rdf4j")
                 .build();
         config.setConversionMethod("invalidMethod");

         ConversionService service = new ConversionService(config);
         assertThrows(IllegalArgumentException.class, () -> {
             service.convertByQuery(mockRepositoryConnection, mockRepository);
         });
     }
}