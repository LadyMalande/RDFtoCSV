package com.miklosova.rdftocsvw.converter;

import com.miklosova.rdftocsvw.converter.data_structure.*;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.output_processor.FileWrite;
import com.miklosova.rdftocsvw.support.AppConfig;

import com.miklosova.rdftocsvw.support.ConverterHelper;
import lombok.extern.java.Log;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
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

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.miklosova.rdftocsvw.support.StandardModeCsvwIris.CSVW_TableGroup;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

/**
 * The type Split files query converter.
 * This converter works best if the data is so big that the user wants to see in what parts and entities it is broken down into.
 * This converter will not damage conversion of ONE entity CSV type. For one type CSV this converter produces the same output as the BasicQueryConverter.
 */
@Log
public class SplitFilesQueryConverter extends ConverterHelper implements IQueryParser {
    private static final Logger logger = Logger.getLogger(FileWrite.class.getName());

    /**
     * The Result csv.
     */
    String resultCSV;
    /**
     * The Roots.
     */
    Set<Value> roots;
    /**
     * The Rows.
     */
    ArrayList<Row> rows;
    /**
     * The Keys.
     */
    static Set<Value> keys;
    /**
     * The Synchronized keys.
     */
    static Set<Value> synchronizedKeys;
    /**
     * The All rows.
     */
    ArrayList<ArrayList<Row>> allRows;
    /**
     * The All keys.
     */
    ArrayList<ArrayList<Value>> allKeys;
    /**
     * The Metadata.
     */
    Metadata metadata;
    /**
     * The File names created.
     */
    ArrayList<String> fileNamesCreated;

    /**
     * The Db.
     */
    Repository db;

    /**
     * The Rc.
     */
    RepositoryConnection rc;

    /**
     * The File number x.
     */
    Integer fileNumberX;

    /**
     * The AppConfig instance.
     */
    private AppConfig config;

    /**
     * Instantiates a new Split files query converter.
     *
     * @param db the db
     * @deprecated Use {@link #SplitFilesQueryConverter(Repository, AppConfig)} instead
     */
    @Deprecated
    public SplitFilesQueryConverter(Repository db) {
        keys = new HashSet<>();
        this.db = db;
        this.fileNumberX = 0;
        this.fileNamesCreated = new ArrayList<>();
        this.metadata = new Metadata(null);
        this.config = null;
    }

    /**
     * Instantiates a new Split files query converter with AppConfig.
     *
     * @param db the db
     * @param config the application configuration
     */
    public SplitFilesQueryConverter(Repository db, AppConfig config) {
        keys = new HashSet<>();
        this.db = db;
        this.fileNumberX = 0;
        this.fileNamesCreated = new ArrayList<>();
        this.metadata = new Metadata(config);
        this.config = config;
        super.config = config; // Set parent's config field
    }


    @Override
    public PrefinishedOutput<RowsAndKeys> convertWithQuery(RepositoryConnection rc) {
        this.rc = rc;
        allKeys = new ArrayList<>();
        allRows = new ArrayList<>();
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


    private PrefinishedOutput<RowsAndKeys> queryRDFModel(String queryString, boolean askForTypes) {
        rows = new ArrayList<>();
        PrefinishedOutput<RowsAndKeys> gen = new PrefinishedOutput<>((new RowsAndKeys.RowsAndKeysFactory()).factory());
        config.setConversionHasRdfTypes(askForTypes);
        // Query the data and pass the result as String
        ConnectionPool connectionPool = new ConnectionPool(db, Runtime.getRuntime().availableProcessors());
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
                    roots = new HashSet<>();
                    for (BindingSet solution : result) {
                        // ... and print out the value of the variable binding for ?s and ?n
                        if (solution.getValue("o").stringValue().equalsIgnoreCase(CSVW_TableGroup)) {
                            StandardModeConverter smc = new StandardModeConverter(db, config);
                            return smc.convertWithQuery(this.rc);
                        }

                        roots.add(solution.getValue("s"));

                    }

                    if (roots.isEmpty()) {
                        // NO ROOTS found, find different supplement roots
                        TupleQuery queryForSubstituteRoots = conn.prepareTupleQuery(getQueryForSubstituteRoots(askForTypes));
                        try (TupleQueryResult resultForSubstituteRoots = queryForSubstituteRoots.evaluate()) {
                            for (BindingSet solution : resultForSubstituteRoots) {
                                // ... and print out the value of the variable binding for ?s and ?n

                                roots.add(solution.getValue("s"));

                            }
                        }
                    }
                    countDominantTypes(conn, roots, askForTypes);
                    Value dominantType = getDominantType();

                    recursiveQueryForFiles(connectionPool, dominantType, askForTypes);

                    resultCSV = result.toString();
                }

            }

