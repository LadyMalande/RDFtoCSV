package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.convertor.RowsAndKeys;
import com.miklosova.rdftocsvw.support.StreamingSupport;
import org.eclipse.rdf4j.model.IRI;
import org.jruby.RubyProcess;

import java.io.*;
import java.util.*;

public class StreamingNTriplesMetadataCreator extends StreamingMetadataCreator implements IMetadataCreator {

    // <CSV file url, List of Subject IRIs>: the list of subjects that are used as IDs in a given csv file
    Map<String, List<IRI>> mapOfKnownSubjects;
    // <CSV file url, List of Predicate IRIs>: the list of predicates that are used as IDs in a given csv file
    Map<String, List<IRI>> mapOfKnownPredicates;
    // Table Schema objects mapped to their CSV file urls
    Map<String, TableSchema> tableSchemaByFiles;

    // is the triple being unified by Subject or Predicate (if its unified to previously known data)?
    boolean unifiedBySubject = false;

    // the url for the csv file name that the triple has been unified to
    String currentCSVName = null;
    public StreamingNTriplesMetadataCreator(PrefinishedOutput<RowsAndKeys> data) {
        super(data);
        tableSchemaByFiles = new HashMap<>();
        mapOfKnownPredicates = new HashMap<>();
        mapOfKnownSubjects = new HashMap<>();
        this.metadata = new Metadata();
    }

    @Override
    public Metadata addMetadata(PrefinishedOutput<?> info) {
        //String newCSVname = createNewMetadata(fileNumber);

        //tableSchemaByFiles.put(newCSVname, tableSchema);


        readFileWithStreaming();
        //readInputStream(System.in);

        metadata.jsonldMetadata();
        return metadata;
    }

