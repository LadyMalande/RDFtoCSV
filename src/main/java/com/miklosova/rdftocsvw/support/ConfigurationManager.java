package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.converter.QueryMethods;
import org.apache.commons.cli.*;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static com.miklosova.rdftocsvw.support.ConnectionChecker.isUrl;
import static org.eclipse.rdf4j.model.util.Values.iri;

/**
 * The Configuration manager class. Manages a app.config file that contains all important set parameters for ongoing conversion.
 */
public class ConfigurationManager {
    private static final Logger logger = Logger.getLogger(ConfigurationManager.class.getName());
    /**
     * The constant READ_METHOD.
     */
    public static final String READ_METHOD = "conversion.readMethod";
    /**
     * The constant INTERMEDIATE_FILE_NAMES.
     */
    public static final String INTERMEDIATE_FILE_NAMES = "app.filesInProgress";
    /**
     * The constant OUTPUT_ZIPFILE_NAME.
     */
    public static final String OUTPUT_ZIPFILE_NAME = "output.zipname";
    /**
     * The constant CONVERSION_HAS_BLANK_NODES.
     */
    public static final String CONVERSION_HAS_BLANK_NODES = "conversion.containsBlankNodes";
    /**
     * The constant CONVERSION_HAS_RDF_TYPES.
     */
    public static final String CONVERSION_HAS_RDF_TYPES = "conversion.hasRDFType";
    /**
     * The constant INPUT_FILENAME.
     */
    public static final String INPUT_FILENAME = "input.inputFileName";
    /**
     * The constant OUTPUT_FILENAME.
     */
    public static final String OUTPUT_FILENAME = "input.outputFileName";
    /**
     * The constant OUTPUT_METADATA_FILE_NAME.
     */
    public static final String OUTPUT_METADATA_FILE_NAME = "output.metadataFileName";
    /**
     * The constant OUTPUT_FILE_PATH.
     */
    public static final String OUTPUT_FILE_PATH = "output.filePath";
    /**
     * The constant CONVERSION_METHOD.
     */
    public static final String CONVERSION_METHOD = "conversion.method";
    /**
     * The constant METADATA_ROWNUMS.
     */
    public static final String METADATA_ROWNUMS = "metadata.rownums";
    /**
     * The constant STREAMING_CONTINUOUS.
     */
    public static final String STREAMING_CONTINUOUS = "input.streaming";
    /**
     * The constant FIRST_NORMAL_FORM.
     */
    public static final String FIRST_NORMAL_FORM = "input.firstNormalForm";
    /**
     * Default name for metadata file in case the metadata does not adhere to csv equivalent file name
     * According to <a href="https://www.w3.org/TR/tabular-data-primer/#h-metadata">Tabular Metadata Primer</a>
     */
    public static final String DEFAULT_METADATA_FILENAME = "csv-metadata.json";
    /**
     * The constant DEFAULT_CONVERSION_METHOD.
     */
    public static final String DEFAULT_CONVERSION_METHOD = QueryMethods.BASIC_QUERY.getValue();

    /**
     * The constant MULTIPLE_TABLES_CONVERSION_METHOD.
     */
    public static final String MULTIPLE_TABLES_CONVERSION_METHOD = QueryMethods.SPLIT_QUERY.getValue();
    /**
     * The constant TABLES.
     */
    public static final String TABLES = "conversion.tables";
    /**
     * The constant ONE_TABLE.
     */
    public static final String ONE_TABLE = "one";
    private static final String DEFAULT_PARSING_METHOD = "rdf4j";
    private static final String CONFIG_FILE_NAME = "../app.config";
    /**
     * The constant CONFIG_FILE.
     */
    public static final String CONFIG_FILE = "config.file";
    /**
     * The constant DEFAULT_PATH_APP_CONFIG.
     */
    public static final String DEFAULT_PATH_APP_CONFIG = "config/app.config";
    /**
     * The constant CMD_OPTION_STREAMING.
     */
    public static final String CMD_OPTION_STREAMING = "streaming";
    private static String currentConfigFileName;

