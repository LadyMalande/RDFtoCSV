package com.miklosova.rdftocsvw.output_processor;


import com.miklosova.rdftocsvw.metadata_creator.Triple;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
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

import static com.miklosova.rdftocsvw.metadata_creator.StreamingMetadataCreator.processLineIntoTriple;
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
     * Instantiates a new Streaming n triples write.
     *
     * @param metadata the metadata are already finished
     * @param fileName the file name to write to
     */
    public StreamingNTriplesWrite(Metadata metadata, String fileName) {
        fileToWriteTo = FileWrite.makeFileByNameAndExtension(fileName, "csv");
        assert fileToWriteTo != null;
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES, fileToWriteTo.toString());
        this.metadata = metadata;
        String fileNameFromConfig = ConfigurationManager.getVariableFromConfigFile("input.inputFileName");
        URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
        File file;
        try {
            file = new File(location.toURI().getPath());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String jarDirectory = file.getParentFile().getName();
        this.fileNameToRead = isUrl(fileNameFromConfig) ? (iri(fileNameFromConfig).getLocalName()) : (jarDirectory.equalsIgnoreCase("target")) ? fileNameFromConfig : "../" + fileNameFromConfig;
        processedSubjects = new HashSet<>();
    }

    /**
     * Write to CSV file using metadata information.
     */
    public void writeToFileByMetadata() {

        writeToTheFile(fileToWriteTo, columnHeaders(), false);

        bufferForCSVOutput = new CSVOutputGrid();
        while (gettingNewSubjects()) {
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
    }

    private Object columnHeaders() {
        StringBuilder sb = new StringBuilder();
        for (Column column : metadata.getTables().get(0).getTableSchema().getColumns()) {
            sb.append(column.getTitles()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("\n");
        return sb.toString();
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
                    // add subject
                    sb.append(subject.stringValue());

                } else if (bufferForCSVOutput.getCsvOutputBuffer().get(subject).containsKey(column.getName()) && bufferForCSVOutput.getCsvOutputBuffer().get(subject).get(column.getName()).size() == 1) {
                    if (bufferForCSVOutput.getCsvOutputBuffer().get(subject).get(column.getName()).get(0).isIRI()) {
                        sb.append(bufferForCSVOutput.getCsvOutputBuffer().get(subject).get(column.getName()).get(0));
                    } else if (bufferForCSVOutput.getCsvOutputBuffer().get(subject).get(column.getName()).get(0).isLiteral()) {
                        sb.append(((Literal) bufferForCSVOutput.getCsvOutputBuffer().get(subject).get(column.getName()).get(0)).getLabel());
                    }
                } else if (bufferForCSVOutput.getCsvOutputBuffer().get(subject).containsKey(column.getName()) && bufferForCSVOutput.getCsvOutputBuffer().get(subject).get(column.getName()).size() > 1) {
                    sb.append("\"");
                    if (bufferForCSVOutput.getCsvOutputBuffer().get(subject).get(column.getName()).get(0).isIRI()) {
                        bufferForCSVOutput.getCsvOutputBuffer().get(subject).get(column.getName()).forEach(value -> sb.append(value).append(","));
                    } else if (bufferForCSVOutput.getCsvOutputBuffer().get(subject).get(column.getName()).get(0).isLiteral()) {
                        bufferForCSVOutput.getCsvOutputBuffer().get(subject).get(column.getName()).forEach(value -> sb.append(((Literal) value).getLabel()).append(","));
                    }

                    sb.deleteCharAt(sb.length() - 1);
                    sb.append("\"");
                }
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("\n");
        }
        return sb.toString();
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
                Triple triple = processLineIntoTriple(line);
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
        Triple triple = processLineIntoTriple(line);
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
