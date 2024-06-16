package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.ConversionService;
import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.convertor.RowAndKey;
import com.miklosova.rdftocsvw.convertor.RowsAndKeys;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.FileWrite;
import com.miklosova.rdftocsvw.support.TestSupport;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
@RunWith(Parameterized.class)
public class DatatypesTest extends BaseTest{
    private String nameForTest;
    private String filePath;
    private String filePathForMetadata;
    private String filePathForOutput;
    private String expectedDatatype;
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> configs(){
        return Arrays.asList(new Object[][]{
                { "Datatypes-string", "./src/test/resources/testingInputForTwoEntities.ttl", "./src/test/resources/testingInput.csv-metadata.json", "./src/test/resources/testingInputOutput", "integer"},
                { "Datatypes-anyURI", "./src/test/resources/datatypes-anyURI.ttl", "./src/test/resources/datatypes-anyURI.csv-metadata.json", "./src/test/resources/testingInputOutput", "anyURI"},
                { "Datatypes-boolean", "./src/test/resources/datatypes-boolean.ttl", "./src/test/resources/datatypes-boolean.csv-metadata.json", "./src/test/resources/testingInputOutput", "boolean"},

                //{ "", "", "", "", "", ""},
        });
    }

    public DatatypesTest(String nameForTest, String filePath, String filePathForMetadata, String filePathForOutput, String expectedDatatype) {
        this.nameForTest = nameForTest;
        this.filePath = filePath;
        this.filePathForMetadata = filePathForMetadata;
        this.filePathForOutput = filePathForOutput;
        this.expectedDatatype = expectedDatatype;
    }

    @BeforeEach
    @Override
    void createMetadata(String filePath, String filePathForMetadata){
        System.out.println("Override before each");

        super.createMetadata(filePath, filePathForMetadata);
    }
    @Test
    public void isGivenDatatype() {
        createMetadata(filePath, filePathForMetadata);
        System.out.println("START isGivenDatatype");
        JSONObject jsonObject = readJSONFile(filePathForMetadata);
        JSONArray tables = (JSONArray) jsonObject.get("tables");
        JSONObject table = (JSONObject) tables.get(2);
        JSONObject tableSchema = (JSONObject) table.get("tableSchema");
        JSONArray columns = (JSONArray) tableSchema.get("columns");
        JSONObject testColumn = (JSONObject) columns.stream().filter(column -> ((JSONObject) column).get("name").equals("datatypeTest")).findAny().get();

        Assert.assertEquals(testColumn.get("datatype"), this.expectedDatatype);
    }
}
