package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.io.File;
import java.io.IOException;

/**
 * The interface Input parsing method. This is an interface all Classes eligible for handling input data must implement.
 */
public interface IInputParsingMethod {
    /**
     * Process input repository connection.
     *
     * @param fileToParse the file to parse
     * @param db          the db
     * @return the repository connection
     * @throws IOException the io exception
     */
    RepositoryConnection processInput(File fileToParse, Repository db) throws IOException;
}