    /**
     * Gets config file name.
     *
     * @return the config file name
     */
    public static String getCONFIG_FILE_NAME() {
        currentConfigFileName = null;
        // Lazy initialization (modifies the variable only once)
        // Convert the relative path to a canonical path (removes ../ and resolves symlinks)
        String canonicalPath;
        String fileInDirectory;
        try {
            URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();

            String fileName = "/app.config";
            if (location.toURI().toString().contains("jar:nested:")) {
                // Change the directory in remote place
                //.replaceFirst("!/$", "!/"); // Ensure correct trailing format if needed

                // Create a new URL if required
                //location = new URL(modifiedURLString);
                fileName = location.toString()
                        .replace("jar:nested:", "");
            } else if (location.toURI().toString().contains("nested:")) {
                // Change the directory in remote place
                //.replaceFirst("!/$", "!/"); // Ensure correct trailing format if needed

                // Create a new URL if required
                //location = new URL(modifiedURLString);
                fileName = location.toString()
                        .replace("nested:", "");
            }
            File file;
            if (location.toURI().toString().contains("jar:nested:") || location.toURI().toString().contains("nested:")) {
                file = new File(fileName);
            } else {
                file = new File(location.getPath());
            }

            String jarDirectory = file.getParentFile().getName();


            // Resolve the file path
            File configFile = new File(CONFIG_FILE_NAME);


            canonicalPath = configFile.getCanonicalPath();
            String dirForCanonicalFile = canonicalPath.substring(0, canonicalPath.length() - configFile.getName().length());

            String insertAfter = dirForCanonicalFile + File.separator;
            int insertPosition = canonicalPath.indexOf(insertAfter) + insertAfter.length();
            // Create the new path by inserting jarDirectory
            if (jarDirectory.equalsIgnoreCase("target")) {
                fileInDirectory = canonicalPath.substring(0, insertPosition) + "RDFtoCSV" + File.separator
                        + jarDirectory
                        + File.separator
                        + canonicalPath.substring(insertPosition);


            } else {
                String path = ConfigurationManager.class.getProtectionDomain().getCodeSource().getLocation().getPath();

                // Decode spaces and special characters in the URL
                String currentDir = new File(java.net.URLDecoder.decode(path, StandardCharsets.UTF_8)).getAbsolutePath();

                if (currentDir.contains("/app/nested:")) {
                    currentDir = currentDir.replace("/app/nested:", "");
                }
                fileInDirectory = //canonicalPath.substring(0, insertPosition)+
                        currentDir
                                + File.separator
                                + canonicalPath.substring(insertPosition);
            }
            currentConfigFileName = fileInDirectory;


        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return currentConfigFileName;
    }

    /**
     * Save variable to config file.
     *
     * @param variableName the variable name
     * @param value        the value to save with that variable name
     */
    public static void saveVariableToConfigFile(String variableName, String value) {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(currentConfigFileName)) {
            prop.load(new InputStreamReader(fis, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getCause() + " " + ex.getLocalizedMessage());
        }
        prop.setProperty(variableName, value);

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(currentConfigFileName)))) {
            prop.store(pw, null);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getCause() + " " + e.getLocalizedMessage());
        }

    }

    /**
     * Process config map that came to the library entry point in RDFtoCSV(fileName, configMap) constructor.
     *
     * @param fileName  the file name
     * @param configMap the config map - contains table, readMethod and firstNormalForm information
     */
    public static void processConfigMap(String fileName, Map<String, String> configMap) {
        createConfigFile();
        String tables = ONE_TABLE;
        String readMethod = DEFAULT_PARSING_METHOD;
        String conversionMethod = DEFAULT_CONVERSION_METHOD;
        String firstNormalForm = "false";
        if (configMap != null) {
            if (configMap.containsKey("table")) {
                tables = switch (configMap.get("table")) {
                    case "splitQuery", "MORE", "more" -> "more";
                    default -> "one";
                };
            }
            if (configMap.get("readMethod") != null) {
                readMethod = configMap.get("readMethod");
            }
            conversionMethod = (tables.equalsIgnoreCase(ONE_TABLE)) ? DEFAULT_CONVERSION_METHOD : MULTIPLE_TABLES_CONVERSION_METHOD;
            if (configMap.containsKey("firstNormalForm")) {
                firstNormalForm = configMap.get("firstNormalForm");
            }
        }
        /*
        conversionMethod = switch (readMethod.toLowerCase()) {
            case "bigfilestreaming" -> "bigfilestreaming";
            case "streaming" -> "streaming";
            default -> DEFAULT_PARSING_METHOD;
        };

         */
        //System.out.print("conversion method = " + conversionMethod);
        //System.out.print("read method = " + readMethod);
        saveVariableToConfigFile(CONVERSION_METHOD, conversionMethod);
        saveVariableToConfigFile(TABLES, tables);
        saveVariableToConfigFile(FIRST_NORMAL_FORM, String.valueOf(firstNormalForm));
        saveVariableToConfigFile(READ_METHOD, readMethod);
        saveVariableToConfigFile(INPUT_FILENAME, fileName);
        saveVariableToConfigFile(STREAMING_CONTINUOUS, "false");
        saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES, "");
        saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_BLANK_NODES, "false");
        saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, "true");
        ConfigurationManager.saveVariableToConfigFile("simpleBasicQuery", "false");

        saveVariableToConfigFile(ConfigurationManager.OUTPUT_FILENAME, fileName);
        saveVariableToConfigFile(ConfigurationManager.OUTPUT_FILE_PATH, "");
        saveVariableToConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME, DEFAULT_METADATA_FILENAME);
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.METADATA_ROWNUMS, "false");
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_ZIPFILE_NAME, fileName + "_CSVW.zip");

    }

    /**
     * Get a parameter by its key from app.config file.
     *
     * @param variableName The name of the key to retrieve from the app.config map of parameters.
     * @return String object (value) of the given config variable (key)
     */
    public static String getVariableFromConfigFile(String variableName) {
        Properties prop = new Properties();

        try (FileInputStream fis = new FileInputStream(currentConfigFileName)) {

            prop.load(new InputStreamReader(fis, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getCause() + " " + ex.getLocalizedMessage());
        }

        return prop.getProperty(variableName);
    }

    /**
     * Create the app.config file. Mandatory to be able to write the configurations from the whole conversion process.
     */
    public static void createConfigFile() {
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

    }

    /**
     * Read arg with default options string.
     *
     * @param args    the args from Main parameters
     * @param argName the arg name the name for file argument
     * @return the string of File depending on where the program is run from. Changes if the program runs from /target
     */
    public static String readArgWithDefaultOptions(String[] args, String argName) {
        Options options = addArgsOptions();

        CommandLineParser parser = new DefaultParser();
        if(args.length == 1) {
            throwAdviceInTheTerminal(options);
        }
        try {
            CommandLine cmd = parser.parse(options, args);
            String inputFile = cmd.getOptionValue(argName);
            boolean streamingMethod = cmd.hasOption(CMD_OPTION_STREAMING);
            // Continue with processing...
            if ((inputFile == null && !streamingMethod)) {
                throwAdviceInTheTerminal(options);
            }
            return inputFile;
        } catch (ParseException e) {
            throwAdviceInTheTerminal(options);
        }

        return null;
    }

    /**
     * Load settings from input to config file.
     *
     * @param args the args from the program run parameters
     */
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

            try {
                URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
                File file = new File(location.toURI().getPath());
                String jarDirectory = file.getParentFile().getName();
                String fileInDirectory;
                String fileArgFromArgs = ConfigurationManager.readArgWithDefaultOptions(args, "file");
                if (jarDirectory.equalsIgnoreCase("target")) {
                    fileInDirectory = fileArgFromArgs;
                } else {
                    fileInDirectory = jarDirectory + File.separator + fileArgFromArgs;
                }

                saveVariableToConfigFile(ConfigurationManager.INPUT_FILENAME, fileInDirectory);
            } catch (URISyntaxException e) {
                logger.log(Level.SEVERE, e.getCause() + " " + e.getLocalizedMessage());
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

    /**
     * Sets the configuration for run started in Main. For command line story.
     * @param multipleTables true / false. True if multiple tables are supposed to be made.
     * @param parsingMethod RDF4J STREAMING or BIGFILESTREAMING
     * @param inputFile the input file to parse
     * @param streaming the -s parameter. If true and parsing method is STREAMING, the triples will be read from stdin.
     * @param firstNormalForm If true the output CSV will contain only atomic values in its cells.
     * @param outputFilename The name for output file
     */
    private static void writeOptionsToConfigFile(boolean multipleTables, String parsingMethod, String inputFile, boolean streaming, boolean firstNormalForm, String outputFilename) {
        String conversionMethod;
        File finalConfigFile = new File(currentConfigFileName);

        Properties prop = new Properties();

        try (FileInputStream fis = new FileInputStream(finalConfigFile)) {
            prop.load(fis);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getCause() + " " + ex.getLocalizedMessage());
            System.err.println("Trouble reading config file for the first time");
            System.exit(1);
        }

        String metadataFileName;
        parsingMethod = (parsingMethod != null) ? parsingMethod.toLowerCase() : DEFAULT_PARSING_METHOD;
        String baseFileName;
        if (outputFilename == null) {
            baseFileName = inputFile.split("\\.")[0];
            System.out.println("inputFile in ConfigurationManager = " + inputFile);
            for(String split : inputFile.split("\\.")){
                System.out.println("inputFile.split in ConfigurationManager = " + split);

            }
            if(baseFileName.isEmpty()){
                System.out.println("FileSystems.getDefault().getSeparator() in ConfigurationManager = " + FileSystems.getDefault().getSeparator());
                String separator = FileSystems.getDefault().getSeparator();
                if (inputFile.contains("/")) {
                    separator = "/";
                }
                if (inputFile.contains("\\")) {
                    separator = "\\";
                }
                String[] splitPath = inputFile.split(Pattern.quote(separator));
                baseFileName = splitPath[splitPath.length-1];
                for(String split : splitPath){
                    System.out.println("inputFile.splitPath in ConfigurationManager = " + split);

                }

            }
        } else {
            baseFileName = outputFilename;
            System.out.println("outputFilename != null in ConfigurationManager = " + outputFilename);
        }
        if (isUrl(inputFile)) {
            baseFileName = iri(inputFile).getLocalName();
            System.out.println("isUrl(inputFile) in ConfigurationManager = " + baseFileName);
        }
        System.out.println("baseFileName in ConfigurationManager = " + baseFileName);

        conversionMethod = (!multipleTables) ? DEFAULT_CONVERSION_METHOD : MULTIPLE_TABLES_CONVERSION_METHOD;
        prop.setProperty(ConfigurationManager.TABLES, (!multipleTables) ? "one" : "more");
        /*
        conversionMethod = switch (parsingMethod) {
            case "bigfilestreaming" -> "bigfilestreaming";
            case "streaming" -> "streaming";
            default -> parsingMethod;
        };

         */
        prop.setProperty(ConfigurationManager.OUTPUT_FILENAME, baseFileName);
        prop.setProperty(ConfigurationManager.FIRST_NORMAL_FORM, String.valueOf(firstNormalForm));
        prop.setProperty(ConfigurationManager.CONVERSION_METHOD, conversionMethod);
        prop.setProperty(ConfigurationManager.INTERMEDIATE_FILE_NAMES, "");
        prop.setProperty(ConfigurationManager.CONVERSION_HAS_BLANK_NODES, "false");
        prop.setProperty(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, "true");
        prop.setProperty(ConfigurationManager.OUTPUT_ZIPFILE_NAME,  baseFileName + "_CSVW.zip");
        prop.setProperty(ConfigurationManager.READ_METHOD, parsingMethod);
        prop.setProperty(ConfigurationManager.METADATA_ROWNUMS, "false");
        prop.setProperty(ConfigurationManager.OUTPUT_FILE_PATH, "");
        prop.setProperty(ConfigurationManager.STREAMING_CONTINUOUS, String.valueOf(streaming));
        prop.setProperty("simpleBasicQuery", "false");


        metadataFileName = DEFAULT_METADATA_FILENAME;

        prop.setProperty(ConfigurationManager.OUTPUT_METADATA_FILE_NAME, metadataFileName);

        // Store options to config file
        try {
            PrintWriter pw = new PrintWriter(currentConfigFileName);
            prop.store(pw, null);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getCause() + " " + e.getLocalizedMessage());
        }
    }

    private static Options addArgsOptions() {
        Options options = new Options();
        options.addOption("t", "multipleTables", false, "Enable creation of multiple tables during conversion");
        options.addOption("p", "parsing", true, "Specify the parsing method");
        options.addOption("h", "help", false, "Show the command line options");
        options.addOption("f", "file", true, "File for conversion");
        options.addOption("s", "streaming", false, "Parse the file in streaming mode (continual parsing until stopped)");
        options.addOption("n", "firstNormalForm", false, "Put the output CSV data into first normal form (every cell contains only one entry, no lists of values)");
        return options;
    }

    public static void throwAdviceInTheTerminal(Options options){
        System.err.println("You must specify file input argument to the command line. ");
        printHelpLine(options);
        System.exit(1);
    }

    public static void throwAdviceInTheTerminal(){
        Options options = addArgsOptions();
        System.err.println("You must specify file input argument to the command line. ");
        printHelpLine(options);
        System.exit(1);
    }
}
