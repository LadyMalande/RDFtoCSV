package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.convertor.ConversionService;
import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.output_processor.ZipOutputProcessor;
import org.apache.log4j.BasicConfigurator;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class Main {
    public static void main(String[] args){
        ConfigurationManager.loadSettingsFromInputToConfigFile(args);
        String RDFFileToRead = args[0];
        String delimiter = args[1];
        String CSVFileToWriteTo = args[2];
        String methodChoice = "rdf4j";
        String conversionMethod = "basicQuery";
        // for log4j
        BasicConfigurator.configure();

        // Parse input
        // Create a new Repository.
        Repository db = new SailRepository(new MemoryStore());
        MethodService methodService = new MethodService();
        RepositoryConnection rc = methodService.processInput(RDFFileToRead, methodChoice, db);
        assert(rc != null);

        // Save the intermediate data representation to a variable
        // TODO

        // Transform the data from the model to CSV
        // TODO
        ConversionService cs = new ConversionService();
        PrefinishedOutput convertedToCSV = cs.convertByQuery(rc, conversionMethod, db);

        db.shutDown();
        // Finalize the output to .zip
        // TODO

        ZipOutputProcessor zop = new ZipOutputProcessor();
        zop.processCSVToOutput(convertedToCSV);

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
