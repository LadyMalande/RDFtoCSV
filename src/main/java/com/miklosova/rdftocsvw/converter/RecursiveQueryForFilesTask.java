package com.miklosova.rdftocsvw.converter;

import com.miklosova.rdftocsvw.converter.data_structure.Row;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

import static com.miklosova.rdftocsvw.converter.SplitFilesQueryConverter.queryForSubjects;
import static com.miklosova.rdftocsvw.support.ConverterHelper.rootHasThisType;

public class RecursiveQueryForFilesTask extends RecursiveTask<List<Row>> {
    private final ConnectionPool connectionPool;
    private final Value dominantType;
    private final boolean askForTypes;
    private final Set<Value> roots;
    private final int start;
    private final int end;
    private Set<Value> rootsThatHaveThisType;

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
                //System.out.println("Dividing roots to half: " + start + "-" + end);
                RecursiveQueryForFilesTask leftTask = new RecursiveQueryForFilesTask(connectionPool, dominantType, askForTypes, roots, start, mid, rootsThatHaveThisType);
                RecursiveQueryForFilesTask rightTask = new RecursiveQueryForFilesTask(connectionPool, dominantType, askForTypes, roots, mid, end, rootsThatHaveThisType);
                invokeAll(leftTask, rightTask);
                if (end % 10 == 0) {
                    //System.out.println("List<Row> rightResult = rightTask.join(); " + mid + "-" + end);
                    //System.out.println("List<Row> leftResult = leftTask.join(); " + start + "-" + mid);
                }
                List<Row> leftResult = leftTask.join();
                //System.out.println("List<Row> rightResult = rightTask.join(); " + mid + "-" + end);
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
                        e.printStackTrace();
                    }
                } else {
                    //System.out.println("Root does not have this type");
                }
            }
            return rows;
        }
    }
