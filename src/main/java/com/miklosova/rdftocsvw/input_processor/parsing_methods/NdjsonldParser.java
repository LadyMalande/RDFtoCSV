package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;

public class NdjsonldParser implements IRDF4JParsingMethod {
    @Override
    public RDFFormat processInput(File fileToParse) {
        return RDFFormat.NDJSONLD;
    }
}
