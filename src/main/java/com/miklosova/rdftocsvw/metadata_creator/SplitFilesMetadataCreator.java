package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.Row;
import com.miklosova.rdftocsvw.converter.data_structure.RowAndKey;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.output_processor.FileWrite;

import java.io.File;
import java.util.ArrayList;

public class SplitFilesMetadataCreator implements IMetadataCreator {

    Metadata metadata;
    PrefinishedOutput<RowsAndKeys> data;
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
        File f = new File(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_FILENAME));
        CSVFileTOWriteTo = f.getName();
        System.out.println("SplitFilesMetadataCreator CSVFileTOWriteTo = " + CSVFileTOWriteTo);
    }

    @Override
    public Metadata addMetadata(PrefinishedOutput<?> info) {
        RowsAndKeys rnk = (RowsAndKeys) info.getPrefinishedOutput();
        System.out.println("getRowsAndKeys() size: " + rnk.getRowsAndKeys().size());
        for (RowAndKey rowAndKey : rnk.getRowsAndKeys()) {
            System.out.println("key: " + rowAndKey.getKeys());
            rowAndKey.getKeys().forEach(k -> System.out.print(k + " "));
            System.out.println();
        }
        for (RowAndKey rowAndKey : rnk.getRowsAndKeys()) {

            String newFileName;
            if(FileWrite.hasExtension(CSVFileTOWriteTo, "csv")){
                newFileName = CSVFileTOWriteTo;
            } else {
                newFileName = CSVFileTOWriteTo + fileNumberX + ".csv";
            }

            // Write the rows with respective keys to the current file
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
        FileWrite.writeFilesToConfigFile(allFileNames);

        metadata.addForeignKeys(allRows);
        metadata.jsonldMetadata();
        return metadata;
    }
}
