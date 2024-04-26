package com.miklosova.rdftocsvw.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.miklosova.rdftocsvw.support.FileWrite;

import java.io.File;
import java.io.IOException;

public class Metadata {
    private final String METADATA_FILENAME = "csv-metadata.json";

    /*
    private Cell;
    private Column;
    private Datatype;
    private Dialect;
    private Direction;
    private ForeignKey;
    private NumericFormat;
    private Row;
    private Schema;
    private Table;
    private TableGroups;
    private TableReference;
    private Transformation;

     */

    public Metadata(){

    }

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

}
