package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.converter.RDFtoCSV;
import org.eclipse.rdf4j.rio.RDFParseException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.miklosova.rdftocsvw.output_processor.FileWrite.writeStringArrayAsCSVToFile;

/**
 * Main entry point for calling from command line.
 * To get a list of available arguments, add parameter -h while running the JAR file.
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * The entry point of application.
     *
     * @param args The input arguments.
     */
    public static void main(String[] args) {
        // Capture start time

        if (args.length < 1) {
            System.err.println("Usage: java -jar RDFtoCSV<vrsion>.jar -f <filename>\n for better explanation of arguments, run  java -jar RDFtoCSV<vrsion>.jar -f <filename> -h ");
            System.exit(1);
        }

        String RDFFileToRead = null;
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

            RDFFileToRead = fileInDirectory;
            System.out.println("JAR Directory: " + jarDirectory + " fileInDirectory = " + fileInDirectory);
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, e.getReason() + " " + e.getMessage());
        }

        ConfigurationManager.loadSettingsFromInputToConfigFile(args);

        RDFtoCSV rdFtoCSV;

        if (RDFFileToRead == null) {
            System.err.println("The file passed to the program is null. Usage: java -jar Main <filename>");
            System.exit(1);
        }
        rdFtoCSV = new RDFtoCSV(RDFFileToRead);

        try {
            rdFtoCSV.convertToZip();
        } catch (RDFParseException | IOException rdfParseException) {
            System.out.println(rdfParseException.getMessage());
        }
    }
}
