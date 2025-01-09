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
 * The type Split files metadata creator.
 */
public class SplitFilesMetadataCreator implements IMetadataCreator {

    /**
     * The Metadata.
     */
    Metadata metadata;
    /**
     * The Data.
     */
    PrefinishedOutput<RowsAndKeys> data;
    /**
     * The CSV file to write to.
     */
    String CSVFileTOWriteTo;
    /**
     * The File number x. Is iterated to keep the file names unique.
     */
    Integer fileNumberX;
    /**
     * The All rows. (All tables with all rows)
     */
    ArrayList<ArrayList<Row>> allRows;

    /**
     * The All file names.
     */
    ArrayList<String> allFileNames;

    /**
     * Instantiates a new Split files metadata creator.
     *
     * @param data the data in inner CSV representation
     */
    public SplitFilesMetadataCreator(PrefinishedOutput<RowsAndKeys> data) {
        this.allFileNames = new ArrayList<>();
        this.metadata = new Metadata();
        this.data = data;
        this.allRows = new ArrayList<>();
        this.fileNumberX = 0;
        File f = new File(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_FILENAME));
        CSVFileTOWriteTo = f.getName();
    }

    @Override
    public Metadata addMetadata(PrefinishedOutput<?> info) {
        RowsAndKeys rnk = (RowsAndKeys) info.getPrefinishedOutput();
        for (RowAndKey rowAndKey : rnk.getRowsAndKeys()) {

            String newFileName;
            if (FileWrite.hasExtension(CSVFileTOWriteTo, "csv")) {
                newFileName = CSVFileTOWriteTo;
            } else {
                newFileName = CSVFileTOWriteTo + fileNumberX + ".csv";
            }

            // Write the rows with respective keys to the current file
            metadata.addMetadata(newFileName, rowAndKey.getKeys(), rowAndKey.getRows());
            fileNumberX = fileNumberX + 1;
            allRows.add(rowAndKey.getRows());
            allFileNames.add(newFileName);
        }
        FileWrite.writeFilesToConfigFile(allFileNames);

        metadata.addForeignKeys(allRows);
        metadata.jsonldMetadata();
        return metadata;
    }
}
