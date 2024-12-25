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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.eclipse.rdf4j.model.util.Values.iri;

/**
 * Class containing methods regarding writing to files.
 */
public class FileWrite {
    private static final Logger logger = Logger.getLogger(FileWrite.class.getName());
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
        keys.sort(Comparator.comparing(Value::stringValue));
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
        List<String[]> lines = new ArrayList<>();
        List<Column> orderOfColumnKeys = addHeadersFromMetadata(fileName, metadata, lines);

        for (Row row : rows) {
            //System.out.println("rows number " + rows.size());
            StringBuilder sb = new StringBuilder();

            ////System.out.println("orderOfColumnKeys number " + orderOfColumnKeys.size());
            boolean firstColumn = true;
            List<Map.Entry<Value, TypeIdAndValues>> multivalues = row.columns.entrySet().stream()
                    .filter(entry -> (entry.getValue().values.size() > 1 && entry.getValue().type.equals(TypeOfValue.LITERAL) && entry.getValue().values.get(0).isLiteral() && ((Literal) entry.getValue().values.get(0)).getLanguage().isPresent() && literalHasDifferentLanguageTags(entry.getValue().values) && !allLanguagesAreUnique(entry.getValue().values)))
                    .toList();
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
            // TODO TO OPENCSV writer and by the COMBINATION for first normal form
            if (!combinations.isEmpty()) {
                for (Map<Value, Value> combination : combinations) {
                    ////System.out.println("Combinations size: " + combinations.size());
                    ////System.out.println("Combination: " + combination.entrySet());
                    appendIdByValuePattern(row, orderOfColumnKeys.get(0));
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
                            if (combination.get(iri(multilevelPropertyUrl)) != null) {
                                if (combination.get(iri(multilevelPropertyUrl)).isIRI()) {
                                    if (column.getValueUrl().startsWith("{")) {
                                        sb.append(combination.get(iri(multilevelPropertyUrl)).stringValue());
                                    } else {
                                        sb.append(((IRI) combination.get(iri(multilevelPropertyUrl))).getLocalName());
                                    }
                                } else if (combination.get(iri(multilevelPropertyUrl)).isLiteral()) {
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
                String[] line = new String[lines.get(0).length];
                line[0] = appendIdByValuePattern(row, orderOfColumnKeys.get(0));
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


                        line[i] = appendColumnValueByKey(column, row, sb, 0, multilevelPropertyUrl, metadata);

                    }
                    i++;
                }
                ////System.out.println("row: " + sb.toString() + ".");
                lines.add(line);
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

    public static void writeLinesToCSVFile(File file,  List<String[]> lines, boolean append){
        // Write the updated content back to the file
        try (CSVWriter writer = new CSVWriter(new FileWriter(file, append), ',',
                CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)) {
            // Write array data as a single line
            for (String[] updatedLine : lines) {
                //System.out.println("new headers updated line: " + updatedLine);
                writer.writeNext(updatedLine, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        if(Boolean.parseBoolean(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.FIRST_NORMAL_FORM))) {
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
        return new ArrayList<>();
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

    private static String appendColumnValueByKey(Column column, Row row, StringBuilder sb, int i, String multilevelPropertyUrl, Metadata metadata) {
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
                        return iri.stringValue();
                    } else {
                        //System.out.println("Appending shortened value: " + iri.getLocalName());
                        return iri.getLocalName();
                    }

                } else {
                    // There are multiple values from the language, we need to enclose them in " "
                    sb.append('"');
                    values.forEach(val -> {
                        IRI iri = (IRI) val;
                        if (column.getValueUrl().startsWith("{")) {
                            //System.out.println("Appending whole value: " + iri.stringValue());
                            return iri.stringValue();
                        } else {
                            //System.out.println("Appending shortened value: " + iri.getLocalName());
                            return iri.getLocalName();
                        }
                    });
                    column.setSeparator(",");
                }
            } else if (values.get(0).isLiteral()) {
                if (values.size() == 1) {
                    Literal literal = (Literal) values.get(0);

                    return safeLiteral(literal);
                } else {
                    // There are multiple values from the language, we need to enclose them in " "
                    sb.append('"');
                    values.forEach(val -> {
                        String strValue = ((Literal) val).getLabel();
                        return strValue;
                        sb.append(",");
                    });
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
                return "";
            }
            List<Value> valuesByLang = languageVariations.stream().filter(val -> ((Literal) val).getLanguage().get().equals(column.getLang())).collect(Collectors.toList());
            // There is only one value of this language
            if (!valuesByLang.isEmpty()) {
                if (valuesByLang.size() == 1) {
                    Value val = valuesByLang.get(0);
                    return safeLiteral((Literal) val);
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
                    column.setSeparator(",");
                    //System.out.println(multilevelPropertyUrl + " " + column.getLang() + " column.separator=" + column.getSeparator());
                }
            }


        }
    }

    private static String safeLiteral(Literal literal) {
        if (literal.getLabel().contains(",") && !literal.getLabel().startsWith("\"") && !literal.getLabel().endsWith("\"")) {
            return "\"" + literal.getLabel() + "\"";
        } else {
            return literal.getLabel();
        }
    }

    private static String appendIdByValuePattern(Row row, Column column) {
        if (column.getValueUrl().startsWith("{")) {
            return row.id.stringValue();
        } else {
            if (row.id.isBNode()) {
                return row.id.stringValue();
            } else {
                IRI iri = (IRI) row.id;
                return iri.getLocalName();
            }
        }
    }

    private static List<Column> addHeadersFromMetadata(String fileName, Metadata metadata, List<String[]> lines) {
        List<Column> orderOfColumns = new ArrayList<>();

        System.out.println("addHeadersFromMetadata fileName = " + fileName);
        File fileObject = new File(fileName);
        metadata.getTables().forEach(tables -> System.out.println("tables = " + tables.getUrl()));
        Optional<Table> findTable = metadata.getTables().stream().filter(tables -> tables.getUrl().equals(fileObject.getName())).findFirst();
        System.out.println("addHeadersFromMetadata fileObject.getName() = " + fileObject.getName());
        Table fud = findTable.orElse(null);
        List<String> headersBuffer = new ArrayList<>();

        Column firstColumn = null;
        if (Boolean.getBoolean(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES))) {

            assert fud != null;
            firstColumn = fud.getTableSchema().getColumns().stream().filter(column -> column.getPropertyUrl() == null).findFirst().get();
            headersBuffer.add(firstColumn.getTitles());
        }


        assert fud != null;
        for (Column column : fud.getTableSchema().getColumns()) {
            if (column != firstColumn && column.getVirtual() == null) {
                headersBuffer.add(column.getTitles());
                orderOfColumns.add(column);
            }
        }
        String[] headers = headersBuffer.toArray(new String[0]);
        lines.add(headers);
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

        // Check if the dot exists, and it's not the first or last character
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        } else {
            return "";  // Return an empty string if there's no extension
        }
    }

    /**
     * Make file by name and extension file.
     *
     * @param name The name for the file
     * @param ext  The extension of the file to be made
     * @return Created file object
     */
    public static File makeFileByNameAndExtension(String name, String ext) {
        File newFile = null;
        try {
            if (ext != null) {
                newFile = new File(name + "." + ext);
            } else {
                newFile = new File(name);
            }
            FileWrite.deleteFile(newFile.getAbsolutePath());
            if (newFile.createNewFile()) {
                logger.log(Level.INFO, "File created: " + newFile.getAbsolutePath());
            } else {
                logger.log(Level.INFO, "File already exists: " + newFile.getAbsolutePath());
            }
            return newFile;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An error occured while trying to delete old version of file and create new file : " + newFile.getAbsolutePath());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Write given String to the file (append the String or not).
     *
     * @param file        The file to write to
     * @param something   Something to be written in the file
     * @param appendOrNot Append (true) to the file or write to the file from the beginning
     */
    public static void writeToTheFile(File file, Object something, boolean appendOrNot) {
        try {
            FileWriter myWriter = new FileWriter(file, appendOrNot);

            myWriter.write(something.toString());

            myWriter.close();

            logger.log(Level.INFO, "Successfully wrote to the file " + file.getAbsolutePath() + ".");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete file.
     *
     * @param fileName The name of the file to delete
     */
    public static void deleteFile(String fileName) {
        File myObj = new File(fileName);
        if (myObj.delete()) {
            logger.log(Level.INFO, "The file " + myObj.getAbsolutePath() + " was deleted.");
        }
    }
}
