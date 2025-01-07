package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.TableSchema;
import com.miklosova.rdftocsvw.output_processor.CSVConsolidator;
import com.miklosova.rdftocsvw.output_processor.MetadataConsolidator;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.Main;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.miklosova.rdftocsvw.support.ConnectionChecker.isUrl;
import static org.eclipse.rdf4j.model.util.Values.iri;

public class StreamingMetadataCreator extends MetadataCreator {

    protected String fileNameToRead;
    protected TableSchema tableSchema;
    int fileNumber = 0;
    int lineCounter = 0;

    private static Map<String, Value> mapOfBlanks = new HashMap<>();
    private static int blankNodeCounter = 0;

    public StreamingMetadataCreator() {

        String fileNameFromConfig = ConfigurationManager.getVariableFromConfigFile("input.inputFileName");
        URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
        File file;
        try {
            file = new File(location.toURI().getPath());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String jarDirectory = file.getParentFile().getName();

        this.fileNameToRead = isUrl(fileNameFromConfig) ? (iri(fileNameFromConfig).getLocalName()) : (jarDirectory.equalsIgnoreCase("target")) ? fileNameFromConfig : fileNameFromConfig;
                //"../"
    }



    public static String[] parseTripleFromLine(String line) throws InvalidObjectException {
        // Updated regex to handle URIs, literals, and blank nodes
        String regex = "^(<[^>]*>|_:\\w+)\\s+<([^>]*)>\\s+(\".*?\"(?:@\\w+|\\^\\^<[^>]+>)?|<[^>]*>|_:\\w+)\\s+\\.$";
        Pattern pattern = Pattern.compile(regex);

            Matcher matcher = pattern.matcher(line);

            if (matcher.matches()) {
                String subject = matcher.group(1);
                String predicate = matcher.group(2);
                String object = matcher.group(3);

                // Save into String array
                String[] triple = {subject, predicate, object};
/*
                // Print the results
                System.out.println("Subject: " + triple[0]);
                System.out.println("Predicate: " + triple[1]);
                System.out.println("Object: " + triple[2]);
                System.out.println("--------------------------");

 */
                return triple;
            } else {
                System.out.println("Invalid N-Triple line: " + line);
                throw new InvalidObjectException("Invalid N-Triple line: " + line);
            }

    }

    public static Statement replaceBlankNodesWithIRI(Statement st, String line){
        Resource subject;
        Value object;
        ValueFactory vf = SimpleValueFactory.getInstance();
        String[] triple = {"","",""};
        if(st.getSubject().isBNode() || st.getObject().isBNode()){
            try {
                triple = parseTripleFromLine(line);
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }
        }

        if(st.getObject().isBNode()){
            //System.out.println("Object is BNode");
            if (mapOfBlanks.get(triple[2]) != null) {
                object = mapOfBlanks.get(triple[2]);
                //System.out.println("Object is BNode was already in map " + object);
            } else {
                IRI v = vf.createIRI("https://blank_Nodes_IRI.org/" + blankNodeCounter);

                mapOfBlanks.put(triple[2], v);
                object = mapOfBlanks.get(triple[2]);
                blankNodeCounter++;
                //System.out.println("Object is BNode added new Bnode iri to map " + v.stringValue() + " object is " + object.stringValue());
            }
        } else {
            object = st.getObject();
        }
        if(st.getSubject().isBNode()){
            //System.out.println("Subject is BNode");
            if (mapOfBlanks.get(triple[0]) != null) {
                subject = (IRI) mapOfBlanks.get(triple[0]);
                //System.out.println("Subject is BNode was already in map " + subject);
            } else {
                IRI v = vf.createIRI("https://blank_Nodes_IRI.org/" + blankNodeCounter);
                blankNodeCounter++;
                mapOfBlanks.put(triple[0], v);
                subject = (IRI) mapOfBlanks.get(triple[0]);
                //System.out.println("Subject is BNode added new Bnode iri to map " + v.stringValue() + " subject is " + subject.stringValue());
            }
        } else {
            subject = st.getSubject();
        }

        return vf.createStatement(subject, st.getPredicate(), object);
    }

    static Statement processNTripleLine(String line) {
        AtomicReference<Statement> statementRef = new AtomicReference<>();
        try {
            // Create an RDFParser instance
            RDFParser parser = Rio.createParser(RDFFormat.NTRIPLES);

            // Set a custom RDFHandler to process the parsed statements
            parser.setRDFHandler(new AbstractRDFHandler() {
                @Override
                public void handleStatement(Statement st) {
                    // Custom processing logic for each statement
                    //System.out.println("Parsed Triple: " + st);
                    statementRef.set(st);
                }
            });

            // Parse the single line
            parser.parse(new StringReader(line), "");
        } catch (Exception e) {
            System.err.println("Error parsing line: " + line);
            e.printStackTrace();
        }
        return statementRef.get();
    }

    public void repairMetadataAndMakeItJsonld(Metadata metadata){
        metadata = makeMetadataNameUnique(metadata);
        if (ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.TABLES).equalsIgnoreCase(ConfigurationManager.ONE_TABLE)) {

            metadata = consolidateMetadataAndCSVs(metadata);
        }
        metadata.jsonldMetadata();
    }

