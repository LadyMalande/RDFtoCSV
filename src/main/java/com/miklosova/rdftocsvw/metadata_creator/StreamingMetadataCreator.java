package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.convertor.PrefinishedOutput;
import com.miklosova.rdftocsvw.convertor.RowsAndKeys;
import com.miklosova.rdftocsvw.output_processor.CSVConsolidator;
import com.miklosova.rdftocsvw.output_processor.MetadataConsolidator;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.Main;
import com.miklosova.rdftocsvw.support.StreamingSupport;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import java.io.File;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.miklosova.rdftocsvw.support.ConnectionChecker.isUrl;
import static org.eclipse.rdf4j.model.util.Values.iri;

public class StreamingMetadataCreator extends MetadataCreator {

    protected String fileNameToRead;
    protected TableSchema tableSchema;
    int fileNumber = 0;
    int lineCounter = 0;

    public StreamingMetadataCreator(PrefinishedOutput<RowsAndKeys> data) {

        String fileNameFromConfig = ConfigurationManager.getVariableFromConfigFile("input.inputFileName");
        URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
        File file = null;
        try {
            file = new File(location.toURI().getPath());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String jarDirectory = file.getParentFile().getName();

        this.fileNameToRead = isUrl(fileNameFromConfig) ? (iri(fileNameFromConfig).getLocalName()) : (jarDirectory.equalsIgnoreCase("target")) ? fileNameFromConfig : "../" + fileNameFromConfig;
        ////System.out.println("fileNameToRead = " + fileNameToRead);
    }


    void createFirstColumn() {
        Column firstColumn = new Column();

        firstColumn.setName("Subject");
        firstColumn.setValueUrl("{+Subject}");

        firstColumn.setSuppressOutput(true);
        firstColumn.setTitles("Subject");

        tableSchema.getColumns().add(firstColumn);
    }

    void processLine(String line) {
        Triple triple = StreamingSupport.createTripleFromLine(line);
        addMetadataToTableSchema(triple);
        lineCounter++;
        if (lineCounter % 100 == 0) {
            //System.out.println("Processed " + lineCounter + " lines on input processing to metadata.");
        }
    }

    void addMetadataToTableSchema(Triple triple) {
        Column newColumn = new Column();
        newColumn.createLangFromLiteral(triple.object);
        newColumn.createNameFromIRI(triple.predicate);
        newColumn.setPropertyUrl(triple.predicate.stringValue());
        if (triple.object.isIRI()) {
            newColumn.setValueUrl(((IRI) triple.object).getNamespace() + "{+" + newColumn.getName() + "}");
        } else if (triple.object.isBNode()) {
            newColumn.setValueUrl("{+" + newColumn.getName() + "}");
        }
        newColumn.createDatatypeFromValue(triple.object);
        newColumn.setAboutUrl(triple.subject.getNamespace() + "{+Subject}");
        newColumn.setTitles(newColumn.createTitles(triple.predicate, triple.object));
        if (!thereIsMatchingColumnAlready(newColumn, triple)) {
            tableSchema.getColumns().add(newColumn);
            //System.out.println("Adding new column");
        }
    }

    protected Metadata consolidateMetadataAndCSVs(Metadata oldmeta){
        Metadata oldMetadata = oldmeta;
        MetadataConsolidator mc = new MetadataConsolidator();
        Metadata consolidatedMetadata = mc.consolidateMetadata(oldMetadata);
        CSVConsolidator cc = new CSVConsolidator();
        cc.consolidateCSVs(oldMetadata);
        return consolidatedMetadata;
    }

    boolean thereIsMatchingColumnAlready(Column newColumn, Triple triple) {
        int numberOfNotMatching = 0;
        if (tableSchema.getColumns().isEmpty()) {
            return false;
        }
        for (Column col : tableSchema.getColumns()) {
            ////System.out.println("numberOfNotMatching in the loop = " + numberOfNotMatching);
            if (!col.getName().equalsIgnoreCase(newColumn.getName())) {
                ////System.out.println("Name does not equal: " + col.getName() + " x " + newColumn.getName());
                numberOfNotMatching++;
                continue;
            }
            if (!col.getTitles().equalsIgnoreCase(newColumn.getTitles())) {
                ////System.out.println("Titles does not equal: " + col.getTitles() + " x " + newColumn.getTitles());
                numberOfNotMatching++;
                continue;
            }
            if (!col.getPropertyUrl().equalsIgnoreCase(newColumn.getPropertyUrl())) {
                ////System.out.println("PropertyUrl does not equal: " + col.getPropertyUrl() + " x " + newColumn.getPropertyUrl());
                numberOfNotMatching++;
                continue;
            }
            if (col.getLang() != null && newColumn.getLang() != null && !col.getLang().equalsIgnoreCase(newColumn.getLang())) {
                ////System.out.println("Lang does not equal: " + col.getLang() + " x " + newColumn.getLang());
                numberOfNotMatching++;
                continue;
            }
            if (col.getDatatype() != null && newColumn.getDatatype() != null && !col.getDatatype().equalsIgnoreCase(newColumn.getDatatype())) {
                ////System.out.println("Datatype does not equal: " + col.getDatatype() + " x " + newColumn.getDatatype());
                numberOfNotMatching++;
                continue;
            }
            if (!col.getAboutUrl().equalsIgnoreCase(newColumn.getAboutUrl())
                    && (col.getAboutUrl().indexOf(triple.getSubject().getNamespace()) != 0 || col.getAboutUrl().length() != newColumn.getAboutUrl().length())) {
                // Adjust the metadata so that they are general as the namespaces are not matching

                ////System.out.println("AboutUrl does not equal: " + col.getAboutUrl() + " x " + newColumn.getAboutUrl());
                col.setAboutUrl("{+Subject}");

            }
            if (col.getValueUrl() != null && newColumn.getValueUrl() != null && !col.getValueUrl().equalsIgnoreCase(newColumn.getValueUrl()) && (col.getValueUrl().indexOf(triple.getSubject().getNamespace()) != 0 || col.getValueUrl().length() != newColumn.getValueUrl().length())) {
                // Adjust the metadata so that they are general as the namespaces are not matching

                ////System.out.println("ValueUrl does not equal: " + col.getValueUrl() + " x " + newColumn.getValueUrl());
                col.setValueUrl("{+" + col.getName() + "}");
            }
            return true;
        }
        ////System.out.println("numberOfNotMatching != tableSchema.getColumns().size() " + numberOfNotMatching + " != " + tableSchema.getColumns().size() + "\n");
        return false;
    }

    static Statement processNTripleLine(String line) {
        AtomicReference<Statement> statementRef = new AtomicReference<>();
        try {
            Statement statement = null;
            // Create an RDFParser instance
            RDFParser parser = Rio.createParser(RDFFormat.NTRIPLES);

            // Set a custom RDFHandler to process the parsed statements
            parser.setRDFHandler(new AbstractRDFHandler() {
                @Override
                public void handleStatement(Statement st) {
                    // Custom processing logic for each statement
                    System.out.println("Parsed Triple: " + st);
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
