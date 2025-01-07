package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.JSONLDMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.eclipse.rdf4j.rio.helpers.JSONLDSettings.SECURE_MODE;

public class JsonldParser implements IRDF4JParsingMethod {
    @Override
    public RepositoryConnection processInput(RepositoryConnection conn, File fileToParse) {
        RDFFormat fileFormat = RDFFormat.JSONLD;
        try {
            InputStream targetStream = new FileInputStream(fileToParse);
            ParserConfig parserConfig = Rio.createParser(fileFormat).getParserConfig();
            parserConfig.set(SECURE_MODE, false);
            Model model = Rio.parse(targetStream, fileFormat, parserConfig);
            //conn.add(targetStream, "", parserConfig);
            conn.add(model);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }
}
