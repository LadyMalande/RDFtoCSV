package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.convertor.CSVTableCreator;
import com.miklosova.rdftocsvw.input_processor.InputGateway;
import com.miklosova.rdftocsvw.input_processor.InputProcessor;
import org.apache.log4j.BasicConfigurator;
import org.eclipse.rdf4j.model.Model;

import java.io.UnsupportedEncodingException;

public class Main {
    public static void main(String[] args){
        String RDFFileToRead = args[0];
        String delimiter = args[1];
        String CSVFileToWriteTo = args[2];
        // for log4j
        BasicConfigurator.configure();

        // Parse input
        InputProcessor ig = new InputProcessor();

        // Save the intermediate data representation to a variable
        // TODO

        // Transform the data from the model to CSV
        // TODO

        // Finalize the output to .zip
        // TODO

/*
        CSVTableCreator ctc = new CSVTableCreator(delimiter, CSVFileToWriteTo, RDFFileToRead);
        System.out.println(ctc.getCSVTableAsString());

        ExampleMaker exm = new ExampleMaker();
        exm.makeExample();

        try {
            fr.readRDF("typy-pracovních-vztahů.trig");

        } catch (UnsupportedEncodingException e) {
            System.out.println(e.toString());
        }

 */

    }
}