    private void readFileWithStreaming() {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileNameToRead))) {
            String line;
            // Read file line by line
            while ((line = reader.readLine()) != null) {
                processLine(line);
                //System.out.println(line);  // Process the line (e.g., print it)
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readInputStream(InputStream inputStream){
        Scanner scanner = new Scanner(System.in);
        String inputLine;

        System.out.println("Enter input lines (type '---END_OF_STREAM---' to stop):");

        // Loop until the termination line is encountered
        while (scanner.hasNextLine()) {
            inputLine = scanner.nextLine();
            if ("---END_OF_STREAM---".equals(inputLine)) {
                break;  // Exit the loop when the termination line is entered
            } else {
                processLine(inputLine);
            }
            System.out.println("You entered: " + inputLine);  // Process the input line
        }

        System.out.println("Stream ended.");
        scanner.close();
    }

    @Override
    void processLine(String line){
        currentCSVName = null;
        unifiedBySubject = false;
        tableSchema = null;
        Triple triple = StreamingSupport.createTripleFromLine(line);
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
        newColumn.setTitles(newColumn.createTitles( triple.predicate,triple.object));
        currentCSVName = getCSVNameIfSubjectOrPredicateKnown(triple.getSubject(), triple.getPredicate());
        // Find the tableSchema that describes either Subject or Predicate in the triple
        tableSchema = getTableSchemaOfMatchingMetadata(triple);

        // There is no matching column found in any existing metadata -> Add the column
        if(!thereIsMatchingColumnAlready(newColumn, triple, tableSchema)){

            tableSchema.getColumns().add(newColumn);
            System.out.println("Adding new column");
            try {
                rewriteTheHeadersInCSV(currentCSVName, newColumn.getTitles());
            } catch (IOException e ) {
                throw new RuntimeException(e);
            }
        }
    }

    private void rewriteTheHeadersInCSV(String filePath, String titles) throws FileNotFoundException {
        File file = new File(filePath);
        List<String> lines = new ArrayList<>();

        // Read the file and process it line by line
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if(isFirstLine){
                    isFirstLine = false;
                    String headerWithNewColumnTitle = line + "," + titles;
                    lines.add(headerWithNewColumnTitle);
                } else{
                    String lineWithBlankFinalColumn = line + ",";
                    lines.add(lineWithBlankFinalColumn);
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Write the updated content back to the file

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String updatedLine : lines) {
                System.out.println("new headers updated line: "+ updatedLine);
                writer.write(updatedLine);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("File updated successfully - new headers. "+file.getAbsolutePath());

    }

    private String getCSVNameIfSubjectOrPredicateKnown(IRI subject, IRI predicate) {

        for(Map.Entry<String, List<IRI>> entry : mapOfKnownSubjects.entrySet()){
            System.out.println("Is the subject there? AlreadyThere = " + entry.getValue().get(0) + " | subject = " + subject.stringValue());
            if(entry.getValue().contains(subject)){

                List<IRI> knownPredicates = mapOfKnownPredicates.get(entry.getKey());
                knownPredicates.add(predicate);
                return entry.getKey();
            }
        }

        for(Map.Entry<String, List<IRI>> entry : mapOfKnownPredicates.entrySet()){
            System.out.println("Is the predicate there? AlreadyThere = " + entry.getValue().get(0) + " | predicate = " + predicate.stringValue());
            if(entry.getValue().contains(predicate)){
                List<IRI> knownSubjects = mapOfKnownSubjects.get(entry.getKey());
                knownSubjects.add(subject);
                return entry.getKey();
            }
        }
        // Neither subject nor predicate are known, create new CSVName and add new table to metadata;
        File f = new File(fileNameToRead);
        String newCSVname = f.getName() + fileNumber + ".csv";
        fileNumber++;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(newCSVname))) {
            writer.write("Subject");
            writer.newLine();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Table newTable = new Table(newCSVname);
        //newTable.setTableSchema(new TableSchema());
        //metadata.getTables().add(newTable);
        return newCSVname;
    }

    private TableSchema getTableSchemaOfMatchingMetadata(Triple triple) {
        for(Map.Entry<String, List<IRI>> entry : mapOfKnownSubjects.entrySet()){
            if(entry.getValue().contains(triple.getSubject())){
                return tableSchemaByFiles.get(entry.getKey());
            }
        }
        for(Map.Entry<String, List<IRI>> entry : mapOfKnownPredicates.entrySet()){
            if(entry.getValue().contains(triple.getPredicate())){
                return tableSchemaByFiles.get(entry.getKey());
            }
        }
        // Neither subject or predicate were found -> create new table and new tableSchema
        return createNewTableSchema(triple);
    }

    private TableSchema createNewTableSchema(Triple triple) {
        //File f = new File(fileNameToRead);
        //String newCSVname = f.getName() + fileNumber + ".csv";
        //fileNumber++;

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

    boolean thereIsMatchingColumnAlready(Column newColumn, Triple triple, TableSchema tableSchema) {

        if(tableSchema.getColumns().isEmpty()){
            return false;
        }
        for(Column col: tableSchema.getColumns()){
            //System.out.println("numberOfNotMatching in the loop = " + numberOfNotMatching);
            if(!col.getName().equalsIgnoreCase(newColumn.getName())){
                //System.out.println("Name does not equal: " + col.getName() + " x " + newColumn.getName());
                continue;
            }
            if(!col.getTitles().equalsIgnoreCase(newColumn.getTitles())){
                //System.out.println("Titles does not equal: " + col.getTitles() + " x " + newColumn.getTitles());
                continue;
            }
            if(!col.getPropertyUrl().equalsIgnoreCase(newColumn.getPropertyUrl())){
                //System.out.println("PropertyUrl does not equal: " + col.getPropertyUrl() + " x " + newColumn.getPropertyUrl());
                continue;
            }
            if(col.getLang() != null && newColumn.getLang() != null && !col.getLang().equalsIgnoreCase(newColumn.getLang())){
                //System.out.println("Lang does not equal: " + col.getLang() + " x " + newColumn.getLang());
                continue;
            }
            if(col.getDatatype() != null && newColumn.getDatatype() != null && !col.getDatatype().equalsIgnoreCase(newColumn.getDatatype())){
                //System.out.println("Datatype does not equal: " + col.getDatatype() + " x " + newColumn.getDatatype());
                continue;
            }
            if(!col.getAboutUrl().equalsIgnoreCase(newColumn.getAboutUrl())
                    && (col.getAboutUrl().indexOf(triple.getSubject().getNamespace()) != 0 || col.getAboutUrl().length() != newColumn.getAboutUrl().length())){
                // Adjust the metadata so that they are general as the namespaces are not matching

                //System.out.println("AboutUrl does not equal: " + col.getAboutUrl() + " x " + newColumn.getAboutUrl());
                col.setAboutUrl("{+Subject}");

            }
            if(col.getValueUrl() != null && newColumn.getValueUrl() != null && !col.getValueUrl().equalsIgnoreCase(newColumn.getValueUrl()) && (col.getValueUrl().indexOf(triple.getSubject().getNamespace()) != 0 || col.getValueUrl().length() != newColumn.getValueUrl().length())){
                // Adjust the metadata so that they are general as the namespaces are not matching

                //System.out.println("ValueUrl does not equal: " + col.getValueUrl() + " x " + newColumn.getValueUrl());
                col.setValueUrl("{+" + col.getName() + "}");
            }
            return true;
        }
        //System.out.println("numberOfNotMatching != tableSchema.getColumns().size() " + numberOfNotMatching + " != " + tableSchema.getColumns().size() + "\n");
        return false;
    }

    public void writeTripleToCSV(String filePath, Triple triple) throws IOException {
        File file = new File(filePath);
        List<String> lines = new ArrayList<>();
        boolean isModified = false;

        unifiedBySubject = isThereTheSameSubject(triple.getSubject());
        System.out.println("writeTripleToCSV.unifiedBySubject = " + unifiedBySubject);

        // Read the file and process it line by line
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if(isFirstLine){
                    isFirstLine = false;
                }

                // Add the Object into the correct column in the line as there is already a line with this subject
                else if (!isModified && unifiedBySubject && line.startsWith(triple.subject.stringValue())) {
                    // Split the line into parts by commas
                    String[] parts = line.split(",", -1);

                    System.out.println(line + " --- parts size " + parts.length);

                    int indexOfChangeColumn = getIndexOfCurrentPredicate(triple.getPredicate());

                    // Insert the new value between two commas in the middle (adjust index as needed)
                    if(parts[indexOfChangeColumn] != ""){
                        // There is already a value in the column - add the value and add the separator to metadata
                        if(parts[indexOfChangeColumn].startsWith("\"")){
                            parts[indexOfChangeColumn] = parts[indexOfChangeColumn].substring(0, parts[indexOfChangeColumn].length()-2) + "," + triple.getObject().stringValue() + "\"";
                        } else{
                            parts[indexOfChangeColumn] = "\"" + parts[indexOfChangeColumn] + "," + triple.getObject().stringValue() + "\"";
                            tableSchema.getColumns().get(indexOfChangeColumn).setSeparator(",");
                        }

                    } else{
                        parts[indexOfChangeColumn] = triple.getObject().stringValue();
                    }


                    // Join the parts back into a single line
                    line = String.join(",", parts);

                    isModified = true;  // Mark that the line has been modified
                }
                lines.add(line);  // Add the processed line to the list
                // If the file is still unmodified, we need to add a new predicate column at the end of the row

            }
            if(!isModified && unifiedBySubject){
                lines.add(createLineInCSVByMetadata(triple));
                System.out.println("!isModified && unifiedBySubject" + createLineInCSVByMetadata(triple));
                isModified = true;
            }
             if(!isModified && !unifiedBySubject){
                // The match has been made by matching Predicate = we must create a new line at the end of the file and add values accordingly
                lines.add(createLineInCSVByMetadata(triple));
                 System.out.println("!isModified && !unifiedBySubject" + createLineInCSVByMetadata(triple));
                isModified = true;
            }
        }

        // Write the updated content back to the file
        if (isModified) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String updatedLine : lines) {
                    System.out.println("Updated line: " + updatedLine);
                    writer.write(updatedLine);
                    writer.newLine();
                }
            }
            System.out.println("File updated successfully.");
        } else {
            System.out.println("No matching line found.");
        }
    }

    private int getIndexOfCurrentPredicate(IRI predicate) {
        for(int i = 0; i < tableSchema.getColumns().size(); i++){
            if(!tableSchema.getColumns().get(i).getTitles().equalsIgnoreCase("Subject") && tableSchema.getColumns().get(i).getPropertyUrl().equalsIgnoreCase(predicate.stringValue())){
                return i;
            }
        }
        return -1;
    }

    private boolean isThereTheSameSubject(IRI subject) {

        return mapOfKnownSubjects.get(currentCSVName).contains(subject);
    }

    private String createLineInCSVByMetadata(Triple triple) {
        StringBuilder sb = new StringBuilder();
            sb.append(triple.subject.stringValue());

            TableSchema relevantTS = tableSchemaByFiles.get(currentCSVName);
            for(int i = 0; i < relevantTS.getColumns().size(); i++){
                System.out.println("relevantTS.getColumns().size() = " + relevantTS.getColumns().size());
                if(!relevantTS.getColumns().get(i).getTitles().equalsIgnoreCase("Subject") && relevantTS.getColumns().get(i).getPropertyUrl().equalsIgnoreCase(triple.getPredicate().stringValue())){
                    sb.append(triple.getObject().stringValue());

                }
                sb.append(",");
            }
            sb.deleteCharAt(sb.length()-1);

            return sb.toString();

    }
}
