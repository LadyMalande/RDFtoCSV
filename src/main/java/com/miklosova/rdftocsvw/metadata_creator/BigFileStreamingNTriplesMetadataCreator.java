package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.convertor.RowsAndKeys;
import com.miklosova.rdftocsvw.support.ConfigurationManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class BigFileStreamingNTriplesMetadataCreator extends StreamingMetadataCreator implements IMetadataCreator {


    Metadata metadata;

    public BigFileStreamingNTriplesMetadataCreator(PrefinishedOutput<RowsAndKeys> data) {
        super(data);
        this.metadata = new Metadata();
        System.out.println("fileNameToRead = " + fileNameToRead);
    }

    @Override
    public Metadata addMetadata(PrefinishedOutput<?> info) {
        File f = new File(fileNameToRead);
        Table newTable = new Table(f.getName() + ".csv");
        metadata.getTables().add(newTable);
        tableSchema = new TableSchema();
        tableSchema.setPrimaryKey("Subject");
        createFirstColumn();


        newTable.setTableSchema(tableSchema);

        readFileWithStreaming();
        if (ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.TABLES).equalsIgnoreCase(ConfigurationManager.ONE_TABLE)) {

            metadata = consolidateMetadataAndCSVs(metadata);
        }
        metadata.jsonldMetadata();
        return metadata;
    }

    private void readFileWithStreaming() {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileNameToRead))) {
            String line;
            // Read file line by line
            while ((line = reader.readLine()) != null) {
                processLine(line);
                //System.out.println(line);  // Process the line (e.g., print it)
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
