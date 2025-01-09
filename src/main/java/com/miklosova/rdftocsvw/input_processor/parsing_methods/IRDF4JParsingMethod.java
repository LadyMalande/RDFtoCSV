package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.io.File;

/**
 * The interface for RDF4J parsing methods Sets a method that all RDF4J parsers must implement.
 */
public interface IRDF4JParsingMethod {
    /**
     * Process input repository connection.
     *
     * @param conn        the connection to RDF model on which we can use SPARQL for querying
     * @param fileToParse the file to parse
     * @return the repository connection
     * @throws RuntimeException the runtime exception
     */
    RepositoryConnection processInput(RepositoryConnection conn, File fileToParse) throws RuntimeException;
}
