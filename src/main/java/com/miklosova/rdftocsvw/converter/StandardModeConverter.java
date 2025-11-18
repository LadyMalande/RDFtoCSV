package com.miklosova.rdftocsvw.converter;

import com.miklosova.rdftocsvw.converter.data_structure.*;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.SailConnectionListener;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.miklosova.rdftocsvw.support.StandardModeCsvwIris.*;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

/**
 * The type Standard mode converter.
 * Works for the RDFs that are created by CSV to RDF algorithm in Standard mode. The structure is very specific, so parsing it with regular Basic query or splitFilesQuery does not yield good results.
 */
public class StandardModeConverter implements IQueryParser {
    /**
     * The Roots.
     */
    ArrayList<Value> roots;
    /**
     * The Rows.
     */
    ArrayList<Row> rows;
    /**
     * The Keys.
     */
    ArrayList<Value> keys;
    /**
     * The All rows.
     */
    ArrayList<ArrayList<Row>> allRows;
    /**
     * The All keys.
     */
    ArrayList<ArrayList<Value>> allKeys;
    /**
     * The Db.
     */
    Repository db;

    /**
     * The AppConfig instance.
     */
    private AppConfig config;

    /**
     * Instantiates a new Standard mode converter.
     *
     * @param db the db
     * @deprecated Use {@link #StandardModeConverter(Repository, AppConfig)} instead
     */
    @Deprecated
    public StandardModeConverter(Repository db) {
        this.keys = new ArrayList<>();
        this.db = db;
        this.config = null;
    }

    /**
     * Instantiates a new Standard mode converter with AppConfig.
     *
     * @param db the db
     * @param config the application configuration
     */
    public StandardModeConverter(Repository db, AppConfig config) {
        this.keys = new ArrayList<>();
        this.db = db;
        this.config = config;
    }

    private static String getQueryForObjectBySubjectAndPredicate(Value s, String predicate) {
        SelectQuery selectQuery = Queries.SELECT();

        Variable o = SparqlBuilder.var("o");
        Iri subjectIRI = iri(s.stringValue());
        Iri predicateIRI = iri(predicate);
        selectQuery.select(o).where(subjectIRI.has(predicateIRI, o));
        return selectQuery.getQueryString();
    }

    @Override
    public PrefinishedOutput<RowsAndKeys> convertWithQuery(RepositoryConnection rc) {
        PrefinishedOutput<RowsAndKeys> gen = new PrefinishedOutput<>(new RowsAndKeys.RowsAndKeysFactory().factory());
        // For backward compatibility, also save to ConfigurationManager
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, String.valueOf(true));

        allRows = new ArrayList<>();
        allKeys = new ArrayList<>();

        try (SailRepositoryConnection conn = (SailRepositoryConnection) db.getConnection()) {

            // Open a connection to the database
            NotifyingSailConnection sailConn = (NotifyingSailConnection) conn.getSailConnection();
            sailConn.addConnectionListener(new SailConnectionListener() {

                @Override
                public void statementRemoved(Statement removed) {

                }

                @Override
                public void statementAdded(Statement added) {
                }
            });


            TupleQuery query = conn.prepareTupleQuery(getQueryForSubjectByObject(CSVW_TableGroup));

            try (TupleQueryResult result = query.evaluate()) {
                if (result == null) {
                    return null;
                }
                roots = new ArrayList<>();
                for (BindingSet solution : result) {
                    getTables(conn, solution.getValue("s"));
                }
            }
        }

        for (int i = 0; i < allRows.size(); i++) {
            gen.getPrefinishedOutput().getRowsAndKeys().add(new RowAndKey(allKeys.get(i), allRows.get(i)));
        }