    private Metadata makeMetadataNameUnique(Metadata metadata) {
        ArrayList<Column> allColumns = new ArrayList<>();
        metadata.getTables().forEach(t -> allColumns.addAll(t.getTableSchema().getColumns()));
        TableSchema.makeColumnNamesUnique(allColumns);
        return metadata;
    }

    void createFirstColumn() {
        Column firstColumn = new Column();

        firstColumn.setName("Subject");
        firstColumn.setValueUrl("{+Subject}");

        firstColumn.setSuppressOutput(true);
        firstColumn.setTitles("Subject");

        tableSchema.getColumns().add(firstColumn);
    }

    public void processLine(String line) {
        Statement statement = processNTripleLine(line);
        Statement statementWithIRIs = replaceBlankNodesWithIRI(statement, line);
        Triple triple = new Triple((IRI) statementWithIRIs.getSubject(), statementWithIRIs.getPredicate(), statementWithIRIs.getObject());
        //System.out.println("processed triple before adding to metadata : " + statementWithIRIs.getSubject()+" "+statementWithIRIs.getPredicate()+" "+statementWithIRIs.getObject());
        addMetadataToTableSchema(triple);
        lineCounter++;
    }
    // Process line into triple that certainly does not contain BNodes
    public static Triple processLineIntoTripleIRIsOnly(String line)
    {
        Statement statement = processNTripleLine(line);
        return new Triple((IRI) statement.getSubject(), statement.getPredicate(), statement.getObject());
    }

    public static Triple processLineIntoTriple(String line)
    {
        Statement statement = processNTripleLine(line);
        Statement statementWithIRIs = replaceBlankNodesWithIRI(statement, line);
        return new Triple((IRI) statementWithIRIs.getSubject(), statementWithIRIs.getPredicate(), statementWithIRIs.getObject());
    }
    void addMetadataToTableSchema(Triple triple) {
        Column newColumn = new Column();
        newColumn.createLangFromLiteral(triple.object);
        newColumn.createNameFromIRI(triple.predicate);
        newColumn.setPropertyUrl(triple.predicate.stringValue());
        if (triple.object.isIRI()) {
            newColumn.setValueUrl(((IRI) triple.object).getNamespace() + "{+" + newColumn.getName() + "}");
            System.out.println("valueUrl= "+ newColumn.getValueUrl());
        } else if (triple.object.isBNode()) {
            newColumn.setValueUrl("{+" + newColumn.getName() + "}");
        }
        newColumn.createDatatypeFromValue(triple.object);
        newColumn.setAboutUrl(triple.subject.getNamespace() + "{+Subject}");
        newColumn.setTitles(newColumn.createTitles(triple.predicate, triple.object));
        if (!thereIsMatchingColumnAlready(newColumn, triple)) {
            tableSchema.getColumns().add(newColumn);
        }
    }

    protected Metadata consolidateMetadataAndCSVs(Metadata oldmeta) {
        MetadataConsolidator mc = new MetadataConsolidator();
        Metadata consolidatedMetadata = mc.consolidateMetadata(oldmeta);
        CSVConsolidator cc = new CSVConsolidator();
        cc.consolidateCSVs(oldmeta, consolidatedMetadata);
        return consolidatedMetadata;
    }

    boolean thereIsMatchingColumnAlready(Column newColumn, Triple triple) {
        if (tableSchema.getColumns().isEmpty()) {
            return false;
        }
        for (Column col : tableSchema.getColumns()) {
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
        return false;
    }

    @SuppressWarnings("unused")
    String createNewMetadata(int fileNumber) {


        File f = new File(fileNameToRead);
        String newCSVname = f.getName() + fileNumber + ".csv";
        Table newTable = new Table(newCSVname);


        metadata.getTables().add(newTable);
        tableSchema = new TableSchema();
        tableSchema.setPrimaryKey("Subject");
        createFirstColumn();
        newTable.setTableSchema(tableSchema);
        return newCSVname;
    }
}
