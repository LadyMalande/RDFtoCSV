package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.convertor.ConversionService;
import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.convertor.RowAndKey;
import com.miklosova.rdftocsvw.convertor.RowsAndKeys;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.MetadataService;
import com.miklosova.rdftocsvw.output_processor.ZipOutputProcessor;
import org.apache.log4j.BasicConfigurator;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args){
        ConfigurationManager.loadSettingsFromInputToConfigFile(args);
        String RDFFileToRead = args[0];
        String delimiter = args[1];
        String CSVFileToWriteTo = args[2];
        String conversionMethod = args[3];
        String methodChoice = "rdf4j";
        // for log4j
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

    }


}
