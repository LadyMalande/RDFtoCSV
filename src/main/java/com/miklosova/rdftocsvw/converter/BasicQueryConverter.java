package com.miklosova.rdftocsvw.converter;

import com.miklosova.rdftocsvw.converter.data_structure.*;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.ConverterHelper;
import lombok.extern.java.Log;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.query.*;
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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.miklosova.rdftocsvw.support.StandardModeCsvwIris.CSVW_TableGroup;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

/**
 * The Basic query converter. For RDF4J method, ONE table creation.
 */
@Log
public class BasicQueryConverter extends ConverterHelper implements IQueryParser {
    private static final Logger logger = Logger.getLogger(BasicQueryConverter.class.getName());

    /**
     * The Roots. AboutUrls of the rows
     */
    Set<Value> roots;
    /**
     * The Rows.
     */
    ArrayList<Row> rows;
    /**
     * The Keys. PropertyUrls, column headers
     */
    Set<Value> keys;

    /**
     * The IRI already processed x times.
     */
    Map<Value, Integer> IRIalreadyProcessedTimes;
    /**
     * The Already processed times.
     */
    int alreadyProcessedTimes = 0;
    /**
     * The Db.
     */
    Repository db;

    /**
     * The Rc.
     */
    RepositoryConnection rc;

    /**
     * Instantiates a new Basic query converter.
     *
     * @param db the db
     */
    public BasicQueryConverter(Repository db) {
        this.keys = new HashSet<>();
        this.db = db;
        this.IRIalreadyProcessedTimes = new HashMap<>();
    }

    @Override
    public PrefinishedOutput<RowsAndKeys> convertWithQuery(RepositoryConnection rc) {
        this.rc = rc;
        changeBNodesForIri(rc);
        deleteBlankNodes(rc);
        rows = new ArrayList<>();
        new PrefinishedOutput<>((new RowAndKey.RowAndKeyFactory()).factory());
        PrefinishedOutput<RowAndKey> queryResult;
        String query = getCSVTableQueryForModel(true);
        try {
            queryResult = queryRDFModel(query, true);

        } catch (IndexOutOfBoundsException ex) {
            query = getCSVTableQueryForModel(false);
            queryResult = queryRDFModel(query, false);
        }
        RowsAndKeys rak = new RowsAndKeys();
        ArrayList<RowAndKey> rakArray = new ArrayList<>();
        assert queryResult != null;
        rakArray.add(queryResult.getPrefinishedOutput());
        rak.setRowsAndKeys(rakArray);
        return new PrefinishedOutput<>(rak);
    }


    private PrefinishedOutput<RowAndKey> queryRDFModel(String queryString, boolean askForTypes) throws IndexOutOfBoundsException {
        PrefinishedOutput<RowAndKey> gen = new PrefinishedOutput<>((new RowAndKey.RowAndKeyFactory()).factory());
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, String.valueOf(askForTypes));
        // Query the data and pass the result as String

        // Query in rdf4j
        // Create a new Repository.

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

