package com.miklosova.rdftocsvw.metadata_creator;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.miklosova.rdftocsvw.convertor.Row;
import com.miklosova.rdftocsvw.support.FileWrite;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public List<FileUrlDescriptor> getTables() {
        return tables;
    }

    public void addForeignKeys(ArrayList<ArrayList<Row>> allRows) {
        for(ArrayList<Row> rows : allRows){
            Value id = rows.get(0).id;
            Value type = rows.get(0).type;
            IRI typeIri = (IRI) type;
            String typeLocalName = typeIri.getLocalName();
            String foreignKeyFile = null;
            for(FileUrlDescriptor fileUrlDescriptor : tables){
                if(fileUrlDescriptor.getTableSchema().getColumns().stream().anyMatch(column -> {
                    if(column.getName() != null){
                        return column.getName().equals(typeLocalName);
                    } else{
                        return false;
                    }

                })){
                    foreignKeyFile = fileUrlDescriptor.getUrl();
                }
            }
            Value columnKeyValue = null;
            for(ArrayList<Row> rows2 : allRows){
                for(Row row : rows2){
                    for(Map.Entry<Value, List<Value>> entry : row.map.entrySet()){
                        if(entry.getValue().contains(id)){
                            columnKeyValue = entry.getKey();
                        }
                    }
                }
            }
            if(columnKeyValue != null){
                TableSchema outcomingTableSchema;
                // FIND the file in which the id is and the file in which the columnKeyValue is
                for(FileUrlDescriptor fileDescriptor : tables){
                    Value finalColumnKeyValue = columnKeyValue;
                    if(fileDescriptor.getTableSchema().getColumns().stream().anyMatch(column -> {
                        if(column.getPropertyUrl() != null){
                            return column.getPropertyUrl().equals(finalColumnKeyValue.toString());
                        } else{
                            return false;
                        }

                    })){
                        outcomingTableSchema = fileDescriptor.getTableSchema();
                        IRI iri = (IRI)finalColumnKeyValue;
                        String columnName = iri.getLocalName();
                        if(outcomingTableSchema.getForeignKeys() == null){
                            List<ForeignKey> fk =  new ArrayList<>();
                            fk.add(new ForeignKey(columnName, new Reference(foreignKeyFile, typeLocalName)));
                            outcomingTableSchema.setForeignKeys(fk);
                        } else{
                            List<ForeignKey> fk =  outcomingTableSchema.getForeignKeys();
                            fk.add(new ForeignKey(columnName, new Reference(foreignKeyFile, typeLocalName)));
                            outcomingTableSchema.setForeignKeys(fk);
                        }

                    }
                }
            }
        }
        // TODO enrich metadata with Foreign Keys
    }
}
