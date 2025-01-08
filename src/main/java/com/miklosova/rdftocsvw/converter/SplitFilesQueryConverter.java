package com.miklosova.rdftocsvw.converter;

import com.miklosova.rdftocsvw.converter.data_structure.*;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.ConverterHelper;
import com.miklosova.rdftocsvw.output_processor.FileWrite;
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
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.miklosova.rdftocsvw.support.StandardModeCsvwIris.CSVW_TableGroup;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

@Log
public class SplitFilesQueryConverter extends ConverterHelper implements IQueryParser {
    private static final Logger logger = Logger.getLogger(FileWrite.class.getName());

    String resultCSV;

    long startTime;
    long afterAddingRootsTime;
    long afterCountDominantTypeTime;
    long afterGetDominantTypeTime;

    long addingRootsTime;
    long countDominantTypeTime;
    long getDominantTypeTime;

    long afterRecursiveQueryTime;
    long recursiveQueryTime;
    Set<Value> roots;
    ArrayList<Row> rows;
    static Set<Value> keys;
    static Set<Value> synchronizedKeys;
    ArrayList<ArrayList<Row>> allRows;
    ArrayList<ArrayList<Value>> allKeys;
    Metadata metadata;
    ArrayList<String> fileNamesCreated;

    Repository db;

    RepositoryConnection rc;

    Integer fileNumberX;

    public SplitFilesQueryConverter(Repository db) {
        this.keys = new HashSet<>();
        this.db = db;
        this.fileNumberX = 0;
        this.fileNamesCreated = new ArrayList<>();
        this.metadata = new Metadata();
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
        startTime = System.currentTimeMillis();

        // Calculate total runtime

        rows = new ArrayList<>();
        PrefinishedOutput<RowsAndKeys> gen = new PrefinishedOutput<>((new RowsAndKeys.RowsAndKeysFactory()).factory());
        System.out.println("splitFilesQuery gen instanceof PrefinishedOutput<RowAndKey> " + (gen instanceof PrefinishedOutput<RowsAndKeys>));
        System.out.println("splitFilesQuery gen instanceof PrefinishedOutput<RowAndKey> " + (gen.getPrefinishedOutput() instanceof RowsAndKeys));
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, String.valueOf(askForTypes));
        System.out.println("CONVERSION_HAS_RDF_TYPES at the beginning " + ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES));
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
                    //System.out.println("removed: " + removed);
                }

                @Override
                public void statementAdded(Statement added) {
                    System.out.println("added: " + added);
                }
            });
            //ConnectionPool connectionPool = new ConnectionPool(db, Runtime.getRuntime().availableProcessors());
            logger.log(Level.INFO, "before while time " + String.valueOf(System.currentTimeMillis() - startTime));
            while (!conn.isEmpty()) {

                System.out.println("conn size " + conn.size());
                TupleQuery query = conn.prepareTupleQuery(queryString);
                System.out.println("queryRDFModel after TupleQuery query = conn.prepareTupleQuery(queryString);");
                // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
                try (TupleQueryResult result = query.evaluate()) {
                    System.out.println("queryRDFModel we just iterate over all solutions in the result...");
                    // we just iterate over all solutions in the result...
                    if (result == null) {
                        return null;
                    }
                    roots = new HashSet<>();
                    for (BindingSet solution : result) {
                        //System.out.println("print out the value of the variable binding for ?s and ?n");
                        // ... and print out the value of the variable binding for ?s and ?n
                        if (solution.getValue("o").stringValue().equalsIgnoreCase(CSVW_TableGroup)) {
                            System.out.println("Table Group, engaging in StandardModeConverter in splitQueryConverter");
                            StandardModeConverter smc = new StandardModeConverter(db);
                            return smc.convertWithQuery(this.rc);
                        }

                        roots.add(solution.getValue("s"));

                    }
                    if(afterRecursiveQueryTime == 0){
                        addingRootsTime = System.currentTimeMillis();
                        afterAddingRootsTime = addingRootsTime - startTime;
                    } else{
                        addingRootsTime = System.currentTimeMillis();
                        afterAddingRootsTime = addingRootsTime - afterRecursiveQueryTime;
                    }
                    logger.log(Level.INFO, "AddingRootsTime " + String.valueOf(afterAddingRootsTime));
                    if (roots.isEmpty()) {
                        // NO ROOTS found, find different supplement roots
                        System.out.println("NO ROOTS found, find different supplement roots");
                        TupleQuery queryForSubstituteRoots = conn.prepareTupleQuery(getQueryForSubstituteRoots(askForTypes));
                        try (TupleQueryResult resultForSubstituteRoots = queryForSubstituteRoots.evaluate()) {
                            for (BindingSet solution : resultForSubstituteRoots) {
                                // ... and print out the value of the variable binding for ?s and ?n

                                roots.add(solution.getValue("s"));

                            }
                        }
                    }
                    countDominantTypes(conn, roots, askForTypes);
                    countDominantTypeTime = System.currentTimeMillis();
                    afterCountDominantTypeTime = countDominantTypeTime - addingRootsTime;
                    logger.log(Level.INFO, "Counting dominant types Time " + String.valueOf(afterCountDominantTypeTime));
                    Value dominantType = getDominantType();
                    getDominantTypeTime = System.currentTimeMillis();
                    afterGetDominantTypeTime = getDominantTypeTime - countDominantTypeTime;
                    logger.log(Level.INFO, "get dominant types Time " + String.valueOf(afterGetDominantTypeTime));

                    recursiveQueryForFiles(connectionPool, dominantType, askForTypes);

                    afterRecursiveQueryTime = System.currentTimeMillis();
                    recursiveQueryTime = afterRecursiveQueryTime - getDominantTypeTime;
                    logger.log(Level.INFO, "Counting dominant types Time " + String.valueOf(afterCountDominantTypeTime));

                    //recursiveQueryForFiles(conn, dominantType, askForTypes);
                    System.out.println("Number of files = " + allRows.size());

                    resultCSV = result.toString();
                }

            }

            for(int i = 0; i < allKeys.size(); i++){
                //System.out.println("allKeys.get("+i+") ");
               // allKeys.get(i).forEach(key -> System.out.print((key == null) ? "null" : key.stringValue() + " "));
                //System.out.println();
                //System.out.println("allRows.get("+i+") columns");
                //allRows.get(i).get(0).columns.forEach((key, value) -> {System.out.print(key.stringValue() + " ");});
            }

            for (int i = 0; i < allRows.size(); i++) {
                System.out.println("allRows.size = " + allRows.size());
                System.out.println("allKeys.size = " + allKeys.size());
                gen.getPrefinishedOutput().getRowsAndKeys().add(new RowAndKey(allKeys.get(i), allRows.get(i)));
            }
            ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, String.valueOf(askForTypes));


        }
        //System.out.println("splitFilesQuery gen instanceof PrefinishedOutput<RowAndKey> " + (gen instanceof PrefinishedOutput<RowsAndKeys>));
        //System.out.println("splitFilesQuery gen instanceof PrefinishedOutput<RowAndKey> " + (gen.prefinishedOutput instanceof RowsAndKeys));
        connectionPool.closeAllConnections();
        return gen;

    }

    //    private void recursiveQueryForFiles(RepositoryConnection conn, Value dominantType, boolean askForTypes) {
