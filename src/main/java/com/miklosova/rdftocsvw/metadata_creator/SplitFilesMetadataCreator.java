package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.convertor.Row;
import com.miklosova.rdftocsvw.convertor.RowAndKey;
import com.miklosova.rdftocsvw.convertor.RowsAndKeys;
import com.miklosova.rdftocsvw.support.ConfigurationManager;

import java.util.ArrayList;

public class SplitFilesMetadataCreator implements IMetadataCreator {

    Metadata metadata;
    PrefinishedOutput data;
    String CSVFileTOWriteTo;
    Integer fileNumberX;
    ArrayList<ArrayList<Row>> allRows;

    public SplitFilesMetadataCreator(PrefinishedOutput<RowsAndKeys> data) {
        this.metadata = new Metadata();
        this.data = data;
        this.allRows = new ArrayList<>();
        this.fileNumberX = 0;
        CSVFileTOWriteTo = ConfigurationManager.getVariableFromConfigFile("input.outputFileName");
    }

    @Override
    public Metadata addMetadata(PrefinishedOutput<? extends Object> info) {
        RowsAndKeys rnk = (RowsAndKeys) info.getPrefinishedOutput();
        for(RowAndKey rowAndKey : rnk.getRowsAndKeys()){
            String newFileName = CSVFileTOWriteTo + fileNumberX + ".csv";
            // Write the rows with respective keys to the current file
            metadata.addMetadata(newFileName, rowAndKey.getKeys(), rowAndKey.getRows());
            fileNumberX = fileNumberX + 1;
            allRows.add(rowAndKey.getRows());
        }


        metadata.addForeignKeys(allRows);
        metadata.finalizeMetadata();
        return metadata;
    }
}
