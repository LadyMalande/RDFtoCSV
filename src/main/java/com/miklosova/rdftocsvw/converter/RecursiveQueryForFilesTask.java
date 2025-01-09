package com.miklosova.rdftocsvw.converter;

import com.miklosova.rdftocsvw.converter.data_structure.Row;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.miklosova.rdftocsvw.converter.SplitFilesQueryConverter.queryForSubjects;
import static com.miklosova.rdftocsvw.support.ConverterHelper.rootHasThisType;

/**
 * The type Recursive query for files task.
 */
public class RecursiveQueryForFilesTask extends RecursiveTask<List<Row>> {
    private static final Logger logger = Logger.getLogger(RecursiveQueryForFilesTask.class.getName());

    private final ConnectionPool connectionPool;
    private final Value dominantType;
    private final boolean askForTypes;
    private final Set<Value> roots;
    private final int start;
    private final int end;
    private final Set<Value> rootsThatHaveThisType;

    /**
     * Instantiates a new Recursive query for files task.
     *
     * @param connectionPool        the connection pool
     * @param dominantType          the dominant type
     * @param askForTypes           the ask for types
     * @param roots                 the roots
     * @param start                 the start
     * @param end                   the end
     * @param rootsThatHaveThisType the roots that have this type
     */
    public RecursiveQueryForFilesTask(ConnectionPool connectionPool, Value dominantType, boolean askForTypes, Set<Value> roots, int start, int end, Set<Value> rootsThatHaveThisType) {
        this.connectionPool = connectionPool;
        this.dominantType = dominantType;
        this.askForTypes = askForTypes;
        this.roots = roots;
        this.start = start;
        this.end = end;
        this.rootsThatHaveThisType = rootsThatHaveThisType;
    }

    @Override
    protected List<Row> compute() {
        if (start >= end || start >= roots.size() || end > roots.size()) {
            return new ArrayList<>(); // Return an empty list for invalid ranges
        }
            if (end - start <= Runtime.getRuntime().availableProcessors()) {
                // Convert the Set to a List
                List<Value> rootsList = new ArrayList<>(roots);
                return processBatch(rootsList.subList(start, end), rootsThatHaveThisType);
            } else {
                int mid = (start + end) / 2;
                RecursiveQueryForFilesTask leftTask = new RecursiveQueryForFilesTask(connectionPool, dominantType, askForTypes, roots, start, mid, rootsThatHaveThisType);
                RecursiveQueryForFilesTask rightTask = new RecursiveQueryForFilesTask(connectionPool, dominantType, askForTypes, roots, mid, end, rootsThatHaveThisType);
                invokeAll(leftTask, rightTask);
                List<Row> leftResult = leftTask.join();
                List<Row> rightResult = rightTask.join();
                leftResult.addAll(rightResult);
                return leftResult;
            }
        }

        private List<Row> processBatch (List < Value > batch, Set < Value > rootsThatHaveThisType){

            List<Row> rows = new ArrayList<>();
            for (Value root : batch) {
                if (rootHasThisType(rootsThatHaveThisType, root)) {
                    try {
                        RepositoryConnection conn = connectionPool.getConnection();
                        Row newRow = new Row(root, dominantType, askForTypes);
                        queryForSubjects(conn, newRow, root, dominantType, askForTypes);
                        rows.add(newRow);
                        connectionPool.releaseConnection(conn);
                    } catch (InterruptedException e) {
                        logger.log(Level.SEVERE, "There was an error with Threading when converting RDF data to multiple tables in RDF4J conversion method.");
                    }
                }
            }
            return rows;
        }
    }