        return gen;
    }

    private void getTables(SailRepositoryConnection conn, Value s) {
        TupleQuery query = conn.prepareTupleQuery(getQueryForObjectBySubjectAndPredicate(s, CSVW_table));

        try (TupleQueryResult result = query.evaluate()) {
            for (BindingSet solution : result) {
                rows = new ArrayList<>();
                keys = new ArrayList<>();
                Value tableIRI = solution.getValue("o");
                makeTable(conn, tableIRI);

                allRows.add(rows);
                allKeys.add(keys);
            }
        }
    }

    private void makeTable(SailRepositoryConnection conn, Value tableIRI) {
        TupleQuery queryForUrl = conn.prepareTupleQuery(getQueryForObjectBySubjectAndPredicate(tableIRI, CSVW_url));

        try (TupleQueryResult resultForUrl = queryForUrl.evaluate()) {
            for (BindingSet solution : resultForUrl) {

                Value fileIRI = solution.getValue("o");
                String fileName = extractFileName(fileIRI);
                // For backward compatibility, also save to ConfigurationManager
                ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES, fileName);
                ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_FILENAME, fileName);
                String outputFilePath = (config != null) ? config.getOutputFilePath() : 
                    ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_FILE_PATH);
                ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME,
                        outputFilePath + fileName + "-metadata.json");
            }
        }

        // Get row IRIs to process later
        ArrayList<Value> rowIRIs = getRowIRIs(conn, tableIRI);

        Map<Value, Integer> rowNumsByRowIrisMap = buildRownumMap(conn, rowIRIs);
        if (!rowNumsByRowIrisMap.isEmpty()) {
            // For backward compatibility, also save to ConfigurationManager
            ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.METADATA_ROWNUMS, "true");
        }
        for (Value rowIRI : rowIRIs) {
            addNewRow(conn, rowIRI);
        }
    }

    private void addNewRow(SailRepositoryConnection conn, Value rowIRI) {
        // Get described columns with their values
        TupleQuery queryForDescribes = conn.prepareTupleQuery(getQueryForObjectBySubjectAndPredicate(rowIRI, CSVW_describes));
        try (TupleQueryResult result = queryForDescribes.evaluate()) {
            Row newRow;
            Value columnKey;
            newRow = new Row(rowIRI, null, false);
            for (BindingSet solution : result) {
                Value describesItemIRI = solution.getValue("o");
                TupleQuery queryForColumnValues = conn.prepareTupleQuery(getQueryForObjectAndPredBySubject(describesItemIRI));

                try (TupleQueryResult resultForColumns = queryForColumnValues.evaluate()) {

                    boolean firstEntry = true;
                    for (BindingSet solutionForColumns : resultForColumns) {
                        newRow.type = solutionForColumns.getValue("p");
                        columnKey = solutionForColumns.getValue("p");
                        Value columnValue = solutionForColumns.getValue("o");
                        if (firstEntry) {

                            firstEntry = false;
                        }
                        if (!keys.contains(columnKey)) {
                            keys.add(columnKey);
                        }
                        ArrayList<Value> values = new ArrayList<>();
                        values.add(columnValue);

                        if (columnValue.isIRI()) {
                            newRow.columns.put(columnKey, new TypeIdAndValues(rowIRI, TypeOfValue.IRI, values));

                        } else if (columnValue.isLiteral()) {
                            newRow.columns.put(columnKey, new TypeIdAndValues(rowIRI, TypeOfValue.LITERAL, values));

                        } else {
                            throw new UnsupportedOperationException("BNodes should not exist in this phase");
                        }
                    }
                }
            }
            rows.add(newRow);
        }
    }

    private ArrayList<Value> getRowIRIs(SailRepositoryConnection conn, Value tableIRI) {
        ArrayList<Value> rowIRIs = new ArrayList<>();
        // Tuple query for finding the IRI of a row
        TupleQuery query = conn.prepareTupleQuery(getQueryForObjectBySubjectAndPredicate(tableIRI, CSVW_row));

        try (TupleQueryResult result = query.evaluate()) {
            for (BindingSet solution : result) {
                // Add row IRI to process later
                Value rowIRI = solution.getValue("o");
                rowIRIs.add(rowIRI);

            }
        }
        return rowIRIs;
    }

    private String extractFileName(Value fileValue) {
        IRI fileIri = (IRI) fileValue;
        String[] splitByDoubleSlash = fileIri.toString().split("//");
        return splitByDoubleSlash[1].split("csv")[0] + "csv";
    }

    private Map<Value, Integer> buildRownumMap(SailRepositoryConnection conn, ArrayList<Value> rowIRIs) {
        Map<Value, Integer> map = new HashMap<>();

        for (Value rowIri : rowIRIs) {
            TupleQuery queryForColumnValues = conn.prepareTupleQuery(getQueryForObjectBySubjectAndPredicate(rowIri, CSVW_rownum));
            try (TupleQueryResult resultForColumns = queryForColumnValues.evaluate()) {
                for (BindingSet solutionForColumns : resultForColumns) {

                    Literal rownumLiteral = (Literal) solutionForColumns.getValue("o");
                    Integer rownum = Integer.parseInt(rownumLiteral.getLabel());
                    map.put(rowIri, rownum);
                }
            }
        }

        return map;
    }

    private String getQueryForObjectAndPredBySubject(Value subject) {
        SelectQuery selectQuery = Queries.SELECT();

        Variable o = SparqlBuilder.var("o"), p = SparqlBuilder.var("p");
        Iri subjectIRI = iri(subject.stringValue());
        selectQuery.select(o, p).where(subjectIRI.has(p, o));
        return selectQuery.getQueryString();
    }

    private String getQueryForSubjectByObject(String object) {
        SelectQuery selectQuery = Queries.SELECT();

        Variable s = SparqlBuilder.var("s");
        Iri objectIRI = iri(object);
        selectQuery.select(s).where(s.isA(objectIRI));
        return selectQuery.getQueryString();
    }
}

