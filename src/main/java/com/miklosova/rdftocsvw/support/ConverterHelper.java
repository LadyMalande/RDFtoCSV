package com.miklosova.rdftocsvw.support;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;

import java.util.*;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

public class ConverterHelper {
    public Map<Value, Integer> mapOfTypesAndTheirNumbers;

    String delimiter;
    String CSVFileTOWriteTo;

    public static boolean rootHasThisType(Set<Value> rootsThatHaveSomeType, Value root) {
        return rootsThatHaveSomeType.contains(root);
    }


    public void deleteBlankNodes(RepositoryConnection rc) {
        String del = "DELETE {?s ?p ?o .} WHERE { ?s ?p ?o . FILTER (isBlank(?s) || isBlank(?o))}";
        Update deleteQuery = rc.prepareUpdate(del);
        deleteQuery.execute();
    }

    public void loadConfiguration() {

        delimiter = ConfigurationManager.getVariableFromConfigFile("input.delimiter");

        CSVFileTOWriteTo = ConfigurationManager.getVariableFromConfigFile("input.outputFileName");
    }

    public static Set<Value> rootsThatHaveThisType(RepositoryConnection conn, Value dominantType, boolean askForTypes) {
        //System.out.println("beginning of rootsThatHaveThisType  ");
        Set<Value> compliantRoots = new HashSet<>();
        String queryForPredicates = getSelectQuery(askForTypes, null, null, dominantType.toString());
        //System.out.println("rootsThatHaveThisType got SelectQuery " + queryForPredicates);
        TupleQuery query = conn.prepareTupleQuery(queryForPredicates);
        //System.out.println("rootsThatHaveThisType after preparing TupleQuery ");
        try (TupleQueryResult result = query.evaluate()) {
            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                compliantRoots.add(solution.getValue("s"));
                //System.out.println("compliantRoots.add " + solution.getValue("s"));

            }
        }
        return compliantRoots;
    }

    public void changeBNodesForIri(RepositoryConnection rc) {
        Iterator<Statement> statements = rc.getStatements(null, null, null, true).iterator();
        Map<Value, Value> mapOfBlanks = new HashMap<>();
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
                ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_BLANK_NODES, "true");
                rc.add(statement);
                System.out.println("Replacing blank nodes with " + statement.getSubject().stringValue() + " " + statement.getPredicate().stringValue() + " " + statement.getObject().stringValue());

            }
            counter = counter + 1;
            //System.out.println("Replacing blank nodes " + counter);
        }
    }

    protected static String getSelectQuery(boolean askForTypes, String subject, String predicate, String object) {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        // Create the query to get all data in CSV format
        SelectQuery selectQuery = Queries.SELECT();

        Variable o = SparqlBuilder.var("o"), s = SparqlBuilder.var("s"),
                s_in = SparqlBuilder.var("s_in"), p_in = SparqlBuilder.var("p_in"),
                p = SparqlBuilder.var("p");
        ;
        Iri objectIri = iri(object);
        if (askForTypes) {
            selectQuery.prefix(skos).select(s,o).where(s.isA(objectIri));
        } else {
            selectQuery.prefix(skos).select(s,o).where(s.has(objectIri, o ));
        }

        //System.out.println("askfortypes " + askForTypes + " getSelectQuery query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }
    static <K, V extends Comparable<? super V>>
    List<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {

        List<Map.Entry<K, V>> sortedEntries = new ArrayList<>(map.entrySet());

        sortedEntries.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        return sortedEntries;
    }

    /**
     * Gets query for substitute roots.
     *
     * @param askForTypes the ask for types
     * @return the query for substitute roots
     */
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

        System.out.println("getQueryForSubstituteRoots query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    public String getCSVTableQueryForModel(boolean askForTypes) {
        // Create the query to get all data in CSV format
        SelectQuery selectQuery = Queries.SELECT();

        Variable o = SparqlBuilder.var("o"), s = SparqlBuilder.var("s"),
                s_in = SparqlBuilder.var("s_in"), p_in = SparqlBuilder.var("p_in"),
                p = SparqlBuilder.var("p");

        if (askForTypes) {
            selectQuery.select(s, o).where(s.isA(o).filterNotExists(s_in.has(p_in, s)));
        } else {
            selectQuery.select(s, o).where(s.has(p, o).filterNotExists(s_in.has(p_in, s)));
        }

        System.out.println("getCSVTableQueryForModel query string\n" + selectQuery.getQueryString());
        return selectQuery.getQueryString();
    }

    public Value getDominantType() {
        Value dominantType;

        List<Map.Entry<Value, Integer>> sortedEntries = entriesSortedByValues(mapOfTypesAndTheirNumbers);
        dominantType = sortedEntries.get(0).getKey();

        System.out.println("Chosen dominant type is " + dominantType);

        return dominantType;
    }

    public String getQueryForTypes(Value root, boolean askForTypes) {
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
    public void countDominantTypes(RepositoryConnection conn, Set<Value> roots, boolean askForTypes) {
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
                        //System.out.println("key askForTypes = " + key.stringValue());
                    } else {
                        key = solution.getValue("p");
                        //System.out.println("key !askForTypes = " + key.stringValue());
                    }


                    if (mapOfTypesAndTheirNumbers.containsKey(key)) {
                        Integer oldValue = mapOfTypesAndTheirNumbers.get(key);
                        Integer newValue = oldValue + 1;
                        mapOfTypesAndTheirNumbers.put(key, newValue);
                        //System.out.println("Adding key for sorting predicates: " + key + " number="+newValue);
                    } else {
                        mapOfTypesAndTheirNumbers.put(key, 1);
                        //System.out.println("Adding key for sorting predicates: " + key + " number=1");
                    }
                }

            } catch (QueryEvaluationException ex) {
                System.out.println("There has been a problem with query evaluation ");
                ex.printStackTrace();
            }
        }
    }
        public static String changeIRItoBNode(String query) {
            String newQuery = query.replace("<_:", "_:");
            newQuery = newQuery.replace("> ?p", " ?p");
            newQuery = newQuery.replace("> a", " a");
            return newQuery;
        }

}
