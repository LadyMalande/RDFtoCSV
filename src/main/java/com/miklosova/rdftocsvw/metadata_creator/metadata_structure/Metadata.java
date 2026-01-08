package com.miklosova.rdftocsvw.metadata_creator.metadata_structure;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.miklosova.rdftocsvw.converter.data_structure.Row;
import com.miklosova.rdftocsvw.converter.data_structure.TypeIdAndValues;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.JsonUtil;
import ioinformarics.oss.jackson.module.jsonld.JsonldModule;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Conforming to the must-have annotations for the Group of tables:
 * <a href="https://www.w3.org/TR/2015/REC-tabular-data-model-20151217/#dfn-group-of-tables">#dfn-group-of-tables</a>
 * <a href="https://www.w3.org/TR/2015/REC-tabular-metadata-20151217/#table-groups">#table-groups</a> - specifying only tables as REQUIRED PROPERTIES
 * "notes" annotation is left out as it is not mandatory and may depend heavily on the inside knowledge of the data tables at hand.
 * <p>
 * "@context": "<a href="http://www.w3.org/ns/csvw">http://www.w3.org/ns/csvw</a>" included on basis of: <a href="https://www.w3.org/TR/2015/REC-tabular-metadata-20151217/#top-level-properties">#top-level-properties</a>
 */
@JsonldType("TableGroup")
public class Metadata {
    private static final ObjectMapper SORTED_MAPPER = new ObjectMapper();
    /**
     * Array of files tied to the metadata file
     */
    private final List<Table> tables;
    
    /**
     * Application configuration
     */
    @JsonIgnore
    private AppConfig config;

    /**
     * Instantiates a new Metadata.
     * @deprecated Use {@link #Metadata(AppConfig)} instead
     */
    @Deprecated
    public Metadata() {
        this(null);
    }

    /**
     * Instantiates a new Metadata with AppConfig.
     * @param config the application configuration
     */
    public Metadata(AppConfig config) {
        this.tables = new ArrayList<>();
        this.config = config;
    }

    public String jsonldMetadata() {
        SORTED_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        SORTED_MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
        SORTED_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        SORTED_MAPPER.registerModule(new JsonldModule());

        // Print the resulting JSON
        return JsonUtil.serializeAndWriteToFile(this, this.config);
    }

    public void addMetadata(String newFileName, ArrayList<Value> keys, ArrayList<Row> rows) {
        File filePath = new File(newFileName);
        Table newTable = new Table(filePath.getName(), this.config);
        this.tables.add(newTable);
        newTable.addTableMetadata(keys, rows);
    }

    public List<Table> getTables() {
        return tables;
    }

    /**
     * Gets the application configuration.
     * @return the config
     */
    public AppConfig getConfig() {
        return config;
    }

    /**
     * Sets the application configuration.
     * @param config the config to set
     */
    public void setConfig(AppConfig config) {
        this.config = config;
    }


    public void addForeignKeys(ArrayList<ArrayList<Row>> allRows) {
        for (ArrayList<Row> rows : allRows) {
            Value id = rows.get(0).id;
            Value type = rows.get(0).type;
            String typeLocalName;
            try {
                IRI typeIri = (IRI) type;
                typeLocalName = typeIri.getLocalName();
            } catch (ClassCastException ex) {
                // The type is literal
                typeLocalName = type.stringValue();
            }
            String foreignKeyFile = null;
            for (Table fileUrlDescriptor : tables) {
                String finalTypeLocalName = typeLocalName;
                if (fileUrlDescriptor.getTableSchema().getColumns().stream().anyMatch(column -> {
                    if (column.getName() != null) {
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
                        List<ForeignKey> fk;
                        if (outcomingTableSchema.getForeignKeys() == null) {
                            fk = new ArrayList<>();
                        } else {
                            fk = outcomingTableSchema.getForeignKeys();
                        }
                        fk.add(new ForeignKey(columnName, new Reference(foreignKeyFile, typeLocalName)));
                        outcomingTableSchema.setForeignKeys(fk);

                    }
                }
            }
        }
    }
}
