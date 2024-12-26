package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.convertor.QueryMethods;
import org.apache.commons.cli.*;
import org.apache.log4j.BasicConfigurator;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

public class ConfigurationManager {

    public static final String READ_METHOD = "conversion.readMethod";
    public static final String INTERMEDIATE_FILE_NAMES = "app.filesInProgress";
    public static final String OUTPUT_ZIPFILE_NAME = "output.zipname";
    public static final String CONVERSION_HAS_BLANK_NODES = "conversion.containsBlankNodes";
    public static final String CONVERSION_HAS_RDF_TYPES = "conversion.hasRDFType";
    public static final String INPUT_FILENAME = "input.inputFileName";
    public static final String OUTPUT_FILENAME = "input.outputFileName";
    public static final String OUTPUT_METADATA_FILE_NAME = "output.metadataFileName";
    public static final String OUTPUT_FILE_PATH = "output.filePath";
    public static final String CONVERSION_METHOD = "conversion.method";
    public static final String METADATA_ROWNUMS = "metadata.rownums";
    public static final String STREAMING_CONTINUOUS = "input.streaming";
    public static final String FIRST_NORMAL_FORM = "input.firstNormalForm";
    /**
     * Default name for metadata file in case the metadata does not adhere to csv equivalent file name
     * According to <a href="https://www.w3.org/TR/tabular-data-primer/#h-metadata">Tabular Metadata Primer</a>
     */
    public static final String DEFAULT_METADATA_FILENAME = "csv-metadata.json";
    public static final String DEFAULT_CONVERSION_METHOD = QueryMethods.BASIC_QUERY.getValue();
    public static final String DEFAULT_METHOD = QueryMethods.BASIC_QUERY.getValue();
    public static final String DEFAULT_READ_METHOD = "rdf4j";
    public static final String MULTIPLE_TABLES_CONVERSION_METHOD = QueryMethods.SPLIT_QUERY.getValue();
    public static final String DEFAULT_OUTPUT_ZIPFILE_NAME = "zippedCSVW.zip";
    public static final String TABLES = "conversion.tables";
    public static final String ONE_TABLE = "one";
    private static final String DEFAULT_PARSING_METHOD = "rdf4j";
    private static final String CONFIG_FILE_NAME = "../app.config";
    public static final String CONFIG_FILE = "config.file";
    public static final String DEFAULT_PATH_APP_CONFIG = "config/app.config";
    public static final String CMD_OPTION_STREAMING = "streaming";
    private static String currentConfigFileName;

