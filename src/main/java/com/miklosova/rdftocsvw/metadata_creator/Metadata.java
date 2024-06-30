package com.miklosova.rdftocsvw.metadata_creator;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.miklosova.rdftocsvw.convertor.Row;
import com.miklosova.rdftocsvw.convertor.TypeIdAndValues;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.FileWrite;
import ioinformarics.oss.jackson.module.jsonld.JsonldModule;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Conforming to the must have annotations for the Group of tables:
 *  https://www.w3.org/TR/2015/REC-tabular-data-model-20151217/#dfn-group-of-tables
 *  https://www.w3.org/TR/2015/REC-tabular-metadata-20151217/#table-groups - specifying only tables as REQUIRED PROPERTIES
 *  "notes" annotation is left out as it is not mandatory and may depend heavily on the inside knowledge of the data tables at hand.
 *
 *  "@context": "http://www.w3.org/ns/csvw" included on basis of: https://www.w3.org/TR/2015/REC-tabular-metadata-20151217/#top-level-properties
 */
@JsonldType("TableGroup")
public class Metadata {
    /**
     * Array of files tied to the metadata file
     */
    private List<Table> tables;
    /**
     * an identifier for this group of tables, or null if this is undefined.
     */
    private String id;

    private static final ObjectMapper SORTED_MAPPER = new ObjectMapper();

    public void jsonldMetadata(){
        ObjectMapper objectMapper = new ObjectMapper();
        SORTED_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        SORTED_MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        SORTED_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        SORTED_MAPPER.registerModule(new JsonldModule());
        JsonNode node = SORTED_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL).convertValue(this, JsonNode.class);

        ObjectNode objectNode = ((ObjectNode)node).put("@context", "http://www.w3.org/ns/csvw");
        String personJsonLd = null;
        try {
            personJsonLd = SORTED_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println(personJsonLd);
    }

    public void finalizeMetadata(){
        jsonldMetadata();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        JsonNode node = objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL).convertValue(this, JsonNode.class);
        // Included because of https://www.w3.org/TR/2015/REC-tabular-metadata-20151217/#top-level-properties
        ObjectNode objectNode = ((ObjectNode)node).put("@context", "http://www.w3.org/ns/csvw");
        try {
            String updatedJsonStr = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
            System.out.println(updatedJsonStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String metadataFilename = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME);
        FileWrite.deleteFile(metadataFilename);
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(metadataFilename), objectNode);
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
        File filePath = new File(newFileName);
        Table newTable = new Table(filePath.getName());
        this.tables.add(newTable);
        newTable.addTableMetadata(keys, rows);
        // TODO
    }

    public List<Table> getTables() {
        return tables;
    }

    public void addForeignKeys(ArrayList<ArrayList<Row>> allRows) {
        for(ArrayList<Row> rows : allRows){
            Value id = rows.get(0).id;
            Value type = rows.get(0).type;
            IRI typeIri = (IRI) type;
            String typeLocalName = typeIri.getLocalName();
            String foreignKeyFile = null;
            for(Table fileUrlDescriptor : tables){
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
                    for(Map.Entry<Value, TypeIdAndValues> entry : row.columns.entrySet()){
                        if(entry.getValue().values.contains(id)){
                            columnKeyValue = entry.getKey();
                        }
                    }
                }
            }
            if(columnKeyValue != null){
                TableSchema outcomingTableSchema;
                // FIND the file in which the id is and the file in which the columnKeyValue is
                for(Table fileDescriptor : tables){
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
