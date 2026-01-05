package com.miklosova.rdftocsvw.input_processor.streaming_methods;

import com.miklosova.rdftocsvw.input_processor.parsing_methods.IInputParsingMethod;
import com.miklosova.rdftocsvw.support.AppConfig;

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
    private AppConfig config;

    /**
     * Constructor with AppConfig.
     * @param config the application configuration
     */
    public StreamingMethod(AppConfig config) {
        this.config = config;
    }

    /**
     * Default constructor for backward compatibility.
     * @deprecated Use {@link #StreamingMethod(AppConfig)} instead
     */
    @Deprecated
    public StreamingMethod() {
        this(null);
    }

    @Override
    public RepositoryConnection processInput(File fileToParse, Repository db) throws RDFParseException, IOException {
        if (config != null) {
            // Store the input filename in config if available
            // Note: This is primarily for tracking purposes in streaming mode
        
        }
        return null;
    }
}
