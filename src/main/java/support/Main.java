package support;

import convertor.CSVTableCreator;
import org.apache.log4j.BasicConfigurator;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.UnsupportedEncodingException;

public class Main {
    public static void main(String[] args){
        FileReader fr = new FileReader();
        FileWrite fw = new FileWrite();
        try {
            //Model m = fr.readRDF("model3.jsonld");
            //FileWrite.writeRDFTypedToFile("model3.ttl", m, RDFFormat.TURTLE);

            Model m = fr.readRDF("vhodnosti-pro-typ-zamestnance.jsonld");
            //FileWrite.writeRDFTypedToFile("vhodnosti-pro-typ-zamestnance.ttl", m, RDFFormat.TURTLE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }




        CSVTableCreator ctc = new CSVTableCreator(";");
        System.out.println(ctc.getCSVTableAsString());
/*
        ExampleMaker exm = new ExampleMaker();
        exm.makeExample();


 */


        BasicConfigurator.configure();


        try {
            fr.readRDF("typy-pracovních-vztahů.trig");

        } catch (UnsupportedEncodingException e) {
            System.out.println(e.toString());
        }

    }
}
