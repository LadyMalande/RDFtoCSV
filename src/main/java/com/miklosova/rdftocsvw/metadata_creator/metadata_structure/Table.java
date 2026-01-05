package com.miklosova.rdftocsvw.metadata_creator.metadata_structure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.miklosova.rdftocsvw.converter.data_structure.Row;
import com.miklosova.rdftocsvw.support.AppConfig;

import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;
import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * The Table class as defined by CSV on the Web Specification.
 */
@JsonldType("Table")
@SuppressWarnings("unused")
public class Table {
    /**
     * Name for the file that is connected to the metadata
     */
    private String url;
    /**
     * Object containing all the information about the given table in file from url attribute
     */
    private TableSchema tableSchema;

    /**
     * A list of transformation definitions for given table
     */
    private List<Transformation> transformations;

    /**
     * Application configuration
     */
    @JsonIgnore
    private AppConfig config;


    /**
     * Instantiates a new Table.
     *
     * @param url the url
     */
    public Table(String url) {
        this(url, null);
    }

    public Table(AppConfig config) {
        this.config = config;
    }

    /**
     * Instantiates a new Table with AppConfig.
     *
     * @param url the url
     * @param config the application configuration
     */
    public Table(String url, AppConfig config) {
        this.url = url;
        this.config = config;
    }

    /**
     * Instantiates a new Table.
     */
    public Table() {
        this(null, null);
    }

    /**
     * Gets transformations.
     *
     * @return the transformations
     */
    public List<Transformation> getTransformations() {
        return transformations;
    }

    /**
     * Sets transformations.
     *
     * @param transformations the transformations
     */
    public void setTransformations(List<Transformation> transformations) {
        this.transformations = transformations;
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets url.
     *
     * @param url the url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets table schema.
     *
     * @return the table schema
     */
    public TableSchema getTableSchema() {
        return tableSchema;
    }

    /**
     * Sets table schema.
     *
     * @param tableSchema the table schema
     */
    public void setTableSchema(TableSchema tableSchema) {
        this.tableSchema = tableSchema;
    }

    /**
     * Gets application configuration.
     *
     * @return the application configuration
     */
    public AppConfig getConfig() {
        return config;
    }

    /**
     * Sets application configuration.
     *
     * @param config the application configuration
     */
    public void setConfig(AppConfig config) {
        this.config = config;
    }

    /**
     * Add table metadata.
     *
     * @param keys the keys (headers)
     * @param rows the rows of the inner CSV representation
     */
    public void addTableMetadata(ArrayList<Value> keys, ArrayList<Row> rows) {
        this.tableSchema = new TableSchema(keys, rows, this.config);
        this.tableSchema.addTableSchemaMetadata(this.config);
        addTransformations(this.config);
    }


    /**
     * Add transformations. They are added to the Table if there are blank nodes present.
     * @deprecated Use {@link #addTransformations(AppConfig)} instead
     */
    @Deprecated
    public void addTransformations() {
        addTransformations(null);
    }

    /**
     * Add transformations with AppConfig. They are added to the Table if there are blank nodes present.
     * @param config the application configuration
     */
    public void addTransformations(AppConfig config) {
        if (config == null || config.getConversionHasBlankNodes() == null) {
            return; // Skip if no config available
        }
        String hasBlankNodes = String.valueOf(config.getConversionHasBlankNodes());
        if (hasBlankNodes.equalsIgnoreCase("true")) {
            this.transformations = new ArrayList<>();
            this.transformations.add(new Transformation(
                    "https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/main/scripts/transformationForBlankNodesStreamed.js",
                    "http://www.iana.org/assignments/media-types/application/javascript",
                    "http://www.iana.org/assignments/media-types/turtle",
                    "rdf",
                    "RDF format used as the output format in the transformation from CSV to RDF"
            ));
        }
    }
}
