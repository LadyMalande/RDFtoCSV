package com.miklosova.rdftocsvw.converter;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionPool {
    private final BlockingQueue<RepositoryConnection> connectionPool;
    private final Repository repository;

    public ConnectionPool(Repository repository, int poolSize) {
        this.repository = repository;
        this.connectionPool = new ArrayBlockingQueue<>(poolSize);
        System.out.println("poolSize " + poolSize);
        initializeConnections(poolSize);
    }

    private void initializeConnections(int poolSize) {
        for (int i = 0; i < poolSize; i++) {
            connectionPool.add(repository.getConnection());
        }
    }

    public RepositoryConnection getConnection() throws InterruptedException {
        return connectionPool.take();
    }

    public void releaseConnection(RepositoryConnection conn) {
        connectionPool.offer(conn);
    }

    public void closeAllConnections() {
        connectionPool.forEach(RepositoryConnection::close);
    }
}