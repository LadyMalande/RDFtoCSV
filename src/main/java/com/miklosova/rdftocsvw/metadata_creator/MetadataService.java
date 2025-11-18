package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.output_processor.FileWrite;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.ConfigurationManager;

/**
 * The Metadata service. Chooses correct Metadata creator according to chosen conversion method.
 */
public class MetadataService {
    private MetadataGateway metadataGateway;
    private AppConfig config;

    /**
     * Default constructor for backward compatibility.
     * @deprecated Use {@link #MetadataService(AppConfig)} instead
     */
    @Deprecated
    public MetadataService() {
        this.config = null;
    }

    /**
     * Constructor with AppConfig.
     * @param config The application configuration
     */
    public MetadataService(AppConfig config) {
        this.config = config;
    }

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
     *
     * @param data data in inner CSV representation. They are null for streaming methods.
     */
    private void processMetadataCreation(PrefinishedOutput<RowsAndKeys> data) {
        String conversionChoice;
        String extension;
        String streamingContinuous;
        
        if (config != null) {
            conversionChoice = config.getConversionMethod();
            extension = FileWrite.getFileExtension(config.getFile());
            streamingContinuous = config.getStreamingContinuous().toString();
        } else {
            // Backward compatibility - use ConfigurationManager
            conversionChoice = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD);
            extension = FileWrite.getFileExtension(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INPUT_FILENAME));
            streamingContinuous = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.STREAMING_CONTINUOUS);
        }

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
                if (!extension.equalsIgnoreCase("nt") && streamingContinuous.equalsIgnoreCase("false")) {
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