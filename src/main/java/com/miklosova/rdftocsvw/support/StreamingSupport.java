package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.metadata_creator.Triple;
import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.eclipse.rdf4j.rio.helpers.NTriplesUtil.parseLiteral;

public class StreamingSupport {
    public static Triple createTripleFromLine(String line) {
        // Regular expression to find text between < and >
        Pattern pattern = Pattern.compile("<(.*?)>");
        Matcher matcher = pattern.matcher(line);
        IRI subject = null;
        IRI predicate = null;
        Value object = null;
        // Iterate through the matches and print the results
        int i = 1;
        while (matcher.find()) {
            if (i == 1) {
                subject = iri(matcher.group(1)); // Prints the content between < and >
                //System.out.println("subject " + subject);
            } else if (i == 2) {
                predicate = iri(matcher.group(1));
                //System.out.println("predicate " + predicate);
            } else if (i == 3) {
                object = iri(matcher.group(1));
                System.out.println("object " + object);
            }
            i++;
        }
        if (object == null) {
            object = createLiteralHere(line);
        }

        Triple t = new Triple(subject, predicate, object);
        return t;
    }


    public void readNTriples(){
        try (Reader reader = new FileReader("example.nt")) {
            // Create an N-Triples parser
            Rio.createParser(RDFFormat.NTRIPLES).setRDFHandler(new RDFHandler() {
                @Override
                public void startRDF() throws RDFHandlerException {
                    System.out.println("Start parsing N-Triples.");
                }

                @Override
                public void endRDF() throws RDFHandlerException {
                    System.out.println("Finished parsing N-Triples.");
                }

                @Override
                public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
                    // No namespace handling in N-Triples
                }

                @Override
                public void handleStatement(Statement st) throws RDFHandlerException {
                    // Process each triple (subject, predicate, object)
                    System.out.println("Triple: " + st);
                }

                @Override
                public void handleComment(String comment) throws RDFHandlerException {
                    // Handle comments if any
                    System.out.println("Comment: " + comment);
                }
            }).parse(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static Value createLiteralHere(String line) {
        ValueFactory factory = SimpleValueFactory.getInstance();
        // Regex to match the string between the last '>' and the final '.'
        //Pattern pattern = Pattern.compile(">\\s*(.+?)\\s*\\.");
        //Pattern pattern = Pattern.compile(">([^>]*?)\\.[^.]*$");
        Pattern pattern = Pattern.compile("^(?:[^\\s\\t]+[ \\t]+){2}\"((?:[^\"\\\\]|\\\\[\"\\\\bnrt])*?)(?:\"(?:\\^\\^<[^>]+>|@[a-zA-Z\\-]+)?)\"[ \\t]*\\.$");
        Matcher matcher = pattern.matcher(line);
        Literal value = null;
        System.out.println("line " + line);
        int i = 1;
        while (matcher.find()) {
            if (i == 1) {
                String literalWithDatatype = matcher.group(1).trim();

                System.out.println("literalWithDatatype " + StringEscapeUtils.unescapeJava(literalWithDatatype));

                value = parseLiteral(StringEscapeUtils.unescapeJava(literalWithDatatype), factory);
                ////System.out.println("Parsed Literal with Datatype: " + value.getLabel() + " " + value.getLanguage() + value.getDatatype());
            }
            i++;
        }
        /*
        if (i < 3) {
            throw new IllegalArgumentException("The n triples file is malformed.");
        }

         */


        return value;
    }
}
