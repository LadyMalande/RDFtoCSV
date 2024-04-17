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

    public RDFFormat processInput(File fileToRead) {
        String fileName = fileToRead.getName();
        inputGateway = new InputGateway();
        processExtension(fileName);

        RDFFormat parsedInput = inputGateway.processInput(fileToRead);
        System.out.println("Processed file: " + fileToRead.getAbsolutePath());
        return parsedInput;
    }

    private void processExtension(String fileName){
        String[] splitName = fileName.split("\\.");
        String fileExtension = splitName[splitName.length - 1];

        switch (fileExtension) {
            case "ttl":
                inputGateway.setParsingMethod(new TurtleParser());
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
                throw new IllegalArgumentException("Invalid payment method");
        }
    }

}
