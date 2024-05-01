package com.miklosova.rdftocsvw.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Table;
import com.miklosova.rdftocsvw.convertor.Row;
import com.miklosova.rdftocsvw.support.FileWrite;
import org.eclipse.rdf4j.model.Value;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Metadata {
    private final String METADATA_FILENAME = "csv-metadata.json";

    /**
     * Array of files tied to the metadata file
     */
    private List<FileUrlDescriptor> tables;

    public void finalizeMetadata(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        JsonNode node = objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL).convertValue(this, JsonNode.class);
        ObjectNode objectNode = ((ObjectNode)node).put("@context", "http://www.w3.org/ns/csvw");
        try {
            String updatedJsonStr = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
            System.out.println(updatedJsonStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        FileWrite.deleteFile(METADATA_FILENAME);

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(METADATA_FILENAME), objectNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Transform JSON object to serialization

        // Write json to file
    }

    public Metadata() {
        this.tables = new ArrayList<>();
    }

    public void addMetadata(String newFileName, ArrayList<Value> keys, ArrayList<Row> rows) {
        FileUrlDescriptor newTable = new FileUrlDescriptor(newFileName);
        this.tables.add(newTable);
        newTable.addTableMetadata(keys, rows);
        // TODO
    }

    public void addForeignKeys(ArrayList<ArrayList<Row>> allRows) {
        for(FileUrlDescriptor fs : tables){

        }
        // TODO enrich metadata with Foreign Keys
    }
}
