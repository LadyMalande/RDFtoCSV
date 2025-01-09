package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.IRDF4JParsingMethod;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFParseException;

import java.io.File;
import java.io.IOException;

/**
 * The Input gateway. For RDF4J method.
 */
public class InputGateway {
    private IRDF4JParsingMethod parsingMethod;

    /**
     * Sets parsing method.
     *
     * @param parsingMethod the parsing method
     */
    public void setParsingMethod(IRDF4JParsingMethod parsingMethod) {
        this.parsingMethod = parsingMethod;
    }

    /**
     * Process input repository connection.
     *
     * @param conn          the connection to repository for SPARQL querying
     * @param fileToProcess the RDF file to process
     * @return the repository connection for SPARQL querying
     * @throws RDFParseException the rdf parse exception
     * @throws IOException       the io exception
     * @throws OutOfMemoryError  the out of memory error - if that happens, it switches the conversion method to STREAMING
     */
    public RepositoryConnection processInput(RepositoryConnection conn, File fileToProcess) throws RDFParseException, IOException, OutOfMemoryError {

        return parsingMethod.processInput(conn, fileToProcess);

    }

}
