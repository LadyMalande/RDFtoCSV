package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.IInputParsingMethod;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFParseException;

import java.io.File;
import java.io.IOException;

/**
 * The Method gateway. Part of the Strategy pattern for switching classes for converting the data.
 */
public class MethodGateway {
    private IInputParsingMethod parsingMethod;

    /**
     * Gets parsing method.
     *
     * @return the parsing method
     */
    public IInputParsingMethod getParsingMethod() {
        return parsingMethod;
    }

    /**
     * Sets parsing method.
     *
     * @param parsingMethod the parsing method
     */
    public void setParsingMethod(IInputParsingMethod parsingMethod) {
        this.parsingMethod = parsingMethod;
    }

    /**
     * Process input repository connection so that the other methods can use SPARQL querying on the data.
     *
     * @param fileToProcess the RDF file to process
     * @param db            the Repository for the connection
     * @return the repository connection from the supplied file
     * @throws RDFParseException the rdf parse exception - usually malformed RDF format
     * @throws IOException       the io exception
     */
    public RepositoryConnection processInput(File fileToProcess, Repository db) throws RDFParseException, IOException {
        return parsingMethod.processInput(fileToProcess, db);
    }
}
