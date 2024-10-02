package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.convertor.*;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.MetadataService;
import com.miklosova.rdftocsvw.output_processor.ZipOutputProcessor;
import org.apache.log4j.BasicConfigurator;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

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
            if(jarDirectory.equalsIgnoreCase("target")){
                fileInDirectory =  args[0];
            } else {
                fileInDirectory = jarDirectory + File.separator + args[0];
            }

            RDFFileToRead = fileInDirectory;
            System.out.println("JAR Directory: " + jarDirectory + " fileInDirectory = " + fileInDirectory);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        ConfigurationManager.loadSettingsFromInputToConfigFile(args);

        //System.out.println(RDFFileToRead);
        RDFtoCSV rdFtoCSV;

        if(RDFFileToRead == null){
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
            //rdFtoCSV.convertToZip();

            System.out.println(rdFtoCSV.getCSVTableAsString());
        } catch (RDFParseException rdfParseException) {
            String newFileName = rdFtoCSV.getOutputFileName() + ".csv";
            File f = new File(newFileName);
            System.out.println(rdfParseException.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        // Capture end time
        long endTime = System.currentTimeMillis();

        // Calculate total runtime
        long totalTime = endTime - startTime;

        // Output the total time
        System.out.println("Program ran for " + totalTime + " milliseconds.");
    }
}
