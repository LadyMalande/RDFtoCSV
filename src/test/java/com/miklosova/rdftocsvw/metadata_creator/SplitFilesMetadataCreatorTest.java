package com.miklosova.rdftocsvw.metadata_creator;

 import com.miklosova.rdftocsvw.converter.*;

 import java.io.IOException;

 import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
 import com.miklosova.rdftocsvw.converter.data_structure.Row;
 import com.miklosova.rdftocsvw.converter.data_structure.RowAndKey;
 import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
 import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
 import com.miklosova.rdftocsvw.support.AppConfig;
 import org.eclipse.rdf4j.model.Value;
 import org.eclipse.rdf4j.repository.Repository;
 import org.eclipse.rdf4j.repository.sail.SailRepository;
 import org.eclipse.rdf4j.sail.memory.MemoryStore;
 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.Test;
 import org.mockito.Mock;
 import com.miklosova.rdftocsvw.output_processor.FileWrite;
 import com.miklosova.rdftocsvw.support.ConfigurationManager;
 import java.util.ArrayList;

 import static org.eclipse.rdf4j.model.util.Values.iri;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.junit.jupiter.api.Assertions.*;
 import org.mockito.MockedStatic;

 import static org.mockito.Mockito.*;
 import static org.mockito.ArgumentMatchers.any;
 import static org.mockito.ArgumentMatchers.eq;

class SplitFilesMetadataCreatorTest {

     @Mock
     private PrefinishedOutput<RowsAndKeys> mockData;

    @Mock
    private RowsAndKeys mockRowsAndKeys;

     @Mock
     private Metadata mockMetadata;

     @Mock
     private ConfigurationManager mockConfigurationManager;

     @Mock
     private FileWrite mockFileWrite;

     private SplitFilesMetadataCreator creator;

     private ArrayList<Value> keys, keys1;

     private Row firstRow, secondRow, thirdRow, fourthRow;

     private ArrayList<Row> rows, rows1;

     private ArrayList<String> fileNames, oneFileNames;
     private ArrayList<ArrayList<Row>> foreignKeys;
    private String[] args;
    private RDFtoCSV rdfToCSV;
    private Repository db;
    private String fileName = "/RDFtoCSV/src/test/resources/differentSerializations/testingInput.nt";

    AppConfig config;

     @BeforeEach
     void setUp() {
         config = new AppConfig.Builder("test.rdf")
                 .parsing("rdf4j")
                 .output("test")
                 .build();
         rdfToCSV = new RDFtoCSV(config);
         db = new SailRepository(new MemoryStore());
         args = new String[]{"-f", "test.rdf", "-p", "rdf4j"};
         ConfigurationManager.loadSettingsFromInputToConfigFile(args);
         ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_FILENAME, "test");
         mockData = (PrefinishedOutput<RowsAndKeys>) mock(PrefinishedOutput.class);

         when(mockData.getPrefinishedOutput()).thenReturn(new RowsAndKeys());
         mockMetadata =  mock(Metadata.class);

         creator = new SplitFilesMetadataCreator(mockData, config);
         creator.metadata = mockMetadata;
         keys = new ArrayList<>();
         keys.add(iri("http://predicate1.cz"));
         keys.add(iri("http://predicate2.cz"));
         keys1 = new ArrayList<>();
         keys1.add(iri("http://predicate3.cz"));
         keys1.add(iri("http://predicate4.cz"));
         firstRow = new Row(iri("http://subject1.cz"), true);
         secondRow = new Row(iri("http://subject2.cz"), iri("http://predicate1.cz") , true);
         rows = new ArrayList<>();
         rows.add(firstRow);
         rows.add(secondRow);
         thirdRow = new Row(iri("http://subject3.cz"), true);
         fourthRow = new Row(iri("http://subject4.cz"), iri("http://predicate2.cz") , true);
         rows1 = new ArrayList<>();
         rows1.add(thirdRow);
         rows1.add(fourthRow);
         foreignKeys = new ArrayList<>();
         foreignKeys.add(rows1);
         foreignKeys.add(rows);
         fileNames = new ArrayList<>();
         fileNames.add("test0.csv");
         fileNames.add("test1.csv");
         oneFileNames = new ArrayList<>();
         oneFileNames.add("test0.csv");
     }

     //BaseRock generated method id: ${testConstructor}, hash: 06806DE2C2593F7D3A31DD018B067B0A
     @Test
     void testConstructor() {
         assertNotNull(creator.allFileNames);
         assertNotNull(creator.allRows);
         assertEquals(0, creator.fileNumberX);
         assertEquals("test", creator.CSVFileTOWriteTo);
     }

     //BaseRock generated method id: ${testAddMetadata}, hash: 53BC9A4D4D7CF5B22EECF6EBB10279D1
     @Test
     void testAddMetadata() {
         RowsAndKeys rowsAndKeys = new RowsAndKeys();
         rowsAndKeys.getRowsAndKeys().add(new RowAndKey(keys1, rows1));
         rowsAndKeys.getRowsAndKeys().add(new RowAndKey(keys, rows));
         when(mockData.getPrefinishedOutput()).thenReturn(rowsAndKeys);
         try (MockedStatic<FileWrite> fileWriteMockedStatic = mockStatic(FileWrite.class)) {
             Metadata result = creator.addMetadata(mockData);
             assertEquals(mockMetadata, result);
             verify(mockMetadata, times(1)).addMetadata(anyString(), eq(keys), eq(rows));
            System.out.println();
             verify(mockMetadata).addForeignKeys(foreignKeys);
             verify(mockMetadata).jsonldMetadata();
             fileWriteMockedStatic.verify(() -> FileWrite.writeFilesToConfigFile(fileNames, config));
         }
     }

     //BaseRock generated method id: ${testAddMetadataWithIntermediateFileNames}, hash: CA06063653257BD0D11341EBAF28B49A
     @Test
     void testAddMetadataWithIntermediateFileNames() {
         RowsAndKeys rowsAndKeys = new RowsAndKeys();

         rowsAndKeys.getRowsAndKeys().add(new RowAndKey(keys, rows));
         when(mockData.getPrefinishedOutput()).thenReturn(rowsAndKeys);
         try (MockedStatic<FileWrite> fileWriteMockedStatic = mockStatic(FileWrite.class)) {
             creator.addMetadata(mockData);
             verify(mockMetadata).addMetadata("test0.csv", keys, rows);
             fileWriteMockedStatic.verify(() -> FileWrite.writeFilesToConfigFile(oneFileNames, config));
         }
     }

     //BaseRock generated method id: ${testAddMetadataWithEmptyRowsAndKeys}, hash: 10CC44D3388246A2749AD4D2CE865180
     @Test
     void testAddMetadataWithEmptyRowsAndKeys() {
         RowsAndKeys emptyRowsAndKeys = new RowsAndKeys();
         ArrayList<ArrayList<Row>> emptyForeignKeys = new ArrayList<>();
         ArrayList<String> emptyFileNames = new ArrayList<>();
         when(mockData.getPrefinishedOutput()).thenReturn(emptyRowsAndKeys);
         try (MockedStatic<FileWrite> fileWriteMockedStatic = mockStatic(FileWrite.class)) {
             Metadata result = creator.addMetadata(mockData);
             assertEquals(mockMetadata, result);
             verify(mockMetadata, never()).addMetadata(anyString(), eq(keys), eq(rows));
             verify(mockMetadata).addForeignKeys(emptyForeignKeys);
             verify(mockMetadata).jsonldMetadata();
             fileWriteMockedStatic.verify(() -> FileWrite.writeFilesToConfigFile(emptyFileNames, config));
         }
     }
}