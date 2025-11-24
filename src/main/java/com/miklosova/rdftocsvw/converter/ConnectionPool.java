package com.miklosova.rdftocsvw.converter;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The Connection pool for multiple threads handling the querying.
 */
public class ConnectionPool {
    private final BlockingQueue<RepositoryConnection> connectionPool;
    private final Repository repository;

    /**
     * Instantiates a new Connection pool.
     *
     * @param repository the repository
     * @param poolSize   the pool size
     */
    public ConnectionPool(Repository repository, int poolSize) {
        this.repository = repository;
        this.connectionPool = new ArrayBlockingQueue<>(poolSize);
        initializeConnections(poolSize);
    }

    private void initializeConnections(int poolSize) {
        for (int i = 0; i < poolSize; i++) {
            connectionPool.add(repository.getConnection());
        }
    }

    /**
     * Gets connection.
     *
     * @return the connection
     * @throws InterruptedException the interrupted exception
     */
    public RepositoryConnection getConnection() throws InterruptedException {
        return connectionPool.take();
    }

    /**
     * Release connection.
     *
     * @param conn the connection for querying
     */
    public void releaseConnection(RepositoryConnection conn) {
        connectionPool.offer(conn);
    }

    /**
     * Close all connections.
     */
    public void closeAllConnections() {
        connectionPool.forEach(RepositoryConnection::close);
    }
}
