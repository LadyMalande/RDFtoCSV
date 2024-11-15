package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.convertor.QueryMethods;
import org.apache.commons.cli.*;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
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
    public static final String DEFAULT_CONVERSION_METHOD = "basicQuery";
    public static final String DEFAULT_OUTPUT_ZIPFILE_NAME = "zippedCSVW.zip";
    private static final String DEFAULT_PARSING_METHOD = "rdf4j";
    private static final String CONFIG_FILE_NAME = "../app.config";
    private static String currentConfigFileName;

    public static String getCONFIG_FILE_NAME() {
        currentConfigFileName = null;
        // Lazy initialization (modifies the variable only once)
        // Convert the relative path to a canonical path (removes ../ and resolves symlinks)
        String canonicalPath;
        String fileInDirectory;
        try {
            URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
            File file = new File(location.toURI().getPath());
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
            String fileNameBeingRead = null;
            // Create the new path by inserting jarDirectory
            if (jarDirectory.equalsIgnoreCase("target")) {
                //System.out.println("jarDirectory.equalsIgnoreCase(\"target\"");
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
            currentConfigFileName = fileInDirectory;


        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return currentConfigFileName;
    }

    public static void saveVariableToConfigFile(String variableName, String value) {
        //System.out.println("new String value with encoding for variable(" + variableName + "): " + value);
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(currentConfigFileName)) {
            prop.load(new InputStreamReader(fis, Charset.forName("UTF-8")));
        } catch (IOException ex) {
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

    public static void processConfigMap(Map<String, String> configMap) {
        String queryMethod = QueryMethods.BASIC_QUERY.getValue();
        if (configMap != null && configMap.containsKey("table")) {
            queryMethod = switch (configMap.get("table")) {
                case "splitQuery", "more" -> QueryMethods.SPLIT_QUERY.getValue();
                default -> QueryMethods.BASIC_QUERY.getValue();
            };
        }
        if (ConfigurationManager.getVariableFromConfigFile(CONVERSION_METHOD) == null)
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

        try (FileInputStream fis = new FileInputStream(currentConfigFileName)) {
            prop.load(new InputStreamReader(fis, Charset.forName("UTF-8")));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return prop.getProperty(variableName);
    }

    private static void createConfigFile() {

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

    }

    public static String readArgWithDefaultOptions(String[] args, String argName) {
        Options options = addArgsOptions();

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            String inputFile = cmd.getOptionValue(argName);
            boolean streamingMethod = cmd.hasOption("streaming");
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
            String multipleTables = cmd.getOptionValue("tables");
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

    private static void writeOptionsToConfigFile(String conversionMethod, String parsingMethod, String inputFile, boolean streaming, boolean firstNormalForm, String outputFilename) {
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

        String metadataFileName = null;
        parsingMethod = (parsingMethod != null) ? parsingMethod : DEFAULT_PARSING_METHOD;
        String baseFileName;
        if (outputFilename == null) {
            baseFileName = inputFile.split("\\.")[0];
        } else {
            baseFileName = outputFilename;
        }

        conversionMethod = (conversionMethod == null) ? DEFAULT_CONVERSION_METHOD : conversionMethod;
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
        if (metadataFileName == null) {
            metadataFileName = DEFAULT_METADATA_FILENAME;
        }
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
        options.addOption("t", "tables", true, "Enable creation of multiple tables during conversion");
        options.addOption("p", "parsing", true, "Specify the parsing method");
        options.addOption("h", "help", false, "Show the command line options");
        options.addOption("f", "file", true, "File for conversion");
        options.addOption("s", "streaming", false, "Parse the file in streaming mode (continual parsing until stopped)");
        options.addOption("n", "firstNormalForm", false, "Put the output CSV data into first normal form (every cell contains only one entry, no lists of values)");
        options.addOption("o", "output", true, "Output file name base. Will be given .csv extension. If not set, the name of the input file is taken.");
        return options;
    }

    /**
     * Set the configuration parameters in app.config
     * Set defaults if none are provided
     * Set parameters given in args if args are provided
     *
     * @param args Parameters provided in command line/parameters of conversion
     */
    /*
    public static void loadSettingsFromInputToConfigFile(String[] args) {
        Properties prop = new Properties();

        String metadataFileName = null;
        String parsingMethod = "rdf4j";
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
            System.out.println("args.length == 2");
        } else if (args.length == 3) {
            conversionMethod = args[1];
            parsingMethod = args[2];
            System.out.println("args.length == 3");
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
        System.out.println("property set prop.conversionMethod," + conversionMethod);
        prop.setProperty(ConfigurationManager.INTERMEDIATE_FILE_NAMES, "");
        prop.setProperty(ConfigurationManager.CONVERSION_HAS_BLANK_NODES, "false");
        prop.setProperty(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, "true");
        prop.setProperty(ConfigurationManager.OUTPUT_ZIPFILE_NAME, "compressed.zip");
        System.out.println("property set prop.setProperty(ConfigurationManager.READ_METHOD," + parsingMethod);
        prop.setProperty(ConfigurationManager.READ_METHOD, parsingMethod);
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
        System.out.println("Get value from config: " + ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.READ_METHOD));
    }

     */
}
