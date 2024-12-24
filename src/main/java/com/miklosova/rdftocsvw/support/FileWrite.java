package com.miklosova.rdftocsvw.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.miklosova.rdftocsvw.convertor.Row;
import com.miklosova.rdftocsvw.convertor.TypeIdAndValues;
import com.miklosova.rdftocsvw.convertor.TypeOfValue;
import com.miklosova.rdftocsvw.metadata_creator.Column;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.Table;
import com.opencsv.CSVWriter;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.eclipse.rdf4j.model.util.Values.iri;

/**
 * Class containing methods regarding writing to files.
 */
public class FileWrite {

    /*
    public static String saveCSVFileFromRows(String fileName, ArrayList<Value> keys, ArrayList<Row> rows, String delimiter){
        StringBuilder forOutput = new StringBuilder();
        File f = FileWrite.makeFileByNameAndExtension( fileName, "csv");
        //System.out.println("File f = " + f);
        StringBuilder sb1 = new StringBuilder();
        sb1.append("id" + delimiter);
        keys.forEach(key -> sb1.append(key + delimiter));
        sb1.deleteCharAt(sb1.length() - 1);
        sb1.append("\n");
        sb1.append("\n");
        FileWrite.writeToTheFile(f, sb1.toString());
        for(Row row : rows){
            ////System.out.println();
            StringBuilder sb = new StringBuilder();
            sb.append(row.id).append(delimiter);
            for(Value key : keys){
                ////System.out.println("Number of keys: " + keys.size());
                sb.append(row.columns.get(key)).append(delimiter);
                //System.out.println("in entry set " + row.columns.get(key) + ".");

            }

            sb.deleteCharAt(sb.length() - 1);
            sb.append("\n");
            //System.out.println("row saveCSFFileFromRows 4 parameters: " + sb.toString() + ".");
            //FileWrite.writeTotheFile(f, sb.toString());
            forOutput.append(sb);
            FileWrite.writeToTheFile(f, sb.toString());
        }
        //System.out.println("Written rows from rows to the file " + forOutput.toString() + ".");
        //FileWrite.writeToTheFile(f, forOutput.toString());
        //System.out.println("Written CSV from rows to the file " + f + ".");
        return forOutput.toString();
        //FileWrite.writeTotheFile(f, resultCSV);

    }

     */

