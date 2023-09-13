package support;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.util.Set;

public class FileWrite {

    public static File makeFile(int i) {
        try {
            File newFile = new File("output" + i + ".txt");
            if (newFile.createNewFile()) {
                System.out.println("File created: " + newFile.getName());
            } else {
                System.out.println("File already exists.");
            }
            return newFile;
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return null;
        }
    }

    public static File makeFileByNameAndExtension(String name, String ext) {
        try {
            File newFile = new File(name + "." + ext);
            FileWrite.deleteFile(newFile.getName());
            if (newFile.createNewFile()) {
                System.out.println("File created: " + newFile.getName());
            } else {
                System.out.println("File already exists.");

            }
            return newFile;
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return null;
        }
    }

    public static void writeSubjectsTotheFile(File file, Set<Resource> resources) {
        try {
            FileWriter myWriter = new FileWriter(file.getName());
            for(Resource r : resources){
                myWriter.write(r.toString());
                myWriter.write("\n");
            }
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void writeTotheFile(File file, Object something) {
        try {

            FileWriter myWriter = new FileWriter(file.getName(),true);

            myWriter.write(something.toString());

            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void deleteFile(String fileName){
        File myObj = new File(fileName);
        if (myObj.delete()) {
            System.out.println("Deleted the file: " + myObj.getName());
        } else {
            System.out.println("Failed to delete the file.");
        }
    }

    public static void writeRDFTypedToFile(String filename, Model model, RDFFormat format){
        try {
        FileOutputStream out = new FileOutputStream(filename);
        RDFWriter writer = Rio.createWriter(format, out);

            writer.startRDF();
            for (Statement st: model) {
                writer.handleStatement(st);
            }
            writer.endRDF();

        out.close();
        }
        catch (RDFHandlerException e) {
            // oh no, do something!
        } catch(IOException ioex){

        }
    }


}
