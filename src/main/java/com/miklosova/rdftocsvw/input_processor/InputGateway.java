package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.IRDF4JParsingMethod;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;

import java.io.File;
import java.io.IOException;

public class InputGateway {
    private IRDF4JParsingMethod parsingMethod;

    public void setParsingMethod(IRDF4JParsingMethod parsingMethod) {
        this.parsingMethod = parsingMethod;
    }

    public RepositoryConnection processInput(RepositoryConnection conn, File fileToProcess) throws RDFParseException, IOException {

        return parsingMethod.processInput(conn , fileToProcess);
    }

}
