package com.miklosova.rdftocsvw.support;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ConfigurationManager {

    public static final String READ_METHOD = "converion.readMethod";
    public static final String TABLE_METHOD = "conversion.tableMethod";
    private static final String CONFIG_FILE_NAME = "./src/main/resources/app.config";
    public static final String INTERMEDIATE_FILE_NAMES = "app.filesInProgress";
    public static final String OUTPUT_ZIPFILE_NAME = "output.zipname";

    public static final String CONVERSION_HAS_BLANK_NODES = "conversion.containsBlankNodes";
    public static final String CONVERSION_HAS_RDF_TYPES = "conversion.hasRDFType";
    public static final String OUTPUT_FILENAME = "input.outputFileName";
    public static final String OUTPUT_METADATA_FILE_NAME = "output.metadataFileName";
    public static final String CONVERSION_METHOD = "conversion.method";

    /**
     * Default name for metadata file in case the metadata does not adhere to csv quivalent file name
     * According to https://www.w3.org/TR/tabular-data-primer/#h-metadata
     */
    public static final String DEFAULT_METADATA_FILENAME = "csv-metadata.json";

    public static void saveVariableToConfigFile(String variableName, String value){
        //System.out.println("new String value with encoding for variable(" + variableName + "): " + value );
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_NAME)) {
            prop.load(new InputStreamReader(fis, Charset.forName("UTF-8")));
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }
        prop.setProperty(variableName, value);
        //System.out.println("Set configuration of "+ variableName +" to: " + prop.getProperty(variableName));

        //for(String fileNames : file.list()) System.out.println(fileNames);
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(CONFIG_FILE_NAME)))){
            //PrintWriter pw = new PrintWriter(CONFIG_FILE_NAME);
            prop.store(pw, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Get a parameter by its key from app.config file.
     * @param variableName The name of the key to retrieve from the app.config map of parameters.
     * @return
     */
    public static String getVariableFromConfigFile(String variableName){
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_NAME)) {
            prop.load(new InputStreamReader(fis, Charset.forName("UTF-8")));
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }
        //System.out.println("Get configuration of "+ variableName +" : " + prop.getProperty(variableName));
        return prop.getProperty(variableName);
    }

    /**
     * Set the configuration parameters in app.config
     * Set defaults if none are provided
     * Set parameters given in args if args are provided
     * @param args Parameters provided in command line/parameters of conversion
     */
    public static void loadSettingsFromInputToConfigFile(String[] args){
        String metadataFileName = null;
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
            conversionMethod = args[1];
            
        } else if(args.length == 3){
            conversionMethod = args[1];
            conversionMethod = args[2];
        } else if(args.length == 4){
            conversionMethod = args[1];
            conversionMethod = args[2];
            metadataFileName = args[3];
        } else{
            
        }
        if(CSVFileToWriteTo == null){
            CSVFileToWriteTo = "CSVfileToWriteTo";
        }
        conversionMethod = (conversionMethod == null) ? "splitQuery" : conversionMethod;
        prop.setProperty("input.outputFileName", CSVFileToWriteTo);
        //System.out.println("Set configuration of input.outputFileName to: " + prop.getProperty("input.outputFileName"));
        prop.setProperty("conversion.method", conversionMethod);
        //System.out.println("Set configuration of conversion.method to: " + prop.getProperty("conversion.method"));
        prop.setProperty(ConfigurationManager.CONVERSION_HAS_BLANK_NODES, "false");
        prop.setProperty(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, "true");
        prop.setProperty(ConfigurationManager.OUTPUT_ZIPFILE_NAME, "compressed.zip");
        prop.setProperty(ConfigurationManager.READ_METHOD, "rdf4j");
        if(metadataFileName == null){
            metadataFileName = DEFAULT_METADATA_FILENAME;
        }
        prop.setProperty(ConfigurationManager.OUTPUT_METADATA_FILE_NAME, metadataFileName);


        //for(String fileNames : file.list()) System.out.println(fileNames);
        try {
            PrintWriter pw = new PrintWriter(CONFIG_FILE_NAME);

            prop.store(pw, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
