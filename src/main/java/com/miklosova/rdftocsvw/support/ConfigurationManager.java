package com.miklosova.rdftocsvw.support;

import java.io.*;
import java.util.Properties;

public class ConfigurationManager {

    public static final String READ_METHOD = "converion.readMethod";
    public static final String TABLE_METHOD = "conversion.tableMethod";
    private static final String CONFIG_FILE_NAME = "./src/main/resources/app.config";
    public static final String INTERMEDIATE_FILE_NAMES = "app.filesInProgress";
    public static final String OUTPUT_ZIPFILE_NAME = "output.zipname";

    public static final String CONVERSION_HAS_BLANK_NODES = "conversion.containsBlankNodes";
    public static final String CONVERSION_HAS_RDF_TYPES = "conversion.hasRDFType";
    public static final String INPUT_OUTPUT_FILENAME = "input.outputFileName";
    public static final String OUTPUT_METADATA_FILE_NAME = "output.metadataFileName";
    public static final String CONVERSION_METHOD = "conversion.method";

    public static void saveVariableToConfigFile(String variableName, String value){
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_NAME)) {
            prop.load(fis);
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }
        prop.setProperty(variableName, value);
        System.out.println("Set configuration of "+ variableName +" to: " + prop.getProperty(variableName));

        //for(String fileNames : file.list()) System.out.println(fileNames);
        try {
            PrintWriter pw = new PrintWriter(CONFIG_FILE_NAME);
            prop.store(pw, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getVariableFromConfigFile(String variableName){
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_NAME)) {
            prop.load(fis);
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }
        System.out.println("Get configuration of "+ variableName +" : " + prop.getProperty(variableName));
        return prop.getProperty(variableName);
    }

    public static void loadSettingsFromInputToConfigFile(String[] args){
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_NAME)) {
            prop.load(fis);
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }

        String RDFFileToRead = args[0];
        String CSVFileToWriteTo = null;
        String conversionMethod = null;
        if(args.length == 2){
            CSVFileToWriteTo = args[1];
            
        } else if(args.length == 3){
            CSVFileToWriteTo = args[1];
            conversionMethod = args[2];
        } else{
            
        }
        if(CSVFileToWriteTo == null){
            CSVFileToWriteTo = "CSVfileToWriteTo";
        }
        conversionMethod = (conversionMethod == null) ? "splitQuery" : conversionMethod;
        prop.setProperty("input.outputFileName", CSVFileToWriteTo);
        System.out.println("Set configuration of input.outputFileName to: " + prop.getProperty("input.outputFileName"));
        prop.setProperty("conversion.method", conversionMethod);
        System.out.println("Set configuration of conversion.method to: " + prop.getProperty("conversion.method"));

        //for(String fileNames : file.list()) System.out.println(fileNames);
        try {
            PrintWriter pw = new PrintWriter(CONFIG_FILE_NAME);

            prop.store(pw, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
