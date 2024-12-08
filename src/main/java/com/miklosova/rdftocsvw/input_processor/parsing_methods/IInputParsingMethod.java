package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.io.File;
import java.io.IOException;

public interface IInputParsingMethod {
    RepositoryConnection processInput(File fileToParse, Repository db) throws IOException;
}
