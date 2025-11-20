package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.TableSchema;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.ConfigurationManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The BigFileStreaming N-Triples metadata creator. First reads the data and creates metadata. During second read it
 * writes the data according to metadata to files.
 * This method is very slow for even a bit larger files.
 */
public class BigFileStreamingNTriplesMetadataCreator extends StreamingMetadataCreator implements IMetadataCreator {

    private static final Logger logger = Logger.getLogger(BigFileStreamingNTriplesMetadataCreator.class.getName());
    /**
     * The Metadata.
     */
    Metadata metadata;
    /**
     * The Counter for keeping track of processed triples.
     */
    int counter;

    /**
     * Instantiates a new Big file streaming n triples metadata creator.
     *
     * @param data the data
     * @deprecated Use {@link #BigFileStreamingNTriplesMetadataCreator(PrefinishedOutput, AppConfig)} instead
     */
    @Deprecated
    public BigFileStreamingNTriplesMetadataCreator(PrefinishedOutput<RowsAndKeys> data) {
        this(data, null);
    }

    /**
     * Instantiates a new Big file streaming n triples metadata creator with AppConfig.
     *
     * @param data the data
     * @param config the application configuration
     */
    public BigFileStreamingNTriplesMetadataCreator(PrefinishedOutput<RowsAndKeys> data, AppConfig config) {
        super(config);
        this.metadata = new Metadata(config);
    }

    @Override
    public Metadata addMetadata(PrefinishedOutput<?> info) {
        File f = new File(fileNameToRead);
        Table newTable = new Table(f.getName() + ".csv");
        config.setIntermediateFileNames(f.getName() + ".csv");
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES, f.getName() + ".csv");
        metadata.getTables().add(newTable);
        tableSchema = new TableSchema();
        tableSchema.setPrimaryKey("Subject");
        createFirstColumn();


        newTable.setTableSchema(tableSchema);

        readFileWithStreaming();
        repairMetadataAndMakeItJsonld(metadata);

        return metadata;
    }

    private void readFileWithStreaming() {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileNameToRead))) {
            String line;
            // Read file line by line
            while ((line = reader.readLine()) != null) {
                processLine(line);
                if (counter % 10000 == 0) {
                    logger.log(Level.INFO, "counter of processed triples " + counter);
                }
                counter++;
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "There was an error while trying to process the RDF file with BigFileStreaming method.");
        }
    }


}
