package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.convertor.Row;
import com.miklosova.rdftocsvw.convertor.RowAndKey;
import com.miklosova.rdftocsvw.convertor.RowsAndKeys;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.FileWrite;

import java.util.ArrayList;

public class SplitFilesMetadataCreator implements IMetadataCreator {

    Metadata metadata;
    PrefinishedOutput data;
    String CSVFileTOWriteTo;
    Integer fileNumberX;
    ArrayList<ArrayList<Row>> allRows;

    ArrayList<String> allFileNames;

    public SplitFilesMetadataCreator(PrefinishedOutput<RowsAndKeys> data) {
        this.allFileNames = new ArrayList<>();
        this.metadata = new Metadata();
        this.data = data;
        this.allRows = new ArrayList<>();
        this.fileNumberX = 0;
        CSVFileTOWriteTo = ConfigurationManager.getVariableFromConfigFile("input.outputFileName");
        System.out.println("CSVFileTOWriteTo = " + CSVFileTOWriteTo);
    }

    @Override
    public Metadata addMetadata(PrefinishedOutput<? extends Object> info) {
        RowsAndKeys rnk = (RowsAndKeys) info.getPrefinishedOutput();
        System.out.println("getRowsAndKeys() size: " + rnk.getRowsAndKeys().size());
        for (RowAndKey rowAndKey : rnk.getRowsAndKeys()) {
            System.out.println("key: " + rowAndKey.getKeys());
            rowAndKey.getKeys().forEach(k -> System.out.print(k + " "));
            System.out.println();
            for (Row r : rowAndKey.getRows()) {
                //System.out.println("id: " + r.id);
                //System.out.println("type: " + r.type);
                //r.columns.entrySet().stream().forEach(entry -> System.out.println( "Key of row:" + entry.getKey().toString()
                //        + " id:"+ entry.getValue().id + " type:" + entry.getValue().type + "columns:" + entry.getValue().values));
            }

        }
        for (RowAndKey rowAndKey : rnk.getRowsAndKeys()) {

            String newFileName = CSVFileTOWriteTo + fileNumberX + ".csv";

            // Write the rows with respective keys to the current file
            //rowAndKey.getKeys().forEach(k -> System.out.println("Key " + k));
            System.out.println("INTERMEDIATE_FILE_NAMES: " + ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES));
            if (!ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES).isEmpty()) {
                newFileName = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES);
            }
            System.out.println("newFileName: " + newFileName);
            metadata.addMetadata(newFileName, rowAndKey.getKeys(), rowAndKey.getRows());
            fileNumberX = fileNumberX + 1;
            allRows.add(rowAndKey.getRows());
            allFileNames.add(newFileName);
        }
        FileWrite.writeFilesToconfigFile(allFileNames);

        metadata.addForeignKeys(allRows);
        metadata.jsonldMetadata();
        ;
        return metadata;
    }
}
