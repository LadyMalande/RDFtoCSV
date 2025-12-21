package com.miklosova.rdftocsvw.output_processor;


import com.miklosova.rdftocsvw.metadata_creator.StreamingMetadataCreator;
import com.miklosova.rdftocsvw.metadata_creator.Triple;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.support.AppConfig;

import com.miklosova.rdftocsvw.support.Main;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.miklosova.rdftocsvw.output_processor.FileWrite.writeToTheFile;
import static com.miklosova.rdftocsvw.support.ConnectionChecker.isUrl;
import static org.eclipse.rdf4j.model.util.Values.iri;

/**
 * The class of Streaming methods with N-Triples and writing methods to CSV files.
 */
public class StreamingNTriplesWrite {

    private static final Logger logger = Logger.getLogger(StreamingNTriplesWrite.class.getName());

    private final String fileNameToRead;
    private final Metadata metadata;

    /**
     * Serves for buffering output.
     */
    private int lineIndexOfProcessed = 0;

    private Set<IRI> currentSubjects;
    private final Set<IRI> processedSubjects;
    private CSVOutputGrid bufferForCSVOutput;

    private final File fileToWriteTo;
    
    /**
     * The AppConfig instance.
     */
    private AppConfig config;
    
    /**
     * Helper for processing triples with blank node handling.
     */
    private StreamingMetadataCreator metadataCreator;

    /**
     * Instantiates a new Streaming n triples write.
     *
     * @param metadata the metadata are already finished
     * @param fileName the file name to write to
     * @deprecated Use {@link #StreamingNTriplesWrite(Metadata, String, AppConfig)} instead
     */
    @Deprecated
    public StreamingNTriplesWrite(Metadata metadata, String fileName) {
        this(metadata, fileName, null);
    }

    /**
     * Instantiates a new Streaming n triples write with AppConfig.
     *
     * @param metadata the metadata are already finished
     * @param fileName the file name to write to
     * @param config the application configuration
     */
    public StreamingNTriplesWrite(Metadata metadata, String fileName, AppConfig config) {
        this.config = config;
        fileToWriteTo = FileWrite.makeFileByNameAndExtension(fileName, "csv");
        assert fileToWriteTo != null;
        config.setIntermediateFileNames(fileToWriteTo.toString());
        this.metadata = metadata;
        // Use the resolved input file name (same as StreamingMetadataCreator)
        String fileNameFromConfig = config.getInputFileName();
        this.fileNameToRead = isUrl(fileNameFromConfig) ? (iri(fileNameFromConfig).getLocalName()) :  fileNameFromConfig;
        processedSubjects = new HashSet<>();
        this.metadataCreator = new StreamingMetadataCreator(config);
    }

    /**
     * Write to CSV file using metadata information.
     */
    public void writeToFileByMetadata() {
        com.miklosova.rdftocsvw.support.ProgressLogger.startStage(com.miklosova.rdftocsvw.support.ProgressLogger.Stage.WRITING);

        writeToTheFile(fileToWriteTo, columnHeaders(), false);

        bufferForCSVOutput = new CSVOutputGrid();
        int batchCount = 0;
        while (gettingNewSubjects()) {
            batchCount++;
            com.miklosova.rdftocsvw.support.ProgressLogger.logProgress(
                com.miklosova.rdftocsvw.support.ProgressLogger.Stage.WRITING, 
                Math.min(90, batchCount * 10), 
                String.format("Writing batch %d (subjects: %d)", batchCount, currentSubjects.size())
            );
            
            int i = 1;
            try (BufferedReader reader = new BufferedReader(new FileReader(fileNameToRead))) {
                String line;
                // Read file line by line
                while ((line = reader.readLine()) != null) {
                    // Skip lines until the desired line
                    if (i >= lineIndexOfProcessed) {
                        processLine(line, bufferForCSVOutput);
                    }
                    i++;
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "There was an exception while writing to the CSV file using metadata in Streaming method ");
            }
            bufferForCSVOutput.print();
            writeToOutputFile(bufferForCSVOutput);
            processedSubjects.addAll(currentSubjects);
        }
        
        com.miklosova.rdftocsvw.support.ProgressLogger.completeStage(com.miklosova.rdftocsvw.support.ProgressLogger.Stage.WRITING);
    }

