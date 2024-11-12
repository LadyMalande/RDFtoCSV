package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.IInputParsingMethod;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFParseException;

import java.io.File;
import java.io.IOException;

public class MethodGateway {
    private IInputParsingMethod parsingMethod;

    public void setParsingMethod(IInputParsingMethod parsingMethod) {
        this.parsingMethod = parsingMethod;
    }

    public RepositoryConnection processInput(File fileToProcess, Repository db) throws RDFParseException, IOException {
        return parsingMethod.processInput(fileToProcess, db);
    }
}
