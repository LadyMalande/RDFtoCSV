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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class BasicQueryMetadataCreatorTest extends BaseTest {

    @Mock
    private PrefinishedOutput<RowsAndKeys> mockData;


    @Mock
    private FileWrite mockFileWrite;

    private BasicQueryMetadataCreator creator;

    @BeforeEach
    void setUp() {
        rdfToCSV = new RDFtoCSV(config);
        db = new SailRepository(new MemoryStore());
        creator = new BasicQueryMetadataCreator(mockData, config);
    }

    @Test
    void testConstructor() {

    }

    @Test
    void testAddMetadata() {

    }

    @Test
    void testAddMetadataWithSplitFiles() {

    }

    @Test
    void testAddMetadataWithExistingIntermediateFileNames() {

    }
}