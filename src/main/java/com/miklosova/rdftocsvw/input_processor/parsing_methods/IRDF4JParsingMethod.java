package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.io.File;

public interface IRDF4JParsingMethod {
    RepositoryConnection processInput(RepositoryConnection conn, File fileToParse) throws RuntimeException;
}
