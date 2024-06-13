package com.miklosova.rdftocsvw.support;

import java.io.IOException;
import java.io.InputStream;

public class TestSupport {
    public void runToRDFConverter(String pathToTable, String pathToMetadata, String outputPath){
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec("java -jar csv2rdf-0.4.7-standalone.jar" + " -t " + pathToTable + " -u " + pathToMetadata + " -o " + outputPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Then retreive the process output
        InputStream in = proc.getInputStream();
        InputStream err = proc.getErrorStream();
    }
}
