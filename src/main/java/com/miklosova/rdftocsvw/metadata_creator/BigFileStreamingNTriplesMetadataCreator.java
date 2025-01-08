package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.TableSchema;
import com.miklosova.rdftocsvw.output_processor.FileWrite;
import com.miklosova.rdftocsvw.support.ConfigurationManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BigFileStreamingNTriplesMetadataCreator extends StreamingMetadataCreator implements IMetadataCreator {

    private static final Logger logger = Logger.getLogger(BigFileStreamingNTriplesMetadataCreator.class.getName());
    Metadata metadata;
    int counter;

    public BigFileStreamingNTriplesMetadataCreator(PrefinishedOutput<RowsAndKeys> data) {
        super();
        this.metadata = new Metadata();
        System.out.println("fileNameToRead = " + fileNameToRead);
    }

    @Override
    public Metadata addMetadata(PrefinishedOutput<?> info) {
        File f = new File(fileNameToRead);
        System.out.println("filenametoRead " + fileNameToRead);
        System.out.println("f.getName() " + f.getName());
        Table newTable = new Table(f.getName() + ".csv");
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
                //System.out.println(line);  // Process the line (e.g., print it)
                if(counter % 10000 == 0){
                    System.out.println(counter);
                    logger.log(Level.INFO, "counter of processed triples " + counter);
                }
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
