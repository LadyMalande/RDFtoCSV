package com.miklosova.rdftocsvw.input_processor.parsing_methods;

import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;

public interface IRDF4JParsingMethod {
    RDFFormat processInput(File fileToParse);
}
