package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.output_processor.FileWrite;

/**
 * The Metadata service. Chooses correct Metadata creator according to chosen conversion method.
 */
public class MetadataService {
    private MetadataGateway metadataGateway;

    /**
     * Create metadata metadata.
     *
     * @param po the inner representation of the CSV
     * @return the created metadata
     */
    public Metadata createMetadata(PrefinishedOutput<RowsAndKeys> po) {

        metadataGateway = new MetadataGateway();
        processMetadataCreation(po);
        return metadataGateway.processInput(po);
    }

    /**
     * Choose the Metadata creator according to the conversion method set.
     * @param data data in inner CSV representation. They are null for streaming methods.
     */
    private void processMetadataCreation(PrefinishedOutput<RowsAndKeys> data) {
        String conversionChoice = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD);
        String extension = FileWrite.getFileExtension(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INPUT_FILENAME));

        switch (conversionChoice.toLowerCase()) {
            case "basicquery":
                if (data != null && data.getPrefinishedOutput() != null) {
                    metadataGateway.setMetadataCreator(new BasicQueryMetadataCreator(data));
                } else {
                    throw new IllegalArgumentException("Invalid data type for basicQuery");
                }
                break;
            case "splitquery":
                if (data != null && data.getPrefinishedOutput() != null) {
                    metadataGateway.setMetadataCreator(new SplitFilesMetadataCreator(data));
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
                if (!extension.equalsIgnoreCase("nt") && ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.STREAMING_CONTINUOUS).equalsIgnoreCase("false")) {
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