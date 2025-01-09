package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
/**
 * The HdtParser parser from RDF4J library. Did not work, but maybe will in the future.
 */
@Deprecated
public class HdtParser implements IRDF4JParsingMethod {
    @Override
    public RepositoryConnection processInput(RepositoryConnection conn, File fileToParse) {
        RDFFormat fileFormat = RDFFormat.HDT;
        try {
            InputStream targetStream = new FileInputStream(fileToParse);

            conn.add(targetStream, "", fileFormat);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }
}
