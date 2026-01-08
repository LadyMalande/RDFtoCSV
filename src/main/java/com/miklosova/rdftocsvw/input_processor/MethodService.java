package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.RDF4JMethod;
import com.miklosova.rdftocsvw.input_processor.streaming_methods.StreamingMethod;
import com.miklosova.rdftocsvw.output_processor.FileWrite;
import com.miklosova.rdftocsvw.support.AppConfig;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
     * @throws IOException if the URL cannot be downloaded
     */
    private String processFileOrIRI(String fileName) throws IOException {
        try {
            // Convert internationalized domain names (IDN) to ASCII punycode
            URL originalUrl = new URL(fileName);
            String asciiHost = IDN.toASCII(originalUrl.getHost());
            
            // Reconstruct URL with ASCII host
            String asciiUrlString = originalUrl.getProtocol() + "://" + asciiHost;
            if (originalUrl.getPort() != -1) {
                asciiUrlString += ":" + originalUrl.getPort();
            }
            if (originalUrl.getPath() != null) {
                asciiUrlString += originalUrl.getPath();
            }
            if (originalUrl.getQuery() != null) {
                asciiUrlString += "?" + originalUrl.getQuery();
            }
            
            URL url = new URL(asciiUrlString);

            String[] splitURI = fileName.split("/");
            String newFileName = splitURI[splitURI.length - 1];

            // Download file using binary stream with proper HTTP headers
            URLConnection connection = url.openConnection();
            
            // Set headers to mimic a browser request
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection httpConn = (HttpURLConnection) connection;
                httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                httpConn.setRequestProperty("Accept", "text/turtle, application/rdf+xml, application/ld+json, application/n-triples, application/n-quads, application/trig, text/n3, */*");
                httpConn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                httpConn.setRequestProperty("Connection", "keep-alive");
            }
            
            try (InputStream in = connection.getInputStream();
                 FileOutputStream out = new FileOutputStream(newFileName)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            logger.info( "Successfully Downloaded. NewFileName is " + newFileName);
            return newFileName;
        } catch (MalformedURLException e) {
            //System.err.println("URL is invalid: " + fileName + ". Returns original filename in processFileOrIRI method.");
            return fileName;
            // the URL is not in a valid form
        } catch (IOException ex) {
            String errorMessage = "Failed to download file from URL: " + fileName + 
                                ". Please check the URL is accessible and try again.";
            System.err.println(errorMessage);
            System.err.println("Detailed error: " + ex.getClass().getName() + ": " + ex.getMessage());
            ex.printStackTrace();

            throw new IOException(errorMessage, ex);
            // the connection couldn't be established
        }

    }

    private void processMethodChoice(String methodChoice) {
        //logger.info("methodChoice=" + methodChoice);
        switch (methodChoice.toLowerCase()) {
            case "rdf4j" -> methodGateway.setParsingMethod(new RDF4JMethod());
            case "streaming", "bigfilestreaming" -> methodGateway.setParsingMethod(new StreamingMethod(config));
            default -> throw new IllegalArgumentException("Invalid reading method: " + methodChoice);
        }
    }
}
