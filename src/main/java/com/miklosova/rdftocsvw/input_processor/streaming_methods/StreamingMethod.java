package com.miklosova.rdftocsvw.input_processor.streaming_methods;

import com.miklosova.rdftocsvw.input_processor.ParsingService;
import com.miklosova.rdftocsvw.input_processor.parsing_methods.IInputParsingMethod;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import lombok.extern.java.Log;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFParseException;

import java.io.File;
import java.io.IOException;

@Log
public class StreamingMethod implements IInputParsingMethod{
    @Override
    public RepositoryConnection processInput(File fileToParse, Repository db) throws RDFParseException, IOException {

        ParsingService ps = new ParsingService();

        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INPUT_FILENAME, fileToParse.getAbsolutePath());
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_METHOD, "streamingNTriples");
        System.out.println("Saved input file name: " + fileToParse.getAbsolutePath());

        return null;
    }
}
