package com.miklosova.rdftocsvw.metadata_creator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.miklosova.rdftocsvw.convertor.Row;
import com.miklosova.rdftocsvw.convertor.TypeIdAndValues;
import com.miklosova.rdftocsvw.convertor.TypeOfValue;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.ConnectionChecker;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Values;

import java.util.*;
import java.util.stream.Collectors;
@JsonldType("Schema")
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
        // This only works for the tables that have different types of entities

        this.primaryKey = createAboutUrl(this.keys.get(0));
        //System.out.println("columns");
        //rows.get(0).columns.entrySet().forEach( column -> System.out.println(column.getKey()));
        this.columns = createColumns(this.rows);
        this.rowTitles = new ArrayList<>();
        addRowTitles();


    }

    private void addRowTitles(){
        if(ConnectionChecker.checkConnection()){
            this.columns.forEach(column -> {
                if ((column.getVirtual() == null) || (column.getVirtual() != null && !column.getVirtual())
                ) {
                    if((column.getSuppressOutput() == null)){
                        Dereferencer dereferencer = new Dereferencer(column.getPropertyUrl());
                        //System.out.println("Dereference = " + column.getName());
                        try {
                            this.rowTitles.add(dereferencer.getTitle());
                        } catch(NullPointerException noElement){
                            this.rowTitles.add(column.getName());
                        }
                    }

                }
            });

        } else {
            this.columns.forEach(column -> {
                if (column.getVirtual() == null || (column.getVirtual() != null && !column.getVirtual())) {
                    this.rowTitles.add(column.getName());
                }
            });
        }

    }

    /**
     * Creates annotations for columns present in the data.
     *
     * The virtual columns are created the last as by specification of
     * https://www.w3.org/TR/2015/REC-tabular-metadata-20151217/#columns - virtual
     * @return List of Column objects annotating individual columns that should be present in the table.
     */
    private List<Column> createColumns(List<Row> rows) {
        List<Column> listOfColumns = new ArrayList<>();
       // if(Boolean.getBoolean(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES))) {
            listOfColumns.add(createIdColumnWithType());
        List<Map.Entry<Value, TypeIdAndValues>> columns = getColumnsFromRows();



        for(Map.Entry<Value, TypeIdAndValues> column : columns){
                //System.out.println("Column " + column.getKey());
                boolean namespaceIsTheSame = isNamespaceTheSameForAllRows(rows, column.getKey());
                Column newColumn = new Column(column, namespaceIsTheSame);
                boolean subjectNamespaceIsTheSame = isNamespaceTheSameForAllSubjects(rows, column.getKey());
                newColumn.createColumn(rows.get(0), subjectNamespaceIsTheSame);
                if (newColumn.getLang() != null) {
                    //System.out.println("Column lang not null");
                    enrichMetadataWithLangVariations(column, listOfColumns, rows.get(0));
                } else {
                    listOfColumns.add(newColumn);
                }


        }
        if(rows.get(0).isRdfType){
            listOfColumns.add(createVirtualTypeColumn(rows.get(0).id));
        }


        return listOfColumns;
    }

    private boolean isNamespaceTheSameForAllRows(List<Row> rows, Value columnPredicate) {
        if(columnPredicate.stringValue().equalsIgnoreCase(rows.get(0).type.stringValue())){
            return isNamespaceTheSameForAllPrimary(rows);
        }
        //System.out.println("predicateOfColumn ="+ columnPredicate.stringValue());
        if(rows.get(0).columns != null) {
            if(rows.get(0).columns.get(columnPredicate) != null) {
                if (rows.get(0).columns.get(columnPredicate).type == TypeOfValue.LITERAL) {
                    return false;
                }
            }
        } else {
            return false;
        }
        //rows.forEach(row -> row.columns.entrySet().forEach(column -> System.out.println(column.getKey())));
        List<Row> hasIRI = rows.stream().filter(row -> {
            if(row.columns.get(columnPredicate) != null) {
                return row.columns.get(columnPredicate).values.get(0).isIRI();
            } else {return false;}
        }).toList();
        final String namespace = ((IRI)hasIRI.get(0).columns.get(columnPredicate).values.get(0)).getNamespace();
        for(Row row : rows){
            if(!row.columns.entrySet().stream().filter(column -> column.getKey().stringValue().equalsIgnoreCase(columnPredicate.stringValue())).allMatch(entry ->
                    entry.getValue().values.stream().allMatch(
                            value -> {
                                return ((IRI)value).getNamespace().equalsIgnoreCase(namespace);
                            }))){
                return false;
            }
        }
        return true;

    }

    private List<Map.Entry<Value, TypeIdAndValues>> getColumnsFromRows() {
        List<Map.Entry<Value, TypeIdAndValues>> columns = new ArrayList<>();
        for(Value columnPredicate : keys) {
            for (Row r : rows) {
                for (Map.Entry<Value, TypeIdAndValues> entry : r.columns.entrySet()) {


                    if (!columns.stream().anyMatch(p -> p.getKey().stringValue().equalsIgnoreCase(columnPredicate.stringValue())) && entry.getKey().stringValue().equalsIgnoreCase(columnPredicate.stringValue())) {
                        //System.out.println("added to columns in metadata: " + entry.getKey());
                        columns.add(entry);
                    }
                }
            }
        }

        return columns;
    }

    private void enrichMetadataWithLangVariations(Map.Entry<Value, TypeIdAndValues> column, List<Column> listOfColumns, Row row) {
        List<Map.Entry<Value, TypeIdAndValues>> langVariations = new ArrayList<>();
        Set<String> langVariationsAdded = new HashSet<>();
        for(Value literal : column.getValue().values){
            Literal langTag = (Literal) literal;
            //System.out.println("add " + langTag.getLanguage().get());
            langVariationsAdded.add(langTag.getLanguage().get());
        }
        for(String langVar : langVariationsAdded){
            Value newId = column.getValue().id;
            TypeOfValue newType = column.getValue().type;
            TypeIdAndValues newValue = new TypeIdAndValues(newId, newType, new ArrayList<>());
            //System.out.println("langVar " + langVar);
            newValue.values.addAll(column.getValue().values.stream().filter(obj -> obj.toString().substring(obj.toString().length() - 2, obj.toString().length()).contains(langVar))
                    .toList());
            Map.Entry<Value, TypeIdAndValues> newEntry = new AbstractMap.SimpleEntry<Value, TypeIdAndValues>(column.getKey(), newValue);
            langVariations.add(newEntry);

        }
        for(Map.Entry<Value, TypeIdAndValues> lang : langVariations){
            System.out.println("Adding lang variation ");
            Column newColumn = new Column(lang, false);
            newColumn.createColumn(row, isNamespaceTheSameForAllSubjects(rows,lang.getKey()));
            // Check whether the language variation is already in the data
/*
            if(listOfColumns.stream()
                    .filter(item -> item.getLang() != null)
                    .collect(Collectors.toList()).stream()
                    .filter(item -> item.getLang()
                            .contains(newColumn.getLang()))
                    .collect(Collectors.toList()).isEmpty()){
                listOfColumns.add(newColumn);
            }

 */
            listOfColumns.add(newColumn);

        }
    }

    private Column createVirtualTypeColumn(Value id) {
        Value type = rows.get(0).type;
        Value value = rows.get(0).id;
        boolean namespaceIsTheSame = isNamespaceTheSameForAllPrimary(rows);
        Row row = new Row(null, true);
        row.columns = new HashMap<>();
        Column newColumn = new Column( null, namespaceIsTheSame);
        newColumn.addVirtualTypeColumn(type, value, id);
        return newColumn;
    }

    private Column createIdColumnWithType() {
        Value type = rows.get(0).type;
        Value value = rows.get(0).id;
        boolean isRdfType = rows.get(0).isRdfType;
        boolean namespaceIsTheSame = isNamespaceTheSameForAllPrimary(rows);
        Column newColumn = new Column(null, namespaceIsTheSame);
        newColumn.addFirstColumn(type, value, isRdfType, namespaceIsTheSame);
        return newColumn;
    }

    public static boolean isNamespaceTheSameForAllSubjects(List<Row> rows, Value type) {
        List<Row> nonnulls = rows.stream().filter(row -> row.columns.get(type) != null).toList();
        //System.out.println("the same subjects " + type.stringValue());
        if(nonnulls.size() < rows.size()){
            return false;
        }
        final String namespace = ((IRI)nonnulls.get(0).columns.get(type).id).getNamespace();

        return rows.stream().allMatch(row -> {
            /*
            System.out.println("nanespace=" + namespace + " "
                    + ((IRI) (row.columns).get(type).id).getNamespace()
                    + " equals? " + ((IRI) (row.columns).get(type).id).getNamespace().equalsIgnoreCase(namespace));

             */
                    return ((IRI) (row.columns).get(type).id).getNamespace().equalsIgnoreCase(namespace);
        }
                );
    }

    public static boolean isNamespaceTheSameForAllPrimary(List<Row> rows) {

        final String namespace = ((IRI)rows.get(0).id).getNamespace();
        return rows.stream().allMatch(row ->
                ((IRI) (row.id)).getNamespace().equalsIgnoreCase(namespace));
    }

    private String createAboutUrl(Value key0){
        Value type = rows.get(0).type;
        Value value = rows.get(0).id;
        boolean isRdfType = rows.get(0).isRdfType;
        if(value.isBNode()){
            return null;
        } else {
            IRI valueIri = (IRI) value;
            String theNameOfTheColumn;
            if (isRdfType) {
                theNameOfTheColumn = getLastSectionOfIri(type);
                // We dont know how aboutUrl is supposed to look like because we dont know semantic ties to the iris
                this.aboutUrl = valueIri.getNamespace() + "{+" + theNameOfTheColumn + "}";
            } else {

                // We dont know how aboutUrl is supposed to look like because we dont know semantic ties to the iris
                this.aboutUrl = valueIri.getNamespace() + "{+" + "Subjekt" + "}";
                theNameOfTheColumn = "Subjekt";
            }
            return theNameOfTheColumn;
        }



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
