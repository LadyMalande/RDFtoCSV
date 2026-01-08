package com.miklosova.rdftocsvw.output_processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.miklosova.rdftocsvw.converter.data_structure.Row;
import com.miklosova.rdftocsvw.converter.data_structure.TypeIdAndValues;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.JsonUtil;
import com.opencsv.CSVWriter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
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
     * @deprecated Use {@link #writeFilesToConfigFile(ArrayList, AppConfig)} instead
     */
    @Deprecated
    public static void writeFilesToConfigFile(ArrayList<String> fileNamesCreated) {
        writeFilesToConfigFile(fileNamesCreated, null);
    }

    /**
     * Creates a one line of file names of created CSVs separated by commas and writes it to a config or AppConfig.
     *
     * @param fileNamesCreated array list of created CSV file names
     * @param config the application configuration
     */
    public static void writeFilesToConfigFile(ArrayList<String> fileNamesCreated, AppConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("AppConfig cannot be null");
        }
        StringBuilder sb = new StringBuilder();
        String outputPath = config.getOutputFilePath();
        //System.out.println("writeFilesToConfigFile   config.getOutputFilePath() = " + config.getOutputFilePath());
        
        for (String fileName : fileNamesCreated) {
            //logger.info("[will go to intermediateFileNames]fileName from fileNamesCreated: " + fileName);
            // If fileName is not an absolute path and we have an output path, build full path
            boolean isAbsolutePath = (fileName.length() >= 3 && Character.isLetter(fileName.charAt(0)) && 
                                      fileName.charAt(1) == ':') || fileName.startsWith("/");
            
            if (!isAbsolutePath && outputPath != null && !outputPath.isEmpty()) {
                // Prepend output directory to relative filename
                File outputFile = new File(outputPath);
                File parentDir = outputFile.getParentFile();
                
                if (parentDir != null) {
                    //logger.info("[parentDir: " + parentDir.getAbsolutePath());
                    // The filename must get to the short version instead of the relative one
                    File file = new File(fileName);
                    String shortLocalFileName = file.getName();
                    //fileName = new File(parentDir, shortLocalFileName).getAbsolutePath();
                    //fileName = new File("../", shortLocalFileName).getAbsolutePath();
                    fileName = String.valueOf(new File("./", shortLocalFileName));
                }
            }
            
            sb.append(fileName).append(",");
        }
        //System.out.println("newFileName writeFilesToConfigFile   allFileNames = " + sb.toString());
        config.setIntermediateFileNames(sb.toString());
    }

    /**
     * Save the CSV file from ArrayList of Rows created during conversion.
     *
     * @param fileName The name for the created CSV file.
     * @param rows     ArrayList of Rows created by conversion by rdf4j method.
     * @param metadata Metadata created during the rdf4j method.
     * @return The contents of the file with headers as String.
     * @deprecated Use {@link #saveCSVFileFromRows(String, ArrayList, Metadata, AppConfig)} instead
     */
    @Deprecated
    public static String saveCSVFileFromRows(String fileName, ArrayList<Row> rows, Metadata metadata) {
        return saveCSVFileFromRows(fileName, rows, metadata, null);
    }

    /**
     * Save the CSV file from ArrayList of Rows created during conversion.
     *
     * @param fileName The name for the created CSV file.
     * @param rows     ArrayList of Rows created by conversion by rdf4j method.
     * @param metadata Metadata created during the rdf4j method.
     * @param config   the application configuration
     * @return The contents of the file with headers as String.
     */
    public static String saveCSVFileFromRows(String fileName, ArrayList<Row> rows, Metadata metadata, AppConfig config) {
        //logger.info("fileName for final .csv file before changing = " + fileName);
        // Only extract basename if this is a relative path with / separator (URL or Unix-style path from old code)
        // Don't strip path if it's an absolute Windows path (C:\...) or Unix path (starts with /)
        boolean isAbsolutePath = (fileName.length() >= 3 && Character.isLetter(fileName.charAt(0)) && 
                                  fileName.charAt(1) == ':') || fileName.startsWith("/");
        if (!isAbsolutePath && fileName.contains("/")) {
            fileName = (fileName.split("/"))[fileName.split("/").length - 1];
        }
        //logger.info("fileName for final .csv file after changing = " + fileName);
        fileName = getFullPathOfFile(fileName);
        //logger.info("fileName for final .csv file = " + fileName);
        ObjectNode originalMetadataJSON = null;
        try {
            originalMetadataJSON = JsonUtil.serializeWithContext(metadata);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Unable to process the Metadata object into JSON.");
        }

        File f = FileWrite.makeFileByNameAndExtension(fileName, null);

        List<String[]> lines = new ArrayList<>();
        List<Column> orderOfColumnKeys = addHeadersFromMetadata(fileName, metadata, lines, config);
        
        // Get the actual first column (Subject column) from metadata
        File fileObject = new File(fileName);
        Optional<Table> findTable = metadata.getTables().stream()
            .filter(tables -> tables.getUrl().equals(fileObject.getName()))
            .findFirst();
        Column actualFirstColumn = null;
        if (findTable.isPresent()) {
            actualFirstColumn = findTable.get().getTableSchema().getColumns().stream()
                .filter(column -> column.getPropertyUrl() == null)
                .findFirst()
                .orElse(null);
        }
        
        final Column firstColumnForWriting = actualFirstColumn;

        for (Row row : rows) {

            boolean firstColumn;
            List<Map.Entry<Value, TypeIdAndValues>> multivalues = row.columns.entrySet().stream()
                    .filter(entry -> (entry.getValue().values.size() > 1))
                    .toList();

            List<Map<Value, Value>> combinations = generateCombinations(multivalues, config);

            String[] line = new String[lines.get(0).length];
            int i = 0;
            // Write CSV rows in first normal form -> write out the combinations without having lists in cells
            if (!combinations.isEmpty()) {
                for (Map<Value, Value> combination : combinations) {
                    i = 0;
                    line = new String[lines.get(0).length];

                    line[0] = (appendIdByValuePattern(row, firstColumnForWriting != null ? firstColumnForWriting : orderOfColumnKeys.get(0)));
                    //logger.info("line[0]=" + line[0]);

                    firstColumn = true;

                    for (Column column : orderOfColumnKeys) {
                        i++;
                        String multilevelPropertyUrl = "";
                        if (column.getPropertyUrl() != null) {
                            multilevelPropertyUrl = column.getOriginalColumnKey().stringValue();
                        }
                        boolean hasRdfTypes = (config != null && config.getConversionHasRdfTypes() != null) ? 
                            config.getConversionHasRdfTypes() : true;
                        if (!hasRdfTypes && firstColumn) {
                            firstColumn = false;
                            i--;
                        } else {
                            if (combination.get(iri(multilevelPropertyUrl)) != null) {
                                if (combination.get(iri(multilevelPropertyUrl)).isIRI()) {
                                    // If valueUrl is a prefix pattern (contains {+ but doesn't start with it), extract local name
                                    // If valueUrl is a simple pattern (starts with {+), use full IRI
                                    if (column.getValueUrl() != null && column.getValueUrl().contains("{+") && !column.getValueUrl().trim().startsWith("{+")) {
                                        line[i] = ((IRI) combination.get(iri(multilevelPropertyUrl))).getLocalName();
                                    } else {
                                        line[i] = combination.get(iri(multilevelPropertyUrl)).stringValue();
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
                line[0] = appendIdByValuePattern(row, firstColumnForWriting != null ? firstColumnForWriting : orderOfColumnKeys.get(0));
                //logger.info("line[0]=" + line[0]);
                i++;
                firstColumn = true;
                for (Column column : orderOfColumnKeys) {
                    boolean hasRdfTypes = (config != null && config.getConversionHasRdfTypes() != null) ? 
                        config.getConversionHasRdfTypes() : true;
                    if (!hasRdfTypes && firstColumn) {
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
            JsonUtil.serializeAndWriteToFile(metadata, config);
        }
        FileWrite.writeLinesToCSVFile(f, lines, true);
        
        // Return filename instead of reading entire file into memory
        return fileName;
    }

    /**
     * Read CSV file contents as a string. Use cautiously with large files.
     * For web service use only - CLI should use file-based operations.
     *
     * @param fileName the file name to read
     * @param maxLines maximum number of lines to read (0 = unlimited, but risky for large files)
     * @return the file contents as string
     */
    public static String readCSVFileAsString(String fileName, int maxLines) {
        fileName = getFullPathOfFile(fileName);
        File f = new File(fileName);
        
        if (!f.exists()) {
            return "Error: File not found: " + fileName;
        }
        
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(line);
                lineCount++;
                
                if (maxLines > 0 && lineCount >= maxLines) {
                    sb.append("\n... (truncated at ").append(maxLines).append(" lines)");
                    break;
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading CSV file: " + fileName, e);
            return "Error reading file: " + e.getMessage();
        }
        
        return sb.toString();
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
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(file, append), StandardCharsets.UTF_8), ',',
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
     * @deprecated Use {@link #generateCombinations(List, AppConfig)} instead
     */
    @Deprecated
    public static List<Map<Value, Value>> generateCombinations(List<Map.Entry<Value, TypeIdAndValues>> listOfLists) {
        return generateCombinations(listOfLists, null);
    }

    /**
     * Generate combinations from the Columns containing multiple values in one cell.
     *
     * @param listOfLists the list of lists
     * @param config the application configuration
     * @return the list of generated combinations
     */
    public static List<Map<Value, Value>> generateCombinations(List<Map.Entry<Value, TypeIdAndValues>> listOfLists, AppConfig config) {
        boolean firstNormalForm = (config != null && config.getFirstNormalForm() != null) ? 
            config.getFirstNormalForm() : true;
        if (firstNormalForm) {
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
                    // If valueUrl is a simple pattern (starts with {+), use full IRI
                    // If valueUrl is a prefix pattern (contains {+ but doesn't start with it), use local name
                    if (column.getValueUrl().trim().startsWith("{+")) {
                        return iri.stringValue();
                    } else if (column.getValueUrl().contains("{+")) {
                        return iri.getLocalName();
                    } else {
                        return iri.stringValue();
                    }

                } else {
                    // There are multiple values from the language, we need to separate them with comma
                    StringBuilder sbForMultipleLangVariations = new StringBuilder();
                    values.forEach(val -> {
                        IRI iri = (IRI) val;
                        // If valueUrl is a simple pattern (starts with {+), use full IRI
                        // If valueUrl is a prefix pattern (contains {+ but doesn't start with it), use local name
                        if (column.getValueUrl().trim().startsWith("{+")) {
                            sbForMultipleLangVariations.append(iri.stringValue());
                        } else if (column.getValueUrl().contains("{+")) {
                            sbForMultipleLangVariations.append(iri.getLocalName());
                        } else {
                            sbForMultipleLangVariations.append(iri.stringValue());
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
        // Return raw label - CSVWriter will handle quoting/escaping automatically
        return literal.getLabel();
    }

    private static String appendIdByValuePattern(Row row, Column column) {
        // Handle blank nodes (including those with blank_Nodes_IRI prefix)
        if (row.id.isBNode() || row.id.stringValue().startsWith("https://blank_Nodes_IRI")) {
            if (row.id.isBNode()) {
                return row.id.stringValue();
            } else {
                // For blank_Nodes_IRI, extract just the local part (the number)
                IRI iri = (IRI) row.id;
                return iri.getLocalName();
            }
        }
        //logger.info("appendIdByValuePattern - column.getName(): " + column.getName());
        //logger.info("appendIdByValuePattern - column.getName(): " + column.getTitles());
        //logger.info("appendIdByValuePattern - column.getValueUrl(): " + column.getValueUrl());
        /*logger.info("appendIdByValuePattern - column.getValueUrl().contains(\"{+\"): " + column.getValueUrl().contains("{+"));
        logger.info("appendIdByValuePattern - !column.getValueUrl().startsWith(\"{+\") " + !column.getValueUrl().startsWith("{+"));
*/
        // Check if the valueUrl is a partial pattern (e.g., "https://example.com/{+Concept}")
        // If so, we need to extract the local name to fill the pattern
        if (column.getValueUrl() != null && column.getValueUrl().contains("{+") && !column.getValueUrl().startsWith("{+")) {
            // Partial pattern - extract local name
            IRI iri = (IRI) row.id;
            return iri.getLocalName();
        }
        
        // For simple pattern {+Subject} or no pattern, return the full IRI
        return row.id.stringValue();
    }

    private static List<Column> addHeadersFromMetadata(String fileName, Metadata metadata, List<String[]> lines, AppConfig config) {
        //logger.info("fileName in addHeadersFromMetadata: " + fileName);

        List<Column> orderOfColumns = new ArrayList<>();

        File fileObject = new File(fileName);
        Optional<Table> findTable = metadata.getTables().stream().filter(tables -> tables.getUrl().equals(fileObject.getName())).findFirst();
        Table fud = findTable.orElse(null);
        
        if (fud == null) {
            logger.log(Level.SEVERE, "Table not found in metadata for file: " + fileObject.getName());
            logger.log(Level.SEVERE, "Available tables in metadata: " + 
                metadata.getTables().stream().map(Table::getUrl).collect(java.util.stream.Collectors.joining(", ")));
            throw new IllegalStateException("Table not found in metadata for file: " + fileObject.getName() + 
                ". Available tables: " + metadata.getTables().stream().map(Table::getUrl).collect(java.util.stream.Collectors.joining(", ")));
        }
        
        List<String> headersBuffer = new ArrayList<>();

        Column firstColumn = null;
        boolean hasRdfTypes = (config != null && config.getConversionHasRdfTypes() != null) ? 
            config.getConversionHasRdfTypes() : true;
        if (hasRdfTypes) {
            Optional<Column> firstColOpt = fud.getTableSchema().getColumns().stream()
                .filter(column -> column.getPropertyUrl() == null)
                .findFirst();
            
            if (firstColOpt.isPresent()) {
                firstColumn = firstColOpt.get();
                headersBuffer.add(firstColumn.getTitles());
            } else {
                logger.log(Level.WARNING, "No first column (with null propertyUrl) found in table: " + fud.getUrl());
            }
        }

        for (Column column : fud.getTableSchema().getColumns()) {
            if (column != firstColumn && column.getVirtual() == null) {
                String columnTitle = column.getTitles();
                //logger.info("Column header - name: " + column.getName() + ", title: " + columnTitle);
                headersBuffer.add(columnTitle);
                orderOfColumns.add(column);
            }
        }
        String[] headers = headersBuffer.toArray(new String[0]);
        //logger.info("Final CSV headers: " + String.join(", ", headers));
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
        try (OutputStreamWriter myWriter = new OutputStreamWriter(new FileOutputStream(file, appendOrNot), StandardCharsets.UTF_8)) {
            myWriter.write(something.toString());

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
