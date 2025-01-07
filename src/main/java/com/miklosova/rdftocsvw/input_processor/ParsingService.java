package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ParsingService {

    private InputGateway inputGateway;

    public RepositoryConnection processInput(RepositoryConnection conn, File fileToRead) throws RDFParseException, IOException {
        String fileName = fileToRead.getName();
        System.out.println("String fileName = fileToRead.getName();: " + fileName);
        inputGateway = new InputGateway();


        if (Rio.getParserFormatForMIMEType("application/n-quads").isPresent()) {
            System.out.println("N-Quads format is recognized.");
        } else {
            System.out.println("N-Quads format is not recognized.");
        }

        processExtension(fileName);

        conn = inputGateway.processInput(conn, fileToRead);
        System.out.println("processInput: Processed file: " + fileToRead.getAbsolutePath());
        return conn;
    }

    private void processExtension(String fileName) {
        System.out.println("processExtension " + fileName);

        String[] splitName = fileName.split("\\.");
        String fileExtension = splitName[splitName.length - 1];
        System.out.println("fileExtension " + fileExtension);
        switch (fileExtension) {
            case "ttl" -> {
                inputGateway.setParsingMethod(new TurtleParser());
                System.out.println("turtle parser set ");
            }
            case "brf" -> inputGateway.setParsingMethod(new BinaryParser());
            case "hdt" -> inputGateway.setParsingMethod(new HdtParser());
            case "jsonld" -> inputGateway.setParsingMethod((new JsonldParser()));
            case "n3" -> inputGateway.setParsingMethod(new N3Parser());
            case "ndjsonld", "jsonl", "ndjson" -> inputGateway.setParsingMethod(new NdjsonldParser());
            case "nq" -> inputGateway.setParsingMethod(new NquadsParser());
            case "nt" -> inputGateway.setParsingMethod(new NtriplesParser());
            case "xhtml", "html" -> inputGateway.setParsingMethod(new RdfaParser());
            case "rj" -> inputGateway.setParsingMethod(new RdfjsonParser());
            case "rdf", "rdfs", "owl", "xml" -> {System.out.println("Extension rdf\", \"rdfs\", \"owl\", \"xml");inputGateway.setParsingMethod(new RdfxmlParser());}
            case "trig" -> inputGateway.setParsingMethod(new TrigParser());
            case "trigs" -> inputGateway.setParsingMethod(new TrigstarParser());
            case "trix" -> inputGateway.setParsingMethod(new TrixParser());
            case "ttls" -> inputGateway.setParsingMethod(new TurtlestarParser());
            default -> throw new IllegalArgumentException("Invalid file extension");
        }

    }

    private String processURI(String fileName) throws IOException {
        try {
            URL url = new URL(fileName);
            URLConnection conn = url.openConnection();
            conn.connect();

            BufferedReader readr =
                    new BufferedReader(new InputStreamReader(url.openStream()));

            String[] splitURI = fileName.split("\\.");
            String nameForFile = splitURI[splitURI.length - 2];
            String extension = splitURI[splitURI.length - 1];
            String fileNameToSave = nameForFile + "." + extension;
            // Enter filename in which you want to download
            BufferedWriter writer =
                    new BufferedWriter(new FileWriter(fileNameToSave));


            // read each line from stream till end
            String line;
            while ((line = readr.readLine()) != null) {
                writer.write(line);
            }

            readr.close();
            writer.close();
            System.out.println("Successfully Downloaded.");
            return fileNameToSave;
        } catch (MalformedURLException e) {
            System.out.println("URL is invalid");
            throw e;

            // the URL is not in a valid form
        } catch (IOException ex) {
            System.out.println("the connection couldn't be established");
            ex.printStackTrace();
            throw ex;
            // the connection couldn't be established
        }

    }


}
