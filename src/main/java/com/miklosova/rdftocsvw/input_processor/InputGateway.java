package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.IRDF4JParsingMethod;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;

public class InputGateway {
    private IRDF4JParsingMethod parsingMethod;

    public void setParsingMethod(IRDF4JParsingMethod parsingMethod) {
        this.parsingMethod = parsingMethod;
    }

    public RepositoryConnection processInput(RepositoryConnection conn, File fileToProcess) {

        return parsingMethod.processInput(conn , fileToProcess);
    }

}