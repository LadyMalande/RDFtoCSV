package com.miklosova.rdftocsvw.input_processor;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.IParsingMethod;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;

public class InputGateway {
    private IParsingMethod parsingMethod;

    public void setParsingMethod(IParsingMethod parsingMethod) {
        this.parsingMethod = parsingMethod;
    }

    public RDFFormat processInput(File fileToProcess) {
        RDFFormat format = parsingMethod.processInput(fileToProcess);
        return parsingMethod.processInput(fileToProcess);
    }

}
