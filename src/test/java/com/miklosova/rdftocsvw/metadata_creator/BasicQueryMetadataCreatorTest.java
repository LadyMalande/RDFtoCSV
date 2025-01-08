package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.support.BaseTest;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.converter.*;
import com.miklosova.rdftocsvw.output_processor.FileWrite;
import com.miklosova.rdftocsvw.support.ConfigurationManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class BasicQueryMetadataCreatorTest extends BaseTest {

    @Mock
    private PrefinishedOutput<RowsAndKeys> mockData;

    @Mock
    private ConfigurationManager mockConfigManager;

    @Mock
    private FileWrite mockFileWrite;

    private BasicQueryMetadataCreator creator;

    @BeforeEach
    void setUp() {

        rdfToCSV = new RDFtoCSV(fileName);
        db = new SailRepository(new MemoryStore());
        args = new String[]{"-f", "test.rdf", "-p", "rdf4j"};
        ConfigurationManager.loadSettingsFromInputToConfigFile(args);
        creator = new BasicQueryMetadataCreator(mockData);
    }

    //BaseRock generated method id: ${testConstructor}, hash: 9F3B0CC4A3D45958C981D72396EA0EF3
    @Test
    void testConstructor() {
        //assertNotNull(creator.allFileNames);
        //assertNotNull(creator.metadata);
        //assertNotNull(creator.allRows);
        //assertEquals(0, creator.fileNumberX);
    }

    //BaseRock generated method id: ${testAddMetadata}, hash: C5D6256437311B268E8F5DC9DB02AAF6
    @Test
    void testAddMetadata() {
        //RowsAndKeys rowsAndKeys = mock(RowsAndKeys.class);
        //RowAndKey rowAndKey = mock(RowAndKey.class);
        //List<RowAndKey> rowAndKeyList = new ArrayList<>();
        //rowAndKeyList.add(rowAndKey);
        //List<Row> rows = new ArrayList<>();
        //Row row = mock(Row.class);
        //rows.add(row);
        //List<String> keys = new ArrayList<>();
        //keys.add("testKey");
        //when(mockData.getPrefinishedOutput()).thenReturn(rowsAndKeys);
        //when(rowsAndKeys.getRowsAndKeys()).thenReturn(rowAndKeyList);
        //when(rowAndKey.getRows()).thenReturn(rows);
        //when(rowAndKey.getKeys()).thenReturn(keys);
        //when(row.type).thenReturn("testType");
        //when(row.columns).thenReturn(new HashMap<>());
        /*try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class);
    MockedStatic<FileWrite> mockedFileWrite = mockStatic(FileWrite.class)) {
    mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_FILENAME)).thenReturn("testOutput");
    mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES)).thenReturn("");
    Metadata result = creator.addMetadata(mockData);
    assertNotNull(result);
    assertEquals(1, creator.fileNumberX);
    assertEquals(1, creator.allRows.size());
    assertEquals(1, creator.allFileNames.size());
    mockedConfigManager.verify(() -> ConfigurationManager.saveVariableToConfigFile(eq(ConfigurationManager.INTERMEDIATE_FILE_NAMES), anyString()));
    mockedFileWrite.verify(() -> FileWrite.writeFilesToConfigFile(creator.allFileNames));
}*/
    }

    //BaseRock generated method id: ${testAddMetadataWithSplitFiles}, hash: 0B52A1C2188CC832B57171FDDE2667CE
    @Test
    void testAddMetadataWithSplitFiles() {
        //PrefinishedOutput<?> mockInfo = mock(PrefinishedOutput.class);
        //when(mockInfo.getPrefinishedOutput()).thenThrow(ClassCastException.class);
        //Metadata result = creator.addMetadata(mockInfo);
        //assertNotNull(result);
        //assertTrue(result instanceof Metadata);
    }

    //BaseRock generated method id: ${testAddMetadataWithExistingIntermediateFileNames}, hash: 3CBC511F7647D5F790C7A5CB879CBA06
    @Test
    void testAddMetadataWithExistingIntermediateFileNames() {
        //RowsAndKeys rowsAndKeys = mock(RowsAndKeys.class);
        //RowAndKey rowAndKey = mock(RowAndKey.class);
        //List<RowAndKey> rowAndKeyList = new ArrayList<>();
        //rowAndKeyList.add(rowAndKey);
        //List<Row> rows = new ArrayList<>();
        //Row row = mock(Row.class);
        //rows.add(row);
        //List<String> keys = new ArrayList<>();
        //keys.add("testKey");
        //when(mockData.getPrefinishedOutput()).thenReturn(rowsAndKeys);
        //when(rowsAndKeys.getRowsAndKeys()).thenReturn(rowAndKeyList);
        //when(rowAndKey.getRows()).thenReturn(rows);
        //when(rowAndKey.getKeys()).thenReturn(keys);
        //when(row.type).thenReturn("testType");
        //when(row.columns).thenReturn(new HashMap<>());
        /*try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class);
    MockedStatic<FileWrite> mockedFileWrite = mockStatic(FileWrite.class)) {
    mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_FILENAME)).thenReturn("testOutput");
    mockedConfigManager.when(() -> ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES)).thenReturn("existingFile.csv");
    Metadata result = creator.addMetadata(mockData);
    assertNotNull(result);
    assertEquals(1, creator.fileNumberX);
    assertEquals(1, creator.allRows.size());
    assertEquals(1, creator.allFileNames.size());
    assertEquals("existingFile.csv", creator.allFileNames.get(0));
    mockedConfigManager.verify(() -> ConfigurationManager.saveVariableToConfigFile(eq(ConfigurationManager.INTERMEDIATE_FILE_NAMES), eq("existingFile.csv,existingFile.csv")));
    mockedFileWrite.verify(() -> FileWrite.writeFilesToConfigFile(creator.allFileNames));
}*/
    }
}