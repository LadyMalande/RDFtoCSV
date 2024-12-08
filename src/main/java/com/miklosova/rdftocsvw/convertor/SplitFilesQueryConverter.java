package com.miklosova.rdftocsvw.convertor;

import com.miklosova.rdftocsvw.metadata_creator.Metadata;
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
public class SplitFilesQueryConverter extends ConverterHelper implements IQueryParser {

    public Map<Value, Integer> mapOfTypesAndTheirNumbers;
    String resultCSV;
    ArrayList<Value> roots;
    ArrayList<Row> rows;
    ArrayList<Value> keys;
    ArrayList<ArrayList<Row>> allRows;
    ArrayList<ArrayList<Value>> allKeys;
    Metadata metadata;
    ArrayList<String> fileNamesCreated;
    String delimiter;
    String CSVFileTOWriteTo;
    Repository db;

    RepositoryConnection rc;

    Integer fileNumberX;

    public SplitFilesQueryConverter(Repository db) {
        this.keys = new ArrayList<>();
        this.db = db;
        this.fileNumberX = 0;
        this.fileNamesCreated = new ArrayList<>();
        this.metadata = new Metadata();
    }

    static <K, V extends Comparable<? super V>>
    List<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {

        List<Map.Entry<K, V>> sortedEntries = new ArrayList<>(map.entrySet());

        sortedEntries.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        return sortedEntries;
    }

    public void changeBNodesForIri(RepositoryConnection rc) {
        Iterator<Statement> statements = rc.getStatements(null, null, null, true).iterator();
        Map<Value, Value> mapOfBlanks = new HashMap<>();
        //System.out.println("Iterator size: " + Iterators.size(statements));
        int counter = 0;
        int i = 0;
        while (statements.hasNext()) {
            Statement st = statements.next();
            Statement statement = null;
            IRI subj = null;
            if (st.getSubject().isBNode()) {
                if (mapOfBlanks.get(st.getSubject()) != null) {
                    ValueFactory vf = SimpleValueFactory.getInstance();
                    subj = (IRI) mapOfBlanks.get(st.getSubject());
                    statement = vf.createStatement((IRI) mapOfBlanks.get(st.getSubject()), st.getPredicate(), st.getObject());

                } else {
                    ValueFactory vf = SimpleValueFactory.getInstance();
                    IRI v = vf.createIRI("https://blank_Nodes_IRI.org/" + i);
                    //IRI v = (IRI) iri("https://blank_Nodes_IRI.org/" + i);
                    i++;
                    mapOfBlanks.put(st.getSubject(), v);
                    subj = (IRI) mapOfBlanks.get(st.getSubject());
                    statement = vf.createStatement((IRI) mapOfBlanks.get(st.getSubject()), st.getPredicate(), st.getObject());
                }
            }
            if (st.getObject().isBNode()) {
                if (mapOfBlanks.get(st.getObject()) != null) {
                    ValueFactory vf = SimpleValueFactory.getInstance();
                    subj = (subj == null) ? (IRI) st.getSubject() : subj;
                    statement = vf.createStatement(subj, st.getPredicate(), mapOfBlanks.get(st.getObject()));
                    rc.add(statement);
                } else {
                    ValueFactory vf = SimpleValueFactory.getInstance();
                    IRI v = vf.createIRI("https://blank_Nodes_IRI.org/" + i);

                    mapOfBlanks.put(st.getObject(), v);
                    subj = (subj == null) ? (IRI) st.getSubject() : subj;
                    statement = vf.createStatement(subj, st.getPredicate(), mapOfBlanks.get(st.getObject()));
                    i++;
                }
            }
            if (statement != null) {
                rc.add(statement);

                System.out.println(statement.getSubject() + " " + statement.getPredicate() + " " + statement.getObject());
                System.out.println(st);
            }
            counter = counter + 1;
        }
        System.out.println("Count " + counter);
    }

    @Override
    public PrefinishedOutput<RowsAndKeys> convertWithQuery(RepositoryConnection rc) {
        this.rc = rc;
        loadConfiguration();
        changeBNodesForIri(rc);
        deleteBlankNodes(rc);
        PrefinishedOutput<RowsAndKeys> queryResult;
        String query = getCSVTableQueryForModel(true);
        try {
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

        //System.out.println("getCSVTableQueryForModel query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    private PrefinishedOutput<RowsAndKeys> queryRDFModel(String queryString, boolean askForTypes) {
        allKeys = new ArrayList<>();
        allRows = new ArrayList<>();
        rows = new ArrayList<>();
        PrefinishedOutput<RowsAndKeys> gen = new PrefinishedOutput<>(new RowsAndKeys.RowsAndKeysFactory());
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, String.valueOf(askForTypes));
        System.out.println("CONVERSION_HAS_RDF_TYPES at the beginning " + ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES));
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
                    for (BindingSet solution : result) {
                        // ... and print out the value of the variable binding for ?s and ?n
                        if (solution.getValue("o").stringValue().equalsIgnoreCase(CSVW_TableGroup)) {
                            System.out.println("Table Group, engaging in StandardModeConverter in splitQueryConverter");
                            StandardModeConverter smc = new StandardModeConverter(db);
                            return smc.convertWithQuery(this.rc);
                        }
                        if (!roots.contains(solution.getValue("s"))) {
                            System.out.println("root " + solution.getValue("s"));
                            roots.add(solution.getValue("s"));
                        }
                    }

