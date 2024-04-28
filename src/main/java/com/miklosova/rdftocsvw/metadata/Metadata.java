package com.miklosova.rdftocsvw.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.miklosova.rdftocsvw.convertor.Row;
import com.miklosova.rdftocsvw.support.FileWrite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Metadata {
    private final String METADATA_FILENAME = "csv-metadata.json";

    /**
     * Array of files tied to the metadata file
     */
    private FileUrlDescriptor[] tables;

    private void finalizeMetadata(){
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.convertValue(this, JsonNode.class);
        ObjectNode objectNode = ((ObjectNode)node).put("@context", "http://www.w3.org/ns/csvw");
        try {
            String updatedJsonStr = new ObjectMapper().writeValueAsString(objectNode);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        FileWrite.deleteFile(METADATA_FILENAME);

        try {
            objectMapper.writeValue(new File(METADATA_FILENAME), this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Transform JSON object to serialization

        // Write json to file
    }

    public void addMetadata(String newFileName, ArrayList<String> keys, ArrayList<Row> rows) {
        // TODO
    }
}
