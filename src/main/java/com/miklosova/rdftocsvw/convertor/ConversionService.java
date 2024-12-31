package com.miklosova.rdftocsvw.convertor;

import com.miklosova.rdftocsvw.support.ConfigurationManager;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class ConversionService {
    private ConversionGateway conversionGateway;

    public PrefinishedOutput<RowsAndKeys> convertByQuery(RepositoryConnection rc, Repository db) {
        if (rc == null) {
            return null;
        }
        conversionGateway = new ConversionGateway();
        processConversionType(db);
        PrefinishedOutput<RowsAndKeys> convertedInput = conversionGateway.processInput(rc);
        System.out.println("Processed file: \n" + convertedInput);
        return convertedInput;
    }

    private void processConversionType(Repository db) {
        String conversionChoice = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD);

        switch (conversionChoice) {
            case "basicQuery", "trivial" -> conversionGateway.setConversionMethod(new BasicQueryConverter(db));
            case "splitQuery" -> conversionGateway.setConversionMethod(new SplitFilesQueryConverter(db));
            default -> throw new IllegalArgumentException("Invalid payment method");
        }
    }
}
