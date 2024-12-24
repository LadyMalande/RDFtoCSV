package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.convertor.Row;
import com.miklosova.rdftocsvw.convertor.RowAndKey;
import com.miklosova.rdftocsvw.convertor.RowsAndKeys;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.FileWrite;

import java.util.ArrayList;

public class BasicQueryMetadataCreator extends MetadataCreator implements IMetadataCreator {
    Metadata metadata;
    PrefinishedOutput<RowsAndKeys> data;
    String CSVFileTOWriteTo;
    Integer fileNumberX;
    ArrayList<ArrayList<Row>> allRows;

    ArrayList<String> allFileNames;

    public BasicQueryMetadataCreator(PrefinishedOutput<RowsAndKeys> data) {
        this.allFileNames = new ArrayList<>();
        this.metadata = new Metadata();
        this.data = data;
        this.allRows = new ArrayList<>();
        this.fileNumberX = 0;
        CSVFileTOWriteTo = ConfigurationManager.getVariableFromConfigFile("input.outputFileName");
        System.out.println("CSVFileTOWriteTo = " + CSVFileTOWriteTo);
    }

    @Override
    public Metadata addMetadata(PrefinishedOutput<?> info) {
        RowAndKey rnk;
        try {
            rnk = (RowAndKey) info.getPrefinishedOutput();
        } catch (ClassCastException ex) {
            SplitFilesMetadataCreator smc = new SplitFilesMetadataCreator(null);
            return smc.addMetadata(info);
        }

        System.out.println("getRowsAndKeys() size: " + rnk.getRows().size());
        for (Row row : rnk.getRows()) {
            System.out.println("key: " + row.type);
            row.columns.forEach((k, v) -> System.out.print(k + " " + v));
            System.out.println();
        }


        String newFileName = CSVFileTOWriteTo + fileNumberX + ".csv";

        // Write the rows with respective keys to the current file
        System.out.println("INTERMEDIATE_FILE_NAMES: " + ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES));
        if (!ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES).isEmpty()) {
            newFileName = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES);
        }
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES, ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES) + "," + newFileName);
        System.out.println("newFileName: " + newFileName);
        metadata.addMetadata(newFileName, rnk.getKeys(), rnk.getRows());
        fileNumberX = fileNumberX + 1;
        allRows.add(rnk.getRows());
        allFileNames.add(newFileName);

        FileWrite.writeFilesToConfigFile(allFileNames);

        metadata.addForeignKeys(allRows);
        metadata.jsonldMetadata();
        return metadata;
    }
}
