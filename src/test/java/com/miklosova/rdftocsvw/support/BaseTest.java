package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import org.eclipse.rdf4j.repository.Repository;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.AfterEach;

import java.io.FileReader;
import java.util.logging.Logger;

public class BaseTest {
    public static final Logger logger = Logger.getLogger(BaseTest.class.getName());
    private static final Logger LOGGER = Logger.getLogger(BaseTest.class.getName());
    public Metadata testMetadata;
    public Repository db;
    private String PROCESS_METHOD = "rdf4j";

    @AfterEach
    public void tearDown() {
        if (db != null) {
            db.shutDown();
        }

    }

    public JSONObject readJSONFile(String path) {
        JSONParser parser = new JSONParser();
        try {
            logger.info("This is the " + 1 + " time I say 'Hello World'.");
            //LOGGER.log(Level.FINE, "readJSONFile");
            Object obj = parser.parse(new FileReader(path));
            JSONObject jsonObject = (JSONObject) obj;
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
