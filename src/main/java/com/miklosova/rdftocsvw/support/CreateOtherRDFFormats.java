package com.miklosova.rdftocsvw.support;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class CreateOtherRDFFormats {

    private Model model;

    public CreateOtherRDFFormats(Model m) {
        this.model = m;
    }

    public void writeModelToFile(String fileName, RDFFormat rdfFormat) {

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Rio.write(model, outputStream, rdfFormat);
    }

}
