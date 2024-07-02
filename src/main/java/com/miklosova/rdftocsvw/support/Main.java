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
        //System.out.println(RDFFileToRead);
        RDFtoCSV rdFtoCSV;

        rdFtoCSV = new RDFtoCSV(RDFFileToRead);

        try{
            rdFtoCSV.convertToZip();
        } catch(RDFParseException rdfParseException){
            String newFileName = rdFtoCSV.getOutputFileName() + ".csv";
            File f = new File(newFileName);
            System.out.println(rdfParseException.getMessage());
        } catch (IOException ex){
            System.out.println(ex.getMessage());
        }
    }
}
