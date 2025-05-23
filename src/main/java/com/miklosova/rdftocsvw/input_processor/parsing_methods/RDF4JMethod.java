package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import com.miklosova.rdftocsvw.input_processor.ParsingService;
import lombok.extern.java.Log;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFParseException;

import java.io.File;
import java.io.IOException;

/**
 * The type RDF4J method for parsing data. Chooses the correct RDF serialization parser. Returns connection ready for SPARQL querying.
 */
@Log
public class RDF4JMethod implements IInputParsingMethod {
    @Override
    public RepositoryConnection processInput(File fileToParse, Repository db) throws RDFParseException, IOException {

        ParsingService ps = new ParsingService();

        // Query in rdf4j

        // Open a connection to the database
        RepositoryConnection conn = db.getConnection();
        //RDFFormat fileFormat = ps.processInput(conn, fileToParse);
        // add the RDF data from the inputstream directly to our database
        conn = ps.processInput(conn, fileToParse);
        if (conn.isEmpty())
            throw new RuntimeException("No loader registered for file type \"." + fileToParse.getAbsolutePath() + "\" files OR 'NO TRIPLES FOUND'");
        // add the RDF data from the inputstream directly to our database

        return conn;
    }
}
