package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.output_processor.FileWrite;

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

        switch (conversionChoice.toLowerCase()) {
            case "basicquery":
                if (data != null && data.getPrefinishedOutput() != null) {
                    metadataGateway.setMetadataCreator(new BasicQueryMetadataCreator((PrefinishedOutput<RowsAndKeys>) data));
                } else {
                    throw new IllegalArgumentException("Invalid data type for basicQuery");
                }
                break;
            case "splitquery":
                if (data != null && data.getPrefinishedOutput() != null) {
                    metadataGateway.setMetadataCreator(new SplitFilesMetadataCreator((PrefinishedOutput<RowsAndKeys>) data));
                } else {
                    throw new IllegalArgumentException("Invalid data type for splitQuery");
                }
                break;
            case "bigfilestreaming":

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
                    metadataGateway.setMetadataCreator(new StreamingNTriplesMetadataCreator());
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid conversion method");
        }
    }
}