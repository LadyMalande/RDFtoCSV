package support;

import org.apache.log4j.BasicConfigurator;

import java.io.UnsupportedEncodingException;

public class Main {
    public static void main(String[] args){
        ExampleMaker exm = new ExampleMaker();
        exm.makeExample();
        /*
        BasicConfigurator.configure();

        FileReader fr = new FileReader();
        try {
            fr.readRDF("typy-pracovních-vztahů.trig");

        } catch (UnsupportedEncodingException e) {
            System.out.println(e.toString());
        }
         */
    }
}
