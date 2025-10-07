package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.TableSchema;
import com.miklosova.rdftocsvw.output_processor.FileWrite;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Streaming n triples metadata creator.
 */
public class StreamingNTriplesMetadataCreator extends StreamingMetadataCreator implements IMetadataCreator {
    private static final Logger logger = Logger.getLogger(StreamingNTriplesMetadataCreator.class.getName());

    /**
     * The Map of known subjects. <CSV file url, List of Subject IRIs>: the list of subjects that are used as IDs in a given csv file
     */

    Map<String, List<IRI>> mapOfKnownSubjects;
    /**
     * The Map of known predicates. <CSV file url, List of Predicate IRIs>: the list of predicates that are used as IDs in a given csv file
     */

    Map<String, List<IRI>> mapOfKnownPredicates;
    /**
     * The Table schema by files. Table Schema objects mapped to their CSV file urls
     */
    Map<String, TableSchema> tableSchemaByFiles;

    /**
     * The Unified by subject. Is the triple being unified by Subject or Predicate (if it's unified to previously known data)?
     */
    boolean unifiedBySubject = false;

    /**
     * The Current csv name. The url for the csv file name that the triple has been unified to
     */

    String currentCSVName = null;

    /**
     * Instantiates a new Streaming n triples metadata creator.
     */
    public StreamingNTriplesMetadataCreator() {
        super();
        tableSchemaByFiles = new HashMap<>();
        mapOfKnownPredicates = new HashMap<>();
        mapOfKnownSubjects = new HashMap<>();
        this.metadata = new Metadata();
    }

    @Override
    public Metadata addMetadata(PrefinishedOutput<?> info) {

        if (ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.STREAMING_CONTINUOUS).equalsIgnoreCase("true")) {
            readInputStream();
        } else {
            readFileWithStreaming();
        }

        repairMetadataAndMakeItJsonld(metadata);

