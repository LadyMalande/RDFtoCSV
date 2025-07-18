package com.miklosova.rdftocsvw.output_processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.miklosova.rdftocsvw.converter.data_structure.Row;
import com.miklosova.rdftocsvw.converter.data_structure.TypeIdAndValues;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.JsonUtil;
import com.opencsv.CSVWriter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
                        sb.append(row.columns.get(columnName).values.get(0).stringValue()).append(",");
                    }
                } else {
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
        System.out.println("newFileName writeFilesToConfigFile   allFileNames = " + sb.toString());
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
        logger.info("fileName for final .csv file before changing = " + fileName);
        fileName = (fileName.split("/"))[fileName.split("/").length - 1];
        logger.info("fileName for final .csv file after changing = " + fileName);
        fileName = getFullPathOfFile(fileName);
        logger.info("fileName for final .csv file = " + fileName);
        ObjectNode originalMetadataJSON = null;
        try {
            originalMetadataJSON = JsonUtil.serializeWithContext(metadata);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Unable to process the Metadata object into JSON.");
        }

        File f = FileWrite.makeFileByNameAndExtension(fileName, null);

        List<String[]> lines = new ArrayList<>();
        List<Column> orderOfColumnKeys = addHeadersFromMetadata(fileName, metadata, lines);

        for (Row row : rows) {

            boolean firstColumn;
            List<Map.Entry<Value, TypeIdAndValues>> multivalues = row.columns.entrySet().stream()
                    .filter(entry -> (entry.getValue().values.size() > 1))
                    .toList();

            List<Map<Value, Value>> combinations = generateCombinations(multivalues);

            String[] line = new String[lines.get(0).length];
            int i = 0;
            // Write CSV rows in first normal form -> write out the combinations without having lists in cells
            if (!combinations.isEmpty()) {
                for (Map<Value, Value> combination : combinations) {
                    i = 0;
                    line = new String[lines.get(0).length];

                    line[0] = (appendIdByValuePattern(row, orderOfColumnKeys.get(0)));


                    firstColumn = true;

                    for (Column column : orderOfColumnKeys) {
                        i++;
                        String multilevelPropertyUrl = "";
                        if (column.getPropertyUrl() != null) {
                            multilevelPropertyUrl = column.getOriginalColumnKey().stringValue();
                        }
                        if (!Boolean.getBoolean(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES)) && firstColumn) {
                            firstColumn = false;
                            i--;
                        } else {
                            if (combination.get(iri(multilevelPropertyUrl)) != null) {
                                if (combination.get(iri(multilevelPropertyUrl)).isIRI()) {
                                    if (column.getValueUrl().startsWith("{")) {
                                        line[i] = combination.get(iri(multilevelPropertyUrl)).stringValue();
                                    } else {
                                        line[i] = ((IRI) combination.get(iri(multilevelPropertyUrl))).getLocalName();
                                    }
                                } else if (combination.get(iri(multilevelPropertyUrl)).isLiteral()) {
                                    line[i] = safeLiteral((Literal) combination.get(iri(multilevelPropertyUrl)));
                                }
                            } else {
                                line[i] = appendColumnValueByKey(column, row, multilevelPropertyUrl);
                            }
                        }
                    }
                    lines.add(line);
                }

            } else {
                // Write CSV rows without first normal form -> make lists in cells
                line[0] = appendIdByValuePattern(row, orderOfColumnKeys.get(0));
                i++;
                firstColumn = true;
                for (Column column : orderOfColumnKeys) {

                    if (!Boolean.getBoolean(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES)) && firstColumn) {
                        firstColumn = false;
                        i--;
                    } else {

                        ValueFactory vf = SimpleValueFactory.getInstance();
                        IRI propertyUrlIRI = vf.createIRI(column.getPropertyUrl());
                        String multilevelPropertyUrl = (column.getLang() != null) ? propertyUrlIRI.getNamespace() + column.getName().substring(0, column.getName().length() - 3) : propertyUrlIRI.getNamespace() + column.getName();
                        line[i] = appendColumnValueByKey(column, row, multilevelPropertyUrl);
                    }
                    i++;
                }
                lines.add(line);
            }
        }

        ObjectNode metadataNow = null;
        try {
            metadataNow = JsonUtil.serializeWithContext(metadata);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Unable to process the Metadata object into JSON with @context.");

        }

        if (originalMetadataJSON != metadataNow) {
            JsonUtil.serializeAndWriteToFile(metadata);

        }
        FileWrite.writeLinesToCSVFile(f, lines, true);
        try {
            assert f != null;
            return String.join("\n", Files.readLines(f, Charsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Write lines to csv file.
     *
     * @param file   the file
     * @param lines  the lines
     * @param append the append
     */
    public static void writeLinesToCSVFile(File file, List<String[]> lines, boolean append) {
        // Write the updated content back to the file
        try (CSVWriter writer = new CSVWriter(new FileWriter(file, append), ',',
                CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)) {
            // Write array data as a single line
            for (String[] updatedLine : lines) {
                writer.writeNext(updatedLine, false);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An error occurred while writing the lines to CSV using OpenCSV. For file " + file.getAbsolutePath());
        }
    }

    private static String getFullPathOfFile(String fileName) {

        return fileName;
    }

    /**
     * Generate combinations from the Columns containing multiple values in one cell.
     *
     * @param listOfLists the list of lists
     * @return the list of generated combinations
     */
    public static List<Map<Value, Value>> generateCombinations(List<Map.Entry<Value, TypeIdAndValues>> listOfLists) {
        if (Boolean.parseBoolean(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.FIRST_NORMAL_FORM))) {
            // Map of predicatesOfColumns and Values in the Column
            List<Map<Value, Value>> resultingRowOfFormerMultivalues = new ArrayList<>();
            if (!listOfLists.isEmpty() && listOfLists.get(0).getValue().values.get(0).isLiteral() && ((Literal) listOfLists.get(0).getValue().values.get(0)).getLanguage().isPresent()) {
            } else {
                generateCombinationsHelper(listOfLists, resultingRowOfFormerMultivalues, 0, new HashMap<>());

            }
            return resultingRowOfFormerMultivalues;
        }
        return new ArrayList<>();
    }

    private static void generateCombinationsHelper(List<Map.Entry<Value, TypeIdAndValues>> listOfLists, List<Map<Value, Value>> result, int depth, Map<Value, Value> current) {
        if (depth == listOfLists.size()) {
            result.add(new HashMap<>(current));
            return;
        }

        Map.Entry<Value, TypeIdAndValues> currentList = listOfLists.get(depth);
        for (Value item : currentList.getValue().values) {
            current.put(currentList.getKey(), item);
            generateCombinationsHelper(listOfLists, result, depth + 1, current);
            current.remove(currentList.getKey(), item); // Backtrack
        }
    }

    private static String appendColumnValueByKey(Column column, Row row, String multilevelPropertyUrl) {
        IRI iri2;

        try {
            iri2 = iri(multilevelPropertyUrl);
        } catch (NullPointerException ex) {
            iri2 = iri(column.getValueUrl());
        }

        List<Value> values;
        if (row.columns.get(iri2) == null) {
            values = null;
        } else {
            values = row.columns.get(iri2).values;
        }
        if (column.getLang() == null) {

            // If it is IRI, parse it by valueURL. If it is literal, just write down its Label.
            if (values == null) {
                // The Column is empty, put empty Value to the file
                return ("");
            } else if (values.get(0).isIRI()) {
                if (values.size() == 1) {
                    IRI iri = (IRI) values.get(0);
                    if (column.getValueUrl().startsWith("{")) {
                        return iri.stringValue();
                    } else {
                        return iri.getLocalName();
                    }

                } else {
                    // There are multiple values from the language, we need to separate them with comma
                    StringBuilder sbForMultipleLangVariations = new StringBuilder();
                    values.forEach(val -> {
                        IRI iri = (IRI) val;
                        if (column.getValueUrl().startsWith("{")) {
                            sbForMultipleLangVariations.append(iri.stringValue());
                        } else {
                            sbForMultipleLangVariations.append(iri.getLocalName());
                        }
                        sbForMultipleLangVariations.append(",");
                    });
                    sbForMultipleLangVariations.deleteCharAt(sbForMultipleLangVariations.length() - 1);
                    column.setSeparator(",");
                    return sbForMultipleLangVariations.toString();
                }
            } else if (values.get(0).isLiteral()) {
                if (values.size() == 1) {
                    Literal literal = (Literal) values.get(0);
                    return safeLiteral(literal);
                } else {
                    // There are multiple values from the language, we need to enclose them in " "
                    StringBuilder sbForMultipleLangVariations = new StringBuilder();
                    values.forEach(val -> {
                        String strValue = ((Literal) val).getLabel();
                        sbForMultipleLangVariations.append(strValue);
                        sbForMultipleLangVariations.append(",");
                    });
                    sbForMultipleLangVariations.deleteCharAt(sbForMultipleLangVariations.length() - 1);
                    column.setSeparator(",");
                    return sbForMultipleLangVariations.toString();
                }
            } else {
                // There should be no blanks
                return "";
            }
        }
        // Language versions split
        else {

            if (values == null) {
                return "";
            }


            List<Value> valuesByLang = values.stream().filter(val -> (((Literal) val).getLanguage().isPresent()) ? ((Literal) val).getLanguage().get().equals(column.getLang()) : false).collect(Collectors.toList());
            // There is only one value of this language
            if (!valuesByLang.isEmpty()) {
                if (valuesByLang.size() == 1) {
                    Value val = valuesByLang.get(0);
                    return safeLiteral((Literal) val);
                } else {
                    // There are multiple values from the language, we need to enclose them in " "
                    StringBuilder sbForMultipleLangVariations = new StringBuilder();
                    valuesByLang.forEach(val -> {
                        String strValue = ((Literal) val).getLabel();
                        sbForMultipleLangVariations.append(strValue);
                        sbForMultipleLangVariations.append(",");
                    });
                    sbForMultipleLangVariations.deleteCharAt(sbForMultipleLangVariations.length() - 1);
                    column.setSeparator(",");

                    return sbForMultipleLangVariations.toString();
                }
            } else {
                // This should not happen
                return "";
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

        File fileObject = new File(fileName);
        Optional<Table> findTable = metadata.getTables().stream().filter(tables -> tables.getUrl().equals(fileObject.getName())).findFirst();
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
                logger.log(Level.INFO, "File created: " + newFile.getAbsolutePath() + ". Writing data...");
            } else {
                logger.log(Level.INFO, "File already exists: " + newFile.getAbsolutePath());
            }
            return newFile;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An error occurred while trying to delete old version of file and create new file : " + newFile.getAbsolutePath());
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
            logger.log(Level.SEVERE, "An error occurred while trying to write to file : " + file.getAbsolutePath());
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
            //logger.log(Level.INFO, "The file " + myObj.getAbsolutePath() + " was deleted.");
        }
    }

    /**
     * Has extension boolean.
     *
     * @param fileName  the file name
     * @param extension the extension
     * @return the boolean
     */
    public static boolean hasExtension(String fileName, String extension) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex > 0) && (lastDotIndex < fileName.length() - 1) && fileName.substring(lastDotIndex + 1).equalsIgnoreCase(extension);
    }
}
