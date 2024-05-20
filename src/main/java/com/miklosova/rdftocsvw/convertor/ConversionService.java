package com.miklosova.rdftocsvw.convertor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.*;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.util.ArrayList;

public class ConversionService {
    private ConversionGateway conversionGateway;
    private BinaryParser basicQuery;

    public PrefinishedOutput convertByQuery(RepositoryConnection rc, Repository db) {

        conversionGateway = new ConversionGateway();
        processConversionType( db);
        PrefinishedOutput convertedInput = conversionGateway.processInput(rc);
        System.out.println("Processed file: \n" + convertedInput);
        return convertedInput;
    }

    private void processConversionType(Repository db){
        String conversionChoice = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD);

        switch (conversionChoice) {
            case "basicQuery":
            conversionGateway.setConversionMethod(new BasicQueryConverter(db));
                break;
            case "splitQuery":
                conversionGateway.setConversionMethod(new SplitFilesQueryConverter(db));
                break;
            case "codelistQuery":
                conversionGateway.setConversionMethod(new CodelistQueryConverter(db));
                break;
            default:
                throw new IllegalArgumentException("Invalid payment method");
        }
    }
}
