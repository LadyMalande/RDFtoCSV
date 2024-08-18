package com.miklosova.rdftocsvw.convertor;

import com.miklosova.rdftocsvw.support.ConfigurationManager;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.SailConnectionListener;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;

import java.util.ArrayList;
import java.util.Map;

import static com.miklosova.rdftocsvw.support.StandardModeCSVWIris.*;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

public class StandardModeConverter implements IQueryParser{
    ArrayList<Value> roots;
    ArrayList<Row> rows;
    ArrayList<Value> keys;
    ArrayList<ArrayList<Row>> allRows;
    ArrayList<ArrayList<Value>> allKeys;

    public Map<String, Integer> mapOfPredicatesAndTheirNumbers;
    public Map<Value, Integer> mapOfTypesAndTheirNumbers;
    String delimiter;
    String CSVFileTOWriteTo;
    Repository db;

    public StandardModeConverter(Repository db) {
        this.keys = new ArrayList<>();
        this.db = db;
    }
    @Override
    public PrefinishedOutput convertWithQuery(RepositoryConnection rc) {
            PrefinishedOutput<RowsAndKeys> gen = new PrefinishedOutput<RowsAndKeys>(new RowsAndKeys.RowsAndKeysFactory());
            ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, String.valueOf(true));
            System.out.println("CONVERSION_HAS_RDF_TYPES at the beginning " + ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES));

            allRows = new ArrayList<>();
            allKeys = new ArrayList<>();

            try (SailRepositoryConnection conn = (SailRepositoryConnection) db.getConnection()) {

                // Open a connection to the database
                NotifyingSailConnection sailConn = (NotifyingSailConnection) conn.getSailConnection();
                sailConn.addConnectionListener(new SailConnectionListener() {

                    @Override
                    public void statementRemoved(Statement removed) {
                        //System.out.println("removed: " + removed);
                    }

                    @Override
                    public void statementAdded(Statement added) {
                        System.out.println("added: " + added);
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

                rows.forEach(k -> System.out.println("Row: " + k.id.stringValue() + " " + k.columns.entrySet()));
            }

        for(int i = 0; i < allRows.size(); i++){
            //System.out.println("Adding rowAndKey #: " + i);
            gen.prefinishedOutput.rowsAndKeys.add(new RowAndKey(allKeys.get(i),allRows.get(i)));
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
        TupleQuery query = conn.prepareTupleQuery(getQueryForObjectBySubjectAndPredicate(tableIRI, CSVW_row));

        ArrayList<Value> rowIRIs = new ArrayList<>();
        
        try (TupleQueryResult result = query.evaluate()) {
            for (BindingSet solution : result) {

                Value rowIRI = solution.getValue("o");
                rowIRIs.add(rowIRI);
                
            }
        }
        sortRowIRIsByRownums(rowIRIs);
        for(Value rowIRI : rowIRIs) {
            TupleQuery queryForDescribes = conn.prepareTupleQuery(getQueryForObjectBySubjectAndPredicate(rowIRI, CSVW_describes));
            try (TupleQueryResult result = queryForDescribes.evaluate()) {
                for (BindingSet solution : result) {
                    Value describesItemIRI = solution.getValue("o");
                    TupleQuery queryForColumnValues = conn.prepareTupleQuery(getQueryForObjectAndPredBySubject(describesItemIRI));
                    try (TupleQueryResult resultForColumns = queryForColumnValues.evaluate()) {
                        Row newRow = null;
                        boolean firstEntry = true;
                        for (BindingSet solutionForColumns : resultForColumns) {
                            Value columnKey = solutionForColumns.getValue("p");
                            Value columnValue = solutionForColumns.getValue("o");
                            if(firstEntry){
                                System.out.println("id for Row = " + rowIRI.stringValue());
                                newRow = new Row(rowIRI, columnKey, false);
                                firstEntry = false;
                            }
                            if(!keys.contains(columnKey)){
                                keys.add(columnKey);
                            }
                            ArrayList<Value> values = new ArrayList<>();
                            values.add(columnValue);

                            if(columnValue.isIRI()){
                                newRow.columns.put(columnKey, new TypeIdAndValues(rowIRI, TypeOfValue.IRI, values));

                            } else if(columnValue.isLiteral()) {
                                newRow.columns.put(columnKey, new TypeIdAndValues(rowIRI, TypeOfValue.LITERAL, values));

                            } else {
                                throw new UnsupportedOperationException("BNodes should not exist in this phase");
                            }
                        }
                        rows.add(newRow);
                    }
                }
            }
        }
    }

    private String getQueryForObjectAndPredBySubject(Value subject) {
        SelectQuery selectQuery = Queries.SELECT();

        Variable o = SparqlBuilder.var("o"), p = SparqlBuilder.var("p");
        Iri subjectIRI = iri(subject.stringValue());
        selectQuery.select(o, p).where(subjectIRI.has(p, o));
        System.out.println("getQueryForObjectBySubjectAndPredicate query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    private void sortRowIRIsByRownums(ArrayList<Value> rowIRIs) {
    }

    private String getQueryForObjectBySubjectAndPredicate(Value s, String predicate) {
        SelectQuery selectQuery = Queries.SELECT();

        Variable o = SparqlBuilder.var("o");
        Iri subjectIRI = iri(s.stringValue());
        Iri predicateIRI = iri(predicate);
        selectQuery.select(o).where(subjectIRI.has(predicateIRI, o));
        System.out.println("getQueryForObjectBySubjectAndPredicate query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    private String getQueryForSubjectByObject(String object){
        SelectQuery selectQuery = Queries.SELECT();

        SimpleValueFactory rdf = SimpleValueFactory.getInstance();
        Variable s = SparqlBuilder.var("s");
        Iri objectIRI = iri(object);
        selectQuery.select(s).where(s.isA(objectIRI));
        System.out.println("getQueryForSubjectByObject query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }
}

