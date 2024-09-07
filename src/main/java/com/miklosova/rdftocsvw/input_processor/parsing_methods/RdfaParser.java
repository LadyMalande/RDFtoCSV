package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.ParseErrorLogger;
import org.eclipse.rdf4j.rio.helpers.RDFaParserSettings;
import org.eclipse.rdf4j.rio.helpers.RDFaVersion;

import java.io.*;

public class RdfaParser implements IRDF4JParsingMethod {
    @Override
    public RepositoryConnection processInput(RepositoryConnection conn, File fileToParse) {
        try {
            // According to https://github.com/eclipse-rdf4j/rdf4j/issues/512 the work on RDFa parser is in progress.
            // The RDFa format is not supported in the current version of this RDFtcCSV converter.
            InputStream inputStream = new FileInputStream(fileToParse);

            ParserConfig parserConfig = new ParserConfig();
            parserConfig.set(RDFaParserSettings.RDFA_COMPATIBILITY, RDFaVersion.RDFA_1_0);
            Model model = Rio.parse(inputStream, "", RDFFormat.RDFA,
                    parserConfig, SimpleValueFactory.getInstance(), new ParseErrorLogger());

            conn.add(model);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return conn;
    }
}
