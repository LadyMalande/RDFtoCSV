package com.miklosova.rdftocsvw;

import com.miklosova.rdftocsvw.convertor.ConversionService;
import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.convertor.RowAndKey;
import com.miklosova.rdftocsvw.convertor.RowsAndKeys;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.MetadataService;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.FileWrite;
import com.miklosova.rdftocsvw.support.TestSupport;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class OriginalIsSubsetOfCSVTest {
        private String nameForTest;
        private String originalRdfFile;
        private String newRdfFile;

        @Parameterized.Parameters(name = "{0}")
        public static Collection<Object[]> configs(){
            return Arrays.asList(new Object[][]{
                    { "SplitQuerySmallDataset", "./src/test/resources/OriginalIsSubsetOfCSV/nace-cz1.trig", "./src/test/resources/OriginalIsSubsetOfCSV/idOutuput.ttl"},
            });
        }

        public OriginalIsSubsetOfCSVTest(String nameForTest, String originalRdfFile, String newRdfFile) {
            this.nameForTest = nameForTest;
            this.originalRdfFile = originalRdfFile;
            this.newRdfFile = newRdfFile;
        }
        @Test
        public void originalIsSubsetOfResult() {

            try {
                Assert.assertTrue(TestSupport.isRDFSubsetOfTerms(this.newRdfFile, this.originalRdfFile));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
