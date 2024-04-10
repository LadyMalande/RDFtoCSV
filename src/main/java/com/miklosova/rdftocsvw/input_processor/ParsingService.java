package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.*;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;

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

    public void processInput(File fileToRead) {
        String fileName = fileToRead.getName();
        processExtension(fileName);
        RDFFormat parsedInput = inputGateway.processInput(fileToRead);
        System.out.println("Processed file: " + fileToRead.getAbsolutePath());
    }

    private void processExtension(String fileName){
        String[] splitName = fileName.split("\\.");
        String fileExtension = splitName[splitName.length - 1];

        switch (fileExtension) {
            case "ttl":
                inputGateway.setParsingMethod(turtleParser);
                break;
            case "brf":
                inputGateway.setParsingMethod(binaryParser);
                break;
            case "hdt":
                inputGateway.setParsingMethod(hdtParser);
                break;
            case "jsonld":
                inputGateway.setParsingMethod(jsonldParser);
                break;
            case "n3":
                inputGateway.setParsingMethod(n3Parser);
                break;
            case "ndjsonld":
            case "jsonl":
            case "ndjson":
                inputGateway.setParsingMethod(ndjsonldParser);
                break;
            case "nq":
                inputGateway.setParsingMethod(nquadsParser);
                break;
            case "nt":
                inputGateway.setParsingMethod(ntriplesParser);
                break;
            case "xhtml":
            case "html":
                inputGateway.setParsingMethod(rdfaParser);
                break;
            case "rj":
                inputGateway.setParsingMethod(rdfjsonParser);
                break;
            case "rdf":
            case "rdfs":
            case "owl":
            case "xml":
                inputGateway.setParsingMethod(rdfxmlParser);
                break;
            case "trig":
                inputGateway.setParsingMethod(trigParser);
                break;
            case "trigs":
                inputGateway.setParsingMethod(trigstarParser);
                break;
            case "trix":
                inputGateway.setParsingMethod(trixParser);
                break;
            case "ttls":
                inputGateway.setParsingMethod(turtlestarParser);
                break;
            default:
                throw new IllegalArgumentException("Invalid payment method");
        }
    }

}
