package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.*;
import com.miklosova.rdftocsvw.input_processor.parsing_methods.BinaryParser;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class MetadataService {
    private MetadataGateway metadataGateway;

    public Metadata createMetadata(PrefinishedOutput<RowsAndKeys> po) {

        metadataGateway = new MetadataGateway();
        processMetadataCreation(po);
        Metadata metadata = metadataGateway.processInput(po);
        System.out.println("Processed metadata: \n" + metadata);
        return metadata;
    }

    private void processMetadataCreation(PrefinishedOutput<RowsAndKeys> data) {
        String conversionChoice = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD);

        switch (conversionChoice) {
            case "basicQuery":
                if (data != null && data.getPrefinishedOutput() != null) {
                    metadataGateway.setMetadataCreator(new BasicQueryMetadataCreator((PrefinishedOutput<RowsAndKeys>) data));
                } else {
                    throw new IllegalArgumentException("Invalid data type for basicQuery");
                }
                break;
            case "splitQuery":
                if (data != null && data.getPrefinishedOutput() != null) {
                    metadataGateway.setMetadataCreator(new SplitFilesMetadataCreator((PrefinishedOutput<RowsAndKeys>) data));
                } else {
                    throw new IllegalArgumentException("Invalid data type for splitQuery");
                }
                break;
            case "codelistQuery":
                if (data != null && data.getPrefinishedOutput() != null) {
                    metadataGateway.setMetadataCreator(new CodelistQueryMetadataCreator((PrefinishedOutput<RowsAndKeys>) data));
                } else {
                    throw new IllegalArgumentException("Invalid data type for codelistQuery");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid conversion method");
        }
    }
}