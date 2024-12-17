package com.miklosova.rdftocsvw.convertor;

import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.ConverterHelper;
import lombok.extern.java.Log;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.query.*;
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

import java.util.*;

import static com.miklosova.rdftocsvw.support.StandardModeCSVWIris.CSVW_TableGroup;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

@Log
public class BasicQueryConverter extends ConverterHelper implements IQueryParser {

    ArrayList<Value> roots;
    ArrayList<Row> rows;
    ArrayList<Value> keys;
    String delimiter;
    String CSVFileTOWriteTo;
    Repository db;

    RepositoryConnection rc;

    public BasicQueryConverter(Repository db) {
        this.keys = new ArrayList<>();
        this.db = db;
    }

    @Override
    public PrefinishedOutput convertWithQuery(RepositoryConnection rc) {
        this.rc = rc;
        loadConfiguration();
        changeBNodesForIri(rc);
        deleteBlankNodes(rc);
        rows = new ArrayList<>();
        PrefinishedOutput queryResult;
        String query = getCSVTableQueryForModel(true);
        try {
            //System.out.println("Query at top level in convertWithQuery\n" + query);
            queryResult = queryRDFModel(query, true);

        } catch (IndexOutOfBoundsException ex) {
            query = getCSVTableQueryForModel(false);
            queryResult = queryRDFModel(query, false);
        }
        return queryResult;
    }

    private void deleteBlankNodes(RepositoryConnection rc) {
        String del = "DELETE {?s ?p ?o .} WHERE { ?s ?p ?o . FILTER (isBlank(?s) || isBlank(?o))}";
        Update deleteQuery = rc.prepareUpdate(del);
        deleteQuery.execute();
    }

    private void loadConfiguration() {

        delimiter = ConfigurationManager.getVariableFromConfigFile("input.delimiter");
        System.out.println("READ delimiter from input.delimiter to: " + delimiter);

        CSVFileTOWriteTo = ConfigurationManager.getVariableFromConfigFile("input.outputFileName");
        System.out.println("READ delimiter from input.CSVFileTOWriteTo to: " + CSVFileTOWriteTo);
    }

    private String getCSVTableQueryForModel(boolean askForTypes) {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        // Create the query to get all data in CSV format
        SelectQuery selectQuery = Queries.SELECT();

        Variable o = SparqlBuilder.var("o"), s = SparqlBuilder.var("s"),
                s_in = SparqlBuilder.var("s_in"), p_in = SparqlBuilder.var("p_in"),
                p = SparqlBuilder.var("p");

        if (askForTypes) {
            selectQuery.prefix(skos).select(s, o).where(s.isA(o).filterNotExists(s_in.has(p_in, s)));
        } else {
            selectQuery.prefix(skos).select(s, o).where(s.has(p, o).filterNotExists(s_in.has(p_in, s)));
        }

        //System.out.println("getCSVTableQueryForModel query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    public String getQueryForSubstituteRoots(boolean askForTypes) {
        // Create the query to get all data in CSV format
        SelectQuery selectQuery = Queries.SELECT();

        Variable o = SparqlBuilder.var("o"), s = SparqlBuilder.var("s"),
                p = SparqlBuilder.var("p");

        if (askForTypes) {
            selectQuery.select(s, o).where(s.isA(o));
        } else {
            selectQuery.select(s, o).where(s.has(p, o));
        }

        //System.out.println("getQueryForSubstituteRoots query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    private PrefinishedOutput queryRDFModel(String queryString, boolean askForTypes) {

        PrefinishedOutput<RowAndKey> gen = new PrefinishedOutput<>(new RowAndKey.RowAndKeyFactory());
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, String.valueOf(askForTypes));
        //System.out.println("CONVERSION_HAS_RDF_TYPES at the beginning " + ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES));
        // Query the data and pass the result as String

        // Query in rdf4j
        // Create a new Repository.

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