                    if (roots.isEmpty()) {
                        // NO ROOTS found, find different supplement roots
                        System.out.println("NO ROOTS found, find different supplement roots");
                        TupleQuery queryForSubstituteRoots = conn.prepareTupleQuery(getQueryForSubstituteRoots(askForTypes));
                        try (TupleQueryResult resultForSubstituteRoots = queryForSubstituteRoots.evaluate()) {
                            for (BindingSet solution : resultForSubstituteRoots) {
                                // ... and print out the value of the variable binding for ?s and ?n
                                if (!roots.contains(solution.getValue("s"))) {
                                    roots.add(solution.getValue("s"));
                                }
                            }
                        }
                    }
                    countDominantTypes(conn, roots, askForTypes);
                    Value dominantType = getDominantType();
                    recursiveQueryForFiles(conn, dominantType, askForTypes);
                    System.out.println("Number of files = " + allRows.size());

                    resultCSV = result.toString();
                }
            }
            for (int i = 0; i < allRows.size(); i++) {
                gen.prefinishedOutput.rowsAndKeys.add(new RowAndKey(allKeys.get(i), allRows.get(i)));
                ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, String.valueOf(askForTypes));
            }
        }
        return gen;
    }

    private void recursiveQueryForFiles(RepositoryConnection conn, Value dominantType, boolean askForTypes) {
        // Make new rows and keys for the current file
        rows = new ArrayList<>();
        keys = new ArrayList<>();

        List<Value> rootsThatHaveThisType = rootsThatHaveThisType(conn, dominantType, askForTypes);
        for (Value root : roots) {
            // If the root does not have the dominant type, it will be processed later
            if (rootHasThisType(rootsThatHaveThisType, root)) {
                // new Row with the found subject as its id
                Row newRow = new Row(root, dominantType, askForTypes);

                System.out.println("Number of Roots in recursiveQuery: " + roots.size() + " root: " + root.stringValue());
                // Query the model for individual rows lead by the roots and having the predicates as the headers in the file
                queryForSubjects(conn, newRow, root, dominantType, askForTypes);
                rows.add(newRow);
            }
        }

        allRows.add(rows);
        allKeys.add(keys);
    }

    private void queryForSubjects(RepositoryConnection conn, Row newRow, Value root, Value dominantType, boolean askForTypes) {
        String queryToGetAllPredicatesAndObjects = getQueryToGetObjectsForRoot(root, dominantType, askForTypes);
        TupleQuery query = conn.prepareTupleQuery(queryToGetAllPredicatesAndObjects);
        try (TupleQueryResult result = query.evaluate()) {

            newRow.id = root;

            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                // We found a root row
                // Old value in the column have IRI objects and the new object is IRI
                if (newRow.columns.keySet().stream().anyMatch(key -> key.toString().equalsIgnoreCase(solution.getBinding("p").getValue().toString()))) {
                    System.out.println("KEY STRING MATCHES");
                } else {
                    System.out.println("KEY STRING NOT MATCHES in keyset: ");
                }
                if (newRow.columns.containsKey(solution.getBinding("p").getValue()) &&
                        newRow.columns.get(solution.getBinding("p").getValue()).type == TypeOfValue.IRI &&
                        solution.getBinding("o").getValue().isIRI()) {
                    List<Value> oldStringValue = newRow.columns.get(solution.getBinding("p").getValue()).values;
                    oldStringValue.add(solution.getBinding("o").getValue());
                    TypeIdAndValues oldTypeIdAndValues = newRow.columns.get(solution.getBinding("p").getValue());
                    oldTypeIdAndValues.values = oldStringValue;
                    newRow.columns.put(solution.getBinding("p").getValue(), oldTypeIdAndValues);

                } else if (newRow.columns.containsKey(solution.getBinding("p").getValue()) &&
                        newRow.columns.get(solution.getBinding("p").getValue()).type == TypeOfValue.BNODE &&
                        solution.getBinding("o").getValue().isBNode()) {
                    List<Value> oldStringValue = newRow.columns.get(solution.getBinding("p").getValue()).values;
                    oldStringValue.add(solution.getBinding("o").getValue());
                    TypeIdAndValues oldTypeIdAndValues = newRow.columns.get(solution.getBinding("p").getValue());
                    oldTypeIdAndValues.values = oldStringValue;
                    newRow.columns.put(solution.getBinding("p").getValue(), oldTypeIdAndValues);

                } else if (newRow.columns.containsKey(solution.getBinding("p").getValue()) &&
                        newRow.columns.get(solution.getBinding("p").getValue()).type == TypeOfValue.LITERAL &&
                        solution.getBinding("o").getValue().isLiteral()) {
                    List<Value> oldStringValue = newRow.columns.get(solution.getBinding("p").getValue()).values;
                    oldStringValue.add(solution.getBinding("o").getValue());
                    TypeIdAndValues oldTypeIdAndValues = newRow.columns.get(solution.getBinding("p").getValue());
                    oldTypeIdAndValues.values = oldStringValue;
                    newRow.columns.put(solution.getBinding("p").getValue(), oldTypeIdAndValues);

                } else { // There is no such key (column) in the map
                    TypeOfValue newType = (solution.getBinding("o").getValue().isIRI()) ? TypeOfValue.IRI :
                            (solution.getBinding("o").getValue().isBNode()) ? TypeOfValue.BNODE : TypeOfValue.LITERAL;
                    newRow.columns.put(solution.getBinding("p").getValue(), new TypeIdAndValues(root, newType,
                            new ArrayList<>(List.of(solution.getBinding("o").getValue()))));


                }


                if (!keys.contains(solution.getValue("p"))) {
                    keys.add(solution.getValue("p"));
                }

                Resource subject;
                if (root.isBNode()) {
                    SimpleValueFactory vf = SimpleValueFactory.getInstance();
                    BNode rooty = (BNode) root;
                    // Create a blank node with a specific identifier
                    subject = vf.createBNode(rooty.getID());
                } else {
                    subject = Values.iri(root.toString());
                }

                IRI predicate = Values.iri(solution.getValue("p").toString());
                //System.out.println("Wanting to delete =  " + subject + ", " + predicate +  ", " + ""  + solution.getValue("o").toString());
                if (subject.isBNode()) {
                    conn.remove(null, predicate, solution.getValue("o"));
                }
                conn.remove(subject, predicate, solution.getValue("o"));

            }
        } catch (QueryEvaluationException ex) {
            System.out.println("QueryEvaluationException");
            ex.printStackTrace();
        }
    }

    private String getQueryToGetObjectsForRoot(Value root, Value dominantType, boolean askForTypes) {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        SelectQuery selectQuery = Queries.SELECT();
        String query;
        Variable o = SparqlBuilder.var("o"), p = SparqlBuilder.var("p");
        Iri subjectIRI = iri(root.toString());
        Iri dominantTypeIRI = iri(dominantType.toString());
        if (askForTypes) {
            query = selectQuery.prefix(skos).select(p, o).where(subjectIRI.has(p, o).andIsA(dominantTypeIRI)).getQueryString();
        } else {
            query = selectQuery.prefix(skos).select(p, o).where(subjectIRI.has(p, o)).getQueryString();
        }
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

    private void countDominantTypes(RepositoryConnection conn, ArrayList<Value> roots, boolean askForTypes) {
        mapOfTypesAndTheirNumbers = new HashMap<>();

        for (Value root : roots) {
            String queryForPredicates = getQueryForTypes(root, askForTypes);
            TupleQuery query = conn.prepareTupleQuery(queryForPredicates);

            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                for (BindingSet solution : result) {
                    Value key;
                    if (askForTypes) {
                        key = solution.getValue("o");
                    } else {
                        key = solution.getValue("p");
                    }


                    if (mapOfTypesAndTheirNumbers.containsKey(key)) {
                        Integer oldValue = mapOfTypesAndTheirNumbers.get(key);
                        Integer newValue = oldValue + 1;
                        mapOfTypesAndTheirNumbers.put(key, newValue);
                    } else {
                        mapOfTypesAndTheirNumbers.put(key, 1);
                        System.out.println("Adding key for sorting predicates: " + key + " number=1");
                    }
                }

            } catch (QueryEvaluationException ex) {
                System.out.println("There has been a problem with query evaluation ");
                ex.printStackTrace();
            }
        }
    }

    private String getQueryForTypes(Value root, boolean askForTypes) {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        SelectQuery selectQuery = Queries.SELECT();
        String query;
        Variable o = SparqlBuilder.var("o"), p = SparqlBuilder.var("p");
        Iri subjectIRI = iri(root.toString());

        if (askForTypes) {
            query = selectQuery.prefix(skos).select(o).where(subjectIRI.isA(o)).getQueryString();
        } else {
            query = selectQuery.prefix(skos).select(p, o).where(subjectIRI.has(p, o)).getQueryString();
        }
        if (root.isBNode()) {
            query = changeIRItoBNode(query);
        }
        return query;
    }

    private Value getDominantType() {
        Value dominantType;

        List<Map.Entry<Value, Integer>> sortedEntries = entriesSortedByValues(mapOfTypesAndTheirNumbers);
        dominantType = sortedEntries.get(0).getKey();

        System.out.println("Chosen dominant type is " + dominantType);

        return dominantType;
    }
}
