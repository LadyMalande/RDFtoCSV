package com.miklosova.rdftocsvw.support;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.miklosova.rdftocsvw.output_processor.FileWrite;
import ioinformarics.oss.jackson.module.jsonld.JsonldModule;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class that enriches Metadata JSON object with mandatory header of @context.
 */
public class JsonUtil {

    private static final Logger logger = Logger.getLogger(JsonUtil.class.getName());
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String CONTEXT_KEY = "@context";
    private static final String CONTEXT_VALUE = "http://www.w3.org/ns/csvw";

    /**
     * Serialize with context the object node - expecting Metadata.
     *
     * @param obj Usually the Metadata object.
     * @return Metadata serialized into JSON.
     * @throws JsonProcessingException The json processing exception.
     */
    public static ObjectNode serializeWithContext(Object obj) throws JsonProcessingException {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new JsonldModule());
        // Serialize the object to a JSON tree
        ObjectNode jsonObject = mapper.valueToTree(obj);

        // Create a new ObjectNode to hold the final JSON with @context
        ObjectNode resultNode = mapper.createObjectNode();

        // Add @context field at the top
        resultNode.put(CONTEXT_KEY, CONTEXT_VALUE);

        // Add all original fields from the serialized object
        resultNode.setAll(jsonObject);

        return resultNode;

    }



    /**
     * Write json to file using AppConfig.
     *
     * @param resultNode The serialized object to write to file.
     * @param config The application configuration
     */
    public static void writeJsonToFile(ObjectNode resultNode, AppConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("AppConfig cannot be null");
        }
        
        // Serialize the final object to a JSON string
        String metadataFilename = config.getOutputMetadataFileName();
        
        // Check if metadata contains multiple tables
        // If it does, use standard "csv-metadata.json" filename (overrides -o option)
        if (resultNode.has("tables") && resultNode.get("tables").isArray() && resultNode.get("tables").size() > 1) {
            // Multiple tables - use csv-metadata.json in the output directory
            File originalFile = new File(metadataFilename);
            File parentDir = originalFile.getParentFile();
            metadataFilename = parentDir != null ? 
                new File(parentDir, "csv-metadata.json").getPath() : 
                "csv-metadata.json";
            
            // Update the config so the new filename is used everywhere
            config.setOutputMetadataFileName(metadataFilename);
            
            logger.log(Level.INFO, "Multiple tables detected (" + resultNode.get("tables").size() + 
                " tables), overriding -o option to use standard filename: csv-metadata.json");
        }
        
        logger.log(Level.INFO, "Writing metadata to: " + metadataFilename);
        FileWrite.deleteFile(metadataFilename);
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(metadataFilename), resultNode);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getCause() + " " + e.getLocalizedMessage());
        }
    }

    /**
     * Serialize and write to file and return the serialized JSON object string.
     *
     * @param obj The object to serialize and write to file.
     * @return The serialized JSON object String.
     * @deprecated Use {@link #serializeAndWriteToFile(Object, AppConfig)} instead
     */
    @Deprecated
    public static String serializeAndWriteToFile(Object obj) {
        throw new UnsupportedOperationException("serializeAndWriteToFile requires AppConfig. Use serializeAndWriteToFile(obj, config) instead.");
    }

    /**
     * Serialize and write to file using AppConfig and return the serialized JSON object string.
     *
     * @param obj The object to serialize and write to file.
     * @param config The application configuration
     * @return The serialized JSON object String.
     */
    public static String serializeAndWriteToFile(Object obj, AppConfig config) {
        ObjectNode resultNode = null;
        try {
            resultNode = serializeWithContext(obj);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, e.getCause() + " " + e.getLocalizedMessage());
        }
        writeJsonToFile(resultNode, config);
        try {
            return mapper.writeValueAsString(resultNode);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, e.getCause() + " " + e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * Serialize and return pretty string as String object.
     *
     * @param obj The object to serialize into JSON.
     * @return The pretty print String of serialized object.
     */
    public static String serializeAndReturnPrettyString(Object obj) {
        ObjectNode resultNode = null;
        try {
            resultNode = serializeWithContext(obj);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, e.getCause() + " " + e.getLocalizedMessage());
        }
        try {
            return mapper.writeValueAsString(resultNode);
        } catch (JsonProcessingException e) {
            e.getOriginalMessage();
            return null;
        }
    }
}
