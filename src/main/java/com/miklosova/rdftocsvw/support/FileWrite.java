package com.miklosova.rdftocsvw.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.miklosova.rdftocsvw.convertor.Row;
import com.miklosova.rdftocsvw.convertor.TypeIdAndValues;
import com.miklosova.rdftocsvw.convertor.TypeOfValue;
import com.miklosova.rdftocsvw.metadata_creator.Column;
import com.miklosova.rdftocsvw.metadata_creator.Table;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import static org.eclipse.rdf4j.model.util.Values.iri;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
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

    /*
    public static String saveCSVFileFromRows(String fileName, ArrayList<Value> keys, ArrayList<Row> rows, String delimiter){
        StringBuilder forOutput = new StringBuilder();
        File f = FileWrite.makeFileByNameAndExtension( fileName, "csv");
        System.out.println("File f = " + f);
        StringBuilder sb1 = new StringBuilder();
        sb1.append("id" + delimiter);
        keys.forEach(key -> sb1.append(key + delimiter));
        sb1.deleteCharAt(sb1.length() - 1);
        sb1.append("\n");
        sb1.append("\n");
        FileWrite.writeToTheFile(f, sb1.toString());
        for(Row row : rows){
            //System.out.println();
            StringBuilder sb = new StringBuilder();
            sb.append(row.id).append(delimiter);
            for(Value key : keys){
                //System.out.println("Number of keys: " + keys.size());
                sb.append(row.columns.get(key)).append(delimiter);
                System.out.println("in entry set " + row.columns.get(key) + ".");

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

     */

    public static String getFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();

        // Use Files.newBufferedReader for simplicity
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }

        return content.toString();
    }


    public static boolean isUTF8Encoded(String filePath) throws IOException {
        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
        String content = new String(fileContent, StandardCharsets.UTF_8);
        return content.equals(new String(content.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
    }
    public static void writeFilesToconfigFile(ArrayList<String> fileNamesCreated) {
        System.out.println("fileNamesCreated[0]  = " + fileNamesCreated.get(0));
        StringBuilder sb = new StringBuilder();
        fileNamesCreated.forEach(fileName -> sb.append(fileName).append(","));
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES,sb.toString());
    }

    public static String saveCSVFileFromRows(String fileName, ArrayList<Row> rows, Metadata metadata){
        fileName = getFullPathOfFile(fileName);
        ObjectNode originalMetadataJSON = null;
        try {
            originalMetadataJSON = JsonUtil.serializeWithContext(metadata);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        //System.out.println("saveCSVFileFromRows beginning");
        StringBuilder forOutput = new StringBuilder();
        File f = FileWrite.makeFileByNameAndExtension( fileName, null);
        //System.out.println("File f: " + f.getAbsolutePath());
        StringBuilder sb1 = new StringBuilder();
        List<Column> orderOfColumnKeys = addHeadersFromMetadata(fileName, metadata, sb1);
        FileWrite.writeToTheFile(f, sb1.toString());
        
        for(Row row : rows){
            System.out.println("rows number " + rows.size());
            StringBuilder sb = new StringBuilder();

            //System.out.println("orderOfColumnKeys number " + orderOfColumnKeys.size());
            boolean firstColumn = true;
            List<Map.Entry<Value, TypeIdAndValues>> multivalues = row.columns.entrySet().stream()
                    .filter(entry -> (entry.getValue().values.size() > 1 && entry.getValue().type.equals(TypeOfValue.LITERAL) && entry.getValue().values.get(0).isLiteral() && ((Literal)entry.getValue().values.get(0)).getLanguage().isPresent() && literalHasDifferentLanguageTags(entry.getValue().values) && !allLanguagesAreUnique(entry.getValue().values) ))
                    .toList();;
                    //System.out.println("multivalues.size() " + multivalues.size());
            multivalues.forEach(multivalue -> System.out.print("multivalue: " + multivalue.getValue().values + ", "));
            List<Map<Value, Value>> combinations = generateCombinations(multivalues);
            combinations.forEach(combination -> {
                        for (Map.Entry<Value, Value> entry : combination.entrySet()) {
                            //System.out.print("k,v=" + entry.getKey().stringValue() + ": " + entry.getValue().stringValue());

                        System.out.println(entry);
                    }

                //System.out.println();
            });

            //System.out.println("combinations:");
            //System.out.println("cize of combinations " + combinations.size());
            int i = 0;
            if(!combinations.isEmpty()){
                for(Map<Value,Value> combination : combinations){
                    //System.out.println("Combinations size: " + combinations.size());
                    //System.out.println("Combination: " + combination.entrySet());
                    appendIdByValuePattern(row, metadata, sb, orderOfColumnKeys.get(0));
                    //System.out.println("Combination #"  + i);
                    i++;
                    firstColumn = true;

                    ValueFactory vf = SimpleValueFactory.getInstance();


                    for(Column column : orderOfColumnKeys){

                        String multilevelPropertyUrl = "";
                        if(column.getPropertyUrl() != null){
                            IRI propertyUrlIRI = vf.createIRI(column.getPropertyUrl());
                            multilevelPropertyUrl = (column.getLang() != null) ? column.getOriginalColumnKey().stringValue() : column.getOriginalColumnKey().stringValue();
                            System.out.println("multilevelPropertyUrl = " + multilevelPropertyUrl );
                        }


                        //System.out.println("Columns by keys " + column.getName());
                        if(!Boolean.getBoolean(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES)) && firstColumn) {
                            firstColumn = false;
                        } else{
                            if(combination.get((IRI)iri(multilevelPropertyUrl)) != null){
                                if(combination.get((IRI)iri(multilevelPropertyUrl)).isIRI()){
                                    if(column.getValueUrl().startsWith("{")){
                                        sb.append(((IRI)combination.get(iri(multilevelPropertyUrl))).stringValue());
                                    } else{
                                        sb.append(((IRI)combination.get((IRI)iri(multilevelPropertyUrl))).getLocalName());
                                    }
                                } else if(combination.get((IRI)iri(multilevelPropertyUrl)).isLiteral()){
                                    System.out.println("appending literal " + safeLiteral((Literal)combination.get((IRI)iri(multilevelPropertyUrl))));
                                    sb.append(safeLiteral((Literal)combination.get((IRI)iri(multilevelPropertyUrl))));
                                }
                                sb.append(",");
                            } else {
                                System.out.println("orderOfColumnKeys: " + column.getName());
                                appendColumnValueByKey(column, row, sb, 0, multilevelPropertyUrl);
                            }
                        }
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append("\n");
                }
                System.out.println("forOutput : " + forOutput.toString());
                forOutput.append(sb);
            } else {
                appendIdByValuePattern(row, metadata, sb, orderOfColumnKeys.get(0));
                System.out.println("Combination #"  + i);
                i++;
                firstColumn = true;
                for(Column column : orderOfColumnKeys){

                    //System.out.println("Columns by keys " + column.getName());
                    if(!Boolean.getBoolean(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES)) && firstColumn) {
                        firstColumn = false;
                    } else{

                        //System.out.println("orderOfColumnKeys: " + column.getName());
                        ValueFactory vf = SimpleValueFactory.getInstance();
                        IRI propertyUrlIRI = vf.createIRI(column.getPropertyUrl());
                        String multilevelPropertyUrl = propertyUrlIRI.getNamespace() + column.getName();


                        appendColumnValueByKey(column, row, sb, 0, multilevelPropertyUrl);

                    }
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.append("\n");
                //System.out.println("row: " + sb.toString() + ".");
                forOutput.append(sb);
            }





        }


        //System.out.println("Written rows from rows to the file " + forOutput.toString() + ".");
        ObjectNode metadataNow = null;
        try {
            metadataNow = JsonUtil.serializeWithContext(metadata);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println("Original metadata:\n" + originalMetadataJSON);
        System.out.println("New metadata:\n" + metadataNow);
        if(originalMetadataJSON != metadataNow){
            System.out.println("Rewriting metadata file");
            JsonUtil.serializeAndWriteToFile(metadata);

        }
        FileWrite.writeToTheFile(f, forOutput.toString());
        System.out.println("Written CSV from rows to the file " + f + ".");
        //System.out.println("saveCSVFileFromRows end");
        return forOutput.toString();

    }

    private static String getFullPathOfFile(String fileName) {

        return ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_FILE_PATH) + fileName;
    }

    private static boolean allLanguagesAreUnique(List<Value> values) {
        ArrayList<String> languageTags = new ArrayList<>();
        for(Value v : values){
            Literal l = (Literal) v;
            if(!languageTags.contains(l.getLanguage().toString())){
                languageTags.add(l.getLanguage().toString());
            }
        }
        if(languageTags.size() == values.size()){
            return true;
        } else {
            return false;
        }
    }

    private static boolean literalHasDifferentLanguageTags(List<Value> values) {
        ArrayList<String> languageTags = new ArrayList<>();
        for(Value v : values){
            Literal l = (Literal) v;
            if(!languageTags.contains(l.getLanguage().toString())){
                languageTags.add(l.getLanguage().toString());
            }
        }
        if(languageTags.size() > 1){
            return true;
        } else {
            return false;
        }
    }

    public static List<Map<Value,Value>> generateCombinations(List<Map.Entry<Value, TypeIdAndValues>> listOfLists) {
        // Map of predicatesOfColumns and Values in the Column
        List<Map<Value,Value>> resultingRowOfFormerMultivalues = new ArrayList<>();
        if(!listOfLists.isEmpty() && listOfLists.get(0).getValue().values.get(0).isLiteral() && ((Literal)listOfLists.get(0).getValue().values.get(0)).getLanguage().isPresent()){
            Integer maxDepth = getMaxDepthForDifferentLanguageTags(listOfLists);
            //generateCombinationsHelper(maxDepth, resultingRowOfFormerMultivalues, 0, new HashMap<Value,Value>());

        } else{
            generateCombinationsHelper(listOfLists, resultingRowOfFormerMultivalues, 0, new HashMap<Value,Value>());

        }
        return resultingRowOfFormerMultivalues;
    }

    private static Integer getMaxDepthForDifferentLanguageTags(List<Map.Entry<Value, TypeIdAndValues>> listOfLists) {
        return 0;
    }

    private static void generateCombinationsHelper(List<Map.Entry<Value, TypeIdAndValues>>  listOfLists, List<Map<Value,Value>> result, int depth, Map<Value,Value> current) {
        if (depth == listOfLists.size()) {
            result.add(new HashMap<>(current));
            //System.out.println("Result.add " + current);
            return;
        }

        Map.Entry<Value, TypeIdAndValues> currentList = listOfLists.get(depth);
        for (Value item : currentList.getValue().values) {
            current.put(currentList.getKey(), item);
            generateCombinationsHelper(listOfLists, result, depth + 1, current);
            current.remove(currentList.getKey(), item); // Backtrack
        }
    }

    private static void appendColumnValueByKey(Column column, Row row, StringBuilder sb, int i, String multilevelPropertyUrl) {
        // Simple go through
        IRI iri2;
        /*
        System.out.println("Column  name= " + column.getName() );
        System.out.println("Column  titles= " + column.getTitles() );
        System.out.println("Column  lang= " + column.getLang() );
        System.out.println("Column  virtual= " + column.getVirtual() );
        System.out.println("Column  datatype= " + column.getDatatype() );
        System.out.println("Column  entryset empty = " + row.columns.entrySet().isEmpty() );

         */
        try{
            iri2 = iri(multilevelPropertyUrl);
            //System.out.println("Column iri(column.getPropertyUrl()) = " + iri2 );
        } catch(NullPointerException ex){
            iri2 = iri(column.getValueUrl());
        }
        System.out.println("iri2 = "  + iri2.stringValue());
        for(Map.Entry<Value, TypeIdAndValues> row2: row.columns.entrySet()){
            //System.out.println(row2.getKey() + " val= " + row2.getValue().values.get(0)+ " row get value size " + row2.getValue().values.size());
        }
        //System.out.println(row.columns);
       // System.out.println("iri2 = " + iri2);
        List<Value> values;
        if(row.columns.get(iri2) == null){
            values = null;
        } else {

            values = row.columns.get(iri2).values;
            System.out.println("row.columns.get(iri2) == null "  + values);
        }
        if (column.getLang() == null){

            // TODO if it is IRI, parse it by valueURL. If it is literal, just write down its Label.
            //System.out.println("column.getPropertyUrl() = " + column.getPropertyUrl());
            //row.columns.entrySet().forEach(entry -> System.out.println(entry.getKey() + " is key to: " + entry.getValue().values));

            if(values == null){
                // The Column is empty, put empty Value to the file
                sb.append("");
            } else if(values.get(0).isIRI()){
                if(values.size() == 1){
                    IRI iri = (IRI) values.get(0);
                    if(column.getValueUrl().startsWith("{")){
                        sb.append(iri.stringValue());
                    } else{
                        sb.append(iri.getLocalName());
                    }

                } else{
                    // There are multiple values from the language, we need to enclose them in " "
                    sb.append('"');
                    values.forEach(val -> {
                        String strValue = ((IRI)val).getLocalName();
                        sb.append(strValue);
                        sb.append(",");});
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append('"');
                    column.setSeparator(",");
                }
            } else if(values.get(0).isLiteral()){
                if(values.size() == 1){
                    Literal literal = (Literal) values.get(0);

                    sb.append(safeLiteral(literal));
                } else{
                    // There are multiple values from the language, we need to enclose them in " "
                    sb.append('"');
                    values.forEach(val -> {
                        String strValue = ((Literal)val).getLabel();
                        sb.append(strValue);
                        sb.append(",");});
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append('"');
                    column.setSeparator(",");
                }
            }
        }
        // Language versions split
        else {
            //System.out.println("column.getPropertyUrl() = " + column.getPropertyUrl());
            //System.out.println(values);
            List<Value> languageVariations =  values;
            if(languageVariations == null){sb.append(","); return;}
            List<Value> valuesByLang = languageVariations.stream().filter(val -> ((Literal)val).getLanguage().get().equals(column.getLang())).collect(Collectors.toList());
            // There is only one value of this language
            if(!valuesByLang.isEmpty()){
                if(valuesByLang.size() == 1){
                    Value val = valuesByLang.get(0);
                    sb.append(safeLiteral((Literal)val));
                } else {
                    System.out.println();
                    System.out.println(multilevelPropertyUrl + " " + column.getLang() + " column.separator=" + column.getSeparator());
                    // There are multiple values from the language, we need to enclose them in " "
                    sb.append('"');
                    valuesByLang.forEach(val -> {
                        String strValue = ((Literal)val).getLabel();
                        System.out.println("Literal value multivalue: " + strValue);
                        sb.append(strValue);
                        sb.append(",");});
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append('"');
                    column.setSeparator(",");
                    System.out.println(multilevelPropertyUrl + " " + column.getLang() + " column.separator=" + column.getSeparator());
                }
            }


        }

        sb.append(",");
    }

    private static String safeLiteral(Literal literal) {
        if(literal.getLabel().contains(",") && !literal.getLabel().startsWith("\"") && !literal.getLabel().endsWith("\"")){
            return "\"" + literal.getLabel() + "\"";
        } else {
            return literal.getLabel();
        }
    }

    private static void appendIdByValuePattern(Row row, Metadata metadata, StringBuilder sb, Column column) {
        if(column.getValueUrl().startsWith("{")){
            sb.append(row.id.stringValue());
            sb.append(",");
        } else{
            if(row.id.isBNode()){
                sb.append(row.id);
                sb.append(",");
            } else {
                IRI iri = (IRI) row.id;
                String value = iri.getLocalName();
                sb.append(value);
                sb.append(",");
            }
        }
    }

    private static List<Column> addHeadersFromMetadata(String fileName, Metadata metadata, StringBuilder sb1) {
        List<Column> orderOfColumns = new ArrayList<>();
        System.out.println("addHeadersFromMetadata fileName = " + fileName);
        File fileObject = new File(fileName);
        metadata.getTables().forEach(tables -> System.out.println("tables = " + tables.getUrl()));
        Table fud = metadata.getTables().stream().filter(tables -> tables.getUrl().equals(fileObject.getName())).findFirst().get();
        Column firstColumn = null;
        if(Boolean.getBoolean(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES))){

            firstColumn = fud.getTableSchema().getColumns().stream().filter(column -> column.getPropertyUrl() == null).findFirst().get();
            sb1.append(firstColumn.getTitles());
            sb1.append(",");
        }


        for(Column column : fud.getTableSchema().getColumns()){
            if(column != firstColumn && column.getVirtual() == null ){
                sb1.append(column.getTitles());
                sb1.append(",");
                orderOfColumns.add(column);
                //System.out.println("Added column to ordered columns: " + column.getTitles() + " " + column.getName() + " " + column.getVirtual() + " " + column.getPropertyUrl());
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
