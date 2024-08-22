package com.miklosova.rdftocsvw.convertor;

import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.MetadataService;
import com.miklosova.rdftocsvw.output_processor.FinalizedOutput;
import com.miklosova.rdftocsvw.output_processor.ZipOutputProcessor;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.FileWrite;
import org.apache.log4j.BasicConfigurator;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.jruby.RubyProcess;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

public class RDFtoCSV {
    /**
     * Mandatory, sets the original RDF file to convert.
     */
    private String fileName;
    /**
     * Optional, sets the method for conversion
     */
    private String method;
    /**
     * Optional, sets the base name for outputting CSVs
     */
    private String outputFileName;
    private String tableMethod;
    private String readMethod;
    private String filePathForOutput;
    private String metadataFilename;

    public final String DEFAULT_OUTPUT_FILE_NAME = "tabularDataOutput";

    public final String DEFAULT_METHOD = "splitQuery";
    public final String DEFAULT_TABLE_METHOD = "";

    public final String DEFAULT_READ_METHOD = "rdf4j";

    public RDFtoCSV(String fileName) {
        this.fileName = fileName;
        System.out.println("this.filename" + this.fileName);
        this.metadataFilename = this.fileName + ".csv-metadata.json";
        this.filePathForOutput = this.fileName + "TestOutput";
    }


    Repository db;
    MethodService methodService;
    RepositoryConnection rc;

    public String getOutputFileName() {
        return outputFileName;
    }

    /**
     * Default conversion method, returns zipped file
     */
    public FinalizedOutput<ZipOutputStream> convertToZip() throws IOException {
        this.configure();

        parseInput();

        PrefinishedOutput po = convertData();

        Metadata metadata = createMetadata(po);

        // Enrich metadata with online reachable data - disabled if offline
        // TODO

        // Write data to CSV by the metadata prepared

        writeToCSV(po, metadata);


        return finalizeOutput(po);
        // Finalize the output to .zip

    }

    private void writeToCSV(PrefinishedOutput po, Metadata metadata) {
        String allFiles = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES);
        String[] files = allFiles.split(",");
        try {
            RowsAndKeys rnk = (RowsAndKeys) po.getPrefinishedOutput();
            int i = 0;

            for(RowAndKey rowAndKey : rnk.getRowsAndKeys()){
            /*System.out.println("writeToCSV ");
            rowAndKey.getKeys().forEach(e -> System.out.print(e + ", "));
            System.out.println();
            rowAndKey.getRows().forEach(e -> System.out.print(e.id + ", "));
            System.out.println();
            rowAndKey.getRows().forEach(e -> System.out.print(e.columns.toString() + ", "));
            System.out.println();

             */
                System.out.println("FileWrite for i= " + i + " rowAndKey = " + rowAndKey.getKeys() + " getRows= " + rowAndKey.getRows());
                String newFileName = files[i];
                FileWrite.saveCSVFileFromRows(newFileName, rowAndKey.getRows(), metadata);
                i++;
            }
        }catch(ClassCastException ex){
            RowAndKey rnk = (RowAndKey) po.getPrefinishedOutput();


            String newFileName = files[0];
            System.out.println("ClassCastException FileWrite for newfilename= " + newFileName + " rowAndKey = ");
            FileWrite.saveCSVFileFromRows(newFileName, rnk.getRows(), metadata);
        }
        //System.out.println("rnk size " + rnk.getRowsAndKeys().size());


        db.shutDown();
    }

    private FinalizedOutput<ZipOutputStream> finalizeOutput(PrefinishedOutput po) {
        ZipOutputProcessor zop = new ZipOutputProcessor();
        return zop.processCSVToOutput(po);
    }

    public Metadata createMetadata(PrefinishedOutput po) {
        // Convert intermediate data into basic metadata
        MetadataService ms = new MetadataService();
        return ms.createMetadata(po);
    }

    private PrefinishedOutput convertData() {
        // Convert the table to intermediate data for processing into metadata
        ConversionService cs = new ConversionService();
        return cs.convertByQuery(rc, db);
    }

    public PrefinishedOutput convertData(RepositoryConnection repositoryConnection, Repository repository) {
        // Convert the table to intermediate data for processing into metadata
        ConversionService cs = new ConversionService();
        return cs.convertByQuery(repositoryConnection, repository);
    }

    public void parseInput() throws IOException {
        // Parse input
        // Create a new Repository.
        db = new SailRepository(new MemoryStore());
        MethodService methodService = new MethodService();
        rc = methodService.processInput(fileName, readMethod, db);
        assert(rc != null);
    }

    public RepositoryConnection createRepositoryConnection(Repository repository, String filename, String readMethod) throws IOException {
        // Parse input
        // Create a new Repository.
        MethodService methodService = new MethodService();
        RepositoryConnection repositoryConnection = methodService.processInput(filename, readMethod, repository);
        assert(repositoryConnection != null);
        return repositoryConnection;
    }

    public void configure() {
        BasicConfigurator.configure();

        String m = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD);
        method = (m != null) ? m : DEFAULT_METHOD;
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_METHOD, method);

        tableMethod = (tableMethod != null) ? tableMethod : DEFAULT_TABLE_METHOD;
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.TABLE_METHOD, tableMethod);

        readMethod = (readMethod != null) ? readMethod : DEFAULT_READ_METHOD;
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.READ_METHOD, readMethod);

        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME, metadataFilename);

        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_FILENAME, filePathForOutput);
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES, "");
    }
}
