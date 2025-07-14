package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.Row;
import com.miklosova.rdftocsvw.converter.data_structure.RowAndKey;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.output_processor.FileWrite;
import com.miklosova.rdftocsvw.support.ConfigurationManager;

import java.io.File;
import java.util.ArrayList;

/**
 * The Basic query metadata creator. For RDF4J method, for ONE table creation.
 */
public class BasicQueryMetadataCreator extends MetadataCreator implements IMetadataCreator {
    /**
     * The Metadata.
     */
    Metadata metadata;
    /**
     * The Data = inner representation of the CSV with additional information apart from the cell values.
     */
    PrefinishedOutput<RowsAndKeys> data;
    /**
     * The Csv file to write to.
     */
    String CSVFileTOWriteTo;
    /**
     * The All rows = The whole Table representation.
     */
    ArrayList<ArrayList<Row>> allRows;

    /**
     * The All file names.
     */
    ArrayList<String> allFileNames;

    /**
     * Instantiates a new Basic query metadata creator.
     *
     * @param data the data
     */
    public BasicQueryMetadataCreator(PrefinishedOutput<RowsAndKeys> data) {
        this.allFileNames = new ArrayList<>();
        this.metadata = new Metadata();
        this.data = data;
        this.allRows = new ArrayList<>();
        File f = new File(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_FILENAME));
        CSVFileTOWriteTo = f.getName();

    }

    @Override
    public Metadata addMetadata(PrefinishedOutput<?> info) {
        RowAndKey rnk;

        try {
            RowsAndKeys rnks = (RowsAndKeys) info.getPrefinishedOutput();
            rnk = rnks.getRowsAndKeys().get(0);
        } catch (ClassCastException ex) {
            SplitFilesMetadataCreator smc = new SplitFilesMetadataCreator(null);
            return smc.addMetadata(info);
        }
        String newFileName;
        if (FileWrite.hasExtension(CSVFileTOWriteTo, "csv")) {
            newFileName = CSVFileTOWriteTo;
        } else {
            newFileName = CSVFileTOWriteTo + ".csv";
        }


        // Write the rows with respective keys to the current file

        //ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES, ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES) + "," + newFileName);
        metadata.addMetadata(newFileName, rnk.getKeys(), rnk.getRows());
        allRows.add(rnk.getRows());
        allFileNames.add(newFileName);

        FileWrite.writeFilesToConfigFile(allFileNames);

        metadata.addForeignKeys(allRows);
        metadata.jsonldMetadata();
        return metadata;
    }
}
