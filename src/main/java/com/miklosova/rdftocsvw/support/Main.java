package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.convertor.*;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.MetadataService;
import com.miklosova.rdftocsvw.output_processor.ZipOutputProcessor;
import org.apache.log4j.BasicConfigurator;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args){
        ConfigurationManager.loadSettingsFromInputToConfigFile(args);
        String RDFFileToRead = args[0];
        System.out.println(RDFFileToRead);
        RDFtoCSV rdFtoCSV;
        if(args.length < 2){
            rdFtoCSV = new RDFtoCSV(RDFFileToRead);
        } else {
            rdFtoCSV = new RDFtoCSV(RDFFileToRead);
        }
        try{
            rdFtoCSV.convertToZip();
        } catch(RDFParseException rdfParseException){
            String newFileName = rdFtoCSV.getOutputFileName() + ".csv";
            File f = new File(newFileName);
            System.out.println(rdfParseException.getMessage());
        } catch (IOException ex){
            System.out.println(ex.getMessage());
        }

        // for log4j
        /*
        BasicConfigurator.configure();
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_METHOD, conversionMethod);

        // =========  Normal go through =====

        // Parse input
        // Create a new Repository.
        Repository db = new SailRepository(new MemoryStore());
        MethodService methodService = new MethodService();
        RepositoryConnection rc = methodService.processInput(RDFFileToRead, methodChoice, db);
        assert(rc != null);
        // Convert the table to intermediate data for processing into metadata
        ConversionService cs = new ConversionService();
        PrefinishedOutput prefinishedOutput = cs.convertByQuery(rc, db);
        // Convert intermediate data into basic metadata
        MetadataService ms = new MetadataService();
        Metadata metadata = ms.createMetadata(prefinishedOutput);

        // Enrich metadata with online reachable data - disabled if offline
        // TODO

        // Write data to CSV by the metadata prepared

        RowsAndKeys rnk = (RowsAndKeys) prefinishedOutput.getPrefinishedOutput();
        int i = 0;
        ArrayList<String> fileNamesCreated = new ArrayList<>();
        for(RowAndKey rowAndKey : rnk.getRowsAndKeys()){
            String newFileName = CSVFileToWriteTo + i + ".csv";
            FileWrite.saveCSVFileFromRows(newFileName, rowAndKey.getRows(), metadata);
            fileNamesCreated.add(newFileName);
            i++;
        }
        FileWrite.writeFilesToconfigFile(fileNamesCreated);
        db.shutDown();

        // Finalize the output to .zip
        ZipOutputProcessor zop = new ZipOutputProcessor();
        zop.processCSVToOutput(prefinishedOutput);

         */

    }


}
