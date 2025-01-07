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
        File f = new File(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_FILENAME));
        CSVFileTOWriteTo = f.getName();
        System.out.println("BasicQueryMetadataCreator CSVFileTOWriteTo = " + CSVFileTOWriteTo);
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

        System.out.println("getRowsAndKeys() size: " + rnk.getRows().size());
        for (Row row : rnk.getRows()) {
            System.out.println("key: " + row.type);
            row.columns.forEach((k, v) -> System.out.print(k + " " + v));
            System.out.println();
        }
        String newFileName;
    if(FileWrite.hasExtension(CSVFileTOWriteTo, "csv")){
        newFileName = CSVFileTOWriteTo;
    } else {
        newFileName = CSVFileTOWriteTo + fileNumberX + ".csv";
    }


        // Write the rows with respective keys to the current file
        System.out.println("INTERMEDIATE_FILE_NAMES: " + ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES));
        /*
        if (!ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES).isEmpty()) {
            String[] split = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES).split(",");
            if(split.length > 1){
                // There are more tables ready, because of standard mode that had more tables inside
                SplitFilesMetadataCreator sfmc = new SplitFilesMetadataCreator(this.data);
                return sfmc.addMetadata(info);
            }
            newFileName = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES);
        }

         */
        //ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES, ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES) + "," + newFileName);
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
