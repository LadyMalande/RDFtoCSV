package com.miklosova.rdftocsvw.metadata_creator.metadata_structure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.miklosova.rdftocsvw.converter.data_structure.Row;
import com.miklosova.rdftocsvw.converter.data_structure.TypeIdAndValues;
import com.miklosova.rdftocsvw.converter.data_structure.TypeOfValue;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.ConnectionChecker;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

import java.util.*;

/**
 * The Table schema from CSVW metadata specification. Contains methods creating the inner structure of the metadata.
 */
@JsonldType("Schema")
public class TableSchema {
    @JsonIgnore
    private AppConfig config;
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

    /**
     * The headers IRI, property URLs
     */
    private List<Value> keys;
    /**
     * Inner representation of rows of the table
     */
    private List<Row> rows;

    /**
     * Instantiates a new Table schema.
     */
    public TableSchema() {
        this.columns = new ArrayList<>();
    }

    public TableSchema(AppConfig config) {
        this.columns = new ArrayList<>();
        this.config = config;
    }


    /**
     * Instantiates a new Table schema.
     *
     * @param keys the keys
     * @param rows the rows
     * @deprecated Use {@link #TableSchema(List, List, AppConfig)} instead
     */
    @Deprecated
    public TableSchema(List<Value> keys, List<Row> rows) {
        this(keys, rows, null);
    }

    /**
     * Instantiates a new Table schema with AppConfig.
     *
     * @param keys the keys
     * @param rows the rows
     * @param config the application configuration
     */
    public TableSchema(List<Value> keys, List<Row> rows, AppConfig config) {
        this.keys = keys;
        this.rows = rows;
        this.config = config;
    }

    /**
     * Is type the same for all primary boolean.
     *
     * @param rows the rows
     * @return the boolean
     */
    public static boolean isTypeTheSameForAllPrimary(List<Row> rows) {
        List<Row> nonnulls = rows.stream().filter(row -> row.type != null).toList();
        // Some rows do not have type - the type is therefore not the same for all of them
        if (nonnulls.size() != rows.size()) {
            return false;
        }
        Value type = nonnulls.get(0).type;

        return nonnulls.stream().allMatch(row -> row.type.equals(type)
        );
    }

    /**
     * Is namespace the same for all subjects boolean.
     *
     * @param rows the rows
     * @param type the type
     * @return the boolean
     */
    public static boolean isNamespaceTheSameForAllSubjects(List<Row> rows, Value type) {
        List<Row> nonnulls = rows.stream().filter(row -> row.columns.get(type) != null).toList();

        final String namespace = ((IRI) nonnulls.get(0).columns.get(type).id).getNamespace();

        return nonnulls.stream().allMatch(row -> ((IRI) (row.columns).get(type).id).getNamespace().equalsIgnoreCase(namespace)
        );
    }

    /**
     * Is namespace the same for all primary boolean.
     *
     * @param rows the rows
     * @return the boolean
     */
    public static boolean isNamespaceTheSameForAllPrimary(List<Row> rows) {

        final String namespace = ((IRI) rows.get(0).id).getNamespace();
        return rows.stream().allMatch(row ->
                ((IRI) (row.id)).getNamespace().equalsIgnoreCase(namespace));
    }

    /**
     * Gets keys.
     *
     * @return the keys
     */
    @JsonIgnore
    public List<Value> getKeys() {
        return keys;
    }

    /**
     * Sets keys.
     *
     * @param keys the keys
     */
    public void setKeys(List<Value> keys) {
        this.keys = keys;
    }

    /**
     * Gets rows.
     *
     * @return the rows
     */
    @JsonIgnore
    public List<Row> getRows() {
        return rows;
    }

    /**
     * Sets rows.
     *
     * @param rows the rows
     */
    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    /**
     * Add table schema metadata.
     * @deprecated Use {@link #addTableSchemaMetadata(AppConfig)} instead
     */
    @Deprecated
    public void addTableSchemaMetadata() {
        addTableSchemaMetadata(null);
    }

    /**
     * Add table schema metadata with AppConfig.
     * @param config the application configuration
     */
    public void addTableSchemaMetadata(AppConfig config) {
        // Use provided config or instance config
        AppConfig effectiveConfig = (config != null) ? config : this.config;
        this.config = effectiveConfig; // Store for use in other methods
        
        // This only works for the tables that have different types of entities
        this.columns = createColumns(this.rows);

        String metadataRownums = (effectiveConfig != null && effectiveConfig.getMetadataRowNums() != null) ? 
            String.valueOf(effectiveConfig.getMetadataRowNums()) : "false";
        if (metadataRownums != null && metadataRownums.equalsIgnoreCase("true")) {
            addRowNumsColumn();
        }
        addRowTitles();
    }

