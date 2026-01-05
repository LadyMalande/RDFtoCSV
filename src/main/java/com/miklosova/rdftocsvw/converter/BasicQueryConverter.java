package com.miklosova.rdftocsvw.converter;

import com.miklosova.rdftocsvw.converter.data_structure.*;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.support.AppConfig;

import com.miklosova.rdftocsvw.support.ConverterHelper;
import lombok.extern.java.Log;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.SailConnectionListener;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.miklosova.rdftocsvw.support.StandardModeCsvwIris.CSVW_TableGroup;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

/**
 * The Basic query converter. For RDF4J method, ONE table creation.
 *
 * The cascade solution, that is commented in the middle of the file is following:
 *                 This solution works fine for shallow cascade, like Code lists, but it is not detailed enough for complex RDF structures with a lot of dependences between nodes.
 * In this version, there is a simpler solution, with not as pretty CSV, but on the other hand 100% working and data safe.
 *  This version is suitable for files with a lot of columns, but only one type of entity.
 *  If the data is sparse in this table, the SplitFilesQueryConverter will suit data like that much better, as it reduces white spaces.
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
     * Cache of how many times each resource is referenced as an object.
     * Key: Resource URI, Value: reference count
     * Built once upfront to avoid expensive per-triple COUNT queries.
     */
    private Map<String, Integer> objectReferenceCounts;
    /**
     * HashSet to track seen row IDs for O(1) duplicate detection.
     * Avoids O(n) stream.anyMatch() which causes O(n²) overall performance.
     */
    private Set<Value> seenRowIds;
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
     * The AppConfig instance.
     */
    private AppConfig config;

    /**
     * Instantiates a new Basic query converter.
     *
     * @param db the db
     * @deprecated Use {@link #BasicQueryConverter(Repository, AppConfig)} instead
     */
    @Deprecated
    public BasicQueryConverter(Repository db) {
        this.keys = new HashSet<>();
        this.db = db;
        this.IRIalreadyProcessedTimes = new HashMap<>();
        this.config = null;
    }

    /**
     * Instantiates a new Basic query converter with AppConfig.
     *
     * @param db the db
     * @param config the application configuration
     */
    public BasicQueryConverter(Repository db, AppConfig config) {
        this.keys = new HashSet<>();
        this.db = db;
        this.IRIalreadyProcessedTimes = new HashMap<>();
        this.config = config;
        super.config = config; // Set parent's config field
        this.rows = new ArrayList<>();
        this.seenRowIds = new HashSet<>();
    }

    @Override
    public PrefinishedOutput<RowsAndKeys> convertWithQuery(RepositoryConnection rc) {
        com.miklosova.rdftocsvw.support.ProgressLogger.startStage(com.miklosova.rdftocsvw.support.ProgressLogger.Stage.CONVERTING);
        
        this.rc = rc;
        changeBNodesForIri(rc);
        deleteBlankNodes(rc);
        rows = new ArrayList<>();
        new PrefinishedOutput<>((new RowAndKey.RowAndKeyFactory()).factory());
        PrefinishedOutput<RowAndKey> queryResult;
        String query = getCSVTableQueryForModel(true);
        
        com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(com.miklosova.rdftocsvw.support.ProgressLogger.Stage.CONVERTING, 25, "Executing SPARQL queries");
        
        try {
            queryResult = queryRDFModel(query, true);

        } catch (IndexOutOfBoundsException ex) {
            query = getCSVTableQueryForModel(false);
            queryResult = queryRDFModel(query, false);
        }
        
        com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(com.miklosova.rdftocsvw.support.ProgressLogger.Stage.CONVERTING, 75, "Processing query results");
        
        RowsAndKeys rak = new RowsAndKeys();
        ArrayList<RowAndKey> rakArray = new ArrayList<>();
        assert queryResult != null;
        rakArray.add(queryResult.getPrefinishedOutput());
        rak.setRowsAndKeys(rakArray);
        
        com.miklosova.rdftocsvw.support.ProgressLogger.completeStage(com.miklosova.rdftocsvw.support.ProgressLogger.Stage.CONVERTING);
        return new PrefinishedOutput<>(rak);
    }


    private PrefinishedOutput<RowAndKey> queryRDFModel(String queryString, boolean askForTypes) throws IndexOutOfBoundsException {
        PrefinishedOutput<RowAndKey> gen = new PrefinishedOutput<>((new RowAndKey.RowAndKeyFactory()).factory());
        config.setConversionHasRdfTypes(askForTypes);
        // Query the data and pass the result as String

        // Query in rdf4j
        // Create a new Repository.
        
        com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(
            com.miklosova.rdftocsvw.support.ProgressLogger.Stage.CONVERTING, 30, "Preparing SPARQL queries"
        );

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
                com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(
                    com.miklosova.rdftocsvw.support.ProgressLogger.Stage.CONVERTING, 40, "Executing SPARQL query"
                );
                
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
                    
                    com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(
                        com.miklosova.rdftocsvw.support.ProgressLogger.Stage.CONVERTING, 50, 
                        String.format("Processing %d query results", resultList.size())
                    );
                    
                    // Build reference count cache upfront to avoid per-triple COUNT queries
                    //logger.info("[RDF4J-DEBUG] Building object reference count cache...");
                    long cacheStartTime = System.currentTimeMillis();
                    objectReferenceCounts = buildObjectReferenceCounts(conn);
                    long cacheTime = System.currentTimeMillis() - cacheStartTime;
                    //logger.info(String.format("[RDF4J-DEBUG] Built reference cache with %d entries in %dms", objectReferenceCounts.size(), cacheTime));
                    
                    roots = new HashSet<>();
                    int processedCount = 0;
                    int totalResults = resultList.size();
                    long loopStartTime = System.currentTimeMillis();
                    long lastLogTime = loopStartTime;
                    
                    //logger.info("[RDF4J-DEBUG] Starting main processing loop for " + totalResults + " results");
                    
                    for (BindingSet solution : resultList) {
                        processedCount++;
                        
                        // Log every 10,000 rows with timing info
                        if (processedCount % 10000 == 0) {
                            long currentTime = System.currentTimeMillis();
                            long elapsed = currentTime - lastLogTime;
                            long totalElapsed = currentTime - loopStartTime;
                            double rowsPerSecond = (processedCount * 1000.0) / totalElapsed;
                            //logger.info(String.format("[RDF4J-DEBUG] Processed %d/%d rows (%.1f%%) - Last 10K in %dms - Avg %.1f rows/sec", processedCount, totalResults, (processedCount * 100.0 / totalResults), elapsed, rowsPerSecond));
                            lastLogTime = currentTime;
                        }
                        
                        if (processedCount % Math.max(1, totalResults / 5) == 0) {
                            int progress = 50 + (processedCount * 20 / totalResults);
                            com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(
                                com.miklosova.rdftocsvw.support.ProgressLogger.Stage.CONVERTING, progress,
                                String.format("Processing row %d/%d", processedCount, totalResults)
                            );
                        }
                        // ... and print out the value of the variable binding for ?s and ?n
                        if (solution.getValue("o").stringValue().equalsIgnoreCase(CSVW_TableGroup)) {
                            StandardModeConverter smc = new StandardModeConverter(db, config);
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
                        PrefinishedOutput<RowAndKey> queryWasBig = queryForSubjects(conn, newRow, solution.getValue("s"), solution.getValue("s"), 0);
                        if(queryWasBig!=null){
                            return queryWasBig;
                        }

                        long deleteStartTime = System.currentTimeMillis();
                        deleteQueryToGetObjectsForRoot(solution.getValue("s"));
                        long deleteTime = System.currentTimeMillis() - deleteStartTime;
                        if (deleteTime > 100) {
                            logger.warning(String.format("[RDF4J-DEBUG] deleteQueryToGetObjectsForRoot took %dms for row %d/%d",
                                deleteTime, processedCount, totalResults));
                        }

                        // O(1) duplicate check using HashSet instead of O(n) stream
                        if (!seenRowIds.contains(newRow.id)) {
                            rows.add(newRow);
                            seenRowIds.add(newRow.id);
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

            config.setConversionHasRdfTypes(askForTypes);
        } catch (IndexOutOfBoundsException ex) {
            throw new IndexOutOfBoundsException();
        }

        // Verify the output in console
        gen.getPrefinishedOutput().getRows().addAll(rows);
        gen.getPrefinishedOutput().getKeys().addAll(keys);
        return gen;
    }

    private PrefinishedOutput<RowAndKey> queryForSubjects(RepositoryConnection conn, Row newRow, Value root, Value subject, int level) {
        long queryStartTime = System.currentTimeMillis();
        String queryToGetAllPredicatesAndObjects = getQueryToGetObjectsForRoot(subject);
        TupleQuery query = conn.prepareTupleQuery(queryToGetAllPredicatesAndObjects);
        try (TupleQueryResult result = query.evaluate()) {
            long queryExecTime = System.currentTimeMillis() - queryStartTime;
            if (queryExecTime > 100) {
                logger.warning(String.format("[RDF4J-DEBUG] queryForSubjects took %dms for subject: %s (level=%d)", 
                    queryExecTime, subject.stringValue(), level));
            }


            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                // We found a root row
                Value keyForColumnsMap = solution.getBinding("p").getValue();
                if (level != 0) {
                    ValueFactory valueFactory = SimpleValueFactory.getInstance();
//System.out.println("count of stream for p="+keyForColumnsMap.stringValue()+" and subject="+ subject.stringValue() +" " + result.);
                    // Create a new IRI

                    String newValueForMap = solution.getBinding("p").getValue().stringValue() + "_MULTILEVEL_";// + ((IRI) subject).getLocalName();
                    keyForColumnsMap = valueFactory.createIRI(newValueForMap);
                }
                if (newRow.columns.containsKey(keyForColumnsMap) &&
                        newRow.columns.get(keyForColumnsMap).type == TypeOfValue.IRI &&
                        solution.getBinding("o").getValue().isIRI()) {
                    List<Value> oldStringValue = newRow.columns.get(keyForColumnsMap).values;
                    if(newRow.columns.entrySet().stream().anyMatch((column) -> oldStringValue.contains(column.getValue().id))){
                        // If there is any value, that is having another chain of objects going from it, abort this chaining and make a new simpler csv
                        return convertData();
                    }
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

                    if(newRow.columns.entrySet().stream().anyMatch((column) -> column.getValue().values.contains(subject) && column.getValue().values.size() > 1)){
                        // If the subject is in a list of values, abort to avoid cross subject/object littering
                        // Or if the object goes to a list that is not empty and any of its neighbors has a chain going {solved in branch above}
                        // Process the data in a simple way to avoid nested values in nested values
                        return convertData();
                    }
                    newRow.columns.put(keyForColumnsMap, new TypeIdAndValues(subject, newType,
                            new ArrayList<>(List.of(solution.getBinding("o").getValue()))));

                }

                keys.add(keyForColumnsMap);
                /*
                // This route would make cascade header structure, but the problem is, when the data start to multiply and each of the contents starts having its own structure.
                // This solution works fine for shallow cascade, like Code lists, but it is not detailed enough for complex RDF structures with a lot of dependences between nodes.
                // Note: simpleBasicQuery check removed with ConfigurationManager
                if (solution.getValue("o") != null && solution.getValue("o").isIRI()) {
                        if (solution.getValue("o") != root) {
                            queryForSubjects(conn, newRow, root, solution.getValue("o"), level + 1);
                        }
                    }
                }

                 */

                // Delete the triple from the storage
                Resource subjectToDelete = Values.iri(subject.toString());
                IRI predicate = Values.iri(solution.getValue("p").toString());
                Value object = solution.getValue("o");
                
                // If the object is an IRI, update its reference count since we're about to delete this triple
                if (object.isIRI()) {
                    String objectKey = object.stringValue();
                    Integer objRefCount = objectReferenceCounts.getOrDefault(objectKey, 0);
                    if (objRefCount > 0) {
                        // Decrement because this triple (subject->predicate->object) is being deleted
                        objectReferenceCounts.put(objectKey, objRefCount - 1);
                    }
                }
                
                // Check if the SUBJECT appears as an object in other triples (is it referenced elsewhere?)
                String subjectKey = subjectToDelete.toString();
                Integer refCount = objectReferenceCounts.getOrDefault(subjectKey, 0);
                
                if (refCount <= 1) {
                    // Subject is only referenced once (or not at all) - safe to delete all its triples
                    conn.remove(subjectToDelete, predicate, object);
                    // After deletion, this subject is no longer referenced anywhere
                    objectReferenceCounts.put(subjectKey, 0);
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
        return null;
    }

    /**
     * Build a cache of how many times each resource appears as an object in the repository.
     * This replaces hundreds of thousands of individual COUNT queries with a single GROUP BY query.
     * 
     * @param conn Repository connection
     * @return Map of resource URI -> reference count
     */
    private Map<String, Integer> buildObjectReferenceCounts(RepositoryConnection conn) {
        Map<String, Integer> counts = new HashMap<>();
        
        // Single query to count all object references - much faster than individual COUNT queries
        String query = """
            SELECT ?o (COUNT(?s) AS ?count)
            WHERE {
                ?s ?p ?o .
                FILTER(isIRI(?o))
            }
            GROUP BY ?o
            """;
        
        TupleQuery tq = conn.prepareTupleQuery(query);
        try (TupleQueryResult result = tq.evaluate()) {
            while (result.hasNext()) {
                BindingSet bs = result.next();
                String objectUri = bs.getValue("o").stringValue();
                int count = Integer.parseInt(((Literal) bs.getValue("count")).getLabel());
                counts.put(objectUri, count);
            }
        }
        
        return counts;
    }
    
    /**
     * @deprecated This method is too slow - replaced by objectReferenceCounts cache
     */
    @Deprecated
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
        // Delete all triples where root is the subject
        String del = "DELETE WHERE { <" + root.stringValue() + "> ?p ?o . }";
        Update deleteQuery = rc.prepareUpdate(del);
        deleteQuery.execute();
    }

    public PrefinishedOutput<RowAndKey> convertData() {
        // Convert the table to intermediate data for processing into metadata
        ConversionService cs = new ConversionService(config);
        Repository newdb = new SailRepository(new MemoryStore());
        MethodService methodService = new MethodService(config);

        if (config == null) {
            throw new IllegalStateException("AppConfig is required");
        }
        String readMethod = config.getParsing();
        String inputFilename = config.getFile();
        try {
            config.setSimpleBasicQuery(true);
            RepositoryConnection newRc = methodService.processInput(inputFilename, readMethod, newdb);
            PrefinishedOutput<RowsAndKeys> rk = new PrefinishedOutput<>((new RowsAndKeys.RowsAndKeysFactory()).factory());
            rk = cs.convertByQuery(newRc, newdb);
            PrefinishedOutput<RowAndKey> rk1 = new PrefinishedOutput<>((new RowAndKey.RowAndKeyFactory()).factory());
            rk1.setPrefinishedOutput(rk.getPrefinishedOutput().getRowsAndKeys().get(0));
            return rk1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

