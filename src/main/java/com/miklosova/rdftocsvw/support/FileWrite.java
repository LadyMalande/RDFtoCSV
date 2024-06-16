package com.miklosova.rdftocsvw.support;

import com.miklosova.rdftocsvw.convertor.Row;
import com.miklosova.rdftocsvw.metadata_creator.Column;
import com.miklosova.rdftocsvw.metadata_creator.FileUrlDescriptor;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import static org.eclipse.rdf4j.model.util.Values.iri;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public static String saveCSVFileFromRows(String fileName, ArrayList<Value> keys, ArrayList<Row> rows, String delimiter){
        StringBuilder forOutput = new StringBuilder();
        File f = FileWrite.makeFileByNameAndExtension( fileName, "csv");
        System.out.println("File f = " + f);
        StringBuilder sb1 = new StringBuilder();
        sb1.append("id" + delimiter);
        keys.forEach(key -> sb1.append(key + delimiter));
        sb1.deleteCharAt(sb1.length() - 1);
        sb1.append("\n");
        FileWrite.writeToTheFile(f, sb1.toString());
        for(Row row : rows){
            System.out.println();
            StringBuilder sb = new StringBuilder();
            sb.append(row.id).append(delimiter);
            for(Value key : keys){
                //System.out.println("Number of keys: " + keys.size());
                sb.append(row.map.get(key)).append(delimiter);
                System.out.println("in entry set " + row.map.get(key) + ".");

            }

            sb.deleteCharAt(sb.length() - 1);
            sb.append("\n");
            System.out.println("row saveCSFFileFromRows 4 parameters: " + sb.toString() + ".");
            //FileWrite.writeTotheFile(f, sb.toString());
            forOutput.append(sb);
            FileWrite.writeToTheFile(f, sb.toString());
        }
        System.out.println("Written rows from rows to the file " + forOutput.toString() + ".");
        //FileWrite.writeToTheFile(f, forOutput.toString());
        System.out.println("Written CSV from rows to the file " + f + ".");
        return forOutput.toString();
        //FileWrite.writeTotheFile(f, resultCSV);

    }

    public static void writeFilesToconfigFile(ArrayList<String> fileNamesCreated) {
        StringBuilder sb = new StringBuilder();
        fileNamesCreated.forEach(fileName -> sb.append(fileName + ","));
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES,sb.toString());
    }

    public static String saveCSVFileFromRows(String fileName, ArrayList<Row> rows, Metadata metadata){
        StringBuilder forOutput = new StringBuilder();
        File f = FileWrite.makeFileByNameAndExtension( fileName, null);
    System.out.println("File f: " + f.getAbsolutePath());
        StringBuilder sb1 = new StringBuilder();
        List<Column> orderOfColumnKeys = addHeadersFromMetadata(fileName, metadata, sb1);
        FileWrite.writeToTheFile(f, sb1.toString());
        
        for(Row row : rows){
            
            StringBuilder sb = new StringBuilder();
            appendIdByValuePattern(row, metadata, sb);

            for(Column column : orderOfColumnKeys){
                appendColumnValueByKey(column, row, sb);
            }

            sb.deleteCharAt(sb.length() - 1);
            sb.append("\n");
            System.out.println("row: " + sb.toString() + ".");
            forOutput.append(sb);
        }
        System.out.println("Written rows from rows to the file " + forOutput.toString() + ".");
        FileWrite.writeToTheFile(f, forOutput.toString());
        System.out.println("Written CSV from rows to the file " + f + ".");
        return forOutput.toString();

    }

    private static void appendColumnValueByKey(Column column, Row row, StringBuilder sb) {
        // Simple go through
        List<Value> values = row.map.get(iri(column.getPropertyUrl()));
        if(column.getLang() == null){
            // TODO if it is IRI, parse it by valueURL. If it is literal, just write down its Label.
            System.out.println("column.getPropertyUrl() = " + column.getPropertyUrl());
            row.map.entrySet().forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));

            assert values != null;
            if(values.get(0).isIRI()){
                if(values.size() == 1){
                    IRI iri = (IRI) values.get(0);
                    sb.append(iri.getLocalName());
                } else{
                    // There are multiple values from the language, we need to enclose them in " "
                    sb.append('"');
                    values.forEach(val -> {
                        String strValue = ((IRI)val).getLocalName();
                        sb.append(strValue);
                        sb.append(",");});
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append('"');
                }
            } else if(values.get(0).isLiteral()){
                if(values.size() == 1){
                    Literal literal = (Literal) values.get(0);
                    sb.append(literal.getLabel());
                } else{
                    // There are multiple values from the language, we need to enclose them in " "
                    sb.append('"');
                    values.forEach(val -> {
                        String strValue = ((Literal)val).getLabel();
                        sb.append(strValue);
                        sb.append(",");});
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append('"');
                }
            }
        }
        // Language versions split
        else {
            System.out.println("column.getPropertyUrl() = " + column.getPropertyUrl());
            System.out.println(values);
            List<Value> languageVariations =  values;
            if(languageVariations == null){sb.append(","); return;}
            List<Value> valuesByLang = languageVariations.stream().filter(val -> ((Literal)val).getLanguage().get().equals(column.getLang())).collect(Collectors.toList());
            // There is only one value of this language
            if(valuesByLang.size() == 1){
                Value val = valuesByLang.get(0);
                sb.append(((Literal)val).getLabel());
            } else {
                // There are multiple values from the language, we need to enclose them in " "
                sb.append('"');
                valuesByLang.forEach(val -> {
                    String strValue = ((Literal)val).getLabel();
                    sb.append(strValue);
                    sb.append(",");});
                sb.deleteCharAt(sb.length() - 1);
                sb.append('"');
            }

        }

        sb.append(",");
    }

    private static void appendIdByValuePattern(Row row, Metadata metadata, StringBuilder sb) {
        IRI iri = (IRI) row.id;
        String value = iri.getLocalName();
        sb.append(value);
        sb.append(",");
    }

    private static List<Column> addHeadersFromMetadata(String fileName, Metadata metadata, StringBuilder sb1) {
        List<Column> orderOfColumns = new ArrayList<>();
        System.out.println("addHeadersFromMetadata fileName = " + fileName);
        File fileObject = new File(fileName);
        metadata.getTables().forEach(tables -> System.out.println("tables = " + tables.getUrl()));
        FileUrlDescriptor fud = metadata.getTables().stream().filter(tables -> tables.getUrl().equals(fileObject.getName())).findFirst().get();
        Column firstColumn = fud.getTableSchema().getColumns().stream().filter(column -> column.getPropertyUrl() == null).findFirst().get();
        sb1.append(firstColumn.getTitles());
        sb1.append(",");
        for(Column column : fud.getTableSchema().getColumns()){
            if(column != firstColumn && column.getVirtual() == null ){
                sb1.append(column.getTitles());
                sb1.append(",");
                orderOfColumns.add(column);
                System.out.println("Added column to ordered columns: " + column.getTitles() + " " + column.getName() + " " + column.getVirtual() + " " + column.getPropertyUrl());
            }
        }
        sb1.deleteCharAt(sb1.length() - 1);
        sb1.append("\n");

        return orderOfColumns;
    }

    public static File makeFileByNameAndExtension(String name, String ext) {
        try {
            File newFile;
            if(ext != null) {
                newFile = new File(name + "." + ext);
            } else {
                System.out.println("newFile: " + name);

                newFile = new File(name);
                System.out.println("newFile.getName(): " + newFile.getName());
            }
            FileWrite.deleteFile(newFile.getAbsolutePath());
            if (newFile.createNewFile()) {
                System.out.println("File created: " + newFile);
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

            FileWriter myWriter = new FileWriter(file,true);

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
            System.out.println("Deleted the file: " + myObj);
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
