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
import org.jsoup.helper.ValidationException;

import java.util.*;

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
     */
    private String primaryKey;
    /**
     * http://example.org/country/{code}
     */
    private String aboutUrl;
    /**
     * Array of names of columns that should summarize what the row is about (usually the primary key)
     */
    private List<String> rowTitles;

    private List<Value> keys;
    private List<Row> rows;

    public TableSchema() {
        this.columns = new ArrayList<>();
    }




    public TableSchema(List<Value> keys, List<Row> rows) {
        this.keys = keys;
        this.rows = rows;

    }

    public static boolean isTypeTheSameForAllPrimary(List<Row> rows) {
        List<Row> nonnulls = rows.stream().filter(row -> row.type != null).toList();
        // Some rows do not have type - the type is therefore not the same for all of them
        if (nonnulls.size() != rows.size()) {
            return false;
        }
        System.out.println("Deciding whether this type is the same for all rows : " + nonnulls.get(0).type.stringValue());

        Value type = nonnulls.get(0).type;

        return nonnulls.stream().allMatch(row -> {

                    System.out.println("type=" + type + " id="
                            + row.id
                            + " equals? " + row.type.equals(type));
                    return row.type.equals(type);
                }
        );
    }

    public static boolean isNamespaceTheSameForAllSubjects(List<Row> rows, Value type) {
        List<Row> nonnulls = rows.stream().filter(row -> row.columns.get(type) != null).toList();
        System.out.println("the same subjects predicate: " + type.stringValue());

        final String namespace = ((IRI) nonnulls.get(0).columns.get(type).id).getNamespace();

        return nonnulls.stream().allMatch(row -> ((IRI) (row.columns).get(type).id).getNamespace().equalsIgnoreCase(namespace)
        );
    }

    public static boolean isNamespaceTheSameForAllPrimary(List<Row> rows) {

        final String namespace = ((IRI) rows.get(0).id).getNamespace();
        return rows.stream().allMatch(row ->
                ((IRI) (row.id)).getNamespace().equalsIgnoreCase(namespace));
    }

    @JsonIgnore
    public List<Value> getKeys() {
        return keys;
    }

    public void setKeys(List<Value> keys) {
        this.keys = keys;
    }

    @JsonIgnore
    public List<Row> getRows() {
        return rows;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    public void addTableSchemaMetadata() {
        // TODO process the IRIs
        // This only works for the tables that have different types of entities
        this.columns = createColumns(this.rows);
        this.rowTitles = new ArrayList<>();
        if (ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.METADATA_ROWNUMS).equalsIgnoreCase("true")) {
            addRowNumsColumn();
        }
        addRowTitles();
    }

    private void addRowNumsColumn() {
        Column rownumsColumn = new Column(null, false);
        rownumsColumn.setName("rowNum");
        rownumsColumn.setTitles("Row Number");
        rownumsColumn.setDatatype("integer");
        rownumsColumn.setVirtual(true);
        rownumsColumn.setValueUrl("{_row}");

        this.columns.add(rownumsColumn);
    }

    private void addRowTitles() {
        if (ConnectionChecker.checkConnection()) {

            this.columns.forEach(column -> {

                if ((column.getVirtual() == null) || (column.getVirtual() != null && !column.getVirtual())) {
                    Dereferencer dereferencer = new Dereferencer(column.getPropertyUrl());
                    try {
                        this.rowTitles.add(dereferencer.getTitle());
                        System.out.println("dereferencer.getTitle(): " + dereferencer.getTitle());

                    } catch(NullPointerException | ValidationException noElement){
                        System.out.println("Row name in create rowTitles: " + column.getName() + ", column title: " + column.getTitles());
                        this.rowTitles.add(column.getName());
                    }
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
     * <p>
     * The virtual columns are created the last as by specification of
     * https://www.w3.org/TR/2015/REC-tabular-metadata-20151217/#columns - virtual
     *
     * @return List of Column objects annotating individual columns that should be present in the table.
     */
    private List<Column> createColumns(List<Row> rows) {
        List<Column> listOfColumns = new ArrayList<>();
        Column firstColumn = createIdColumnWithType();
        this.primaryKey = firstColumn.getName();
        listOfColumns.add(firstColumn);
        List<Map.Entry<Value, TypeIdAndValues>> columns = getColumnsFromRows();


        for (Map.Entry<Value, TypeIdAndValues> column : columns) {
            boolean namespaceIsTheSame = isNamespaceTheSameForAllRows(rows, column.getKey());
            Column newColumn = new Column(column, namespaceIsTheSame);
            System.out.println("isNamespaceTheSameForAllRows " + namespaceIsTheSame + " for column " + column.getKey());
            boolean subjectNamespaceIsTheSame = isNamespaceTheSameForAllSubjects(rows, column.getKey());
            System.out.println("isNamespaceTheSameForAllSubjects " + subjectNamespaceIsTheSame + " for column " + column.getKey());
            newColumn.createColumn(rows.get(0), subjectNamespaceIsTheSame, this.rows);
            if (newColumn.getLang() != null) {
                System.out.println("Column lang not null " + newColumn.getLang());
                enrichMetadataWithLangVariations(column, listOfColumns, rows);
            } else {
                listOfColumns.add(newColumn);
            }
        }
        if (rows.get(0).isRdfType) {
            listOfColumns.add(createVirtualTypeColumn(rows.get(0).id));
        }


        return listOfColumns;
    }

    private boolean isNamespaceTheSameForAllRows(List<Row> rows, Value columnPredicate) {
        System.out.println("Column predicate = " + columnPredicate.stringValue());
        if (columnPredicate.stringValue().equalsIgnoreCase(rows.get(0).type.stringValue())) {
            return isNamespaceTheSameForAllPrimary(rows);
        }
        List<Row> rowsWithNonEmptyPredicate = rows.stream()
                .filter(row -> row.columns.get(columnPredicate) != null) // Adjust getColumns() as per your implementation
                .toList();
        List<Row> rowsWithColumnPredicateLiteral = rowsWithNonEmptyPredicate.stream()
                .filter(row -> row.columns.get(columnPredicate).type == TypeOfValue.LITERAL) // Adjust getColumns() as per your implementation
                .toList();
        if (!rowsWithNonEmptyPredicate.isEmpty()) {
            System.out.println("rowsWithNonEmptyPredicate.isEmpty()  false");

            if (!rowsWithColumnPredicateLiteral.isEmpty()) {
                System.out.println("!rowsWithColumnPredicateLiteral.isEmpty() " + rowsWithNonEmptyPredicate.size() + " != " + rowsWithColumnPredicateLiteral.size());
                return false;
            }
        } else {
            return false;
        }
        for (Row row : rowsWithNonEmptyPredicate) {

            String namespaceForFirstValueInColUmn = ((IRI) row.columns.get(columnPredicate).values.get(0)).getNamespace();
            for (Value valueOfColumn : row.columns.get(columnPredicate).values) {
                if (valueOfColumn.isLiteral()) {
                    return false;
                }
                if (!((IRI) valueOfColumn).getNamespace().equalsIgnoreCase(namespaceForFirstValueInColUmn)) {
                    return false;
                }
            }
            System.out.println(columnPredicate + "namespace might be the same? values: " + row.columns.get(columnPredicate).values + " id: " + row.columns.get(columnPredicate).id);
        }


        List<Row> hasIRI = rows.stream().filter(row -> {
            if (row.columns.get(columnPredicate) != null) {
                return row.columns.get(columnPredicate).values.get(0).isIRI();
            } else {
                return false;
            }
        }).toList();
        final String namespace = ((IRI) hasIRI.get(0).columns.get(columnPredicate).values.get(0)).getNamespace();
        for (Row row : rows) {
            if (!row.columns.entrySet().stream().filter(column -> column.getKey().stringValue().equalsIgnoreCase(columnPredicate.stringValue())).allMatch(entry ->
                    entry.getValue().values.stream().allMatch(
                            value -> ((IRI) value).getNamespace().equalsIgnoreCase(namespace)))) {
                return false;
            }
        }
        return true;

    }

    private List<Map.Entry<Value, TypeIdAndValues>> getColumnsFromRows() {
        List<Map.Entry<Value, TypeIdAndValues>> columns = new ArrayList<>();
        for (Value columnPredicate : keys) {
            for (Row r : rows) {
                for (Map.Entry<Value, TypeIdAndValues> entry : r.columns.entrySet()) {


                    if (columns.stream().noneMatch(p -> p.getKey().stringValue().equalsIgnoreCase(columnPredicate.stringValue())) && entry.getKey().stringValue().equalsIgnoreCase(columnPredicate.stringValue())) {
                        columns.add(entry);
                    }
                }
            }
        }

        return columns;
    }

    private void enrichMetadataWithLangVariations(Map.Entry<Value, TypeIdAndValues> column, List<Column> listOfColumns, List<Row> rows) {
        System.out.println("enrichMetadataWithLangVariations ");
        Map<String, Map.Entry<Value, TypeIdAndValues>> langVariations = new HashMap<>();
        Set<String> langVariationsAdded = new HashSet<>();
        List<Row> rowsWithLangVariation = rows.stream().filter(row -> row.columns.get(column.getKey()) != null).toList();

        for (Row row : rowsWithLangVariation) {
            TypeIdAndValues rowToExtractLanguages = row.columns.get(column.getKey());
            for (Value literal : rowToExtractLanguages.values) {
                Literal langTag = (Literal) literal;

                if (!langVariationsAdded.contains(langTag.getLanguage().orElse(null))) {
                    langVariationsAdded.add(langTag.getLanguage().orElse(null));
                    System.out.println("add language " + langTag.getLanguage().orElse(null));
                }
            }
        }
        Map<String, Row> rowsByLangOccurance = new HashMap<>();
        for (String langVar : langVariationsAdded) {
            System.out.println("Trying to add langVariation of " + langVar);
            List<Row> newID = rowsWithLangVariation.stream().filter(row ->
                    {
                        for (Value v : row.columns.get(column.getKey()).values) {
                            if (Objects.equals(((Literal) v).getLanguage().orElse(null), langVar)) {
                                System.out.println("Match for langVariation row id: " + row.id + " for language " + langVar + " value: " + ((Literal) v).getLabel() + " lang: " + ((Literal) v).getLanguage());
                                return true;
                            }
                        }


                        System.out.println("NO Match for langVariation row id: " + row.id + " for language " + langVar + " value: ");
                        return false;
                    }
            ).toList();
            System.out.println("newID is empty " + newID.isEmpty());
            rowsByLangOccurance.put(langVar, newID.get(0));
            Value newId = newID.get(0).columns.get(column.getKey()).id;
            TypeOfValue newType = newID.get(0).columns.get(column.getKey()).type;

            TypeIdAndValues newValue = new TypeIdAndValues(newId, newType, new ArrayList<>());
            int LENGTH_OF_LANGVAR = langVar.length();
            newValue.values.addAll(newID.get(0).columns.get(column.getKey()).values.stream().filter(obj -> obj.toString().substring(obj.toString().length() - LENGTH_OF_LANGVAR).contains(langVar))
                    .toList());
            Map.Entry<Value, TypeIdAndValues> newEntry = new AbstractMap.SimpleEntry<>(column.getKey(), newValue);
            newValue.values.forEach(v -> System.out.println("new value from lang Variation " + langVar + " " + v));
            langVariations.put(langVar, newEntry);

        }
        rowsByLangOccurance.forEach((k, v) -> System.out.println(k + ": " + v.id.stringValue()));
        langVariations.forEach((k, v) -> System.out.println(k + ": " + v.getKey().stringValue() + " : " + v.getValue().values.get(0)));
        for (Map.Entry<String, Row> entry : rowsByLangOccurance.entrySet()) {
            System.out.println("Adding lang variation " + langVariations.get(entry.getKey()).getValue().values + " key:" + entry.getKey() + " value:" + entry.getValue());
            boolean namespaceIsTheSame = isNamespaceTheSameForAllRows(rows, langVariations.get(entry.getKey()).getKey());

            Column newColumn = new Column(langVariations.get(entry.getKey()), namespaceIsTheSame);
            newColumn.createColumn(entry.getValue(), isNamespaceTheSameForAllSubjects(rows, langVariations.get(entry.getKey()).getKey()), this.rows);

            listOfColumns.add(newColumn);
        }
    }

    private Column createVirtualTypeColumn(Value id) {
        Value type = rows.get(0).type;
        boolean namespaceIsTheSame = isNamespaceTheSameForAllPrimary(rows);
        Row row = new Row(null, true);
        row.columns = new HashMap<>();
        Column newColumn = new Column(null, namespaceIsTheSame);
        boolean isTypeSame = isTypeTheSameForAllPrimary(rows);
        newColumn.addVirtualTypeColumn(type, id, isTypeSame);
        return newColumn;
    }

    private Column createIdColumnWithType() {
        Value type = rows.get(0).type;
        Value value = rows.get(0).id;
        boolean isRdfType = rows.get(0).isRdfType;
        boolean namespaceIsTheSame = isNamespaceTheSameForAllPrimary(rows);
        Column newColumn = new Column(null, namespaceIsTheSame);
        boolean typeIsTheSame = isTypeTheSameForAllPrimary(rows);
        newColumn.addFirstColumn(type, value, isRdfType, namespaceIsTheSame, typeIsTheSame);
        return newColumn;
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

    public Column getColumnByName(String name) {
        Optional<Column> foundColumn = columns.stream()
                .filter(column -> column.getName().equalsIgnoreCase(name))
                .findFirst(); // returns an Optional
        return foundColumn.orElse(null);
    }
}