    public static String getCONFIG_FILE_NAME() {
        currentConfigFileName = null;
        // Lazy initialization (modifies the variable only once)
        // Convert the relative path to a canonical path (removes ../ and resolves symlinks)
        String canonicalPath;
        String fileInDirectory;
        try {
            URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
            System.out.println("URL location.toString from getCONFIG_FILE_NAME " + location.toString());
            System.out.println("URL location.getPath from getCONFIG_FILE_NAME " + location.getPath());
            System.out.println("URL location.toURI() from getCONFIG_FILE_NAME " + location.toURI());
            System.out.println("URL location.toURI().getPath() from getCONFIG_FILE_NAME " + location.toURI().getPath());
            String fileName = "/app.config";
            if(location.toURI().toString().contains("jar:nested:")){
                // Change the directory in remote place
                //.replaceFirst("!/$", "!/"); // Ensure correct trailing format if needed

                // Create a new URL if required
                //location = new URL(modifiedURLString);
                fileName = location.toString()
                        .replace("jar:nested:", "");
                System.out.println("Modified URL location.toString from getCONFIG_FILE_NAME " + location);
            } else if (location.toURI().toString().contains("nested:")){
                // Change the directory in remote place
                //.replaceFirst("!/$", "!/"); // Ensure correct trailing format if needed

                // Create a new URL if required
                //location = new URL(modifiedURLString);
                fileName = location.toString()
                        .replace("nested:", "");
                System.out.println("Modified URL location.toString from getCONFIG_FILE_NAME " + location);
            }


            //File file = new File(location.toURI().getPath());
            File file;
            if(location.toURI().toString().contains("jar:nested:") || location.toURI().toString().contains("nested:")){
                file = new File(fileName);
            } else {
                file = new File(location.getPath());
            }
            System.out.println("Created file getCONFIG_FILE_NAME " + file.getAbsolutePath());

            String jarDirectory = file.getParentFile().getName();


            // Resolve the file path
            File configFile = new File(CONFIG_FILE_NAME);


            canonicalPath = configFile.getCanonicalPath();
            String dirForCanonicalFile = canonicalPath.substring(0, canonicalPath.length() - configFile.getName().length());

            System.out.println("location = " + location);
            System.out.println("configFile = " + configFile);
            System.out.println("dirForCanonicalFile = " + dirForCanonicalFile);
            System.out.println("jarDirectory = " + jarDirectory);


            String insertAfter = dirForCanonicalFile + File.separator;
            int insertPosition = canonicalPath.indexOf(insertAfter) + insertAfter.length();
            // Create the new path by inserting jarDirectory
            if (jarDirectory.equalsIgnoreCase("target")) {
                System.out.println("jarDirectory.equalsIgnoreCase(\"target\"");
                fileInDirectory = canonicalPath.substring(0, insertPosition) + "RDFtoCSV" + File.separator
                        + jarDirectory
                        + File.separator
                        + canonicalPath.substring(insertPosition);


            } else {
                String path = ConfigurationManager.class.getProtectionDomain().getCodeSource().getLocation().getPath();

                // Decode spaces and special characters in the URL
                String currentDir = new File(java.net.URLDecoder.decode(path, StandardCharsets.UTF_8)).getAbsolutePath();
                String currentDirsParent = new File(java.net.URLDecoder.decode(path, StandardCharsets.UTF_8)).getParent();
                System.out.println("currentDir " + currentDir);
                System.out.println("currentDirsParent " + currentDirsParent);
                System.out.println(//canonicalPath.substring(0, insertPosition)+
                        currentDir
                        + File.separator
                        + canonicalPath.substring(insertPosition));
                if(currentDir.contains("/app/nested:")){
                    currentDir = currentDir.replace("/app/nested:","");
                }
                fileInDirectory = //canonicalPath.substring(0, insertPosition)+
                         currentDir
                        + File.separator
                        + canonicalPath.substring(insertPosition);
            }
            System.out.println("fileInDirectory " + fileInDirectory);
            System.out.println("canonicalPath.substring(0, insertPosition) " + canonicalPath.substring(0, insertPosition));
            System.out.println("jarDirectory " + jarDirectory);
            System.out.println("File.separator " + File.separator);
            System.out.println("canonicalPath.substring(insertPosition) " + canonicalPath.substring(insertPosition));
            //fileInDirectory = jarDirectory + File.separator + canonicalPath;
            currentConfigFileName = fileInDirectory;
            System.out.println("currentConfigFileName in getCONFIG_FILE_NAME = " + currentConfigFileName);
            File configFileCreated = new File(currentConfigFileName);

        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return currentConfigFileName;
    }

    public static void saveVariableToConfigFile(String variableName, String value) {
        //System.out.println("new String value with encoding for variable(" + variableName + "): " + value);
        System.out.println("saveVariableToConfigFile currentConfigFileName(" + currentConfigFileName + "): " +variableName+ "=" + value);
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(currentConfigFileName)) {
            prop.load(new InputStreamReader(fis, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        prop.setProperty(variableName, value);
        //System.out.println("Set configuration of " + variableName + " to: " + prop.getProperty(variableName));

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(currentConfigFileName)))) {
            prop.store(pw, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void processConfigMap(String fileName, Map<String, String> configMap) {
        createConfigFile();
        //getCONFIG_FILE_NAME();
        String tables = ONE_TABLE;
        String readMethod = DEFAULT_PARSING_METHOD;
        String conversionMethod = QueryMethods.BASIC_QUERY.getValue();
        String firstNormalForm = "true";
        if (configMap != null) {
            if (configMap.containsKey("table")) {
                tables = switch (configMap.get("table")) {
                    case "splitQuery", "more" -> "more";
                    default -> "one";
                };
            }
            readMethod = configMap.getOrDefault("readMethod", readMethod);
            System.out.println("tables = " + tables);
            conversionMethod = (tables.equalsIgnoreCase(ONE_TABLE)) ? QueryMethods.BASIC_QUERY.getValue() : QueryMethods.SPLIT_QUERY.getValue();
            if (configMap.containsKey("firstNormalForm")) {
                firstNormalForm = configMap.get("firstNormalForm");
            }
        }
        // TODO finish implementing all the relevant parameters
        //if (ConfigurationManager.getVariableFromConfigFile(CONVERSION_METHOD) == null)
            saveVariableToConfigFile(CONVERSION_METHOD, conversionMethod);
        //if (ConfigurationManager.getVariableFromConfigFile(TABLES) == null)
            saveVariableToConfigFile(TABLES, tables);
        //if (ConfigurationManager.getVariableFromConfigFile(FIRST_NORMAL_FORM) == null)
            saveVariableToConfigFile(FIRST_NORMAL_FORM, String.valueOf(firstNormalForm));
        //if (ConfigurationManager.getVariableFromConfigFile(READ_METHOD) == null)
            saveVariableToConfigFile(READ_METHOD,readMethod);
        //if(ConfigurationManager.getVariableFromConfigFile(INPUT_FILENAME) == null){
            saveVariableToConfigFile(INPUT_FILENAME,fileName);
            System.out.println("INPUT_FILENAME " + fileName);
        //}
    }

    /**
     * Get a parameter by its key from app.config file.
     *
     * @param variableName The name of the key to retrieve from the app.config map of parameters.
     * @return String object (value) of the given config variable (key)
     */
    public static String getVariableFromConfigFile(String variableName) {
        Properties prop = new Properties();

        System.out.println("currentConfigFileName = " + currentConfigFileName);

        try (FileInputStream fis = new FileInputStream(currentConfigFileName)) {
            prop.load(new InputStreamReader(fis, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return prop.getProperty(variableName);
    }

    public static void createConfigFile(){
        // Define the default config file path (can be made configurable)
        String configFilePath = System.getProperty(CONFIG_FILE, DEFAULT_PATH_APP_CONFIG);

        // Ensure the directory exists
        File configFile = new File(configFilePath);
        currentConfigFileName = configFile.getAbsolutePath();
        File parentDir = configFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                try {
                    throw new IOException("Failed to create directory: " + parentDir.getAbsolutePath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // Create the config file
        if (!configFile.exists()) {
            try {
                if (!configFile.createNewFile()) {
                    throw new IOException("Failed to create file: " + configFilePath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Configuration file created at: " + configFile.getAbsolutePath());
    }
    public static String readArgWithDefaultOptions(String[] args, String argName) {
        Options options = addArgsOptions();

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            String inputFile = cmd.getOptionValue(argName);
            boolean streamingMethod = cmd.hasOption(CMD_OPTION_STREAMING);
            // Continue with processing...
            if (inputFile == null && !streamingMethod) {
                System.err.println("You must specify file input argument to the command line. ");
                printHelpLine(options);
                System.exit(1);
            }
            return inputFile;
        } catch (ParseException e) {
            System.err.println("Error parsing options from arguments: " + e.getMessage());
            System.exit(1);
        }

        return null;
    }

    public static void loadSettingsFromInputToConfigFile(String[] args) {
        createConfigFile();

        Options options = addArgsOptions();

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            boolean multipleTables = cmd.hasOption("multipleTables");
            String parsingMethod = cmd.getOptionValue("parsing");
            boolean help = cmd.hasOption("help");
            String inputFile = cmd.getOptionValue("file");
            boolean streamingMethod = cmd.hasOption("streaming");
            boolean normalForm = cmd.hasOption("firstNormalForm");
            String outputFilename = cmd.getOptionValue("output");

            if (help) {
                printHelpLine(options);

                System.exit(0);
            }
            // Continue with processing...
            if (inputFile == null && !streamingMethod) {
                System.err.println("You must specify file input argument to the command line. ");
                printHelpLine(options);
                System.exit(1);
            }

            try {
                URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
                File file = new File(location.toURI().getPath());
                String jarDirectory = file.getParentFile().getName();
                String fileInDirectory;
                String fileArgFromArgs = ConfigurationManager.readArgWithDefaultOptions(args, "file");
                if (jarDirectory.equalsIgnoreCase("target")) {
                    //fileInDirectory =  args[0];
                    fileInDirectory = fileArgFromArgs;
                } else {
                    // fileInDirectory = jarDirectory + File.separator + args[0];
                    fileInDirectory = jarDirectory + File.separator + fileArgFromArgs;
                }

                saveVariableToConfigFile(ConfigurationManager.INPUT_FILENAME, fileInDirectory);
                System.out.println("JAR Directory: " + jarDirectory + " fileInDirectory = " + fileInDirectory);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            writeOptionsToConfigFile(multipleTables, parsingMethod, inputFile, streamingMethod, normalForm, outputFilename);
        } catch (ParseException e) {
            System.err.println("Error parsing options from arguments: " + e.getMessage());
            System.exit(1);
        }

    }

    private static void printHelpLine(Options options) {
        HelpFormatter formatter = HelpFormatter.builder().get();
        formatter.printHelp("Command line syntax:", options);
    }

    private static void writeOptionsToConfigFile(boolean multipleTables, String parsingMethod, String inputFile, boolean streaming, boolean firstNormalForm, String outputFilename) {
        String conversionMethod = "";
        System.out.println("conversion method=" + conversionMethod + " parsingMethod=" + parsingMethod + " inputFile=" + inputFile + " streaming=" + streaming);
        File finalConfigFile = new File(currentConfigFileName);

        Properties prop = new Properties();

        try (FileInputStream fis = new FileInputStream(finalConfigFile)) {
            prop.load(fis);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Trouble reading config file for the first time");
            System.exit(1);
        }

        String metadataFileName;
        parsingMethod = (parsingMethod != null) ? parsingMethod : DEFAULT_PARSING_METHOD;
        String baseFileName;
        if (outputFilename == null) {
            baseFileName = inputFile.split("\\.")[0];
            System.out.println("baseFileName "+ baseFileName);
        } else {
            baseFileName = outputFilename;
        }

        conversionMethod = (!multipleTables) ? DEFAULT_CONVERSION_METHOD : MULTIPLE_TABLES_CONVERSION_METHOD;
        prop.setProperty(ConfigurationManager.TABLES, (!multipleTables) ? "one" : "more");
        conversionMethod = switch (parsingMethod) {
            case "bigFileStreaming" -> "bigFileStreaming";
            case "streaming" -> "streaming";
            default -> conversionMethod;
        };
        prop.setProperty(ConfigurationManager.OUTPUT_FILENAME, baseFileName);
        prop.setProperty(ConfigurationManager.FIRST_NORMAL_FORM, String.valueOf(firstNormalForm));
        prop.setProperty(ConfigurationManager.CONVERSION_METHOD, conversionMethod);
        System.out.println("property set prop.conversionMethod," + conversionMethod);
        prop.setProperty(ConfigurationManager.INTERMEDIATE_FILE_NAMES, "");
        prop.setProperty(ConfigurationManager.CONVERSION_HAS_BLANK_NODES, "false");
        prop.setProperty(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, "true");
        prop.setProperty(ConfigurationManager.OUTPUT_ZIPFILE_NAME, "compressed.zip");
        System.out.println("property set prop.setProperty(ConfigurationManager.READ_METHOD," + parsingMethod);
        prop.setProperty(ConfigurationManager.READ_METHOD, parsingMethod);
        prop.setProperty(ConfigurationManager.METADATA_ROWNUMS, "false");
        prop.setProperty(ConfigurationManager.OUTPUT_FILE_PATH, "");
        prop.setProperty(ConfigurationManager.STREAMING_CONTINUOUS, String.valueOf(streaming));

        metadataFileName = DEFAULT_METADATA_FILENAME;

        prop.setProperty(ConfigurationManager.OUTPUT_METADATA_FILE_NAME, metadataFileName);

        // Store options to config file
        try {
            PrintWriter pw = new PrintWriter(currentConfigFileName);
            System.out.println("Written to configFile " + currentConfigFileName);
            prop.store(pw, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Get value from config: " + ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.READ_METHOD));
    }

    private static Options addArgsOptions() {
        Options options = new Options();
        options.addOption("t", "multipleTables", false, "Enable creation of multiple tables during conversion");
        options.addOption("p", "parsing", true, "Specify the parsing method");
        options.addOption("h", "help", false, "Show the command line options");
        options.addOption("f", "file", true, "File for conversion");
        options.addOption("s", "streaming", false, "Parse the file in streaming mode (continual parsing until stopped)");
        options.addOption("n", "firstNormalForm", false, "Put the output CSV data into first normal form (every cell contains only one entry, no lists of values)");
        options.addOption("o", "output", true, "Output file name base. Will be given .csv extension. If not set, the name of the input file is taken.");
        return options;
    }

    public static void configure(String metadataFilename, String filePathForOutput) {
        BasicConfigurator.configure();

        String m = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD);
        String method = (m != null) ? m : DEFAULT_METHOD;
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_METHOD, method);
        String readMethod = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.READ_METHOD);
        readMethod = (readMethod != null) ? readMethod : DEFAULT_READ_METHOD;
        System.out.println("readMethod is " + readMethod);
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.READ_METHOD, readMethod);

        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME, metadataFilename);

        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_FILENAME, filePathForOutput);
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES, "");
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_BLANK_NODES, "false");
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, "true");
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.METADATA_ROWNUMS, "false");
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_FILE_PATH, "");
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_ZIPFILE_NAME, ConfigurationManager.DEFAULT_OUTPUT_ZIPFILE_NAME);
    }
}
