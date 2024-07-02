package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MethodService {
    private MethodGateway methodGateway;

    private RDF4JMethod rDF4JMethod;

    public RepositoryConnection processInput(String fileName, String methodChoice, Repository db) throws RDFParseException, IOException {
        methodGateway = new MethodGateway();
        System.out.println("fileName in MethodService.java processInput1: " + fileName);
        processMethodChoice(methodChoice);
        fileName = processFileOrIRI(fileName);
        System.out.println("fileName in MethodService.java processInput: " + fileName);
        File fileToRead = new File(fileName);
        return methodGateway.processInput(fileToRead, db);
    }

    private String processFileOrIRI(String fileName) {
            try {
                URL url = new URL(fileName);

                BufferedReader readr =
                        new BufferedReader(new InputStreamReader(url.openStream()));

                String[] splitURI = fileName.split("/");
                String nameForFile = splitURI[splitURI.length - 1];
                String extension = splitURI[splitURI.length - 1];

                String newFileName = nameForFile;

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
                System.out.println("Successfully Downloaded.");
                return newFileName;
            } catch (MalformedURLException e) {
                System.out.println("URL is invalid");
                //e.printStackTrace();
                return fileName;
                // the URL is not in a valid form
            } catch (IOException ex) {
                System.out.println("the connection couldn't be established");
                //ex.printStackTrace();
                return fileName;
                // the connection couldn't be established
            }

    }

    private void processMethodChoice(String methodChoice){
        switch (methodChoice) {
            case "rdf4j":
                methodGateway.setParsingMethod(new RDF4JMethod());
                break;
            default:
                throw new IllegalArgumentException("Invalid reading method");
        }
    }
}