    /**
     * Write given data structures of Keys and Rows into String.
     *
     * @param keys The keys representing column headers.
     * @param rows The objects containing Row information.
     * @return The string representation of given headers and rows.
     */
    public static String writeToString(ArrayList<Value> keys, ArrayList<Row> rows) {
        // Sorting the list by alphabetical order of getStringValue()
        Collections.sort(keys, new Comparator<Value>() {
            @Override
            public int compare(Value v1, Value v2) {
                return v1.stringValue().compareTo(v2.stringValue());
            }
        });
        StringBuilder sb = new StringBuilder();
        sb.append("Subject,");
        for (Value val : keys) {
            sb.append(val.stringValue()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("\n");
        for (Row row : rows) {
            sb.append(row.id.stringValue()).append(",");
            for (Value columnName : keys) {
                if (row.columns.get(columnName) != null) {
                    if (row.columns.get(columnName).values.size() > 1) {
                        sb.append("\"");
                        for (Value val : row.columns.get(columnName).values) {
                            sb.append(val.stringValue()).append(",");
                        }
                        sb.deleteCharAt(sb.length() - 1);
                        sb.append("\"");
                    } else {
                        System.out.println("Appended " + row.columns.get(columnName).values.get(0).stringValue() + " for " + columnName);
                        sb.append(row.columns.get(columnName).values.get(0).stringValue()).append(",");
                    }
                } else {
                    System.out.println("No value for " + columnName);
                    sb.append(",");
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * Creates a one line of file names of created CSVs separated by commas and writes it to a config file.
     *
     * @param fileNamesCreated array list of created CSV file names
     */
    public static void writeFilesToConfigFile(ArrayList<String> fileNamesCreated) {
        StringBuilder sb = new StringBuilder();
        fileNamesCreated.forEach(fileName -> sb.append(fileName).append(","));
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES, sb.toString());
    }

    /**
     * Save the CSV file from ArrayList of Rows created during conversion.
     *
     * @param fileName The name for the created CSV file.
     * @param rows     ArrayList of Rows created by conversion by rdf4j method.
     * @param metadata Metadata created during the rdf4j method.
     * @return The contents of the file with headers as String.
     */
    public static String saveCSVFileFromRows(String fileName, ArrayList<Row> rows, Metadata metadata) {
        System.out.println("saveCSVFileFromRows fileName " + fileName);
        String split = (fileName.split("/"))[fileName.split("/").length-1];
        System.out.println("saveCSVFileFromRows split " + split);
        fileName = split;
        fileName = getFullPathOfFile(fileName);
        ObjectNode originalMetadataJSON = null;
        try {
            originalMetadataJSON = JsonUtil.serializeWithContext(metadata);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        ////System.out.println("saveCSVFileFromRows beginning");
        StringBuilder forOutput = new StringBuilder();

        File f = FileWrite.makeFileByNameAndExtension(fileName, null);

        System.out.println("File f filename: " + fileName);
        StringBuilder sb1 = new StringBuilder();
        List<Column> orderOfColumnKeys = addHeadersFromMetadata(fileName, metadata, sb1);
        forOutput.append(sb1);
        FileWrite.writeToTheFile(f, sb1.toString(), true);

        for (Row row : rows) {
            //System.out.println("rows number " + rows.size());
            StringBuilder sb = new StringBuilder();

            ////System.out.println("orderOfColumnKeys number " + orderOfColumnKeys.size());
            boolean firstColumn = true;
            List<Map.Entry<Value, TypeIdAndValues>> multivalues = row.columns.entrySet().stream()
                    .filter(entry -> (entry.getValue().values.size() > 1 && entry.getValue().type.equals(TypeOfValue.LITERAL) && entry.getValue().values.get(0).isLiteral() && ((Literal) entry.getValue().values.get(0)).getLanguage().isPresent() && literalHasDifferentLanguageTags(entry.getValue().values) && !allLanguagesAreUnique(entry.getValue().values)))
                    .toList();
            ;
            ////System.out.println("multivalues.size() " + multivalues.size());
            //multivalues.forEach(multivalue -> System.out.print("multivalue: " + multivalue.getValue().values + ", "));
            List<Map<Value, Value>> combinations = generateCombinations(multivalues);
            combinations.forEach(combination -> {
                for (Map.Entry<Value, Value> entry : combination.entrySet()) {
                    ////System.out.print("k,v=" + entry.getKey().stringValue() + ": " + entry.getValue().stringValue());

                    //System.out.println(entry);
                }

                ////System.out.println();
            });

            ////System.out.println("combinations:");
            ////System.out.println("cize of combinations " + combinations.size());
            int i = 0;
            if (!combinations.isEmpty()) {
                for (Map<Value, Value> combination : combinations) {
                    ////System.out.println("Combinations size: " + combinations.size());
                    ////System.out.println("Combination: " + combination.entrySet());
                    appendIdByValuePattern(row, metadata, sb, orderOfColumnKeys.get(0));
                    ////System.out.println("Combination #"  + i);
                    i++;
                    firstColumn = true;

                    ValueFactory vf = SimpleValueFactory.getInstance();


                    for (Column column : orderOfColumnKeys) {

                        String multilevelPropertyUrl = "";
                        if (column.getPropertyUrl() != null) {
                            IRI propertyUrlIRI = vf.createIRI(column.getPropertyUrl());
                            multilevelPropertyUrl = (column.getLang() != null) ? column.getOriginalColumnKey().stringValue() : column.getOriginalColumnKey().stringValue();
                            //System.out.println("multilevelPropertyUrl = " + multilevelPropertyUrl );
                        }


                        ////System.out.println("Columns by keys " + column.getName());
                        if (!Boolean.getBoolean(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES)) && firstColumn) {
                            firstColumn = false;
                        } else {
                            if (combination.get((IRI) iri(multilevelPropertyUrl)) != null) {
                                if (combination.get(iri(multilevelPropertyUrl)).isIRI()) {
                                    if (column.getValueUrl().startsWith("{")) {
                                        sb.append(combination.get(iri(multilevelPropertyUrl)).stringValue());
                                    } else {
                                        sb.append(((IRI) combination.get((IRI) iri(multilevelPropertyUrl))).getLocalName());
                                    }
                                } else if (combination.get((IRI) iri(multilevelPropertyUrl)).isLiteral()) {
                                    //System.out.println("appending literal " + safeLiteral((Literal)combination.get((IRI)iri(multilevelPropertyUrl))));
                                    sb.append(safeLiteral((Literal) combination.get((IRI) iri(multilevelPropertyUrl))));
                                }
                                sb.append(",");
                            } else {
                                //System.out.println("orderOfColumnKeys: " + column.getName());
                                appendColumnValueByKey(column, row, sb, 0, multilevelPropertyUrl, metadata);
                            }
                        }
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append("\n");
                }
                //System.out.println("forOutput : " + forOutput.toString());
                forOutput.append(sb);
            } else {
                appendIdByValuePattern(row, metadata, sb, orderOfColumnKeys.get(0));
                //System.out.println("Combination #"  + i);
                i++;
                firstColumn = true;
                for (Column column : orderOfColumnKeys) {

                    ////System.out.println("Columns by keys " + column.getName());
                    if (!Boolean.getBoolean(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES)) && firstColumn) {
                        firstColumn = false;
                    } else {

                        ////System.out.println("orderOfColumnKeys: " + column.getName());
                        ValueFactory vf = SimpleValueFactory.getInstance();
                        IRI propertyUrlIRI = vf.createIRI(column.getPropertyUrl());
                        String multilevelPropertyUrl = propertyUrlIRI.getNamespace() + column.getName();


                        appendColumnValueByKey(column, row, sb, 0, multilevelPropertyUrl, metadata);

                    }
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.append("\n");
                ////System.out.println("row: " + sb.toString() + ".");
                forOutput.append(sb);
            }


        }


        ////System.out.println("Written rows from rows to the file " + forOutput.toString() + ".");
        ObjectNode metadataNow = null;
        try {
            metadataNow = JsonUtil.serializeWithContext(metadata);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        //System.out.println("Original metadata:\n" + originalMetadataJSON);
        //System.out.println("New metadata:\n" + metadataNow);
        if (originalMetadataJSON != metadataNow) {
            //System.out.println("Rewriting metadata file");
            JsonUtil.serializeAndWriteToFile(metadata);

        }
        FileWrite.writeToTheFile(f, forOutput.toString(), false);
        //System.out.println("Written CSV from rows to the file " + f + ". in saveCSVFileFromRows(String fileName, ArrayList<Row> rows, Metadata metadata)");
        ////System.out.println("saveCSVFileFromRows end");
        return forOutput.toString();

    }

    private static String getFullPathOfFile(String fileName) {

        //return ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_FILE_PATH);// + fileName;
        return fileName;
    }

    /**
     * Write string array as csv to file.
     *
     * @param fileName the file name
     * @param content  the content
     */
    public static void writeStringArrayAsCSVToFile(String fileName, String[] content) {
        File fileToWriteTo = new File(fileName);
        try (CSVWriter writer = new CSVWriter(new FileWriter(fileToWriteTo, true))) {
            // Appends to the file instead of overwriting
            //System.out.println("Writing newline of merged CSV: " + Arrays.toString(line));
            writer.writeNext(content, false);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static boolean allLanguagesAreUnique(List<Value> values) {
        ArrayList<String> languageTags = new ArrayList<>();
        for (Value v : values) {
            Literal l = (Literal) v;
            if (!languageTags.contains(l.getLanguage().toString())) {
                languageTags.add(l.getLanguage().toString());
            }
        }
        if (languageTags.size() == values.size()) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean literalHasDifferentLanguageTags(List<Value> values) {
        ArrayList<String> languageTags = new ArrayList<>();
        for (Value v : values) {
            Literal l = (Literal) v;
            if (!languageTags.contains(l.getLanguage().toString())) {
                languageTags.add(l.getLanguage().toString());
            }
        }
        if (languageTags.size() > 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Generate combinations list.
     *
     * @param listOfLists the list of lists
     * @return the list
     */
    public static List<Map<Value, Value>> generateCombinations(List<Map.Entry<Value, TypeIdAndValues>> listOfLists) {
        // Map of predicatesOfColumns and Values in the Column
        List<Map<Value, Value>> resultingRowOfFormerMultivalues = new ArrayList<>();
        if (!listOfLists.isEmpty() && listOfLists.get(0).getValue().values.get(0).isLiteral() && ((Literal) listOfLists.get(0).getValue().values.get(0)).getLanguage().isPresent()) {
            Integer maxDepth = getMaxDepthForDifferentLanguageTags(listOfLists);
            //generateCombinationsHelper(maxDepth, resultingRowOfFormerMultivalues, 0, new HashMap<Value,Value>());

        } else {
            generateCombinationsHelper(listOfLists, resultingRowOfFormerMultivalues, 0, new HashMap<Value, Value>());

        }
        return resultingRowOfFormerMultivalues;
    }

    private static Integer getMaxDepthForDifferentLanguageTags(List<Map.Entry<Value, TypeIdAndValues>> listOfLists) {
        return 0;
    }

    private static void generateCombinationsHelper(List<Map.Entry<Value, TypeIdAndValues>> listOfLists, List<Map<Value, Value>> result, int depth, Map<Value, Value> current) {
        if (depth == listOfLists.size()) {
            result.add(new HashMap<>(current));
            ////System.out.println("Result.add " + current);
            return;
        }

        Map.Entry<Value, TypeIdAndValues> currentList = listOfLists.get(depth);
        for (Value item : currentList.getValue().values) {
            current.put(currentList.getKey(), item);
            generateCombinationsHelper(listOfLists, result, depth + 1, current);
            current.remove(currentList.getKey(), item); // Backtrack
        }
    }

    private static void appendColumnValueByKey(Column column, Row row, StringBuilder sb, int i, String multilevelPropertyUrl, Metadata metadata) {
        // Simple go through
        IRI iri2;
        /*
        //System.out.println("Column  name= " + column.getName() );
        //System.out.println("Column  titles= " + column.getTitles() );
        //System.out.println("Column  lang= " + column.getLang() );
        //System.out.println("Column  virtual= " + column.getVirtual() );
        //System.out.println("Column  datatype= " + column.getDatatype() );
        //System.out.println("Column  entryset empty = " + row.columns.entrySet().isEmpty() );

         */
        try {
            iri2 = iri(multilevelPropertyUrl);
            ////System.out.println("Column iri(column.getPropertyUrl()) = " + iri2 );
        } catch (NullPointerException ex) {
            iri2 = iri(column.getValueUrl());
        }
        //System.out.println("iri2 = "  + iri2.stringValue());
        for (Map.Entry<Value, TypeIdAndValues> row2 : row.columns.entrySet()) {
            ////System.out.println(row2.getKey() + " val= " + row2.getValue().values.get(0)+ " row get value size " + row2.getValue().values.size());
        }
        ////System.out.println(row.columns);
        // //System.out.println("iri2 = " + iri2);
        List<Value> values;
        if (row.columns.get(iri2) == null) {
            values = null;
        } else {

            values = row.columns.get(iri2).values;
            //System.out.println("row.columns.get(iri2) == null "  + values);
        }
        if (column.getLang() == null) {

            // If it is IRI, parse it by valueURL. If it is literal, just write down its Label.
            ////System.out.println("column.getPropertyUrl() = " + column.getPropertyUrl());
            //row.columns.entrySet().forEach(entry -> //System.out.println(entry.getKey() + " is key to: " + entry.getValue().values));

            if (values == null) {
                // The Column is empty, put empty Value to the file
                sb.append("");
            } else if (values.get(0).isIRI()) {
                if (values.size() == 1) {
                    IRI iri = (IRI) values.get(0);
                    if (column.getValueUrl().startsWith("{")) {
                        //System.out.println("Appending whole value: " + iri.stringValue());
                        sb.append(iri.stringValue());
                    } else {
                        //System.out.println("Appending shortened value: " + iri.getLocalName());
                        sb.append(iri.getLocalName());
                    }

                } else {
                    // There are multiple values from the language, we need to enclose them in " "
                    sb.append('"');
                    values.forEach(val -> {
                        IRI iri = (IRI) val;
                        if (column.getValueUrl().startsWith("{")) {
                            //System.out.println("Appending whole value: " + iri.stringValue());
                            sb.append(iri.stringValue());
                        } else {
                            //System.out.println("Appending shortened value: " + iri.getLocalName());
                            sb.append(iri.getLocalName());
                        }
                        sb.append(",");
                    });
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append('"');
                    column.setSeparator(",");
                }
            } else if (values.get(0).isLiteral()) {
                if (values.size() == 1) {
                    Literal literal = (Literal) values.get(0);

                    sb.append(safeLiteral(literal));
                } else {
                    // There are multiple values from the language, we need to enclose them in " "
                    sb.append('"');
                    values.forEach(val -> {
                        String strValue = ((Literal) val).getLabel();
                        sb.append(strValue);
                        sb.append(",");
                    });
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append('"');
                    column.setSeparator(",");
                }
            }
        }
        // Language versions split
        else {
            ////System.out.println("column.getPropertyUrl() = " + column.getPropertyUrl());
            ////System.out.println(values);
            List<Value> languageVariations = values;
            if (languageVariations == null) {
                sb.append(",");
                return;
            }
            List<Value> valuesByLang = languageVariations.stream().filter(val -> ((Literal) val).getLanguage().get().equals(column.getLang())).collect(Collectors.toList());
            // There is only one value of this language
            if (!valuesByLang.isEmpty()) {
                if (valuesByLang.size() == 1) {
                    Value val = valuesByLang.get(0);
                    sb.append(safeLiteral((Literal) val));
                } else {
                    //System.out.println();
                    //System.out.println(multilevelPropertyUrl + " " + column.getLang() + " column.separator=" + column.getSeparator());
                    // There are multiple values from the language, we need to enclose them in " "
                    sb.append('"');
                    valuesByLang.forEach(val -> {
                        String strValue = ((Literal) val).getLabel();
                        //System.out.println("Literal value multivalue: " + strValue);
                        sb.append(strValue);
                        sb.append(",");
                    });
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append('"');
                    column.setSeparator(",");
                    //System.out.println(multilevelPropertyUrl + " " + column.getLang() + " column.separator=" + column.getSeparator());
                }
            }


        }

        sb.append(",");
    }

    private static String safeLiteral(Literal literal) {
        if (literal.getLabel().contains(",") && !literal.getLabel().startsWith("\"") && !literal.getLabel().endsWith("\"")) {
            return "\"" + literal.getLabel() + "\"";
        } else {
            return literal.getLabel();
        }
    }

    private static void appendIdByValuePattern(Row row, Metadata metadata, StringBuilder sb, Column column) {
        if (column.getValueUrl().startsWith("{")) {
            sb.append(row.id.stringValue());
            sb.append(",");
        } else {
            if (row.id.isBNode()) {
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
        Optional<Table> findTable = metadata.getTables().stream().filter(tables -> tables.getUrl().equals(fileObject.getName())).findFirst();
        System.out.println("addHeadersFromMetadata fileObject.getName() = " + fileObject.getName());
        Table fud = findTable.orElse(null);
        Column firstColumn = null;
        if (Boolean.getBoolean(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES))) {

            firstColumn = fud.getTableSchema().getColumns().stream().filter(column -> column.getPropertyUrl() == null).findFirst().get();
            sb1.append(firstColumn.getTitles());
            sb1.append(",");
        }


        for (Column column : fud.getTableSchema().getColumns()) {
            if (column != firstColumn && column.getVirtual() == null) {
                sb1.append(column.getTitles());
                sb1.append(",");
                orderOfColumns.add(column);
            }
        }
        sb1.deleteCharAt(sb1.length() - 1);
        sb1.append("\n");

        return orderOfColumns;
    }

    /**
     * Gets file extension.
     *
     * @param fileName the file name
     * @return the file extension
     */
    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');

        // Check if the dot exists and it's not the first or last character
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        } else {
            return "";  // Return an empty string if there's no extension
        }
    }

    /**
     * Make file by name and extension file.
     *
     * @param name the name
     * @param ext  the ext
     * @return the file
     */
    public static File makeFileByNameAndExtension(String name, String ext) {
        try {
            File newFile;
            if (ext != null) {
                newFile = new File(name + "." + ext);
                System.out.println("newFile: " + newFile.getAbsolutePath());
            } else {
                System.out.println("newFile: " + name);

                newFile = new File(name);
                System.out.println("newFile.getName(): " + newFile.getName());
            }
            System.out.println("Trying to delete file.getAbsolutePAth " + newFile.getAbsolutePath());
            FileWrite.deleteFile(newFile.getAbsolutePath());
            if (newFile.createNewFile()) {
                //System.out.println("File created: " + newFile);
            } else {
                //System.out.println("File already exists.");

            }
            return newFile;
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Write to the file.
     *
     * @param file        the file
     * @param something   the something
     * @param appendOrNot the append or not
     */
    public static void writeToTheFile(File file, Object something, boolean appendOrNot) {
        try {
            System.out.println("writeToTheFile trying to create FileWriter with file.getName()=" + file.getName() + " and file.getAbsolutePath()=" + file.getAbsolutePath());
            FileWriter myWriter = new FileWriter(file, appendOrNot);

            myWriter.write(something.toString());

            myWriter.close();
            //System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            //System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Delete file.
     *
     * @param fileName the file name
     */
    public static void deleteFile(String fileName) {
        File myObj = new File(fileName);
        if (myObj.delete()) {
            //System.out.println("Deleted the file: " + myObj);
        } else {
            //System.out.println("Failed to delete the file.");
        }
    }
}
