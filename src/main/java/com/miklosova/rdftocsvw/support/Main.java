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
        long startTime = System.currentTimeMillis();

        if (args.length < 1) {
            System.err.println("Usage: java -jar Main <filename>");
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

        // Capture end time
        long endTime = System.currentTimeMillis();

        // Calculate total runtime
        long totalTime = endTime - startTime;

        int numberOfCreatedCSVs = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES).split(",").length;

        File processedFile = new File("../" + ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INPUT_FILENAME));
        if (processedFile.getAbsolutePath().contains("\\..\\..") || processedFile.getAbsolutePath().contains("..\\C")) {
            processedFile = new File(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INPUT_FILENAME));
        }
        System.out.println("INPUT_FILENAME : " + ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INPUT_FILENAME));
        String afterParsingMilis = ConfigurationManager.getVariableFromConfigFile("experiment.afterParsing");
        String afterConvertingMilis = ConfigurationManager.getVariableFromConfigFile("experiment.afterConverting");
        String afterMetadataMilis = ConfigurationManager.getVariableFromConfigFile("experiment.afterMetadata");
        String afterFileWriteMilis = ConfigurationManager.getVariableFromConfigFile("experiment.afterFileWrite");
        long timeInParsing = Long.parseLong(afterParsingMilis) - startTime;
        long timeInConverting = Long.parseLong(afterConvertingMilis) - Long.parseLong(afterParsingMilis);
        long timeInMetadata = Long.parseLong(afterMetadataMilis) - Long.parseLong(afterConvertingMilis);
        long timeInFileWrite = Long.parseLong(afterFileWriteMilis) - Long.parseLong(afterMetadataMilis);
        long timeInZipping = endTime - Long.parseLong(afterFileWriteMilis);

        System.out.println("processedFile : " + processedFile.getAbsolutePath());
        if (processedFile.exists() && processedFile.isFile()) {
            long fileSizeInBytes = processedFile.length();
            double fileSizeInKB = fileSizeInBytes / 1024.0;
            double fileSizeInMB = fileSizeInKB / 1024.0;
            System.out.println("Length of file in bytes: " + fileSizeInBytes);
            System.out.println("Length of file in Kbytes: " + fileSizeInKB);
            System.out.println("Length of file in Mbytes: " + fileSizeInMB);


            writeStringArrayAsCSVToFile("experimentTimeDurations.csv", new String[]{String.valueOf(totalTime),
                    String.valueOf(fileSizeInMB),
                    ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD),
                    ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.TABLES),
                    ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.FIRST_NORMAL_FORM),
                    String.valueOf(numberOfCreatedCSVs),
                    processedFile.getName(),
                    new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()),
                    String.valueOf(timeInParsing),
                    String.valueOf(timeInConverting),
                    String.valueOf(timeInMetadata),
                    String.valueOf(timeInFileWrite),
                    String.valueOf(timeInZipping)
            });

            System.out.printf("File size: %d bytes (%.2f KB, %.2f MB)%n", fileSizeInBytes, fileSizeInKB, fileSizeInMB);
        } else {
            System.out.println("The file does not exist or is not a regular file.");
            writeStringArrayAsCSVToFile("experimentTimeDurations.csv", new String[]{String.valueOf(totalTime),
                    "null",
                    ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD),
                    ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.TABLES),
                    ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.FIRST_NORMAL_FORM),
                    String.valueOf(numberOfCreatedCSVs),
                    processedFile.getName(),
                    new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()),
                    String.valueOf(timeInParsing),
                    String.valueOf(timeInConverting),
                    String.valueOf(timeInMetadata),
                    String.valueOf(timeInFileWrite),
                    String.valueOf(timeInZipping),
                    "8192 MB heap space"});
        }


        // Output the total time
        System.out.println("Program ran for " + totalTime + " milliseconds.");
    }
}
