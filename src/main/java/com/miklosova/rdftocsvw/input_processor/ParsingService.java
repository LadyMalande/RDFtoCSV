package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.*;
import com.miklosova.rdftocsvw.support.ProgressLogger;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFParseException;

import java.io.File;
import java.io.IOException;

/**
 * The Parsing service. Chooses appropriate parsers by the supplied file.
 */
public class ParsingService {

    private InputGateway inputGateway;

    /**
     * Process input repository connection.
     *
     * @param conn       the connection to repository
     * @param fileToRead the file to read
     * @return the repository connection
     * @throws RDFParseException the RDF parse exception - usually bad RDF format
     * @throws IOException       the io exception
     */
    public RepositoryConnection processInput(RepositoryConnection conn, File fileToRead) throws RDFParseException, IOException {
        ProgressLogger.startStage(ProgressLogger.Stage.PARSING);
        
        String fileName = fileToRead.getName();
        inputGateway = new InputGateway();

        processExtension(fileName);

        conn = inputGateway.processInput(conn, fileToRead);
        
        ProgressLogger.completeStage(ProgressLogger.Stage.PARSING);
        return conn;
    }

    /**
     * Chooses appropriate RDF4J parser by the file extension.
     *
     * @param fileName The full RDF file name
     */
    private void processExtension(String fileName) {

        String[] splitName = fileName.split("\\.");
        String fileExtension = splitName[splitName.length - 1];
        switch (fileExtension) {
            case "ttl" -> inputGateway.setParsingMethod(new TurtleParser());
            case "brf" -> inputGateway.setParsingMethod(new BinaryParser());
            case "hdt" -> inputGateway.setParsingMethod(new HdtParser());
            case "jsonld" -> inputGateway.setParsingMethod((new JsonldParser()));
            case "n3" -> inputGateway.setParsingMethod(new N3Parser());
            case "ndjsonld", "jsonl", "ndjson" -> inputGateway.setParsingMethod(new NdjsonldParser());
            case "nq" -> inputGateway.setParsingMethod(new NquadsParser());
            case "nt" -> inputGateway.setParsingMethod(new NtriplesParser());
            case "xhtml", "html" -> inputGateway.setParsingMethod(new RdfaParser());
            case "rj" -> inputGateway.setParsingMethod(new RdfjsonParser());
            case "rdf", "rdfs", "owl", "xml" -> inputGateway.setParsingMethod(new RdfxmlParser());
            case "trig" -> inputGateway.setParsingMethod(new TrigParser());
            case "trigs" -> inputGateway.setParsingMethod(new TrigstarParser());
            case "trix" -> inputGateway.setParsingMethod(new TrixParser());
            case "ttls" -> inputGateway.setParsingMethod(new TurtlestarParser());
            default -> throw new IllegalArgumentException("Invalid file extension");
        }

    }


}