            while (!conn.isEmpty()) {

                TupleQuery query = conn.prepareTupleQuery(queryString);
                // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
                try (TupleQueryResult result = query.evaluate()) {
                    // we just iterate over all solutions in the result...
                    if (result == null) {
                        return null;
                    }
                    roots = new ArrayList<>();
                    //System.out.println(result.stream().count());
                    for (BindingSet solution : result) {
                        // ... and print out the value of the variable binding for ?s and ?n
                        //System.out.println("?subject = " + solution.getValue("s") + " is a o=" + solution.getValue("o") + " added to roots");
                        System.out.println("Table Group=" + CSVW_TableGroup);
                        if (solution.getValue("o").stringValue().equalsIgnoreCase(CSVW_TableGroup)) {
                            //System.out.println("Table Group, engaging in StandardModeConverter");
                            StandardModeConverter smc = new StandardModeConverter(db);
                            return smc.convertWithQuery(this.rc);
                        }
                        if (!roots.contains(solution.getValue("s"))) {
                            roots.add(solution.getValue("s"));
                            //System.out.println("Root: " + solution.getValue("s"));
                        }
                        Row newRow = new Row(solution.getValue("s"), solution.getValue("o"), askForTypes);
                        queryForSubjects(conn, newRow, solution.getValue("s"), solution.getValue("s"),  0);
                        //System.out.println();
                        //System.out.println("askForTypes=" + askForTypes);
                        /*
                        System.out.println("new Row is: " + newRow.id.stringValue() +
                                " type: " + newRow.type.stringValue() +
                                " isRdfType=" + newRow.isRdfType +
                                " newRow columns " + newRow.columns.entrySet());

                         */
                        if (rows.stream().anyMatch(row -> row.id.equals(newRow.id))) {
                            // a row with the same id is already present in the data, don't create new one
                        } else {
                            rows.add(newRow);
                        }

                    }
                    //System.out.println("After loop with results of query " + queryString);
                    //countDominantPredicates(conn, roots);
                    if (roots.isEmpty()) {
                        //System.out.println("Roots is empty");
                        // NO ROOTS found, find different supplement roots
                        TupleQuery queryForSubstituteRoots = conn.prepareTupleQuery(getQueryForSubstituteRoots(askForTypes));
                        try (TupleQueryResult resultForSubstituteRoots = queryForSubstituteRoots.evaluate()) {
                            if (resultForSubstituteRoots.hasNext()) {
                                for (BindingSet solution : resultForSubstituteRoots) {
                                    // ... and print out the value of the variable binding for ?s and ?n
                                    //System.out.println("?subject = " + solution.getValue("s") + " ?predicate = " + solution.getValue("p") + " is a o=" + solution.getValue("o"));
                                    if (!roots.contains(solution.getValue("s"))) {
                                        //System.out.println("Adding to roots in substituteRoots ?subject = " + solution.getValue("s") + " ?predicate = " + solution.getValue("p") + " is a o=" + solution.getValue("o"));
                                        roots.add(solution.getValue("s"));
                                    }
                                }
                            } else {
                                throw new IndexOutOfBoundsException();

                            }

                        }
                    }
                }
                queryString = getCSVTableQueryForModel(true);
            }
            //rows.forEach(k -> System.out.println("Row: " + k.id.stringValue() + " " + k.columns.entrySet()));

            ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, String.valueOf(askForTypes));
        }

        // Verify the output in console
        gen.prefinishedOutput.rows.addAll(rows);
        gen.prefinishedOutput.keys.addAll(keys);

        return gen;
    }

    private void queryForSubjects(RepositoryConnection conn, Row newRow, Value root, Value subject, int level) {
        String queryToGetAllPredicatesAndObjects = getQueryToGetObjectsForRoot(subject);
        TupleQuery query = conn.prepareTupleQuery(queryToGetAllPredicatesAndObjects);
        assert root != null;
        try (TupleQueryResult result = query.evaluate()) {


            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                // We found a root row
                //System.out.println("newRow.columns.containsKey(solution.getBinding(\"p\").getValue() =  " + newRow.columns.containsKey(solution.getBinding("p").getValue()));

                Value keyForColumnsMap = solution.getBinding("p").getValue();
                if (level != 0) {
                    ValueFactory valueFactory = SimpleValueFactory.getInstance();

                    // Create a new IRI

                    String newValueForMap = solution.getBinding("p").getValue().stringValue() + "_MULTILEVEL_";// + ((IRI) subject).getLocalName();
                    //System.out.println("newValueForMap" + newValueForMap);
                    keyForColumnsMap = valueFactory.createIRI(newValueForMap);
                }
                if (newRow.columns.containsKey(keyForColumnsMap) &&
                        newRow.columns.get(keyForColumnsMap).type == TypeOfValue.IRI &&
                        solution.getBinding("o").getValue().isIRI()) {
                    List<Value> oldStringValue = newRow.columns.get(keyForColumnsMap).values;
                    oldStringValue.add(solution.getBinding("o").getValue());
                    TypeIdAndValues oldTypeIdAndValues = newRow.columns.get(keyForColumnsMap);
                    oldTypeIdAndValues.values = oldStringValue;
                    newRow.columns.put(keyForColumnsMap, oldTypeIdAndValues);
                    //System.out.println("IRI already in the map o Added to oldRow.columns predicate = " + solution.getBinding("p").getValue() + "  Value of = " + solution.getBinding("o").getValue());

                } else if (newRow.columns.containsKey(keyForColumnsMap) &&
                        newRow.columns.get(keyForColumnsMap).type == TypeOfValue.LITERAL &&
                        solution.getBinding("o").getValue().isLiteral()) {
                    List<Value> oldStringValue = newRow.columns.get(keyForColumnsMap).values;
                    oldStringValue.add(solution.getBinding("o").getValue());
                    TypeIdAndValues oldTypeIdAndValues = newRow.columns.get(keyForColumnsMap);
                    oldTypeIdAndValues.values = oldStringValue;
                    newRow.columns.put(keyForColumnsMap, oldTypeIdAndValues);
                    //System.out.println("LITERAL already in the map o Added to oldRow.columns predicate = " + solution.getBinding("p").getValue() + "  Value of = " + solution.getBinding("o").getValue());

                } else { // There is no such key (column) in the map
                    //if(!solution.getValue("p").toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")){
                    TypeOfValue newType = (solution.getBinding("o").getValue().isIRI()) ? TypeOfValue.IRI :
                            (solution.getBinding("o").getValue().isBNode()) ? TypeOfValue.BNODE : TypeOfValue.LITERAL;
                    newRow.columns.put(keyForColumnsMap, new TypeIdAndValues(subject, newType,
                            new ArrayList<>(List.of(solution.getBinding("o").getValue()))));
                    //System.out.println("There is no such key in columns map o Added to newRow.columns predicate = " + keyForColumnsMap + "  Value of = " + solution.getBinding("o").getValue());
                    //}
                }

                if (!keys.contains(keyForColumnsMap)) {
                    //keys.forEach(k -> System.out.print("key: " + k));
                    keys.add(keyForColumnsMap);
                }

                if (solution.getValue("o") != null && solution.getValue("o").isIRI()) {
                    //System.out.println("Querying with queryForSubjects for o=" + solution.getValue("o").stringValue());
                    queryForSubjects(conn, newRow, root, solution.getValue("o"),level + 1);
                }

                // Delete the triple from the storage
                Resource subjectToDelete = Values.iri(subject.toString());
                IRI predicate = Values.iri(solution.getValue("p").toString());
                //System.out.println("Wanting to delete =  " + subject + ", " + predicate + ", " + "" + solution.getValue("o").toString());
                if (subjectIsInOnlyOneTripleAsObject(conn, subjectToDelete)) {
                    conn.remove(subjectToDelete, predicate, solution.getValue("o"));
                }


            }
        } catch (QueryEvaluationException ex) {
            System.out.println("QueryEvaluationException");
            ex.printStackTrace();
        }
    }

    private boolean subjectIsInOnlyOneTripleAsObject(RepositoryConnection conn, Resource subjectToDelete) {
        String selectQueryString = """
                ASK {
                  {
                    SELECT (COUNT(?s) AS ?count)
                    WHERE {
                      ?s ?p <%s> .
                    }
                    HAVING (?count <= 1)
                  }
                }""".formatted(subjectToDelete.toString());
        //System.out.println("query to ASK \n" + selectQueryString);

        BooleanQuery query = conn.prepareBooleanQuery(selectQueryString);

        //System.out.println("Result of the ASK: " + query.evaluate());

        return query.evaluate();
    }

    private String getQueryToGetObjectsForRoot(Value root) {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        SelectQuery selectQuery = Queries.SELECT();
        String query;
        Variable o = SparqlBuilder.var("o"), p = SparqlBuilder.var("p");
        Iri subjectIRI = iri(root.toString());
        query = selectQuery.prefix(skos).select(p, o).where(subjectIRI.has(p, o)).getQueryString(); //andIsA(dominantTypeIRI)).getQueryString();
        if (root.isBNode()) {
            query = changeIRItoBNode(query);
        }
        return query;
    }

    private String changeIRItoBNode(String query) {
        String newQuery = query.replace("<_:", "_:");
        newQuery = newQuery.replace("> ?p", " ?p");
        newQuery = newQuery.replace("> a", " a");
        return newQuery;
    }


}

