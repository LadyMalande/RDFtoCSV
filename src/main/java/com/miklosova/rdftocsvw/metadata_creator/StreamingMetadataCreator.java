package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.output_processor.CSVConsolidator;
import com.miklosova.rdftocsvw.output_processor.MetadataConsolidator;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.Main;
import com.miklosova.rdftocsvw.support.StreamingSupport;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import java.io.File;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static com.miklosova.rdftocsvw.support.ConnectionChecker.isUrl;
import static org.eclipse.rdf4j.model.util.Values.iri;

public class StreamingMetadataCreator extends MetadataCreator {

    protected String fileNameToRead;
    protected TableSchema tableSchema;
    int fileNumber = 0;
    int lineCounter = 0;

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

        this.fileNameToRead = isUrl(fileNameFromConfig) ? (iri(fileNameFromConfig).getLocalName()) : (jarDirectory.equalsIgnoreCase("target")) ? fileNameFromConfig : "../" + fileNameFromConfig;
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

    void processLine(String line) {
        Triple triple = StreamingSupport.createTripleFromLine(line);
        addMetadataToTableSchema(triple);
        lineCounter++;
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
        }
    }

    protected Metadata consolidateMetadataAndCSVs(Metadata oldmeta) {
        MetadataConsolidator mc = new MetadataConsolidator();
        Metadata consolidatedMetadata = mc.consolidateMetadata(oldmeta);
        CSVConsolidator cc = new CSVConsolidator();
        cc.consolidateCSVs(oldmeta);
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
