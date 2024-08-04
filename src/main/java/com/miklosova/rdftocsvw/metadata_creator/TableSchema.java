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
import org.jruby.RubyProcess;
import org.jsoup.helper.ValidationException;

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
                System.out.println("col: " + " " + column.getName() + ", column title: " + column.getTitles() + " column.virtual: " + column.getVirtual());

                if ((column.getVirtual() == null) || (column.getVirtual() != null && !column.getVirtual())) {
                    System.out.println("(column.getVirtual() == null)");

                    System.out.println("(column.getSuppressOutput() != null)");
                    Dereferencer dereferencer = new Dereferencer(column.getPropertyUrl());
                    //System.out.println("Dereference = " + column.getName());
                    /*
                    try {
                        this.rowTitles.add(dereferencer.getTitle());
                        System.out.println("dereferencer.getTitle(): " + dereferencer.getTitle());

                    } catch(NullPointerException | ValidationException noElement){
                        System.out.println("Row name in create rowTitles: " + column.getName() + ", column title: " + column.getTitles());
                        this.rowTitles.add(column.getName());
                    }

                     */
                    this.rowTitles.add(column.getName());
                }
            });

        } else {
            this.columns.forEach(column -> {
                if (column.getVirtual() == null || (column.getVirtual() != null && !column.getVirtual())) {
                    System.out.println("column name: " + column.getName() + " column title: " + column.getTitles());
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
                boolean namespaceIsTheSame = isNamespaceTheSameForAllRows(rows, column.getKey(), null);
                Column newColumn = new Column(column, namespaceIsTheSame);
                System.out.println("isNamespaceTheSameForAllRows " + namespaceIsTheSame + " for column " + column.getKey());
                boolean subjectNamespaceIsTheSame = isNamespaceTheSameForAllSubjects(rows, column.getKey(), null);
                System.out.println("isNamespaceTheSameForAllSubjects " + subjectNamespaceIsTheSame + " for column " + column.getKey());
                newColumn.createColumn(rows.get(0), subjectNamespaceIsTheSame);
                if (newColumn.getLang() != null) {
                    System.out.println("Column lang not null " + newColumn.getLang());
                    enrichMetadataWithLangVariations(column, listOfColumns, rows);
                } else {
                    listOfColumns.add(newColumn);
                }


        }
        if(rows.get(0).isRdfType){
            listOfColumns.add(createVirtualTypeColumn(rows.get(0).id));
        }


        return listOfColumns;
    }

    private boolean isNamespaceTheSameForAllRows(List<Row> rows, Value columnPredicate, String lang) {
        System.out.println("Column predicate = " + columnPredicate.stringValue());
        if(columnPredicate.stringValue().equalsIgnoreCase(rows.get(0).type.stringValue())){
            return isNamespaceTheSameForAllPrimary(rows);
        }
        //System.out.println("predicateOfColumn ="+ columnPredicate.stringValue());
        if(rows.get(0).columns != null) {
            System.out.println("rows.get(0).columns != null  true");
            if(rows.get(0).columns.get(columnPredicate) != null) {
                System.out.println("rows.get(0).columns.get(columnPredicate) != null true");
                if (rows.get(0).columns.get(columnPredicate).type == TypeOfValue.LITERAL) {
                    System.out.println("rows.get(0).columns.get(columnPredicate).type = " + rows.get(0).columns.get(columnPredicate).type);
                    return false;
                }
            } else{
                return false;
            }
        } else {
            return false;
        }
        rows.forEach(row -> row.columns.entrySet().forEach(column -> {if(column.getKey().equals(columnPredicate)){System.out.println(column.getKey() + " values: " + column.getValue().values + " id: " + column.getValue().id.toString());}}));

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

    private void enrichMetadataWithLangVariations(Map.Entry<Value, TypeIdAndValues> column, List<Column> listOfColumns, List<Row> rows) {
        Map<String,Map.Entry<Value, TypeIdAndValues>> langVariations = new HashMap<>();
        Set<String> langVariationsAdded = new HashSet<>();
        List<Row> rowsWithLangVariation = rows.stream().filter(row -> row.columns.get(column.getKey()) != null).toList();

        for(Row row : rowsWithLangVariation){
            TypeIdAndValues rowToExtractLanguages = row.columns.get(column.getKey());
            for(Value literal : rowToExtractLanguages.values){
                Literal langTag = (Literal) literal;

                if(!langVariationsAdded.contains(langTag.getLanguage().get())){
                    langVariationsAdded.add(langTag.getLanguage().get());
                    System.out.println("add language " + langTag.getLanguage().get());
                }
            }
        }
        Map<String, Row> rowsByLangOccurance = new HashMap<>();
        for(String langVar : langVariationsAdded){
            System.out.println("Trying to add langVariation of " + langVar);
            List<Row> newID = rowsWithLangVariation.stream().filter(row ->
            {
                for(Value v : row.columns.get(column.getKey()).values){
                    //System.out.println("Match for langVariation row id: " + row.id + " for language " + langVar + " value: " + ((Literal)v).getLabel() + " lang: " + ((Literal)v).getLanguage());
                    if( ((Literal)v).getLanguage().get().equals(langVar)){
                        System.out.println("Match for langVariation row id: " + row.id + " for language " + langVar  + " value: " + ((Literal)v).getLabel() + " lang: " + ((Literal)v).getLanguage());
                        return true;
                    }
                }


                System.out.println("NO Match for langVariation row id: " + row.id + " for language " + langVar + " value: ") ;
                return false;
            }
            ).toList();
            System.out.println("newID is empty " + newID.isEmpty());
            rowsByLangOccurance.put(langVar, newID.get(0));
            Value newId = newID.get(0).columns.get(column.getKey()).id;
            TypeOfValue newType = newID.get(0).columns.get(column.getKey()).type;
            //Value newId = column.getValue().id;
            //TypeOfValue newType = column.getValue().type;
            TypeIdAndValues newValue = new TypeIdAndValues(newId, newType, new ArrayList<>());
            //System.out.println("langVar " + langVar);
            newValue.values.addAll(newID.get(0).columns.get(column.getKey()).values.stream().filter(obj -> obj.toString().substring(obj.toString().length() - 2, obj.toString().length()).contains(langVar))
                    .toList());
            Map.Entry<Value, TypeIdAndValues> newEntry = new AbstractMap.SimpleEntry<Value, TypeIdAndValues>(column.getKey(), newValue);
            newValue.values.forEach(v -> System.out.println("new value from lang Variation " + langVar + " " + v));
            langVariations.put(langVar,newEntry);

        }
        rowsByLangOccurance.forEach((k,v) -> System.out.println(k + ": " + v));
        langVariations.forEach((k,v) -> System.out.println(k + ": " + v));
        for(Map.Entry<String, Row> entry : rowsByLangOccurance.entrySet()){
            System.out.println("Adding lang variation " + langVariations.get(entry.getKey()).getValue().values + " key:" + entry.getKey() + " value:" + entry.getValue());
            boolean namespaceIsTheSame = isNamespaceTheSameForAllRows(rows, langVariations.get(entry.getKey()).getKey(),null);

            Column newColumn = new Column(langVariations.get(entry.getKey()), namespaceIsTheSame);
            newColumn.createColumn(entry.getValue(), isNamespaceTheSameForAllSubjects(rows,langVariations.get(entry.getKey()).getKey(),null));

            listOfColumns.add(newColumn);
        }
        /*
        for(Map.Entry<Value, TypeIdAndValues> lang : langVariations){

            System.out.println("Adding lang variation " + lang.getValue().values);
            boolean namespaceIsTheSame = isNamespaceTheSameForAllRows(rows, lang.getKey());
            Column newColumn = new Column(lang, namespaceIsTheSame);
            rowWithGivenLang =
            newColumn.createColumn(lang, isNamespaceTheSameForAllSubjects(rows,lang.getKey()));
            // Check whether the language variation is already in the data

            listOfColumns.add(newColumn);

        }
        */
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

    public static boolean isNamespaceTheSameForAllSubjects(List<Row> rows, Value type, String lang) {
        List<Row> nonnulls = rows.stream().filter(row -> row.columns.get(type) != null).toList();
        System.out.println("the same subjects predicate: " + type.stringValue());

        final String namespace = ((IRI)nonnulls.get(0).columns.get(type).id).getNamespace();

        return nonnulls.stream().allMatch(row -> {

            System.out.println("namespace=" + namespace + " "
                    + ((IRI) (row.columns).get(type).id).getNamespace()
                    + " from " + (row.columns).get(type).id.toString() + " equals? " + ((IRI) (row.columns).get(type).id).getNamespace().equalsIgnoreCase(namespace));


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
            if(value.isIRI() && value.stringValue().startsWith("https://blank_Nodes_IRI")){
                this.aboutUrl = null;
                return null;
            }
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
