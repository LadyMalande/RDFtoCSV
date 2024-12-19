package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.convertor.RDFtoCSV;
import org.eclipse.rdf4j.rio.RDFParseException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static com.miklosova.rdftocsvw.support.FileWrite.writeStringArrayAsCSVToFile;

public class Main {
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
            File fileToRead = new File(jarDirectory, args[0]);
            String fileInDirectory = null;
            String fileArgFromArgs = ConfigurationManager.readArgWithDefaultOptions(args, "file");
            if (jarDirectory.equalsIgnoreCase("target")) {
                //fileInDirectory =  args[0];
                fileInDirectory = fileArgFromArgs;
            } else {
                // fileInDirectory = jarDirectory + File.separator + args[0];
                fileInDirectory = jarDirectory + File.separator + fileArgFromArgs;
            }

            RDFFileToRead = fileInDirectory;
            System.out.println("JAR Directory: " + jarDirectory + " fileInDirectory = " + fileInDirectory);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        ConfigurationManager.loadSettingsFromInputToConfigFile(args);

        //System.out.println(RDFFileToRead);
        RDFtoCSV rdFtoCSV;

        if (RDFFileToRead == null) {
            System.err.println("The file passed to the program is null. Usage: java -jar Main <filename>");
            System.exit(1);
        }
        // Capture end time
        long endTime1 = System.currentTimeMillis();

        // Calculate total runtime
        long subtotalTime1 = endTime1 - startTime;

        // Output the total time
        System.out.println("Program ran for " + subtotalTime1 + " milliseconds.");

        rdFtoCSV = new RDFtoCSV(RDFFileToRead);

        // Capture end time
        long endTime2 = System.currentTimeMillis();

        // Calculate total runtime
        long subtotalTime2 = endTime2 - startTime;

        // Output the total time
        System.out.println("Program ran for " + subtotalTime2 + " milliseconds. After rdFtoCSV = new RDFtoCSV(RDFFileToRead);");


        try {
            rdFtoCSV.convertToZip();

            //System.out.println(rdFtoCSV.getCSVTableAsString());
        } catch (RDFParseException | IOException rdfParseException) {
            System.out.println(rdfParseException.getMessage());
        }


        // Capture end time
        long endTime = System.currentTimeMillis();

        // Calculate total runtime
        long totalTime = endTime - startTime;

        int numberOfCreatedCSVs = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES).split(",").length;
        File processedFile = new File("../" + ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INPUT_FILENAME));
        System.out.println("INPUT_FILENAME : " + ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INPUT_FILENAME) );

        System.out.println("processedFile : " + processedFile.getAbsolutePath() );
        if (processedFile.exists() && processedFile.isFile()) {
            long fileSizeInBytes = processedFile.length();
            double fileSizeInKB = fileSizeInBytes / 1024.0;
            double fileSizeInMB = fileSizeInKB / 1024.0;
            System.out.println("Length of file in bytes: " + fileSizeInBytes );
            System.out.println("Length of file in Kbytes: " + fileSizeInKB );
            System.out.println("Length of file in Mbytes: " + fileSizeInMB );

            writeStringArrayAsCSVToFile("experimentTimeDurations.csv",new String[] {String.valueOf(totalTime),
                    String.valueOf(fileSizeInMB),
                    ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD),
                    ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.TABLES),
                    ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.FIRST_NORMAL_FORM),
                    String.valueOf(numberOfCreatedCSVs),
                    processedFile.getName()});

            System.out.printf("File size: %d bytes (%.2f KB, %.2f MB)%n", fileSizeInBytes, fileSizeInKB, fileSizeInMB);
        } else {
            System.out.println("The file does not exist or is not a regular file.");
            writeStringArrayAsCSVToFile("experimentTimeDurations.csv",new String[] {String.valueOf(totalTime),
                    "null",
                    ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD),
                    ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.TABLES),
                    ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.FIRST_NORMAL_FORM),
                    String.valueOf(numberOfCreatedCSVs),
                    processedFile.getName()});
        }


        // Output the total time
        System.out.println("Program ran for " + totalTime + " milliseconds.");
    }
}
