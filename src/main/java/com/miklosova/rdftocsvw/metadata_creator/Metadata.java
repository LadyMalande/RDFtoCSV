package com.miklosova.rdftocsvw.metadata_creator;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.miklosova.rdftocsvw.convertor.Row;
import com.miklosova.rdftocsvw.convertor.TypeIdAndValues;
import com.miklosova.rdftocsvw.support.JsonUtil;
import ioinformarics.oss.jackson.module.jsonld.JsonldModule;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Conforming to the must have annotations for the Group of tables:
 * https://www.w3.org/TR/2015/REC-tabular-data-model-20151217/#dfn-group-of-tables
 * https://www.w3.org/TR/2015/REC-tabular-metadata-20151217/#table-groups - specifying only tables as REQUIRED PROPERTIES
 * "notes" annotation is left out as it is not mandatory and may depend heavily on the inside knowledge of the data tables at hand.
 * <p>
 * "@context": "http://www.w3.org/ns/csvw" included on basis of: https://www.w3.org/TR/2015/REC-tabular-metadata-20151217/#top-level-properties
 */
@JsonldType("TableGroup")
public class Metadata {
    private static final ObjectMapper SORTED_MAPPER = new ObjectMapper();
    /**
     * Array of files tied to the metadata file
     */
    private List<Table> tables;
    /**
     * an identifier for this group of tables, or null if this is undefined.
     */
    private String id;

    public Metadata() {
        this.tables = new ArrayList<>();
    }

    public String jsonldMetadata() {
        ObjectMapper objectMapper = new ObjectMapper();
        SORTED_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        SORTED_MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
        SORTED_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        SORTED_MAPPER.registerModule(new JsonldModule());

/*
        JsonNode node = SORTED_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL).convertValue(this, JsonNode.class);

        ObjectNode objectNode = ((ObjectNode)node).put("@context", "http://www.w3.org/ns/csvw");


        String personJsonLd = null;
        try {
            personJsonLd = SORTED_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println(personJsonLd);

 */
        String jsonWithContext = null;

        jsonWithContext = JsonUtil.serializeAndWriteToFile(this);


        // Print the resulting JSON
        System.out.println(jsonWithContext);
        return jsonWithContext;
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
        for (ArrayList<Row> rows : allRows) {
            Value id = rows.get(0).id;
            Value type = rows.get(0).type;
            System.out.println("Type in addForeignKeys: " + type.stringValue());
            String typeLocalName = null;
            try {
                IRI typeIri = (IRI) type;
                typeLocalName = typeIri.getLocalName();
            } catch (ClassCastException ex) {
                // The type is literal
                Literal literal = (Literal) type;
                typeLocalName = type.stringValue();
            }
            String foreignKeyFile = null;
            for (Table fileUrlDescriptor : tables) {
                String finalTypeLocalName = typeLocalName;
                if (fileUrlDescriptor.getTableSchema().getColumns().stream().anyMatch(column -> {
                    if (column.getName() != null) {
                        //System.out.println("Foreign key match? column.getName=" + column.getName() + " typeLocalName=" + typeLocalName);
                        //System.out.println("Foreign key match? equals?=" + column.getName().equals(typeLocalName));
                        return column.getName().equals(finalTypeLocalName);
                    } else {
                        return false;
                    }

                })) {
                    foreignKeyFile = fileUrlDescriptor.getUrl();
                }
            }
            Value columnKeyValue = null;
            for (ArrayList<Row> rows2 : allRows) {
                for (Row row : rows2) {
                    for (Map.Entry<Value, TypeIdAndValues> entry : row.columns.entrySet()) {
                        if (entry.getValue().values.contains(id)) {
                            columnKeyValue = entry.getKey();
                        }
                    }
                }
            }
            if (columnKeyValue != null) {
                TableSchema outcomingTableSchema;
                // FIND the file in which the id is and the file in which the columnKeyValue is
                for (Table fileDescriptor : tables) {
                    Value finalColumnKeyValue = columnKeyValue;
                    if (fileDescriptor.getTableSchema().getColumns().stream().anyMatch(column -> {
                        if (column.getPropertyUrl() != null) {
                            return column.getPropertyUrl().equals(finalColumnKeyValue.toString());
                        } else {
                            return false;
                        }

                    })) {
                        outcomingTableSchema = fileDescriptor.getTableSchema();
                        IRI iri = (IRI) finalColumnKeyValue;
                        String columnName = iri.getLocalName();
                        if (outcomingTableSchema.getForeignKeys() == null) {
                            List<ForeignKey> fk = new ArrayList<>();
                            fk.add(new ForeignKey(columnName, new Reference(foreignKeyFile, typeLocalName)));
                            outcomingTableSchema.setForeignKeys(fk);
                        } else {
                            List<ForeignKey> fk = outcomingTableSchema.getForeignKeys();
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
