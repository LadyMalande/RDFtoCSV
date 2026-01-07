package com.miklosova.rdftocsvw.converter;

import com.miklosova.rdftocsvw.support.AppConfig;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
@Disabled
@ExtendWith(MockitoExtension.class)
class ConnectionPoolTest {

    private ConnectionPool connectionPool;
    private Repository repository;
    private AppConfig config;

   /*  @BeforeEach
    void setUp() {
        repository = new SailRepository(new MemoryStore());
        repository.init();
        
        config = new AppConfig.Builder("test.ttl")
                .build();
        
        connectionPool = new ConnectionPool(repository, config);
    }

    @Test
    void testConstructor() {
        assertNotNull(connectionPool);
    }

    @Test
    void testConstructorWithNullRepository() {
        ConnectionPool nullRepoPool = new ConnectionPool(null, config);
        assertNotNull(nullRepoPool);
    }

    @Test
    void testConstructorWithNullConfig() {
        ConnectionPool nullConfigPool = new ConnectionPool(repository, null);
        assertNotNull(nullConfigPool);
    }

    @Test
    void testGetConnection() {
        RepositoryConnection connection = connectionPool.getConnection();
        assertNotNull(connection);
        assertTrue(connection.isOpen());
    }

    @Test
    void testGetMultipleConnections() {
        RepositoryConnection conn1 = connectionPool.getConnection();
        RepositoryConnection conn2 = connectionPool.getConnection();
        
        assertNotNull(conn1);
        assertNotNull(conn2);
        assertTrue(conn1.isOpen());
        assertTrue(conn2.isOpen());
    }

    @Test
    void testCloseConnection() {
        RepositoryConnection connection = connectionPool.getConnection();
        assertNotNull(connection);
        assertTrue(connection.isOpen());
        
        connection.close();
        assertFalse(connection.isOpen());
    }

    @Test
    void testPoolWithMultipleGetAndClose() {
        // Test getting and closing connections multiple times
        for (int i = 0; i < 5; i++) {
            RepositoryConnection conn = connectionPool.getConnection();
            assertNotNull(conn);
            assertTrue(conn.isOpen());
            conn.close();
        }
    }

    @Test
    void testConnectionPoolShutdown() {
        RepositoryConnection conn = connectionPool.getConnection();
        assertNotNull(conn);
        
        connectionPool.shutdown();
        
        // After shutdown, pool should be closed
        assertFalse(conn.isOpen());
    }

    @Test
    void testShutdownWithoutActiveConnections() {
        connectionPool.shutdown();
        // Should not throw exception
    }

    @Test
    void testShutdownTwice() {
        connectionPool.shutdown();
        connectionPool.shutdown();
        // Should handle multiple shutdowns gracefully
    } */
}
