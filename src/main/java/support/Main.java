package support;

import convertor.CSVTableCreator;
import org.apache.log4j.BasicConfigurator;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.UnsupportedEncodingException;

public class Main {
    public static void main(String[] args){
        String RDFFileToRead = args[0];
        String delimiter = args[1];
        String CSVFileToWriteTo = args[2];
        FileReader fr = new FileReader();
        FileWrite fw = new FileWrite();
        /*
        try {

            //Model m = fr.readRDF("vhodnosti-pro-typ-zamestnance.jsonld");
            Model m = fr.readRDF(RDFFileToRead);
            //FileWrite.writeRDFTypedToFile("vhodnosti-pro-typ-zamestnance.ttl", m, RDFFormat.TURTLE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

         */




        CSVTableCreator ctc = new CSVTableCreator(delimiter, CSVFileToWriteTo, RDFFileToRead);
        System.out.println(ctc.getCSVTableAsString());
/*
        ExampleMaker exm = new ExampleMaker();
        exm.makeExample();


 */


        BasicConfigurator.configure();

/*
        try {
            fr.readRDF("typy-pracovních-vztahů.trig");

        } catch (UnsupportedEncodingException e) {
            System.out.println(e.toString());
        }


 */
    }
}
