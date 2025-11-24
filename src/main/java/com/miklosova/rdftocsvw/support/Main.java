package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.converter.RDFtoCSV;
import org.apache.commons.cli.*;
import org.eclipse.rdf4j.rio.RDFParseException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.miklosova.rdftocsvw.support.ConnectionChecker.isUrl;

/**
 * Main entry point for calling from command line.
 * To get a list of available arguments, add parameter -h while running the JAR file.
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * The entry point of application. The -f argument is mandatory to run the conversion successfully. If argument is missing,
     * helpful guide is thrown to the standard output.
     *
     * @param args The input arguments.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java -jar RDFtoCSV<version>.jar -f <filename>\n for better explanation of arguments, run  java -jar RDFtoCSV<version>.jar -f <filename> -h ");
            System.exit(1);
        }

        // Parse command line arguments
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();
        
        try {
            CommandLine cmd = parser.parse(options, args);
            
            // Check for help flag
            if (cmd.hasOption("h")) {
                printHelp(options);
                System.exit(0);
            }
            
            // Check for required file argument
            if (!cmd.hasOption("f") && !cmd.hasOption("streaming")) {
                System.err.println("Error: File argument (-f) is required unless using streaming mode (-s)");
                printHelp(options);
                System.exit(1);
            }
            
            String fileArg = cmd.getOptionValue("f");
            String RDFFileToRead = resolveFilePath(fileArg);
            
            if (RDFFileToRead == null) {
                System.err.println("The file passed to the program is null. Usage: java -jar Main <filename>");
                System.exit(1);
            }
            
            // Build AppConfig from command line arguments
            AppConfig config = buildConfigFromArgs(cmd, RDFFileToRead);
            
            // Create RDFtoCSV with the new config
            RDFtoCSV rdFtoCSV = new RDFtoCSV(config);
            
            try {
                rdFtoCSV.convertToZip();
            } catch (RDFParseException | IOException rdfParseException) {
                System.err.println(rdfParseException.getMessage());
            }
            
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            printHelp(options);
            System.exit(1);
        }
    }
    
    /**
     * Build AppConfig from parsed command line arguments.
     */
    private static AppConfig buildConfigFromArgs(CommandLine cmd, String filePath) {
        AppConfig.Builder builder = new AppConfig.Builder(filePath);
        
        // Optional parameters
        if (cmd.hasOption("p")) {
            builder.parsing(cmd.getOptionValue("p"));
        }
        
        if (cmd.hasOption("t")) {
            builder.multipleTables(true);
        }
        
        if (cmd.hasOption("s")) {
            builder.streaming(true);
        }
        
        if (cmd.hasOption("n")) {
            builder.firstNormalForm(true);
        }
        
        if (cmd.hasOption("o")) {
            builder.output(cmd.getOptionValue("o"));
        }
        
        // Additional parameters could be added here for:
        // - preferredLanguages
        // - columnNamingConvention  
        // - logLevel
        
        return builder.build();
    }
    
    /**
     * Resolve the file path based on the current execution context.
     */
    private static String resolveFilePath(String fileArg) {
        if (fileArg == null) {
            return null;
        }
        
        if (isUrl(fileArg)) {
            return fileArg;
        }
        
        try {
            URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
            File file = new File(location.toURI().getPath());
            String jarDirectory = file.getParentFile().getName();
            
            if (jarDirectory.equalsIgnoreCase("target")) {
                return fileArg;
            } else {
                return jarDirectory + File.separator + fileArg;
            }
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, e.getReason() + " " + e.getMessage());
            return fileArg;
        }
    }
    
    /**
     * Create command line options.
     */
    private static Options createOptions() {
        Options options = new Options();
        options.addOption("t", "multipleTables", false, "Enable creation of multiple tables during conversion");
        options.addOption("p", "parsing", true, "Specify the parsing method");
        options.addOption("h", "help", false, "Show the command line options");
        options.addOption("f", "file", true, "File for conversion");
        options.addOption("s", "streaming", false, "Parse the file in streaming mode (continual parsing until stopped)");
        options.addOption("n", "firstNormalForm", false, "Put the output CSV data into first normal form (every cell contains only one entry, no lists of values)");
        options.addOption("o", "output", true, "Put the output path for the file");
        return options;
    }
    
    /**
     * Print help information.
     */
    private static void printHelp(Options options) {
        HelpFormatter formatter = HelpFormatter.builder().get();
        formatter.printHelp("RDFtoCSV Command Line Tool", options);
    }
}
