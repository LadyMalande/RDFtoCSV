package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.ConversionService;
import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.LoggerFactory;

import java.io.FileReader;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BaseTest {
    private String PROCESS_METHOD = "rdf4j";
    Metadata testMetadata;
    Repository db;
    private static final Logger LOGGER = Logger.getLogger( BaseTest.class.getName() );

    @BeforeEach
    void createMetadata(String filePath, String filePathForMetadata){
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
