package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;

public class RdfxmlParser implements IParsingMethod {
    @Override
    public RDFFormat processInput(File fileToParse) {
        return RDFFormat.RDFXML;
    }
}