        return metadata;
    }

    private void readFileWithStreaming() {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileNameToRead))) {
            String line;
            // Read file line by line
            while ((line = reader.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "There was an exception while trying to read file with method Streaming.");
        }
    }

    private void readInputStream() {
        Scanner scanner = new Scanner(System.in);
        String inputLine;
        String endingString = "END";

        // Loop until the termination line is encountered
        while (scanner.hasNextLine()) {
            inputLine = scanner.nextLine();
            if (endingString.equals(inputLine)) {
                break;  // Exit the loop when the termination line is entered
            } else {
                processLine(inputLine);
            }
        }
        scanner.close();
    }

    @Override
    public void processLine(String line) {
        currentCSVName = null;
        unifiedBySubject = false;
        tableSchema = null;

        // Parse the single line
        Statement statement = processNTripleLine(line);
        Statement statementWithIRIs = replaceBlankNodesWithIRI(statement, line);
        Triple triple = new Triple((IRI) statementWithIRIs.getSubject(), statementWithIRIs.getPredicate(), statementWithIRIs.getObject());
        addMetadataToTableSchema(triple);
        try {
            writeTripleToCSV(currentCSVName, triple);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    void addMetadataToTableSchema(Triple triple) {
        Column newColumn = new Column();
        newColumn.createLangFromLiteral(triple.object);
        newColumn.createNameFromIRI(triple.predicate);
        newColumn.setPropertyUrl(triple.predicate.stringValue());
        newColumn.setValueUrl("{+" + newColumn.getName() + "}");
        newColumn.createDatatypeFromValue(triple.object);
        newColumn.setAboutUrl("{+Subject}");
        newColumn.setTitles(newColumn.createTitles(triple.predicate, triple.object));
        currentCSVName = getCSVNameIfSubjectOrPredicateKnown(triple.getSubject(), triple.getPredicate());
        if (blankNodeRegisteredToConfig) {
            if (metadata.getTables().stream().anyMatch(table -> table.getUrl().equalsIgnoreCase(currentCSVName))) {
                metadata.getTables().stream().filter(table -> table.getUrl().equalsIgnoreCase(currentCSVName)).findAny().get().addTransformations();
            }
        }
        // Find the tableSchema that describes either Subject or Predicate in the triple
        tableSchema = getTableSchemaOfMatchingMetadata(triple);
        // There is no matching column found in any existing metadata -> Add the column
        if (!thereIsMatchingColumnAlready(newColumn, triple, tableSchema)) {

            tableSchema.getColumns().add(newColumn);

            try {
                rewriteTheHeadersInCSV(currentCSVName, newColumn.getTitles());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void rewriteTheHeadersInCSV(String filePath, String titles) throws FileNotFoundException {
        File file = new File(filePath);
        List<String[]> lines = new ArrayList<>();
        // Read the file and process it line by line
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            boolean isFirstLine = true;
            while ((line = reader.readNext()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    String[] headerWithNewColumnTitle = Arrays.copyOf(line, line.length + 1);

                    // Add the new element at the last position
                    headerWithNewColumnTitle[line.length] = titles;
                    lines.add(headerWithNewColumnTitle);
                } else {
                    String[] lineWithBlankFinalColumn = Arrays.copyOf(line, line.length + 1);

                    // Add the new element at the last position
                    lineWithBlankFinalColumn[line.length] = "";
                    lines.add(lineWithBlankFinalColumn);
                }

            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
        FileWrite.writeLinesToCSVFile(file, lines, false);
    }

    private String getCSVNameIfSubjectOrPredicateKnown(IRI subject, IRI predicate) {

        for (Map.Entry<String, List<IRI>> entry : mapOfKnownSubjects.entrySet()) {
            if (entry.getValue().contains(subject)) {

                List<IRI> knownPredicates = mapOfKnownPredicates.get(entry.getKey());
                knownPredicates.add(predicate);
                return entry.getKey();
            }
        }

        for (Map.Entry<String, List<IRI>> entry : mapOfKnownPredicates.entrySet()) {
            if (entry.getValue().contains(predicate)) {
                List<IRI> knownSubjects = mapOfKnownSubjects.get(entry.getKey());
                knownSubjects.add(subject);
                return entry.getKey();
            }
        }
        // Neither subject nor predicate are known, create new CSVName and add new table to metadata;
        File f = new File(fileNameToRead);
        String newCSVname = f.getName() + fileNumber + ".csv";
        String previousFiles = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES);
        String allFilesUpToNow = (previousFiles != null && previousFiles.isEmpty()) ? newCSVname : previousFiles + "," + newCSVname;
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES, allFilesUpToNow);
        fileNumber++;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(newCSVname))) {
            writer.write("Subject");
            writer.newLine();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return newCSVname;
    }

    private TableSchema getTableSchemaOfMatchingMetadata(Triple triple) {
        for (Map.Entry<String, List<IRI>> entry : mapOfKnownSubjects.entrySet()) {
            if (entry.getValue().contains(triple.getSubject())) {
                return tableSchemaByFiles.get(entry.getKey());
            }
        }
        for (Map.Entry<String, List<IRI>> entry : mapOfKnownPredicates.entrySet()) {
            if (entry.getValue().contains(triple.getPredicate())) {
                return tableSchemaByFiles.get(entry.getKey());
            }
        }
        // Neither subject nor predicate were found -> create new table and new tableSchema
        return createNewTableSchema(triple);
    }

    private TableSchema createNewTableSchema(Triple triple) {
        ArrayList<IRI> knownPredicates = new ArrayList<>();
        knownPredicates.add(triple.getPredicate());
        ArrayList<IRI> knownSubjects = new ArrayList<>();
        knownSubjects.add(triple.getSubject());
        mapOfKnownPredicates.put(currentCSVName, knownPredicates);
        mapOfKnownSubjects.put(currentCSVName, knownSubjects);

        // Create new Table and tableSchema inside it
        Table newTable = new Table(currentCSVName);
        metadata.getTables().add(newTable);
        tableSchema = new TableSchema();
        tableSchema.setPrimaryKey("Subject");
        tableSchemaByFiles.put(currentCSVName, tableSchema);
        createFirstColumn();
        newTable.setTableSchema(tableSchema);
        return tableSchema;
    }

    /**
     * There is matching column already boolean.
     *
     * @param newColumn   the new column
     * @param triple      the triple
     * @param tableSchema the table schema that contains the new column
     * @return the boolean
     */
    boolean thereIsMatchingColumnAlready(Column newColumn, Triple triple, TableSchema tableSchema) {

        if (tableSchema.getColumns().isEmpty()) {
            return false;
        }
        for (Column col : tableSchema.getColumns()) {
            if (!col.getName().equalsIgnoreCase("subject")) {
                if (!col.getName().equalsIgnoreCase(newColumn.getName())) {
                    continue;
                }

                if (!col.getTitles().equalsIgnoreCase(newColumn.getTitles())) {

                    continue;
                }
                if (!col.getPropertyUrl().equalsIgnoreCase(newColumn.getPropertyUrl())) {
                    continue;
                }
                if (col.getLang() != null && newColumn.getLang() != null && !col.getLang().equalsIgnoreCase(newColumn.getLang())) {
                    continue;
                }
                if (col.getDatatype() != null && newColumn.getDatatype() != null && !col.getDatatype().equalsIgnoreCase(newColumn.getDatatype())) {
                    continue;
                }
                if (!col.getAboutUrl().equalsIgnoreCase(newColumn.getAboutUrl())
                        && (col.getAboutUrl().indexOf(triple.getSubject().getNamespace()) != 0 || col.getAboutUrl().length() != newColumn.getAboutUrl().length())) {
                    // Adjust the metadata so that they are general as the namespaces are not matching

                    col.setAboutUrl("{+Subject}");

                }
                if (col.getValueUrl() != null && newColumn.getValueUrl() != null && !col.getValueUrl().equalsIgnoreCase(newColumn.getValueUrl()) && (col.getValueUrl().indexOf(triple.getSubject().getNamespace()) != 0 || col.getValueUrl().length() != newColumn.getValueUrl().length())) {
                    // Adjust the metadata so that they are general as the namespaces are not matching
                    col.setValueUrl("{+" + col.getName() + "}");
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Write triple to CSV.
     *
     * @param filePath the file path
     * @param triple   the triple
     * @throws IOException the io exception
     */
    public void writeTripleToCSV(String filePath, Triple triple) throws IOException {
        List<String[]> rowDataVariationsForSubject = new ArrayList<>();
        boolean isNeedForAddingDataVariations = false;
        int indexOfDataVariationColumn = -1;
        File file = new File(filePath);
        List<String[]> lines = new ArrayList<>();
        boolean isModified = false;

        unifiedBySubject = isThereTheSameSubject(triple.getSubject());

        // Read the file and process it line by line
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            boolean isFirstLine = true;
            while ((line = reader.readNext()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                }

                // Add the Object into the correct column in the line as there is already a line with this subject
                else if (unifiedBySubject && line[0].equalsIgnoreCase(triple.subject.stringValue())) {
                    // Add this line as a data variation to the data variation list

                    // Split the line into parts by commas


                    int indexOfChangeColumn = getIndexOfCurrentPredicate(triple.getPredicate(), triple.getObject());

                    // Insert the new value between two commas in the middle (adjust index as needed)
                    if (indexOfChangeColumn >= line.length) {
                        // The subject has been matched but the line does not contain the predicate column yet -> add it to the end of the line
                        // Create a new array with one additional element
                        String[] extendedArray = Arrays.copyOf(line, line.length + 1);

                        // Add the new element at the last position
                        extendedArray[line.length] = triple.object.stringValue();
                        line = extendedArray;
                        isModified = true;
                    } else if (indexOfChangeColumn != -1 && !line[indexOfChangeColumn].equalsIgnoreCase("")) {
                        if (Boolean.parseBoolean(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.FIRST_NORMAL_FORM))) {
                            // There is already a value in the column - add the value and add the separator to metadata
                            // Create a new line with the data variation
                            if (dataLineVariationIsNotPresent(rowDataVariationsForSubject, line, indexOfChangeColumn)) {
                                rowDataVariationsForSubject.add(line);
                            }
                            isNeedForAddingDataVariations = true;
                            indexOfDataVariationColumn = indexOfChangeColumn;
                            // Check whether the object is the same as the value that is already in the data - that would imply that
                            // I am trying to optimize the data, but that would modify it, so just make the same line duplicitly
                            // If those data really came from RDF
                        } else {
                            line[indexOfChangeColumn] = line[indexOfChangeColumn] + "," + triple.getObject().stringValue();
                            tableSchema.getColumns().get(indexOfChangeColumn).setSeparator(",");
                            isModified = true;
                        }

                    } else {
                        // Add new object at the end of the line
                        line[indexOfChangeColumn] = triple.getObject().stringValue();
                        isModified = true;  // Mark that the line has been modified
                    }

                }
                lines.add(line);  // Add the processed line to the list


            }

            if (isNeedForAddingDataVariations) {
                // Add data variations for all the lines that have the same subject in the list
                appendDataVariationsToCSV(file, rowDataVariationsForSubject, indexOfDataVariationColumn, triple.getObject());
            } else if (!isModified && unifiedBySubject) {
                // If the file is still unmodified, we need to add a new predicate column at the end of the row
                lines.add(createLineStringListByMetadata(triple).toArray(new String[0]));
                isModified = true;
            }


            if (!isModified && !unifiedBySubject) {
                // The match has been made by matching Predicate = we must create a new line at the end of the file and add values accordingly
                // We can just append to the file - the lines wont be written again at the end of this writing modification
                List<String[]> newLine = new ArrayList<>();
                newLine.add(createLineStringListByMetadata(triple).toArray(new String[0]));
                FileWrite.writeLinesToCSVFile(file, newLine, true);
            }

        } catch (CsvValidationException e) {
            logger.log(Level.SEVERE, "There was an exception while trying to write CSV file from triple and there was a validation exception.");

        }

        // Write the updated content back to the file
        if (isModified) {
            FileWrite.writeLinesToCSVFile(file, lines, false);
        }
    }

    private boolean dataLineVariationIsNotPresent(List<String[]> rowDataVariationsForSubject, String[] line, int indexOfChangeColumn) {

        if (rowDataVariationsForSubject.isEmpty()) {
            return true;
        }
        for (String[] value : rowDataVariationsForSubject) {
            int same = 0;
            for (int i = 0; i < line.length; i++) {
                if (i != indexOfChangeColumn && line[i].equalsIgnoreCase(value[i])) {
                    same++;
                }
            }
            if (same == line.length - 1) {
                return false;
            }
        }
        return true;
    }

    private void appendDataVariationsToCSV(File file, List<String[]> linesToMakeVariantFor, int indexOfDataVariationColumn, Value object) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(file, true))) {
            // Appends to the file instead of overwriting
            for (String[] line : linesToMakeVariantFor) {
                String[] copiedArray = Arrays.copyOf(line, line.length);
                copiedArray[indexOfDataVariationColumn] = object.stringValue();
                writer.writeNext(copiedArray, false);
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "There was an exception while trying to append data variations to CSV");
        }
    }

    private int getIndexOfCurrentPredicate(IRI predicate, Value object) {
        for (int i = 0; i < tableSchema.getColumns().size(); i++) {
            if (!tableSchema.getColumns().get(i).getTitles().equalsIgnoreCase("Subject") && isSameLanguagePredicate(tableSchema.getColumns().get(i).getPropertyUrl(), tableSchema.getColumns().get(i).getTitles(), predicate, object)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isSameLanguagePredicate(String predicateToCompare, String titlesToCompare, IRI predicate, Value object) {
        if (predicateToCompare.equalsIgnoreCase(predicate.stringValue())) {
            if (titlesToCompare.charAt(titlesToCompare.length() - 1) == ')' && object.isLiteral()) {
                if (((Literal) object).getLanguage().isPresent()) {
                    String languageTagToCompare = titlesToCompare.substring(titlesToCompare.length() - 3, titlesToCompare.length() - 1);
                    String languageTag = ((Literal) object).getLanguage().get();
                    return languageTagToCompare.equalsIgnoreCase(languageTag);
                } else {
                    return false;
                }
            } else return !object.isLiteral() || (object.isLiteral() && ((Literal) object).getLanguage().isEmpty());
        } else {
            return false;
        }

    }

    private boolean isThereTheSameSubject(IRI subject) {

        return mapOfKnownSubjects.get(currentCSVName).contains(subject);
    }

    private List<String> createLineStringListByMetadata(Triple triple) {
        List<String> list = new ArrayList<>();
        list.add(triple.subject.stringValue());

        TableSchema relevantTS = tableSchemaByFiles.get(currentCSVName);
        for (int i = 0; i < relevantTS.getColumns().size(); i++) {
            if (!relevantTS.getColumns().get(i).getTitles().equalsIgnoreCase("Subject") && relevantTS.getColumns().get(i).getPropertyUrl().equalsIgnoreCase(triple.getPredicate().stringValue())) {
                list.add(triple.getObject().stringValue());

            } else if (!relevantTS.getColumns().get(i).getTitles().equalsIgnoreCase("Subject")) {
                list.add("");
            }
        }
        return list;
    }
}
