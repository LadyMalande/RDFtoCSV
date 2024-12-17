package com.miklosova.rdftocsvw.output_processor;


import com.miklosova.rdftocsvw.metadata_creator.Column;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.Triple;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.FileWrite;
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

import static com.miklosova.rdftocsvw.metadata_creator.StreamingMetadataCreator.processLineIntoTriple;
import static com.miklosova.rdftocsvw.metadata_creator.StreamingMetadataCreator.processLineIntoTripleIRIsOnly;
import static com.miklosova.rdftocsvw.support.ConnectionChecker.isUrl;
import static com.miklosova.rdftocsvw.support.FileWrite.writeToTheFile;
import static org.eclipse.rdf4j.model.util.Values.iri;

public class StreamingNTriplesWrite {
    private final String fileNameToRead;
    private final Metadata metadata;

    private int lineIndexOfProcessed = 0;

    private ArrayList<IRI> currentSubjects;
    private final Set<IRI> processedSubjects;
    private CSVOutputGrid bufferForCSVOutput;

    private final File fileToWriteTo;

    public StreamingNTriplesWrite(Metadata metadata, String fileName) {
        fileToWriteTo = FileWrite.makeFileByNameAndExtension(fileName, "csv");
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES,fileToWriteTo.toString());
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
        //this.fileNameToWriteTo = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_FILE_PATH);
    }

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
                    if (i < lineIndexOfProcessed) {
                        System.out.println("DO NOTHING + i=" + i + " lineIndexOfProcessed=" + lineIndexOfProcessed); // do nothing
                    } else {
                        System.out.println("process line + i=" + i + " lineIndexOfProcessed=" + lineIndexOfProcessed); // do nothing
                        processLine(line, bufferForCSVOutput);
                    }
                    i++;
                    //System.out.println(line);  // Process the line (e.g., print it)
                }
            } catch (IOException e) {
                e.printStackTrace();
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
        //System.out.println("Chunk of CSV: \n" + createdChunkOfCSV);
        writeToTheFile(fileToWriteTo, createdChunkOfCSV, true);
    }

    private String parseOutputGridForCSV(CSVOutputGrid bufferForCSVOutput) {
        StringBuilder sb = new StringBuilder();
        System.out.println("parseOutputGridForCSV");
        bufferForCSVOutput.print();
        for (IRI subject : currentSubjects) {
            for (Column column : metadata.getTables().get(0).getTableSchema().getColumns()) {
                if (column.getName().equalsIgnoreCase("Subject")) {
                    // add subject
                    sb.append(subject.stringValue());
                } else if (bufferForCSVOutput.getCsvOutputBuffer().get(subject).containsKey(column.getName()) && bufferForCSVOutput.getCsvOutputBuffer().get(subject).get(column.getName()).isEmpty()) {
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
        currentSubjects = new ArrayList<>();
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
                if (i < lineIndexOfProcessed - 1) {
                    System.out.println("do nothing i < lineIndexOfProcessed - 1    i = " + i);// do nothing
                } else {

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
                        System.out.println("column triple: " + triple.getSubject() + " " + triple.getPredicate() + " " + triple.getObject());
                        initialMap.put(Column.getNameFromIRI(triple.getPredicate(), triple.getObject()), valueList);
                        System.out.println("initialMap: " + Column.getNameFromIRI(triple.getPredicate(), triple.getObject()) + " : " + initialMap.get(Column.getNameFromIRI(triple.getPredicate(), triple.getObject())).get(0));
                        bufferForCSVOutput.csvOutputBuffer.put(triple.getSubject(), initialMap);

                    }
                }
                i++;
                int maximumOfProcessedSubjects = 10;
                if (currentSubjects.size() == maximumOfProcessedSubjects) {
                    lineIndexOfProcessed = i;
                    System.out.println("lineIndexOfProcessed = " + i);
                    return true;
                }
                if (i % 100 == 0) {
                    System.out.println("Processed " + i + " lines in gettingNewSubjects");
                }
            }
            if (!currentSubjects.isEmpty()) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void processLine(String line, CSVOutputGrid grid) {
        Triple triple = processLineIntoTriple(line);
        System.out.println("Processing line: " + line);
        if (currentSubjects.contains(triple.getSubject())) {
            String keyForColumn = Column.getNameFromIRI(triple.getPredicate(), triple.getObject());
            if (grid.getCsvOutputBuffer().get(triple.getSubject()).containsKey(keyForColumn)) {
                grid.getCsvOutputBuffer().get(triple.getSubject()).get(keyForColumn).add(triple.getObject());
                metadata.getTables().get(0).getTableSchema().getColumnByName(keyForColumn).setSeparator(",");
                System.out.println("initialMap: " + keyForColumn + " : " + grid.getCsvOutputBuffer().get(triple.getSubject()).get(keyForColumn).get(1));
            } else {
                ArrayList<Value> values = new ArrayList<>();
                values.add(triple.getObject());
                grid.getCsvOutputBuffer().get(triple.getSubject()).put(keyForColumn, values);
                System.out.println("initialMap: " + keyForColumn + " : " + values.get(0));
            }
        }
    }

}