            for (int i = 0; i < allRows.size(); i++) {
                gen.getPrefinishedOutput().getRowsAndKeys().add(new RowAndKey(allKeys.get(i), allRows.get(i)));
            }
            config.setConversionHasRdfTypes(askForTypes);


        }
        connectionPool.closeAllConnections();
        return gen;

    }

    private void recursiveQueryForFiles(ConnectionPool connectionPool, Value dominantType, boolean askForTypes) {
        keys = new HashSet<>();
        synchronizedKeys = Collections.synchronizedSet(keys);

        List<Row> resultRows = recursiveQueryForFiles(connectionPool, dominantType, askForTypes, roots);
        allRows.add((ArrayList<Row>) resultRows);
        // Convert the Set to a List
        ArrayList<Value> listOfKeys = new ArrayList<>(synchronizedKeys);
        allKeys.add(listOfKeys);
    }


    /**
     * Recursive query for files list. Each recursion will make a new file with new table sorted by primarily rdf:type and secondarily by a dominant type that is prevalent.
     *
     * @param connectionPool the connection pool
     * @param dominantType   the dominant type
     * @param askForTypes    the ask for types
     * @param roots          the roots
     * @return the list
     */
    public List<Row> recursiveQueryForFiles(ConnectionPool connectionPool, Value dominantType,
                                            boolean askForTypes, Set<Value> roots) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        Set<Value> rootsThatHaveThisType = null;
        try {
            RepositoryConnection conn = connectionPool.getConnection();
            rootsThatHaveThisType = rootsThatHaveThisType(conn, dominantType, askForTypes);
            connectionPool.releaseConnection(conn);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        RecursiveQueryForFilesTask task = new RecursiveQueryForFilesTask(connectionPool, dominantType, askForTypes, roots, 0, roots.size(), rootsThatHaveThisType);
        return forkJoinPool.invoke(task);
    }

    /**
     * Query for subjects.
     *
     * @param conn         the connection to ask with SPARQL query on
     * @param newRow       the new row to ask the data for
     * @param root         the root = aboutUrl, the Subject of all other triples
     * @param dominantType the dominant type
     * @param askForTypes  the ask for types if true, asks for rdf:type objects
     */
    public static void queryForSubjects(RepositoryConnection conn, Row newRow, Value root, Value dominantType,
                                        boolean askForTypes) {
        String queryToGetAllPredicatesAndObjects = getQueryToGetObjectsForRoot(root, dominantType, askForTypes);
        TupleQuery query = conn.prepareTupleQuery(queryToGetAllPredicatesAndObjects);
        try (TupleQueryResult result = query.evaluate()) {

            newRow.id = root;

            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                // We found a root row
                // Old value in the column have IRI objects and the new object is IRI
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


                synchronizedKeys.add(solution.getValue("p"));


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
                if (subject.isBNode()) {
                    conn.remove(null, predicate, solution.getValue("o"));
                }
                conn.remove(subject, predicate, solution.getValue("o"));

            }
        } catch (QueryEvaluationException ex) {
            logger.log(Level.SEVERE, "There was an error trying to evaluate SPARQL query in querying for objects of given subjects");
        }
    }

    private static String getQueryToGetObjectsForRoot(Value root, Value dominantType, boolean askForTypes) {
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


}

