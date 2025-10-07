package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Column;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Table;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.TableSchema;
import com.miklosova.rdftocsvw.output_processor.CSVConsolidator;
import com.miklosova.rdftocsvw.output_processor.MetadataConsolidator;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import java.io.File;
import java.io.InvalidObjectException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.miklosova.rdftocsvw.support.ConnectionChecker.isUrl;
import static org.eclipse.rdf4j.model.util.Values.iri;

/**
 * The Streaming metadata creator. It reads the triple from input and immediately it updates data object and writes the
 * triples data into a CSV.
 * Because of this approach, it is very slow for any data than small.
 * This class contains shared method for BigFileStreaming and Streaming conversion methods.
 */
public class StreamingMetadataCreator extends MetadataCreator {
    private static final Logger logger = Logger.getLogger(StreamingMetadataCreator.class.getName());
    private static final Map<String, Value> mapOfBlanks = new HashMap<>();
    /**
     * The Blank node registered to config.
     */
    static boolean blankNodeRegisteredToConfig;
    private static int blankNodeCounter = 0;
    /**
     * The File name to read.
     */
    protected String fileNameToRead;
    /**
     * The Table schema.
     */
    protected TableSchema tableSchema;
    /**
     * The File number.
     */
    int fileNumber = 0;
    /**
     * The Line counter.
     */
    int lineCounter = 0;

    /**
     * Instantiates a new Streaming metadata creator.
     */
    public StreamingMetadataCreator() {

        String fileNameFromConfig = ConfigurationManager.getVariableFromConfigFile("input.inputFileName");
        /*
        //URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
        //File file = new File("temp.csv");
        try {
            file = new File(location.toURI().getPath());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String jarDirectory = file.getParentFile().getName();
*/
        this.fileNameToRead = isUrl(fileNameFromConfig) ? (iri(fileNameFromConfig).getLocalName()) : fileNameFromConfig;
        //"../"
    }


    /**
     * Parse triple from line string [ ].
     *
     * @param line the line
     * @return the string [ ]
     * @throws InvalidObjectException the invalid object exception
     */
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