            while (!conn.isEmpty()) {
                TupleQuery query = conn.prepareTupleQuery(queryString);
                // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
                try (TupleQueryResult result = query.evaluate()) {
                    // we just iterate over all solutions in the result...
                    if (result == null) {
                        return null;
                    }
                    List<BindingSet> resultList = result.stream().toList(); // Collect into a list

                    if (resultList.isEmpty() && askForTypes) {
                        throw new IndexOutOfBoundsException();
                    }
                    roots = new HashSet<>();
                    for (BindingSet solution : resultList) {
                        // ... and print out the value of the variable binding for ?s and ?n
                        if (solution.getValue("o").stringValue().equalsIgnoreCase(CSVW_TableGroup)) {
                            StandardModeConverter smc = new StandardModeConverter(db);
                            new RowAndKey();
                            RowAndKey smcOutputRowAndKey;
                            PrefinishedOutput<RowAndKey> smcOutput = new PrefinishedOutput<>((new RowAndKey.RowAndKeyFactory()).factory());
                            smcOutputRowAndKey = smc.convertWithQuery(this.rc).getPrefinishedOutput().getRowsAndKeys().get(0);
                            smcOutput.getPrefinishedOutput().getRows().addAll(smcOutputRowAndKey.getRows());
                            smcOutput.getPrefinishedOutput().getKeys().addAll(smcOutputRowAndKey.getKeys());
                            return smcOutput;
                        }
                        roots.add(solution.getValue("s"));

                        Row newRow = new Row(solution.getValue("s"), solution.getValue("o"), askForTypes);
                        queryForSubjects(conn, newRow, solution.getValue("s"), solution.getValue("s"), 0);

                        deleteQueryToGetObjectsForRoot(solution.getValue("s"));


                        if (rows.stream().anyMatch(row -> row.id.equals(newRow.id))) {
                            // a row with the same id is already present in the data, don't create new one
                        } else {
                            rows.add(newRow);
                        }

                    }
                    if (roots.isEmpty()) {
                        // NO ROOTS found, find different supplement roots
                        TupleQuery queryForSubstituteRoots = conn.prepareTupleQuery(getQueryForSubstituteRoots(askForTypes));
                        try (TupleQueryResult resultForSubstituteRoots = queryForSubstituteRoots.evaluate()) {
                            if (resultForSubstituteRoots.hasNext()) {
                                for (BindingSet solution : resultForSubstituteRoots) {
                                    // ... and print out the value of the variable binding for ?s and ?n
                                    roots.add(solution.getValue("s"));
                                }
                            } else {
                                throw new IndexOutOfBoundsException();

                            }

                        }
                    }
                } catch (IndexOutOfBoundsException ex) {
                    throw new IndexOutOfBoundsException();
                }

                queryString = getQueryForSubstituteRoots(askForTypes);
            }

            ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, String.valueOf(askForTypes));
        } catch (IndexOutOfBoundsException ex) {
            throw new IndexOutOfBoundsException();
        }

        // Verify the output in console
        gen.getPrefinishedOutput().getRows().addAll(rows);
        gen.getPrefinishedOutput().getKeys().addAll(keys);
        return gen;
    }

    private void queryForSubjects(RepositoryConnection conn, Row newRow, Value root, Value subject, int level) {
        String queryToGetAllPredicatesAndObjects = getQueryToGetObjectsForRoot(subject);
        TupleQuery query = conn.prepareTupleQuery(queryToGetAllPredicatesAndObjects);
        try (TupleQueryResult result = query.evaluate()) {


            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                // We found a root row
                Value keyForColumnsMap = solution.getBinding("p").getValue();
                if (level != 0) {
                    ValueFactory valueFactory = SimpleValueFactory.getInstance();

                    // Create a new IRI

                    String newValueForMap = solution.getBinding("p").getValue().stringValue() + "_MULTILEVEL_";// + ((IRI) subject).getLocalName();
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

                } else if (newRow.columns.containsKey(keyForColumnsMap) &&
                        newRow.columns.get(keyForColumnsMap).type == TypeOfValue.LITERAL &&
                        solution.getBinding("o").getValue().isLiteral()) {
                    List<Value> oldStringValue = newRow.columns.get(keyForColumnsMap).values;
                    oldStringValue.add(solution.getBinding("o").getValue());
                    TypeIdAndValues oldTypeIdAndValues = newRow.columns.get(keyForColumnsMap);
                    oldTypeIdAndValues.values = oldStringValue;
                    newRow.columns.put(keyForColumnsMap, oldTypeIdAndValues);

                } else { // There is no such key (column) in the map
                    TypeOfValue newType = (solution.getBinding("o").getValue().isIRI()) ? TypeOfValue.IRI :
                            (solution.getBinding("o").getValue().isBNode()) ? TypeOfValue.BNODE : TypeOfValue.LITERAL;
                    newRow.columns.put(keyForColumnsMap, new TypeIdAndValues(subject, newType,
                            new ArrayList<>(List.of(solution.getBinding("o").getValue()))));

                }

                keys.add(keyForColumnsMap);

                if (solution.getValue("o") != null && solution.getValue("o").isIRI()) {
                    if (solution.getValue("o") != root) {
                        queryForSubjects(conn, newRow, root, solution.getValue("o"), level + 1);
                    }
                }

                // Delete the triple from the storage
                Resource subjectToDelete = Values.iri(subject.toString());
                IRI predicate = Values.iri(solution.getValue("p").toString());
                if (subjectIsInOnlyOneTripleAsObject(conn, subjectToDelete)) {
                    conn.remove(subjectToDelete, predicate, solution.getValue("o"));
                } else {
                    if (IRIalreadyProcessedTimes.get(subjectToDelete) != null) {
                        if (IRIalreadyProcessedTimes.get(subjectToDelete) + 1 == alreadyProcessedTimes) {
                            deleteQueryToGetObjectsForRoot(subjectToDelete);
                        } else {
                            IRIalreadyProcessedTimes.put(subjectToDelete, IRIalreadyProcessedTimes.get(subjectToDelete) + 1);
                        }
                    } else {
                        IRIalreadyProcessedTimes.put(subjectToDelete, 1);

                    }
                }


            }
        } catch (QueryEvaluationException ex) {
            logger.log(Level.SEVERE, "Ther was an error evaluation a SPARQL query while converting RDF data to one table.");
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

        String countQuery = """

                    SELECT (COUNT(?s) AS ?count)
                    WHERE {
                      ?s ?p <%s> .
                    }
                  
                """.formatted(subjectToDelete.toString());

        BooleanQuery query = conn.prepareBooleanQuery(selectQueryString);

        TupleQuery queryCount = conn.prepareTupleQuery(countQuery);
        try (TupleQueryResult result = queryCount.evaluate()) {
            for (BindingSet set : result) {
                alreadyProcessedTimes = Integer.parseInt(((Literal) set.getValue("count")).getLabel());
            }

        }

        return query.evaluate();
    }

    private String getQueryToGetObjectsForRoot(Value root) {
        SelectQuery selectQuery = Queries.SELECT();
        String query;
        Variable o = SparqlBuilder.var("o"), p = SparqlBuilder.var("p");
        Iri subjectIRI = iri(root.toString());
        query = selectQuery.select(p, o).where(subjectIRI.has(p, o)).getQueryString(); //andIsA(dominantTypeIRI)).getQueryString();
        if (root.isBNode()) {
            query = changeIRItoBNode(query);
        }
        return query;
    }

    private void deleteQueryToGetObjectsForRoot(Value root) {
        String del = "DELETE { ?s ?p ?o .} WHERE { <" + root.stringValue() + "> ?p ?o .}";
        Update deleteQuery = rc.prepareUpdate(del);
        deleteQuery.execute();

    }


}

