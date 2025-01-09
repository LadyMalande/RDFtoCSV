package com.miklosova.rdftocsvw.metadata_creator.metadata_structure;

import com.miklosova.rdftocsvw.converter.data_structure.Row;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
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
     * Instantiates a new Table.
     *
     * @param url the url
     */
    public Table(String url) {
        this.url = url;
    }

    /**
     * Instantiates a new Table.
     */
    public Table() {
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
     * Add table metadata.
     *
     * @param keys the keys (headers)
     * @param rows the rows of the inner CSV representation
     */
    public void addTableMetadata(ArrayList<Value> keys, ArrayList<Row> rows) {

        this.tableSchema = new TableSchema(keys, rows);
        this.tableSchema.addTableSchemaMetadata();
        addTransformations();
    }


    /**
     * Add transformations. They are added to the Table if there are blank nodes present.
     */
    public void addTransformations() {
        if (ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_BLANK_NODES) != null && ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_BLANK_NODES).equalsIgnoreCase("true")) {
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
