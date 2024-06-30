package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.*;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import org.eclipse.jetty.util.IO;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ParsingService {


    private InputGateway inputGateway;


    private BinaryParser binaryParser;


    private HdtParser hdtParser;
    private JsonldParser jsonldParser;
    private N3Parser n3Parser;
    private NdjsonldParser ndjsonldParser;
    private NquadsParser nquadsParser;
    private NtriplesParser ntriplesParser;
    private RdfaParser rdfaParser;
    private RdfjsonParser rdfjsonParser;
    private RdfxmlParser rdfxmlParser;
    private TrigParser trigParser;
    private TrixParser trixParser;
    private TurtleParser turtleParser;
    private TurtlestarParser turtlestarParser;
    private TrigstarParser trigstarParser;

    public RepositoryConnection processInput(RepositoryConnection conn, File fileToRead) throws RDFParseException, IOException {
        String fileName = fileToRead.getName();
        System.out.println("String fileName = fileToRead.getName();: " + fileName);
        inputGateway = new InputGateway();
       /* try{
            String newFileName = processURI(fileName);
            fileToRead = new File(newFileName);
        } catch(IOException ex){
            //processExtension(fileName);
        }

        */
        processExtension(fileName);


        conn = inputGateway.processInput(conn, fileToRead);
        System.out.println("processInput: Processed file: " + fileToRead.getAbsolutePath());
        return conn;
    }

    private void processExtension(String fileName){
        System.out.println("processExtension " + fileName);

        String[] splitName = fileName.split("\\.");
        String fileExtension = splitName[splitName.length - 1];
        System.out.println("fileExtension " + fileExtension);
            switch (fileExtension) {
                case "ttl":
                    inputGateway.setParsingMethod(new TurtleParser());
                    System.out.println("turtle parser set ");
                    break;
                case "brf":
                    inputGateway.setParsingMethod(new BinaryParser());
                    break;
                case "hdt":
                    inputGateway.setParsingMethod(new HdtParser());
                    break;
                case "jsonld":
                    inputGateway.setParsingMethod(new JsonldParser());
                    break;
                case "n3":
                    inputGateway.setParsingMethod(new N3Parser());
                    break;
                case "ndjsonld":
                case "jsonl":
                case "ndjson":
                    inputGateway.setParsingMethod(new NdjsonldParser());
                    break;
                case "nq":
                    inputGateway.setParsingMethod(new NquadsParser());
                    break;
                case "nt":
                    inputGateway.setParsingMethod(new NtriplesParser());
                    break;
                case "xhtml":
                case "html":
                    inputGateway.setParsingMethod(new RdfaParser());
                    break;
                case "rj":
                    inputGateway.setParsingMethod(new RdfjsonParser());
                    break;
                case "rdf":
                case "rdfs":
                case "owl":
                case "xml":
                    inputGateway.setParsingMethod(new RdfxmlParser());
                    break;
                case "trig":
                    inputGateway.setParsingMethod(new TrigParser());
                    break;
                case "trigs":
                    inputGateway.setParsingMethod(new TrigstarParser());
                    break;
                case "trix":
                    inputGateway.setParsingMethod(new TrixParser());
                    break;
                case "ttls":
                    inputGateway.setParsingMethod(new TurtlestarParser());
                    break;
                default:
                    throw new IllegalArgumentException("Invalid file extension");
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