            return new String[]{subject, predicate, object};
        } else {
            throw new InvalidObjectException("Invalid N-Triple line: " + line);
        }

    }

    /**
     * Replace blank nodes with iri statement.
     *
     * @param st   the st
     * @param line the line
     * @return the statement
     */
    public static Statement replaceBlankNodesWithIRI(Statement st, String line) {
        Resource subject;
        Value object;
        ValueFactory vf = SimpleValueFactory.getInstance();
        String[] triple = {"", "", ""};
        if (st.getSubject().isBNode() || st.getObject().isBNode()) {
            try {
                triple = parseTripleFromLine(line);
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }
        }

        if (st.getObject().isBNode()) {
            if (mapOfBlanks.get(triple[2]) != null) {
                object = mapOfBlanks.get(triple[2]);
            } else {
                if (!blankNodeRegisteredToConfig) {
                    blankNodeRegisteredToConfig = true;
                    ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_BLANK_NODES, String.valueOf(true));
                }
                IRI v = vf.createIRI("https://blank_Nodes_IRI.org/" + blankNodeCounter);

                mapOfBlanks.put(triple[2], v);
                object = mapOfBlanks.get(triple[2]);
                blankNodeCounter++;
            }
        } else {
            object = st.getObject();
        }
        if (st.getSubject().isBNode()) {
            if (mapOfBlanks.get(triple[0]) != null) {
                subject = (IRI) mapOfBlanks.get(triple[0]);
            } else {
                if (!blankNodeRegisteredToConfig) {
                    ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_BLANK_NODES, String.valueOf(true));
                    blankNodeRegisteredToConfig = true;
                }
                IRI v = vf.createIRI("https://blank_Nodes_IRI.org/" + blankNodeCounter);
                blankNodeCounter++;
                mapOfBlanks.put(triple[0], v);
                subject = (IRI) mapOfBlanks.get(triple[0]);
            }
        } else {
            subject = st.getSubject();
        }

        return vf.createStatement(subject, st.getPredicate(), object);
    }

    /**
     * Process n triple line statement.
     *
     * @param line the line
     * @return the statement
     */
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
                    statementRef.set(st);
                }
            });

            // Parse the single line
            parser.parse(new StringReader(line), "");
        } catch (Exception e) {
            System.err.println("Error parsing line: " + line);
            logger.log(Level.SEVERE, "There was an exception while processing a line in streaming metadata creator.");
        }
        return statementRef.get();
    }

    /**
     * Process line into triple that certainly does not contain BNodes
     *
     * @param line the line
     * @return the triple
     */
    public static Triple processLineIntoTripleIRIsOnly(String line) {
        Statement statement = processNTripleLine(line);
        return new Triple((IRI) statement.getSubject(), statement.getPredicate(), statement.getObject());
    }

    /**
     * Process line into triple triple.
     *
     * @param line the line
     * @return the triple
     */
    public static Triple processLineIntoTriple(String line) {
        Statement statement = processNTripleLine(line);
        Statement statementWithIRIs = replaceBlankNodesWithIRI(statement, line);
        return new Triple((IRI) statementWithIRIs.getSubject(), statementWithIRIs.getPredicate(), statementWithIRIs.getObject());
    }

    /**
     * Repair metadata and make it jsonld.
     *
     * @param metadata the metadata
     */
    public void repairMetadataAndMakeItJsonld(Metadata metadata) {
        makeMetadataNameUnique(metadata);
        metadata.getTables().forEach(table -> table.getTableSchema().addRowTitles());
        if (ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.TABLES).equalsIgnoreCase(ConfigurationManager.ONE_TABLE)) {

            metadata = consolidateMetadataAndCSVs(metadata);
        }
        metadata.jsonldMetadata();
    }

    private void makeMetadataNameUnique(Metadata metadata) {
        ArrayList<Column> allColumns = new ArrayList<>();
        metadata.getTables().forEach(t -> allColumns.addAll(t.getTableSchema().getColumns()));
        TableSchema.makeColumnNamesUnique(allColumns);
    }

    /**
     * Create first column.
     */
    void createFirstColumn() {
        Column firstColumn = new Column();

        firstColumn.setName("Subject");
        firstColumn.setValueUrl("{+Subject}");

        firstColumn.setSuppressOutput(true);
        firstColumn.setTitles("Subject");

        tableSchema.getColumns().add(firstColumn);
    }

    /**
     * Process line.
     *
     * @param line the line
     */
    public void processLine(String line) {
        Statement statement = processNTripleLine(line);
        Statement statementWithIRIs = replaceBlankNodesWithIRI(statement, line);
        Triple triple = new Triple((IRI) statementWithIRIs.getSubject(), statementWithIRIs.getPredicate(), statementWithIRIs.getObject());
        addMetadataToTableSchema(triple);
        lineCounter++;
    }

    /**
     * Add metadata to table schema.
     *
     * @param triple the triple
     */
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

    /**
     * Consolidate metadata and cs vs metadata.
     *
     * @param oldmeta the old metadata
     * @return the metadata
     */
    protected Metadata consolidateMetadataAndCSVs(Metadata oldmeta) {
        MetadataConsolidator mc = new MetadataConsolidator();
        Metadata consolidatedMetadata = mc.consolidateMetadata(oldmeta);
        CSVConsolidator cc = new CSVConsolidator();
        cc.consolidateCSVs(oldmeta, consolidatedMetadata);
        return consolidatedMetadata;
    }

    /**
     * There is matching column already in the metadata table.
     *
     * @param newColumn the new column that we are trying to make
     * @param triple    the triple
     * @return the boolean
     */
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
            if (col.getPropertyUrl() != null && newColumn.getPropertyUrl() != null && !col.getPropertyUrl().equalsIgnoreCase(newColumn.getPropertyUrl())) {
                continue;
            }
            if (col.getLang() != null && newColumn.getLang() != null && !col.getLang().equalsIgnoreCase(newColumn.getLang())) {
                continue;
            }
            if (col.getDatatype() != null && newColumn.getDatatype() != null && !col.getDatatype().equalsIgnoreCase(newColumn.getDatatype())) {
                continue;
            }
            if (col.getAboutUrl() != null && newColumn.getAboutUrl() != null && !col.getAboutUrl().equalsIgnoreCase(newColumn.getAboutUrl())
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

    /**
     * Create new metadata string.
     *
     * @return the string
     */
    @SuppressWarnings("unused")
    String createNewMetadata() {


        File f = new File(fileNameToRead);
        String newCSVname = f.getName() + 1 + ".csv";
        Table newTable = new Table(newCSVname);


        metadata.getTables().add(newTable);
        tableSchema = new TableSchema();
        tableSchema.setPrimaryKey("Subject");
        createFirstColumn();
        newTable.setTableSchema(tableSchema);
        return newCSVname;
    }
}
