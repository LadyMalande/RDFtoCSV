package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.convertor.QueryMethods;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

public class ConfigurationManager {
    private static String CONFIG_FILE_NAME = "../app.config";
    public static String getCONFIG_FILE_NAME() {
        // Lazy initialization (modifies the variable only once)
        // Convert the relative path to a canonical path (removes ../ and resolves symlinks)
        String canonicalPath = null;
        String fileInDirectory = null;
        try {
            URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
            File file = new File(location.toURI().getPath());
            String jarDirectory = file.getParentFile().getName();


            // Resolve the file path
            File configFile = new File(CONFIG_FILE_NAME);


            canonicalPath = configFile.getCanonicalPath();
            String dirForCanonicalFile = canonicalPath.substring(0, canonicalPath.length() - configFile.getName().length());
            System.out.println("configFile = " + configFile);
            System.out.println("dirForCanonicalFile = " + dirForCanonicalFile);
            System.out.println("jarDirectory = " + jarDirectory);
            String insertAfter = dirForCanonicalFile + File.separator;
            int insertPosition = canonicalPath.indexOf(insertAfter) + insertAfter.length();

            // Create the new path by inserting jarDirectory
            if(jarDirectory.equalsIgnoreCase("target")){
                fileInDirectory = canonicalPath.substring(0, insertPosition) + "RDFtoCSV" + File.separator
                        + jarDirectory
                        + File.separator
                        + canonicalPath.substring(insertPosition);
            } else {
                fileInDirectory = canonicalPath.substring(0, insertPosition)
                        + jarDirectory
                        + File.separator
                        + canonicalPath.substring(insertPosition);
            }
            //fileInDirectory = jarDirectory + File.separator + canonicalPath;
            CONFIG_FILE_NAME = fileInDirectory;


        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return CONFIG_FILE_NAME;
    }
    public static final String READ_METHOD = "converion.readMethod";

    public static final String INTERMEDIATE_FILE_NAMES = "app.filesInProgress";
    public static final String OUTPUT_ZIPFILE_NAME = "output.zipname";
    public static final String CONVERSION_HAS_BLANK_NODES = "conversion.containsBlankNodes";
    public static final String CONVERSION_HAS_RDF_TYPES = "conversion.hasRDFType";
    public static final String OUTPUT_FILENAME = "input.outputFileName";

    public static final String OUTPUT_METADATA_FILE_NAME = "output.metadataFileName";

    public static final String OUTPUT_FILE_PATH = "output.filePath";
    public static final String CONVERSION_METHOD = "conversion.method";
    public static final String METADATA_ROWNUMS = "metadata.rownums";


    /**
     * Default name for metadata file in case the metadata does not adhere to csv quivalent file name
     * According to <a href="https://www.w3.org/TR/tabular-data-primer/#h-metadata">Tabular Metadata Primer</a>
     */
    public static final String DEFAULT_METADATA_FILENAME = "csv-metadata.json";

    public static final String DEFAULT_CONVERSION_METHOD = "basicQuery";

    public static void saveVariableToConfigFile(String variableName, String value) {
        System.out.println("new String value with encoding for variable(" + variableName + "): " + value);
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_NAME)) {
            prop.load(new InputStreamReader(fis, Charset.forName("UTF-8")));
        } catch (IOException ex) {
        }
        prop.setProperty(variableName, value);
        System.out.println("Set configuration of " + variableName + " to: " + prop.getProperty(variableName));

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(CONFIG_FILE_NAME)))) {
            prop.store(pw, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void processConfigMap(Map<String, String> configMap) {
        String queryMethod = QueryMethods.BASIC_QUERY.getValue();
        if (configMap != null && configMap.containsKey("table")) {
            queryMethod = switch (configMap.get("table")) {
                case "splitQuery", "more" -> QueryMethods.SPLIT_QUERY.getValue();
                default -> QueryMethods.BASIC_QUERY.getValue();
            };
        }
        saveVariableToConfigFile(CONVERSION_METHOD, queryMethod);
        // TODO add more parameters compatible with web service

    }

    /**
     * Get a parameter by its key from app.config file.
     *
     * @param variableName The name of the key to retrieve from the app.config map of parameters.
     * @return
     */
    public static String getVariableFromConfigFile(String variableName) {
        Properties prop = new Properties();

        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_NAME)) {
            prop.load(new InputStreamReader(fis, Charset.forName("UTF-8")));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return prop.getProperty(variableName);
    }

    /**
     * Set the configuration parameters in app.config
     * Set defaults if none are provided
     * Set parameters given in args if args are provided
     *
     * @param args Parameters provided in command line/parameters of conversion
     */
    public static void loadSettingsFromInputToConfigFile(String[] args) {
        Properties prop = new Properties();

        String metadataFileName = null;

        // Now you call the static variable for the first time and set it if needed
        File finalConfigFile = new File(getCONFIG_FILE_NAME());
        System.out.println("finalConfigFile = " + finalConfigFile.getAbsolutePath());
        // Check if the file exists
        if (!finalConfigFile.exists()) {
            try {
                // Try to create the file
                if (finalConfigFile.createNewFile()) {
                    System.out.println("File created: " + finalConfigFile.getName());
                } else {
                    System.out.println("File already exists.");
                }
            } catch (IOException e) {
                System.out.println("An error occurred while creating the file.");
                e.printStackTrace();
            }
        } else {
            System.out.println("File already exists.");
        }
        try (FileInputStream fis = new FileInputStream(finalConfigFile)) {
            prop.load(fis);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Trouble reading config file for the first time");
            System.exit(1);
        }

        String CSVFileToWriteTo = null;
        String conversionMethod = null;
        if (args.length == 2) {
            conversionMethod = args[1];

        } else if (args.length == 3) {
            conversionMethod = args[1];
        } else if (args.length == 4) {
            conversionMethod = args[1];

            metadataFileName = args[3];
        } else {

        }
        if (CSVFileToWriteTo == null) {
            CSVFileToWriteTo = "CSVfileToWriteTo";
        }
        conversionMethod = (conversionMethod == null) ? DEFAULT_CONVERSION_METHOD : conversionMethod;
        prop.setProperty(ConfigurationManager.OUTPUT_FILENAME, CSVFileToWriteTo);
        prop.setProperty(ConfigurationManager.CONVERSION_METHOD, conversionMethod);
        prop.setProperty(ConfigurationManager.INTERMEDIATE_FILE_NAMES, "");
        prop.setProperty(ConfigurationManager.CONVERSION_HAS_BLANK_NODES, "false");
        prop.setProperty(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, "true");
        prop.setProperty(ConfigurationManager.OUTPUT_ZIPFILE_NAME, "compressed.zip");
        prop.setProperty(ConfigurationManager.READ_METHOD, "rdf4j");
        prop.setProperty(ConfigurationManager.METADATA_ROWNUMS, "false");
        prop.setProperty(ConfigurationManager.OUTPUT_FILE_PATH, "");
        if (metadataFileName == null) {
            metadataFileName = DEFAULT_METADATA_FILENAME;
        }
        prop.setProperty(ConfigurationManager.OUTPUT_METADATA_FILE_NAME, metadataFileName);

        try {
            PrintWriter pw = new PrintWriter(CONFIG_FILE_NAME);
            System.out.println("Written to configFile " + CONFIG_FILE_NAME);
            prop.store(pw, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
