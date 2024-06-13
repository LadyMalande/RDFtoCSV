package com.miklosova.rdftocsvw;

import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class fullProcessTest {
    private String nameForTest;
    private String filePath;
    private String filePathForImage;

    private String filePathForOutput;
    private String filePathForMetadata;

    public fullProcessTest(String nameForTest, String filePath, String filePathForMetadata, String filePathForOutput, String filePathForImage) {
        this.nameForTest = nameForTest;
        this.filePath = filePath;
        this.filePathForMetadata = filePathForMetadata;
        this.filePathForOutput = filePathForOutput;
        this.filePathForImage = filePathForImage;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs(){
        return Arrays.asList(new Object[][]{
                { "", "", "", "", ""},
                { "", "", "", "", ""},
        });
    }
    @Test
    void csvIsSameTrig() {
        String filePath = "src/test/resources/testingInput.trig";

        RepositoryConnection rc = ms.processInput(filePath, inputProcessingMethod, db);


        Assert.assertEquals(valuesFromTest, valuesFromTurtle);

    }
}
