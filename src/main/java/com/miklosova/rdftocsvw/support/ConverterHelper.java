package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.support.AppConfig;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

/**
 * The Converter helper contains multiple shared methods.
 */
public class ConverterHelper {
    private static final Logger logger = Logger.getLogger(ConverterHelper.class.getName());
    /**
     * The Map of types and their number of occurrences.
     */
    public Map<Value, Integer> mapOfTypesAndTheirNumbers;
    
    /**
     * The application configuration.
     */
    protected AppConfig config;

    /**
     * Root has this type boolean.
     *
     * @param rootsThatHaveSomeType the roots that have some type
     * @param root                  the root to ask about type
     * @return True if the root is among the set of Values.
     */
    public static boolean rootHasThisType(Set<Value> rootsThatHaveSomeType, Value root) {
        return rootsThatHaveSomeType.contains(root);
    }


    /**
     * Delete blank nodes from given Repository Connection.
     *
     * @param rc the Repository Connection on which to make the SPARQL query
     */
    public void deleteBlankNodes(RepositoryConnection rc) {
        String del = "DELETE {?s ?p ?o .} WHERE { ?s ?p ?o . FILTER (isBlank(?s) || isBlank(?o))}";
        Update deleteQuery = rc.prepareUpdate(del);
        deleteQuery.execute();
    }

    /**
     * Roots that have this type set.
     *
     * @param conn         RepositoryConnection to do SPARQL query on
     * @param dominantType the dominant type to get the Subjects for
     * @param askForTypes  the ask for types - if true then asking for object on rdf:type, if false asking for general
     *                     predicate - dominantType
     * @return the set of Values that are Subjects to the dominantType object.
     */
    public static Set<Value> rootsThatHaveThisType(RepositoryConnection conn, Value dominantType, boolean askForTypes) {
        Set<Value> compliantRoots = new HashSet<>();
        String queryForPredicates = getSelectQuery(askForTypes, dominantType.toString());
        TupleQuery query = conn.prepareTupleQuery(queryForPredicates);
        try (TupleQueryResult result = query.evaluate()) {
            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                compliantRoots.add(solution.getValue("s"));
            }
        }
        return compliantRoots;
    }

    /**
     * Change Blank Nodes for IRI in the whole RepositoryConnection and write information about it to config file.
     *
     * @param rc RepositoryConnection in which the blank nodes should be changed for IRIs
     */
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
                if (config != null) {
                    config.setConversionHasBlankNodes(true);
                }
                rc.add(statement);
            }
            counter = counter + 1;
        }
    }

    /**
     * Gets select query done in SPARQL. Asking for SUBJECT and OBJECT from the triples.
     *
     * @param askForTypes If true then asking for triples with rdf:type as their predicate.
     * @param object      the object of the triple
     * @return the select query
     */
    protected static String getSelectQuery(boolean askForTypes, String object) {
        Prefix skos = SparqlBuilder.prefix(SKOS.NS);
        // Create the query to get all data in CSV format
        SelectQuery selectQuery = Queries.SELECT();

        Variable o = SparqlBuilder.var("o"), s = SparqlBuilder.var("s");

        Iri objectIri = iri(object);
        if (askForTypes) {
            selectQuery.prefix(skos).select(s, o).where(s.isA(objectIri));
        } else {
            selectQuery.prefix(skos).select(s, o).where(s.has(objectIri, o));
        }

        return selectQuery.getQueryString();
    }

    /**
     * Entries sorted by values, returns list.
     *
     * @param <K> the type parameter
     * @param <V> the type parameter
     * @param map the map
     * @return the list of sorted Map
     */
    static <K, V extends Comparable<? super V>>
    List<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {

        List<Map.Entry<K, V>> sortedEntries = new ArrayList<>(map.entrySet());

        sortedEntries.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        return sortedEntries;
    }

    /**
     * Gets query for substitute roots (used when roots are not found with the main method asking for rdf:type).
     *
     * @param askForTypes If true then choosing triples with rdf:type predicate
     * @return the SPARQL query for substitute roots
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

        return selectQuery.getQueryString();
    }

    /**
     * Gets csv table query for model. Gets the ROOTS of the graph meaning they are never acting as objects.
     *
     * @param askForTypes If true then choosing triples with rdf:type predicate
     * @return the SPARQL query for roots of the table
     **/
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

        return selectQuery.getQueryString();
    }

    /**
     * Gets dominant type among the remaining triples.
     *
     * @return the dominant type of triples
     */
    public Value getDominantType() {
        Value dominantType;

        List<Map.Entry<Value, Integer>> sortedEntries = entriesSortedByValues(mapOfTypesAndTheirNumbers);
        dominantType = sortedEntries.get(0).getKey();

        return dominantType;
    }

    /**
     * Gets SPARQL query that returns available objects for given Value as subject.
     *
     * @param root        the subject of the triple we are asking about
     * @param askForTypes If true then choosing triples with rdf:type predicate
     * @return the query for objects for given subject
     */
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

    /**
     * Count dominant types and update the map of occurrences.
     *
     * @param conn        the RepositoryConnection to count the types in
     * @param roots       the subjects of the triples in question
     * @param askForTypes If true then choosing triples with rdf:type predicate
     */
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
                    } else {
                        key = solution.getValue("p");
                    }

                    if (mapOfTypesAndTheirNumbers.containsKey(key)) {
                        Integer oldValue = mapOfTypesAndTheirNumbers.get(key);
                        Integer newValue = oldValue + 1;
                        mapOfTypesAndTheirNumbers.put(key, newValue);
                    } else {
                        mapOfTypesAndTheirNumbers.put(key, 1);
                    }
                }

            } catch (QueryEvaluationException ex) {
                logger.log(Level.SEVERE, ex.getCause() + " " + ex.getLocalizedMessage());
            }
        }
    }

    /**
     * Change IRI into BlankNode string in a SPARQL query.
     *
     * @param query the query to change
     * @return the string of adjusted query
     */
    public static String changeIRItoBNode(String query) {
        String newQuery = query.replace("<_:", "_:");
        newQuery = newQuery.replace("> ?p", " ?p");
        newQuery = newQuery.replace("> a", " a");
        return newQuery;
    }

}
