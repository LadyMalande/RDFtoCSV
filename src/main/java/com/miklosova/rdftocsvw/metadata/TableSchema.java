package com.miklosova.rdftocsvw.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.miklosova.rdftocsvw.convertor.Row;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

import java.util.*;
import java.util.stream.Collectors;

public class TableSchema {
    /**
     * Array of column objects containing information about each column
     */
    private List<Column> columns;
    /**
     * Array of objects containing information about foreign keys in this table
     * Has to be added to the metadata extra after the given foreign keys are dealt with from the model.
     */
    private List<ForeignKey> foreignKeys;
    /**
     * Designates the primary key of the table (usually will be the first column)
     *
     */
    private String primaryKey;
    /**
     *
     * http://example.org/country/{code}
     */
    private String aboutUrl;
    /**
     * Array of names of columns that should summarize what the row is about (usually the primary key)
     */
    private List<String> rowTitles;

    private List<Value> keys;
    private List<Row> rows;

    @JsonIgnore
    public List<Value> getKeys() {
        return keys;
    }

    public TableSchema(List<Value> keys, List<Row> rows) {
        this.keys = keys;
        this.rows = rows;

    }

    @JsonIgnore
    public List<Row> getRows() {
        return rows;
    }

    public void addTableSchemaMetadata() {
        // TODO process the IRIs
        this.primaryKey = createAboutUrl(this.keys.get(0));

        this.rowTitles = new ArrayList<>();
        this.rowTitles.add(primaryKey);

        this.columns = createColumns();
    }

    private List<Column> createColumns() {
        List<Column> listOfColumns = new ArrayList<>();
        for(Map.Entry<Value, List<Value>> column : rows.get(0).map.entrySet()){
            Column newColumn = new Column(column);
            newColumn.createColumn();
            if(newColumn.getLang() !=  null){
                enrichMetadataWithLangVariations(column, listOfColumns);
            } else{
                listOfColumns.add(newColumn);
            }

        }
        listOfColumns.add(createIdColumnWithType());
        listOfColumns.add(createVirtualTypeColumn());
        return listOfColumns;
    }

    private void enrichMetadataWithLangVariations(Map.Entry<Value, List<Value>> column, List<Column> listOfColumns) {
        List<Map.Entry<Value, List<Value>>> langVariations = new ArrayList<>();
        Set<String> langVariationsAdded = new HashSet<>();
        for(Value literal : column.getValue()){
            Literal langTag = (Literal) literal;
            System.out.println("add " + langTag.getLanguage().get());
            langVariationsAdded.add(langTag.getLanguage().get());
        }
        for(String langVar : langVariationsAdded){
            List<Value> newValue = new ArrayList<>();
            System.out.println("langVar" + langVar);
            newValue.add(column.getValue().stream().filter(obj -> obj.toString().substring(obj.toString().length() - 2, obj.toString().length()).contains(langVar))
                    .findFirst()
                    .get());
            Map.Entry<Value, List<Value>> newEntry = new AbstractMap.SimpleEntry<Value, List<Value>>(column.getKey(), newValue);
            langVariations.add(newEntry);

        }
        for(Map.Entry<Value, List<Value>> lang : langVariations){
            Column newColumn = new Column(lang);
            newColumn.createColumn();
            // Check whether the language variation is already in the data

            if(listOfColumns.stream()
                    .filter(item -> item.getLang() != null)
                    .collect(Collectors.toList()).stream().filter(item -> item.getLang().contains(newColumn.getLang())).collect(Collectors.toList()).isEmpty()){
                listOfColumns.add(newColumn);
            }

        }
    }

    private Column createVirtualTypeColumn() {
        Value type = rows.get(0).type;
        Value value = rows.get(0).id;

        Column newColumn = new Column(null);
        newColumn.addVirtualTypeColumn(type, value);
        return newColumn;
    }

    private Column createIdColumnWithType() {
        Value type = rows.get(0).type;
        Value value = rows.get(0).id;

        Column newColumn = new Column(null);
        newColumn.addFirstColumn(type, value);
        return newColumn;
    }

    private String createAboutUrl(Value key0){
        String theNameOfTheColumn = getLastSectionOfIri(key0);
        String partBeforeLastSection = getPartBeforeLastSection(key0);
        // We dont know how aboutUrl is supposed to look like because we dont know semantic ties to the iris
        // this.aboutUrl = partBeforeLastSection + "{" + theNameOfTheColumn + "}";
        return theNameOfTheColumn;
    }

    private String getPartBeforeLastSection(Value key0) {
        IRI iri = (IRI) key0;
        String[] split = key0.toString().split("/");
        String lastPart = iri.getLocalName();
                //split[split.length - 1];

        String firstPart = iri.getNamespace();
        //key0.toString().substring(0, key0.toString().length() - lastPart.length());
        return firstPart;
    }

    private String getLastSectionOfIri(Value key0) {
        IRI iri = (IRI) key0;

        String[] split = key0.toString().split("/");
        return iri.getLocalName();
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(List<ForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getAboutUrl() {
        return aboutUrl;
    }

    public void setAboutUrl(String aboutUrl) {
        this.aboutUrl = aboutUrl;
    }

    public List<String> getRowTitles() {
        return rowTitles;
    }

    public void setRowTitles(List<String> rowTitles) {
        this.rowTitles = rowTitles;
    }

    public void setKeys(List<Value> keys) {
        this.keys = keys;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }
}