package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import com.miklosova.rdftocsvw.input_processor.ParsingService;
import com.miklosova.rdftocsvw.input_processor.RDFAssetManager;
import lombok.extern.java.Log;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.*;
@Log
public class RDF4JMethod implements IInputParsingMethod{
    @Override
    public RepositoryConnection processInput(File fileToParse, Repository db) {

        ParsingService ps = new ParsingService();

        // Query in rdf4j


        // Open a connection to the database
        RepositoryConnection conn = db.getConnection();
        //RDFFormat fileFormat = ps.processInput(conn, fileToParse);
        // add the RDF data from the inputstream directly to our database
        System.out.println("ps.processInput(conn, fileToParse); " + fileToParse);
        conn = ps.processInput(conn, fileToParse);
        if (conn.isEmpty())
            throw new RuntimeException("No loader registered for file type \"." + fileToParse.getAbsolutePath() + "\" files");
        // add the RDF data from the inputstream directly to our database

        return conn;

    }
}
