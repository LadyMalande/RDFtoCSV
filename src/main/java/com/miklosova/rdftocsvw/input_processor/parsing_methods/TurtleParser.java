package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import com.miklosova.rdftocsvw.support.FileModifier;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;

import java.io.*;
import java.nio.file.Path;

public class TurtleParser implements IRDF4JParsingMethod {
    @Override
    public RepositoryConnection processInput(RepositoryConnection conn, File fileToParse) {
        RDFFormat fileFormat = RDFFormat.TURTLE;
        try {
            System.out.println("File to process absolute path: " + fileToParse.getAbsolutePath());
            Path path = fileToParse.getAbsoluteFile().toPath();
            path = path.normalize();
            InputStream targetStream = new FileInputStream(path.toFile());

            conn.add(targetStream, "", fileFormat);
        } catch (RDFParseException rdfParseException) {
            FileModifier.addColonsToIRIsInFile(fileToParse);
            Path path = fileToParse.getAbsoluteFile().toPath();
            path = path.normalize();
            InputStream targetStream;
            try {
                targetStream = new FileInputStream(path.toFile());
                //System.out.println("path.toFile(): " + path.toFile());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            try {
                conn.add(targetStream, "", fileFormat);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }
}

