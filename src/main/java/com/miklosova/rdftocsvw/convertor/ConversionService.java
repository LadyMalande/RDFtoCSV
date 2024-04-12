package com.miklosova.rdftocsvw.convertor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.util.ArrayList;

public class ConversionService {
    private ConversionGateway conversionGateway;
    private BinaryParser basicQuery;

    public PrefinishedOutput convertByQuery(RepositoryConnection rc, String conversionChoice, Repository db) {

        conversionGateway = new ConversionGateway();
        processConversionType(conversionChoice, db);
        PrefinishedOutput convertedInput = conversionGateway.processInput(rc);
        System.out.println("Processed file: \n" + convertedInput);
        return convertedInput;
    }

    private void processConversionType(String conversionChoice, Repository db){

        switch (conversionChoice) {
            case "basicQuery":
            conversionGateway.setConversionMethod(new BasicQueryConverter(db));
                break;
            default:
                throw new IllegalArgumentException("Invalid payment method");
        }
    }
}
