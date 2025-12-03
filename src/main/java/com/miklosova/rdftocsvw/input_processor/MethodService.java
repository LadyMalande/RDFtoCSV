package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.RDF4JMethod;
import com.miklosova.rdftocsvw.input_processor.streaming_methods.StreamingMethod;
import com.miklosova.rdftocsvw.output_processor.FileWrite;
import com.miklosova.rdftocsvw.support.AppConfig;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFParseException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class loads the RDF data into the common RDF model. It runs the checks for which methods for conversion have been chosen and sets parsers accordingly.
 * Part of Strategy pattern.
 */
public class MethodService {
    private MethodGateway methodGateway;
    private static final Logger logger = Logger.getLogger(FileWrite.class.getName());
    private AppConfig config;

    /**
     * Default constructor for backward compatibility.
     * @deprecated Use {@link #MethodService(AppConfig)} instead
     */
    @Deprecated
    public MethodService() {
        this.config = null;
    }

    /**
     * Constructor with AppConfig.
     * @param config The application configuration
     */
    public MethodService(AppConfig config) {
        this.config = config;
    }

    /**
     * Process input repository connection.
     *
     * @param fileName     the file name to convert
     * @param methodChoice the method choice
     * @param db           the Repository to establish RepositoryConnection Upon
     * @return the repository connection to the Repository we will be asking SPARQL queries on
     * @throws RDFParseException the rdf parse exception - thrown if the RDF file is corrupted or in a form that is unable to be parsed by RDF4J library
     * @throws IOException       the io exception
     */
    public RepositoryConnection processInput(String fileName, String methodChoice, Repository db) throws RDFParseException, IOException {
        methodGateway = new MethodGateway();
        processMethodChoice(methodChoice);
        fileName = processFileOrIRI(fileName);
        
        if (config == null) {
            throw new IllegalStateException("AppConfig is required");
        }
        config.setOutputFilePath(fileName);
        // Note: IntermediateFileNames and other runtime values are set elsewhere during conversion
        
        File fileToRead = new File(fileName);
        try {
            return methodGateway.processInput(fileToRead, db);
        } catch(OutOfMemoryError err){
            logger.log(Level.WARNING, "The data is too big to be processed by RDF4J method. The method has been changed to 'BigFileStreaming'. Continuing processing...");
            methodGateway.setParsingMethod(new StreamingMethod(config));
            
            config.setConversionMethod("bigfilestreaming");
            return methodGateway.processInput(fileToRead, db);
        }
    }

    /**
     * Processes the File Name. If its URL and the connection enables the process to download it, it will do so.
     * @param fileName the argument that is given when starting the program (-f parameter value)
     * @return Name of the saved file (if URL is given to the converter, it downloads the file and takes only the file name to this output)
     */
    private String processFileOrIRI(String fileName) {
        try {
            URL url = new URL(fileName);

            BufferedReader readr =
                    new BufferedReader(new InputStreamReader(url.openStream()));

            String[] splitURI = fileName.split("/");

            String newFileName = splitURI[splitURI.length - 1];

            // Enter filename in which you want to download
            BufferedWriter writer =
                    new BufferedWriter(new FileWriter(newFileName));

            // read each line from stream till end
            String line;
            while ((line = readr.readLine()) != null) {
                writer.write(line + "\n");
            }

            readr.close();
            writer.close();
            logger.info( "Successfully Downloaded. NewFileName is " + newFileName);
            return newFileName;
        } catch (MalformedURLException e) {
            System.err.println("URL is invalid: " + fileName + ". Returns original filename in processFileOrIRI method.");
            return fileName;
            // the URL is not in a valid form
        } catch (IOException ex) {
            System.err.println("the connection couldn't be established to download the URL");
            return fileName;
            // the connection couldn't be established
        }

    }

    private void processMethodChoice(String methodChoice) {
        logger.info("methodChoice=" + methodChoice);
        switch (methodChoice.toLowerCase()) {
            case "rdf4j" -> methodGateway.setParsingMethod(new RDF4JMethod());
            case "streaming", "bigfilestreaming" -> methodGateway.setParsingMethod(new StreamingMethod(config));
            default -> throw new IllegalArgumentException("Invalid reading method: " + methodChoice);
        }
    }
}
