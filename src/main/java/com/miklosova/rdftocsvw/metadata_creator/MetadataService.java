package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.*;
import com.miklosova.rdftocsvw.input_processor.parsing_methods.BinaryParser;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class MetadataService {
    private MetadataGateway metadataGateway;

    public Metadata createMetadata(PrefinishedOutput po) {

        metadataGateway = new MetadataGateway();
        processMetadataCreation(po);
        Metadata metadata = metadataGateway.processInput(po);
        System.out.println("Processed metadata: \n" + metadata);
        return metadata;
    }

    private void processMetadataCreation(PrefinishedOutput data){
        String conversionChoice = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD);

        switch (conversionChoice) {
            case "basicQuery":
                metadataGateway.setMetadataCreator(new BasicQueryMetadataCreator(data));
                break;
            case "splitQuery":
                metadataGateway.setMetadataCreator(new SplitFilesMetadataCreator(data));
                break;
            case "codelistQuery":
                metadataGateway.setMetadataCreator(new CodelistQueryMetadataCreator(data));
                break;
            default:
                throw new IllegalArgumentException("Invalid conversion method");
        }
    }
}