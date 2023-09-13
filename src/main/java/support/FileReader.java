package support;

import convertor.RDFParser;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.util.Scanner;
public class FileReader {

    public FileReader() {
    }

    public File readFile(String filename){
        try{
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {

                String data = myReader.nextLine();
                System.out.println(data);
            }

            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return null;
    }

    public Model readRDF(String resource) throws UnsupportedEncodingException {
        String filename = "typy-pracovních-vztahů.nt";
        // read the file 'example-data-artists.ttl' as an InputStream.
        InputStream input = FileReader.class.getResourceAsStream("/" + resource);

        // Rio also accepts a java.io.Reader as input for the parser.
        RDFFormat format = getFileFormatFromExtension(resource);
        Model model = tryParseFromRDFFormats(input, format);

        // To check that we have correctly read the file, let's print out the model to the screen again
        if(model == null){
            throw new UnsupportedEncodingException("You have provided RDF in unsupported format. Please provide different format.");
        } else {
            RDFParser rdfParser = new RDFParser(model);
            //rdfParser.printModelByRows();
        }

        System.out.println(model.getNamespaces());
        return model;
    }

    private RDFFormat getFileFormatFromExtension(String resource) {
        String[] separatedResource = resource.split("\\.");
        String extension = separatedResource[1];
        return switch (extension) {
            case "nq" -> RDFFormat.NQUADS;
            case "nt" -> RDFFormat.NTRIPLES;
            case "ttl" -> RDFFormat.TURTLE;
            case "trig" -> RDFFormat.TRIG;
            case "jsonld" -> RDFFormat.JSONLD;
            case "n3" -> RDFFormat.N3;
            case "" -> RDFFormat.RDFA; // doesnt have precise file extension, has several ones
            case "rdf" -> RDFFormat.RDFXML;
            default -> null;
        };
    }

    private Model tryParseFromRDFFormats(InputStream input, RDFFormat format){
        Model model;
        try {
            model = Rio.parse(input, "", format);
            return model;
        } catch (RDFParseException | IOException e) {e.printStackTrace();}
        return null;
    }

    public void parseRDFforColumns(Model model){

    }
}
