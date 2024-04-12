package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;

public class HdtParser implements IRDF4JParsingMethod {
    @Override
    public RDFFormat processInput(File fileToParse) {
        return RDFFormat.HDT;
    }
}
