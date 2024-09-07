package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import org.apache.commons.math3.linear.IllConditionedOperatorException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;
import java.io.IOException;

public interface IRDF4JParsingMethod {
    RepositoryConnection processInput(RepositoryConnection conn, File fileToParse) throws RuntimeException;
}
