package com.miklosova.rdftocsvw.convertor;

import com.miklosova.rdftocsvw.support.TestSupport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class OriginalIsSubsetOfCSVTest {
    private String nameForTest;
    private String originalRdfFile;
    private String newRdfFile;

    public OriginalIsSubsetOfCSVTest(String nameForTest, String originalRdfFile, String newRdfFile) {
        this.nameForTest = nameForTest;
        this.originalRdfFile = originalRdfFile;
        this.newRdfFile = newRdfFile;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][]{
                {"SplitQuerySmallDataset", "./src/test/resources/OriginalIsSubsetOfCSV/nace-cz1.trig", "./src/test/resources/OriginalIsSubsetOfCSV/idOutuput.ttl"},
        });
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