    private void addRowNumsColumn() {
        Column rownumsColumn = new Column(null, false, config);
        rownumsColumn.setName("rowNum");
        rownumsColumn.setTitles("Row Number");
        rownumsColumn.setDatatype("integer");
        rownumsColumn.setVirtual(true);
        rownumsColumn.setValueUrl("{_row}");

        this.columns.add(rownumsColumn);
    }

    /**
     * Add row titles.
     */
    public void addRowTitles() {
        this.rowTitles = new ArrayList<>();
        if (ConnectionChecker.checkConnection()) {

            this.columns.forEach(column -> {

                if ((column.getVirtual() == null) || (column.getVirtual() != null && !column.getVirtual())) {
                    this.rowTitles.add(column.getName());
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
     * <p>
     * The virtual columns are created the last as by specification of
     * <a href="https://www.w3.org/TR/2015/REC-tabular-metadata-20151217/#columns">#columns</a> - virtual
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
            Column newColumn = new Column(column, namespaceIsTheSame, config);
            boolean subjectNamespaceIsTheSame = isNamespaceTheSameForAllSubjects(rows, column.getKey());
            newColumn.createColumn(rows.get(0), subjectNamespaceIsTheSame, this.rows);
            if (newColumn.getLang() != null) {
                enrichMetadataWithLangVariations(column, listOfColumns, rows);
            } else {
                listOfColumns.add(newColumn);
            }
        }
        makeColumnNamesUnique(listOfColumns);

        if (rows.get(0).isRdfType) {
            listOfColumns.add(createVirtualTypeColumn(rows.get(0).id));
        }


        return listOfColumns;
    }

    /**
     * Make column names unique.
     *
     * @param listOfColumns the list of columns
     */
    public static void makeColumnNamesUnique(List<Column> listOfColumns) {
        Map<String, Integer> knownName = new HashMap<>();
        for(Column c : listOfColumns){

            if(knownName.containsKey(c.getName())){
                String newName = c.getName() + "_" + knownName.get(c.getName());
                knownName.put(c.getName(), knownName.get(c.getName() + 1));
                c.setName(newName);
            } else {
                knownName.put(c.getName(), 1);
            }
        }
    }

    private boolean isNamespaceTheSameForAllRows(List<Row> rows, Value columnPredicate) {
        if(rows.get(0).columns.get(columnPredicate) == null){
            return true;
        }
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

            if (!rowsWithColumnPredicateLiteral.isEmpty()) {
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
        }


        List<Row> hasIRI = rows.stream().filter(row -> {
            if (row.columns.get(columnPredicate) != null) {
                return row.columns.get(columnPredicate).values.get(0).isIRI();
            } else {
                return false;
            }
        }).toList();
        final String namespace = ((IRI) hasIRI.get(0).columns.get(columnPredicate).values.get(0)).getNamespace();
        if(((IRI)rows.get(0).columns.get(columnPredicate).values.get(0)).getLocalName().isEmpty()){
            // the local name is empty => there would be empty space in the resulting csv, do not shorten the text
            return false;
        }
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

            rowLoop: for (Row r : rows) {
                for (Map.Entry<Value, TypeIdAndValues> entry : r.columns.entrySet()) {

                    if (columns.stream().noneMatch(p -> p.getKey().stringValue().equalsIgnoreCase(columnPredicate.stringValue())) && entry.getKey().stringValue().equalsIgnoreCase(columnPredicate.stringValue())) {
                        columns.add(entry);
                        continue rowLoop; // Skip to the next iteration of the row loop as there is not going to be anything new
                    }
                }
            }
        }

        return columns;
    }

    private void enrichMetadataWithLangVariations(Map.Entry<Value, TypeIdAndValues> column, List<Column> listOfColumns, List<Row> rows) {
        Map<String, Map.Entry<Value, TypeIdAndValues>> langVariations = new HashMap<>();
        Set<String> langVariationsAdded = new HashSet<>();
        List<Row> rowsWithLangVariation = rows.stream().filter(row -> row.columns.get(column.getKey()) != null).toList();

        for (Row row : rowsWithLangVariation) {
            TypeIdAndValues rowToExtractLanguages = row.columns.get(column.getKey());
            for (Value literal : rowToExtractLanguages.values) {
                Literal langTag = (Literal) literal;

                if(langTag.getLanguage().isPresent()) {
                    langVariationsAdded.add(langTag.getLanguage().get());
                }
            }
        }
        Map<String, Row> rowsByLangOccurance = new HashMap<>();
        for (String langVar : langVariationsAdded) {
            List<Row> newID = rowsWithLangVariation.stream().filter(row ->
                    {
                        for (Value v : row.columns.get(column.getKey()).values) {

                            if(((Literal) v).getLanguage().isPresent()){
                                if(((Literal) v).getLanguage().get().equalsIgnoreCase(langVar)){
                                    return true;
                                }
                            }

                        }


                        return false;
                    }
            ).toList();
            rowsByLangOccurance.put(langVar, newID.get(0));
            Value newId = newID.get(0).columns.get(column.getKey()).id;
            TypeOfValue newType = newID.get(0).columns.get(column.getKey()).type;

            TypeIdAndValues newValue = new TypeIdAndValues(newId, newType, new ArrayList<>());
            int LENGTH_OF_LANGVAR = langVar.length();
            newValue.values.addAll(newID.get(0).columns.get(column.getKey()).values.stream().filter(obj -> obj.toString().substring(obj.toString().length() - LENGTH_OF_LANGVAR).contains(langVar))
                    .toList());
            Map.Entry<Value, TypeIdAndValues> newEntry = new AbstractMap.SimpleEntry<>(column.getKey(), newValue);
            langVariations.put(langVar, newEntry);

        }
        for (Map.Entry<String, Row> entry : rowsByLangOccurance.entrySet()) {
            boolean namespaceIsTheSame = isNamespaceTheSameForAllRows(rows, langVariations.get(entry.getKey()).getKey());

            Column newColumn = new Column(langVariations.get(entry.getKey()), namespaceIsTheSame, config);
            newColumn.createColumn(entry.getValue(), isNamespaceTheSameForAllSubjects(rows, langVariations.get(entry.getKey()).getKey()), this.rows);

            listOfColumns.add(newColumn);
        }
    }

    private Column createVirtualTypeColumn(Value id) {
        Value type = rows.get(0).type;
        boolean namespaceIsTheSame = isNamespaceTheSameForAllPrimary(rows);
        Row row = new Row(null, true);
        row.columns = new HashMap<>();
        Column newColumn = new Column(null, namespaceIsTheSame, config);
        boolean isTypeSame = isTypeTheSameForAllPrimary(rows);
        newColumn.addVirtualTypeColumn(type, id, isTypeSame);
        return newColumn;
    }

    private Column createIdColumnWithType() {
        Value type = rows.get(0).type;
        Value value = rows.get(0).id;
        boolean isRdfType = rows.get(0).isRdfType;
        boolean namespaceIsTheSame = isNamespaceTheSameForAllPrimary(rows);
        Column newColumn = new Column(null, namespaceIsTheSame, config);
        boolean typeIsTheSame = isTypeTheSameForAllPrimary(rows);

        newColumn.addFirstColumn(type, value, isRdfType, namespaceIsTheSame, typeIsTheSame);
        return newColumn;
    }

    /**
     * Gets columns.
     *
     * @return the columns
     */
    public List<Column> getColumns() {
        return columns;
    }

    /**
     * Sets columns.
     *
     * @param columns the columns
     */
    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    /**
     * Gets foreign keys.
     *
     * @return the foreign keys
     */
    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    /**
     * Sets foreign keys.
     *
     * @param foreignKeys the foreign keys
     */
    public void setForeignKeys(List<ForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    /**
     * Gets primary key.
     *
     * @return the primary key
     */
    public String getPrimaryKey() {
        return primaryKey;
    }

    /**
     * Sets primary key.
     *
     * @param primaryKey the primary key
     */
    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    /**
     * Gets about url.
     *
     * @return the about url
     */
    public String getAboutUrl() {
        return aboutUrl;
    }

    /**
     * Sets about url.
     *
     * @param aboutUrl the about url
     */
    public void setAboutUrl(String aboutUrl) {
        this.aboutUrl = aboutUrl;
    }

    /**
     * Gets row titles.
     *
     * @return the row titles
     */
    public List<String> getRowTitles() {
        return rowTitles;
    }

    /**
     * Sets row titles.
     *
     * @param rowTitles the row titles
     */
    public void setRowTitles(List<String> rowTitles) {
        this.rowTitles = rowTitles;
    }

    /**
     * Gets column by name.
     *
     * @param name the name
     * @return the column by name
     */
    public Column getColumnByName(String name) {
        Optional<Column> foundColumn = columns.stream()
                .filter(column -> column.getName().equalsIgnoreCase(name))
                .findFirst(); // returns an Optional
        return foundColumn.orElse(null);
    }
}
