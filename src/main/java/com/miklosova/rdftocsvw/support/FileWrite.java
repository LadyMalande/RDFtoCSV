package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.convertor.Row;
import com.miklosova.rdftocsvw.metadata.Metadata;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.util.ArrayList;
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

    public static String saveCSFFileFromRows(String fileName, ArrayList<Value> keys, ArrayList<Row> rows, String delimiter){
        StringBuilder forOutput = new StringBuilder();
        //File f = FileWrite.makeFileByNameAndExtension("../" + fileName, "csv");
        File f = FileWrite.makeFileByNameAndExtension( fileName, "csv");


        StringBuilder sb1 = new StringBuilder();
        sb1.append("id" + delimiter);
        keys.forEach(key -> sb1.append(key + delimiter));
        sb1.deleteCharAt(sb1.length() - 1);
        sb1.append("\n");
        FileWrite.writeToTheFile(f, sb1.toString());
        for(Row row : rows){
            StringBuilder sb = new StringBuilder();
            sb.append(row.id).append(delimiter);
            for(Value key : keys){
                sb.append(row.map.get(key)).append(delimiter);
                System.out.println("in entry set " + row.map.get(key) + ".");

            }

            sb.deleteCharAt(sb.length() - 1);
            sb.append("\n");
            System.out.println("row: " + sb.toString() + ".");
            //FileWrite.writeTotheFile(f, sb.toString());
            forOutput.append(sb);
        }
        System.out.println("Written rows from rows to the file " + forOutput.toString() + ".");
        FileWrite.writeToTheFile(f, forOutput.toString());
        System.out.println("Written CSV from rows to the file " + f + ".");
        return forOutput.toString();
        //FileWrite.writeTotheFile(f, resultCSV);

    }

    public static String saveCSFFileFromRows(String fileName, ArrayList<Value> keys, ArrayList<Row> rows, String delimiter, Metadata metadata){
        StringBuilder forOutput = new StringBuilder();
        //File f = FileWrite.makeFileByNameAndExtension("../" + fileName, "csv");
        File f = FileWrite.makeFileByNameAndExtension( fileName, "csv");


        StringBuilder sb1 = new StringBuilder();
        sb1.append("id" + delimiter);
        keys.forEach(key -> sb1.append(key + delimiter));
        sb1.deleteCharAt(sb1.length() - 1);
        sb1.append("\n");
        FileWrite.writeToTheFile(f, sb1.toString());
        for(Row row : rows){
            StringBuilder sb = new StringBuilder();
            sb.append(row.id).append(delimiter);
            for(Value key : keys){
                sb.append(row.map.get(key)).append(delimiter);
                System.out.println("in entry set " + row.map.get(key) + ".");

            }

            sb.deleteCharAt(sb.length() - 1);
            sb.append("\n");
            System.out.println("row: " + sb.toString() + ".");
            //FileWrite.writeTotheFile(f, sb.toString());
            forOutput.append(sb);
        }
        System.out.println("Written rows from rows to the file " + forOutput.toString() + ".");
        FileWrite.writeToTheFile(f, forOutput.toString());
        System.out.println("Written CSV from rows to the file " + f + ".");
        return forOutput.toString();
        //FileWrite.writeTotheFile(f, resultCSV);

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

    public static void writeToTheFile(File file, Object something) {
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