    private Object columnHeaders() {
        StringBuilder sb = new StringBuilder();
        List<Column> columns = metadata.getTables().get(0).getTableSchema().getColumns();
        
        if (columns.isEmpty()) {
            logger.log(Level.WARNING, "No columns found in metadata when generating headers!");
            return "\n";
        }
        
        for (Column column : columns) {
            String title = column.getTitles();
            if (title != null && !title.isEmpty()) {
                sb.append(title).append(",");
            } else {
                logger.log(Level.WARNING, "Column has null or empty title: " + column.getName());
            }
        }
        
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);  // Remove last comma
        }
        sb.append("\n");
        
        String headers = sb.toString();
        logger.log(Level.INFO, "Generated CSV headers: " + headers.trim());
        return headers;
    }

    private void writeToOutputFile(CSVOutputGrid bufferForCSVOutput) {
        String createdChunkOfCSV = parseOutputGridForCSV(bufferForCSVOutput);
        writeToTheFile(fileToWriteTo, createdChunkOfCSV, true);
    }

    private String parseOutputGridForCSV(CSVOutputGrid bufferForCSVOutput) {
        StringBuilder sb = new StringBuilder();
        bufferForCSVOutput.print();
        for (IRI subject : currentSubjects) {
            for (Column column : metadata.getTables().get(0).getTableSchema().getColumns()) {
                if (column.getName().equalsIgnoreCase("Subject")) {
                    // Add subject - check if it needs to be quoted and handle aboutUrl pattern
                    String subjectValue = formatValueByPattern(subject, column.getAboutUrl());
                    sb.append(escapeCsvValue(subjectValue));

                } else if (bufferForCSVOutput.getCsvOutputBuffer().get(subject) != null &&
                           bufferForCSVOutput.getCsvOutputBuffer().get(subject).containsKey(column.getName()) && 
                           bufferForCSVOutput.getCsvOutputBuffer().get(subject).get(column.getName()).size() == 1) {
                    Value value = bufferForCSVOutput.getCsvOutputBuffer().get(subject).get(column.getName()).get(0);
                    if (value.isIRI()) {
                        String formattedValue = formatValueByPattern((IRI) value, column.getValueUrl());
                        sb.append(escapeCsvValue(formattedValue));
                    } else if (value.isLiteral()) {
                        sb.append(escapeCsvValue(((Literal) value).getLabel()));
                    }
                } else if (bufferForCSVOutput.getCsvOutputBuffer().get(subject) != null &&
                           bufferForCSVOutput.getCsvOutputBuffer().get(subject).containsKey(column.getName()) && 
                           bufferForCSVOutput.getCsvOutputBuffer().get(subject).get(column.getName()).size() > 1) {
                    // Multiple values in one cell - join with commas and wrap in quotes
                    StringBuilder multiValue = new StringBuilder();
                    if (bufferForCSVOutput.getCsvOutputBuffer().get(subject).get(column.getName()).get(0).isIRI()) {
                        for (Value value : bufferForCSVOutput.getCsvOutputBuffer().get(subject).get(column.getName())) {
                            String formattedValue = formatValueByPattern((IRI) value, column.getValueUrl());
                            multiValue.append(formattedValue).append(",");
                        }
                    } else if (bufferForCSVOutput.getCsvOutputBuffer().get(subject).get(column.getName()).get(0).isLiteral()) {
                        for (Value value : bufferForCSVOutput.getCsvOutputBuffer().get(subject).get(column.getName())) {
                            multiValue.append(((Literal) value).getLabel()).append(",");
                        }
                    }
                    // Remove trailing comma
                    if (multiValue.length() > 0) {
                        multiValue.deleteCharAt(multiValue.length() - 1);
                    }
                    // Escape the entire multi-value string as a single CSV cell
                    sb.append(escapeCsvValue(multiValue.toString()));
                } else {
                    // Column has no value for this subject - append empty string
                    // (This is critical for CSV alignment!)
                }
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Format an IRI value based on the column's URL pattern (aboutUrl or valueUrl).
     * - If urlPattern is null or starts with "{+", use full IRI
     * - If urlPattern contains "{+" but doesn't start with it (partial pattern like "http://example.org/{+var}"), use local name
     * - Otherwise, use local name
     * 
     * @param iri the IRI to format
     * @param urlPattern the URL pattern from aboutUrl or valueUrl
     * @return the formatted string (either full IRI or local name)
     */
    private String formatValueByPattern(IRI iri, String urlPattern) {
        if (urlPattern == null || urlPattern.startsWith("{+")) {
            // Simple pattern {+Variable} or no pattern - use full IRI
            return iri.stringValue();
        } else if (urlPattern.contains("{+")) {
            // Partial pattern like "https://example.com/{+Variable}" - use local name
            return iri.getLocalName();
        }
        // Default - use local name
        return iri.getLocalName();
    }

    /**
     * Properly escape a CSV value according to RFC 4180.
     * - If value contains comma, quote, or newline, it must be quoted
     * - Internal quotes must be escaped by doubling them
     */
    private String escapeCsvValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        // Quote if contains comma, quote, or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            // Escape internal quotes by doubling them, then wrap in quotes
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private boolean gettingNewSubjects() {
        currentSubjects = new HashSet<>();
        bufferForCSVOutput = new CSVOutputGrid();
        int i = 1;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileNameToRead))) {
            String line;
            // Read file line by line
            Column firstColumn = metadata.getTables().get(0).getTableSchema().getColumns().get(0);

            while ((line = reader.readLine()) != null) {
                Triple triple = metadataCreator.processLineIntoTriple(line);
                if (firstColumn.getValueUrl() == null) {
                    if (triple.getSubject().isBNode()) {
                        firstColumn.setValueUrl("{+Subject}");
                    } else if (triple.getSubject().isIRI()) {
                        firstColumn.setValueUrl(triple.getSubject().getNamespace() + "{+Subject}");
                    }
                }
                // Skip lines until the desired line
                if (i >= lineIndexOfProcessed - 1) {

                    if (!processedSubjects.contains(triple.getSubject()) && !currentSubjects.contains(triple.getSubject())) {
                        if (!firstColumn.getValueUrl().startsWith("{")) {
                            String[] splitValue = firstColumn.getValueUrl().split("\\{");
                            if (triple.getSubject().isIRI() && !((IRI) triple.getObject()).getNamespace().equalsIgnoreCase(splitValue[0])) {
                                firstColumn.setValueUrl("{+Subject}");
                            } else if (triple.getSubject().isBNode()) {
                                firstColumn.setValueUrl("{+Subject}");
                            }
                        }
                        currentSubjects.add(triple.getSubject());
                        HashMap<String, List<Value>> initialMap = new HashMap<>();
                        List<Value> valueList = new ArrayList<>();
                        valueList.add(triple.getObject());
                        initialMap.put(Column.getNameFromIRI(triple.getPredicate(), triple.getObject()), valueList);
                        bufferForCSVOutput.csvOutputBuffer.put(triple.getSubject(), initialMap);

                    }
                }
                i++;
                int maximumOfProcessedSubjects = 10;
                if (currentSubjects.size() == maximumOfProcessedSubjects) {
                    lineIndexOfProcessed = i;
                    return true;
                }
            }
            if (!currentSubjects.isEmpty()) {
                return true;
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "There was an exception while getting new subjects from the file.");
        }
        return false;
    }

    /**
     * Process the incoming line into the CSVOutputGrid Representation
     *
     * @param line Line of triple to process
     * @param grid The temporary representation for the CSV
     */
    private void processLine(String line, CSVOutputGrid grid) {
        Triple triple = metadataCreator.processLineIntoTriple(line);
        if (currentSubjects.contains(triple.getSubject())) {
            String keyForColumn = Column.getNameFromIRI(triple.getPredicate(), triple.getObject());
            if (grid.getCsvOutputBuffer().get(triple.getSubject()).containsKey(keyForColumn)) {
                grid.getCsvOutputBuffer().get(triple.getSubject()).get(keyForColumn).add(triple.getObject());
                metadata.getTables().get(0).getTableSchema().getColumnByName(keyForColumn).setSeparator(",");
            } else {
                ArrayList<Value> values = new ArrayList<>();
                values.add(triple.getObject());
                grid.getCsvOutputBuffer().get(triple.getSubject()).put(keyForColumn, values);
            }
        }
    }

}
