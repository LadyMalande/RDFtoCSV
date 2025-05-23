package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
/**
 * The Trigstar parser from RDF4J library.
 */
public class TrigstarParser implements IRDF4JParsingMethod {
    @Override
    public RepositoryConnection processInput(RepositoryConnection conn, File fileToParse) {
        RDFFormat fileFormat = RDFFormat.TRIGSTAR;
        try {
            InputStream targetStream = new FileInputStream(fileToParse);

            conn.add(targetStream, "", fileFormat);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }
}
