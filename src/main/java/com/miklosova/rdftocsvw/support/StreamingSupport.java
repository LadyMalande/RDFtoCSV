package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.metadata_creator.Triple;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

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
            if(i==1){
                subject = iri(matcher.group(1)); // Prints the content between < and >
                //System.out.println("subject " + subject);
            } else if(i==2){
                predicate = iri(matcher.group(1));
            } else if(i==3){
                object = iri(matcher.group(1));
            }
            i++;
        }
        if(object == null){
            object = createLiteralHere(line);
        }

        Triple t = new Triple(subject, predicate, object);
        return t;
    }



    private static Value createLiteralHere(String line) {
        ValueFactory factory = SimpleValueFactory.getInstance();
        // Regex to match the string between the last '>' and the final '.'
        Pattern pattern = Pattern.compile(">\\s*(.+?)\\s*\\.");
        Matcher matcher = pattern.matcher(line);
        Literal value = null;
        int i = 1;
        while (matcher.find()) {
            if(i==2){
                String literalWithDatatype = matcher.group(1).trim();
                value = parseLiteral(literalWithDatatype, factory);
                //System.out.println("Parsed Literal with Datatype: " + value.getLabel() + " " + value.getLanguage() + value.getDatatype());
            }
            i++;
        }
        if(i<3){
            throw new IllegalArgumentException("The n triples file is malformed.");
        }



        return value;
    }
}
