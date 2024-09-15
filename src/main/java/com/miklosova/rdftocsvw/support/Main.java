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

        rdFtoCSV = new RDFtoCSV(RDFFileToRead);

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
    }
}
