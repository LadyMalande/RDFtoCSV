package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.Row;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;
import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.List;

@JsonldType("Table")
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
    private List<Transformation> transformtaions;


    public List<Transformation> getTransformtaions() {
        return transformtaions;
    }

    public void setTransformtaions(List<Transformation> transformtaions) {
        this.transformtaions = transformtaions;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public TableSchema getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(TableSchema tableSchema) {
        this.tableSchema = tableSchema;
    }

    public Table(String url) {
        this.url = url;
    }

    public void addTableMetadata(ArrayList<Value> keys, ArrayList<Row> rows) {

        this.tableSchema = new TableSchema(keys, rows);
        this.tableSchema.addTableSchemaMetadata();
        addTransformations();
    }



    private void addTransformations() {
        if (ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_BLANK_NODES).equalsIgnoreCase("true")) {
            this.transformtaions = new ArrayList<>();
            this.transformtaions.add(new Transformation(
                    "https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/main/scripts/transformationForBlankNodesStreamed.js",
                    "http://www.iana.org/assignments/media-types/application/javascript",
                    "http://www.iana.org/assignments/media-types/turtle",
                    "rdf",
                    "RDF format used as the output format in the transformation from CSV to RDF"
            ));
        }
    }
}
