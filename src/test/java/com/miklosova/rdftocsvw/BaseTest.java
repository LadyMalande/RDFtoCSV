package com.miklosova.rdftocsvw;

import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import org.eclipse.rdf4j.repository.Repository;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;

import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BaseTest {
    private String PROCESS_METHOD = "rdf4j";
    public Metadata testMetadata;
    public Repository db;
    private static final Logger LOGGER = Logger.getLogger( BaseTest.class.getName() );

    @BeforeEach
    void createMetadata(){
        /*
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME, filePathForMetadata);
        db = new SailRepository(new MemoryStore());
        MethodService methodService = new MethodService();
        RepositoryConnection rc = methodService.processInput(filePath, PROCESS_METHOD, db);
        assert(rc != null);
        // Convert the table to intermediate data for processing into metadata
        ConversionService cs = new ConversionService();
        System.out.println("createMetadata @BeforeEach");
        PrefinishedOutput prefinishedOutput = cs.convertByQuery(rc, db);
        // Convert intermediate data into basic metadata
        MetadataService ms = new MetadataService();
        Metadata metadata = ms.createMetadata(prefinishedOutput);

        this.testMetadata = metadata;

         */
    }

    @After
    public void tearDown() {
        db.shutDown();
    }

    public JSONObject readJSONFile(String path){
        JSONParser parser = new JSONParser();
        try {
            LOGGER.log(Level.FINE, "readJSONFile");
            Object obj = parser.parse(new FileReader(path));
            JSONObject jsonObject = (JSONObject) obj;
            return jsonObject;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