//
//
//        // Make new rows and keys for the current file
//        rows = new ArrayList<>();
//        keys = new ArrayList<>();
//
//        List<Value> rootsThatHaveThisType = rootsThatHaveThisType(conn, dominantType, askForTypes);
//        System.out.println("dominant type = " + dominantType.stringValue() + " rootsThatHaveTHisType size " + rootsThatHaveThisType.size());
//        for (Value root : roots) {
//            //System.out.println("roots number " + roots.size());
//            // If the root does not have the dominant type, it will be processed later
//            if (rootHasThisType(rootsThatHaveThisType, root)) {
//                // new Row with the found subject as its id
//                Row newRow = new Row(root, dominantType, askForTypes);
//
//                //System.out.println("Number of Roots in recursiveQuery: " + roots.size() + " root: " + root.stringValue());
//                // Query the model for individual rows lead by the roots and having the predicates as the headers in the file
//                queryForSubjects(conn, newRow, root, dominantType, askForTypes);
//                //System.out.println("Row: " + newRow.id.stringValue() + " type " + newRow.type.stringValue() + " columns number " + newRow.columns.size());
//                rows.add(newRow);
//            }
//        }
//
//        allRows.add(rows);
//        allKeys.add(keys);
//
//         }
    private void recursiveQueryForFiles(ConnectionPool connectionPool, Value dominantType, boolean askForTypes) {
        System.out.println("start of recursiveQueryForFiles");
        //ArrayList<Row> rows = new ArrayList<>();
        keys = new HashSet<>();
        synchronizedKeys = Collections.synchronizedSet(keys);
        /*
        List<Value> rootsThatHaveThisType = null;
        try {
            rootsThatHaveThisType = rootsThatHaveThisType(connectionPool.getConnection(), dominantType, askForTypes);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("dominant type = " + dominantType.stringValue() + " rootsThatHaveThisType size " + rootsThatHaveThisType.size());
        System.out.println("Runtime.getRuntime().availableProcessors() " + Runtime.getRuntime().availableProcessors());
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Row>> futures = new ArrayList<>();
        int batchSize = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < roots.size(); i += batchSize) {
            List<Value> batch = roots.subList(i, Math.min(i + batchSize, roots.size()));
            for (Value root : batch) {
                if (rootHasThisType(rootsThatHaveThisType, root)) {
                    Callable<Row> task = () -> {
                        System.out.println("in task before connectionPool.getConnection()");
                        System.out.println("Thread: " + Thread.currentThread().getName());
                        RepositoryConnection conn = null;
                        try {
                            conn = connectionPool.getConnection();
                            System.out.println();
                            System.out.println("conn size " + conn.size() + " in Task");
                            System.out.println("in after RepositoryConnection conn = connectionPool.getConnection()");
                            Row newRow = new Row(root, dominantType, askForTypes);
                            System.out.println("before queryForSubjects");
                            queryForSubjects(conn, newRow, root, dominantType, askForTypes);
                            System.out.println("newRow = " + newRow.id + " " + newRow.type.stringValue() + " " + newRow.columns.size());
                            return newRow;
                        } finally {
                            if (conn != null) {
                                connectionPool.releaseConnection(conn);
                                System.out.println("Released connection for root: " + root.stringValue());
                            }
                        }
                    };
                    futures.add(executorService.submit(task));
                }
            }
            for (Future<Row> future : futures) {
                try {
                    //System.out.println("iterate through futures");
                    Row countedRow = future.get();
                    System.out.println("countedRow " + countedRow.id + " " + countedRow.type + " " + countedRow.isRdfType + " " + countedRow.columns.size() + " ");
                    countedRow.columns.entrySet().forEach(column -> System.out.print(column.getKey() + " " + column.getValue().values.get(0) + ", "));
                    rows.add(countedRow);
                } catch (InterruptedException |
                         ExecutionException e) {
                    e.printStackTrace();
                }
            }
            futures.clear();
        }

        System.out.println("before executorService.shutdown();");
        executorService.shutdown();
        System.out.println("Rows.size " + rows.size());
        System.out.println("keys.size " + this.keys.size());
*/


        List<Row> resultRows = recursiveQueryForFiles(connectionPool, dominantType, askForTypes, roots);
        //allRows.add(rows);
        allRows.add((ArrayList<Row>) resultRows);
        // Convert the Set to a List
        ArrayList<Value> listOfKeys = new ArrayList<>(synchronizedKeys);
        allKeys.add(listOfKeys);
        System.out.println("allRows.size " + allRows.size());
    }


        public List<Row> recursiveQueryForFiles (ConnectionPool connectionPool, Value dominantType,
        boolean askForTypes, Set<Value> roots) {
            ForkJoinPool forkJoinPool = new ForkJoinPool();
            Set<Value> rootsThatHaveThisType = null;
            try {RepositoryConnection conn = connectionPool.getConnection();
                rootsThatHaveThisType = rootsThatHaveThisType(conn, dominantType, askForTypes);
                connectionPool.releaseConnection(conn);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            RecursiveQueryForFilesTask task = new RecursiveQueryForFilesTask(connectionPool, dominantType, askForTypes, roots, 0, roots.size(), rootsThatHaveThisType);
            return forkJoinPool.invoke(task);
        }

        public static void queryForSubjects (RepositoryConnection conn, Row newRow, Value root, Value dominantType,
        boolean askForTypes){
            //System.out.println("Going throuugh queryForSubjects");
            String queryToGetAllPredicatesAndObjects = getQueryToGetObjectsForRoot(root, dominantType, askForTypes);
            TupleQuery query = conn.prepareTupleQuery(queryToGetAllPredicatesAndObjects);
            try (TupleQueryResult result = query.evaluate()) {

                newRow.id = root;

                // we just iterate over all solutions in the result...
                for (BindingSet solution : result) {
                    //System.out.println("we just iterate over all solutions in the result...");
                    // We found a root row
                    // Old value in the column have IRI objects and the new object is IRI
                    if (newRow.columns.keySet().stream().anyMatch(key -> key.toString().equalsIgnoreCase(solution.getBinding("p").getValue().toString()))) {
                        //System.out.println("KEY STRING MATCHES");
                    } else {
                        //System.out.println("KEY STRING NOT MATCHES in keyset: ");
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

                        //System.out.println("There is no such key (column) in the map " + solution.getBinding("p").getValue().stringValue());
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
                    //System.out.println("Wanting to delete =  " + subject + ", " + predicate +  ", " + ""  + solution.getValue("o").toString());
                    if (subject.isBNode()) {
                        conn.remove(null, predicate, solution.getValue("o"));
                        System.out.println("Removing blank from conn ");
                    }
                    conn.remove(subject, predicate, solution.getValue("o"));
                    //System.out.println("Removing not blank from conn " + subject.toString() + " predicate " + predicate.stringValue() + " obj " + solution.getValue("o").stringValue());

                }
            } catch (QueryEvaluationException ex) {
                System.out.println("QueryEvaluationException");
                ex.printStackTrace();
            }
        }

        private static String getQueryToGetObjectsForRoot (Value root, Value dominantType,boolean askForTypes){
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

