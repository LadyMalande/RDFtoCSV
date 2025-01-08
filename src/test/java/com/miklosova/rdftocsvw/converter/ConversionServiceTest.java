package com.miklosova.rdftocsvw.converter;

 import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
 import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
 import com.miklosova.rdftocsvw.support.BaseTest;
 import org.eclipse.rdf4j.repository.sail.SailRepository;
 import org.eclipse.rdf4j.sail.memory.MemoryStore;
 import org.junit.jupiter.api.Disabled;
 import org.mockito.MockitoAnnotations;
 import org.eclipse.rdf4j.repository.RepositoryConnection;
 import org.junit.jupiter.api.BeforeEach;
 import com.miklosova.rdftocsvw.support.ConfigurationManager;
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

     @Mock
     private ConfigurationManager mockConfigurationManager;

     @BeforeEach
     void setUp() {
         MockitoAnnotations.openMocks(this);
         conversionService = new ConversionService();
         fileName = "/RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt";
         rdfToCSV = new RDFtoCSV(fileName);
         db = new SailRepository(new MemoryStore());
         args = new String[]{"-f", "test.rdf", "-p", "rdf4j"};
         ConfigurationManager.loadSettingsFromInputToConfigFile(args);
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
         try (var mockedStatic = mockStatic(ConfigurationManager.class)) {
             mockedStatic.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD)).thenReturn("basicQuery");
             PrefinishedOutput<RowsAndKeys> result = conversionService.convertByQuery(mockRepositoryConnection, mockRepository);
             assertNotNull(result);
             assertEquals(expectedOutput, result);
         }
     }

     //BaseRock generated method id: ${testProcessConversionTypeWithValidInput}, hash: 8B5B582BB01C80BA45A15AA8F420F329
     @ParameterizedTest
     @CsvSource({ "basicQuery, rdf4j", "trivial, rdf4j", "splitQuery, rdf4j", "streaming, streaming",
              "bigFileStreaming, bigFileStreaming" })
     void testProcessConversionTypeWithValidInput(String conversionChoice, String readingMethod) throws Exception {
         conn = rdfToCSV.createRepositoryConnection(db, "./src/test/resources/differentSerializations/testingInput.nt", readingMethod.toLowerCase());
         if(conversionChoice.equalsIgnoreCase("streaming") || conversionChoice.toLowerCase().equalsIgnoreCase("bigFileStreaming")){
             assertNull(conn);
         } else {
             try (var mockedStatic = mockStatic(ConfigurationManager.class)) {
                 mockedStatic.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD)).thenReturn(conversionChoice);
                 conversionService.convertByQuery(conn, db);
                 System.out.println(conversionService.getConversionGateway().getConversionMethod().toString());

                 assertNotNull(conversionService.getConversionGateway().getConversionMethod());
                 switch (conversionChoice) {
                     case "basicQuery", "trivial":
                         assertTrue(conversionService.getConversionGateway().getConversionMethod() instanceof BasicQueryConverter);
                         break;
                     case "splitQuery":
                         assertTrue(conversionService.getConversionGateway().getConversionMethod() instanceof SplitFilesQueryConverter);
                         break;
                 }

             }
         }
     }

     //BaseRock generated method id: ${testProcessConversionTypeWithInvalidInput}, hash: 1D9713266D9D8E7955E88244475D5B46
     @Test
     void testProcessConversionTypeWithInvalidInput() throws Exception {
         try (var mockedStatic = mockStatic(ConfigurationManager.class)) {
             mockedStatic.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD)).thenReturn("invalidChoice");
             assertThrows(IllegalArgumentException.class, () -> rdfToCSV.createRepositoryConnection(db, fileName, "invalidChoice"));
         }
     }
}