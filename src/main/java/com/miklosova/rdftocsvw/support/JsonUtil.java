package com.miklosova.rdftocsvw.support;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ioinformarics.oss.jackson.module.jsonld.JsonldModule;

import java.io.File;
import java.io.IOException;

public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String CONTEXT_KEY = "@context";
    private static final String CONTEXT_VALUE = "http://www.w3.org/ns/csvw";

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

    public static void writeJsonToFile(ObjectNode resultNode) {
        // Serialize the final object to a JSON string

        String metadataFilename = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME);
        FileWrite.deleteFile(metadataFilename);
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(metadataFilename), resultNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String serializeAndWriteToFile(Object obj) {
        ObjectNode resultNode = null;
        try {
            resultNode = serializeWithContext(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        writeJsonToFile(resultNode);
        try {
            return mapper.writeValueAsString(resultNode);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String serializeAndReturnPrettyString(Object obj) {
        ObjectNode resultNode = null;
        try {
            resultNode = serializeWithContext(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        try {
            return mapper.writeValueAsString(resultNode);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
