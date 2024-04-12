package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;

public class MethodService {
    private MethodGateway methodGateway;

    private RDF4JMethod rDF4JMethod;

    public RepositoryConnection processInput(String fileName, String methodChoice, Repository db) {
        methodGateway = new MethodGateway();
        processMethodChoice(methodChoice);
        File fileToRead = new File(fileName);
        return methodGateway.processInput(fileToRead, db);
    }

    private void processMethodChoice(String fileName){
        String[] splitName = fileName.split("\\.");
        String fileExtension = splitName[splitName.length - 1];

        switch (fileExtension) {
            case "rdf4j":
                methodGateway.setParsingMethod(new RDF4JMethod());
                break;
            default:
                throw new IllegalArgumentException("Invalid payment method");
        }
    }
}
