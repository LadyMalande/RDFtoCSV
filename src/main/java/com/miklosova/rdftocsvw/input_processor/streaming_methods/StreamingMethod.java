package com.miklosova.rdftocsvw.input_processor.streaming_methods;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.IInputParsingMethod;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import lombok.extern.java.Log;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFParseException;

import java.io.File;
import java.io.IOException;

/**
 * The Streaming method that only acts as a filler for the sake of keeping the conversion pipeline the same for any kind of data.
 */
@Log
public class StreamingMethod implements IInputParsingMethod {
    @Override
    public RepositoryConnection processInput(File fileToParse, Repository db) throws RDFParseException, IOException {

        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INPUT_FILENAME, fileToParse.getAbsolutePath());
        return null;
    }
}
