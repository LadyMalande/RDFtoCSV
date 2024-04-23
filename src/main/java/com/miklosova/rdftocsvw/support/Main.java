package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.convertor.ConversionService;
import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.output_processor.ZipOutputProcessor;
import org.apache.log4j.BasicConfigurator;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
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

        ExampleMaker exm = new ExampleMaker();
        Model exampleModel = exm.makeExampleModel();
        CreateOtherRDFFormats cof = new CreateOtherRDFFormats(exampleModel);
        cof.writeModelToFile("exampleFile.brf", RDFFormat.BINARY);
        //cof.writeModelToFile("exampleFile.hdt", RDFFormat.HDT);
        //cof.writeModelToFile("exampleFile.html", RDFFormat.RDFA);
        cof.writeModelToFile("exampleFile.jsonl", RDFFormat.NDJSONLD);
        cof.writeModelToFile("exampleFile.jsonld", RDFFormat.JSONLD);
        cof.writeModelToFile("exampleFile.n3", RDFFormat.N3);
        cof.writeModelToFile("exampleFile.ndjson", RDFFormat.NDJSONLD);
        cof.writeModelToFile("exampleFile.ndjsonld", RDFFormat.NDJSONLD);
        cof.writeModelToFile("exampleFile.nq", RDFFormat.NQUADS);
        cof.writeModelToFile("exampleFile.nt", RDFFormat.NTRIPLES);
        cof.writeModelToFile("exampleFile.owl", RDFFormat.RDFXML);
        cof.writeModelToFile("exampleFile.rdf", RDFFormat.RDFXML);
        cof.writeModelToFile("exampleFile.rdfs", RDFFormat.RDFXML);
        cof.writeModelToFile("exampleFile.rj", RDFFormat.RDFJSON);
        cof.writeModelToFile("exampleFile.trig", RDFFormat.TRIG);
        cof.writeModelToFile("exampleFile.trigs", RDFFormat.TRIGSTAR);
        cof.writeModelToFile("exampleFile.trix", RDFFormat.TRIX);
        cof.writeModelToFile("exampleFile.ttl", RDFFormat.TURTLE);
        cof.writeModelToFile("exampleFile.ttls", RDFFormat.TURTLESTAR);
        //cof.writeModelToFile("exampleFile.xhtml", RDFFormat.RDFA);
        cof.writeModelToFile("exampleFile.xml", RDFFormat.RDFXML);

        /*
        // =========  Normal go through =====

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
        PrefinishedOutput convertedToCSV = cs.convertByQuery(rc, db);

        db.shutDown();
        // Finalize the output to .zip
        // TODO

        ZipOutputProcessor zop = new ZipOutputProcessor();
        zop.processCSVToOutput(convertedToCSV);

         */
        // =========  Normal go through END =====
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
