package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.IInputParsingMethod;
import com.miklosova.rdftocsvw.input_processor.parsing_methods.IRDF4JParsingMethod;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;

public class MethodGateway {
    private IInputParsingMethod parsingMethod;

    public void setParsingMethod(IInputParsingMethod parsingMethod) {
        this.parsingMethod = parsingMethod;
    }

    public RepositoryConnection processInput(File fileToProcess, Repository db) {
        return parsingMethod.processInput(fileToProcess, db);
    }
}
