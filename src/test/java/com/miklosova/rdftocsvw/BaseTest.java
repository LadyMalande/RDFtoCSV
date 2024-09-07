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

    @After
    public void tearDown() {
        if(db != null){
            db.shutDown();
        }

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
