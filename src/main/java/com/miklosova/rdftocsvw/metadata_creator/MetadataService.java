package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.convertor.RowsAndKeys;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.FileWrite;

public class MetadataService {
    private MetadataGateway metadataGateway;

    public Metadata createMetadata(PrefinishedOutput<RowsAndKeys> po) {

        metadataGateway = new MetadataGateway();
        processMetadataCreation(po);
        //System.out.println("Processed metadata: \n" + metadata);
        return metadataGateway.processInput(po);
    }

    private void processMetadataCreation(PrefinishedOutput<RowsAndKeys> data) {
        String conversionChoice = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD);
        System.out.println(" conversionChoice = " + conversionChoice);
        String extension = FileWrite.getFileExtension(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INPUT_FILENAME));

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
                    metadataGateway.setMetadataCreator(new CodelistQueryMetadataCreator());
                } else {
                    throw new IllegalArgumentException("Invalid data type for codelistQuery");
                }
                break;
            case "bigFileStreaming":

                if (!extension.equalsIgnoreCase("nt")) {
                    throw new IllegalArgumentException("Invalid file extension for parsing streaming data. Expecting extension .nt, was " + extension);
                } else {
                    metadataGateway.setMetadataCreator(new BigFileStreamingNTriplesMetadataCreator(null));
                }
                break;
            case "streaming":
                if (!extension.equalsIgnoreCase("nt")) {
                    throw new IllegalArgumentException("Invalid file extension for parsing streaming data. Expecting extension .nt, was " + extension);
                } else {
                    metadataGateway.setMetadataCreator(new StreamingNTriplesMetadataCreator(null));
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid conversion method");
        }
    }
}