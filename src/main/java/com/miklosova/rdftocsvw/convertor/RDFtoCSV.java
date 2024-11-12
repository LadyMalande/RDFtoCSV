package com.miklosova.rdftocsvw.convertor;

import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.Metadata;
import com.miklosova.rdftocsvw.metadata_creator.MetadataService;
import com.miklosova.rdftocsvw.output_processor.FinalizedOutput;
import com.miklosova.rdftocsvw.output_processor.StreamingNTriplesWrite;
import com.miklosova.rdftocsvw.output_processor.ZipOutputProcessor;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.FileWrite;
import com.miklosova.rdftocsvw.support.JsonUtil;
import org.apache.log4j.BasicConfigurator;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import static com.miklosova.rdftocsvw.support.ConnectionChecker.isUrl;
import static org.eclipse.rdf4j.model.util.Values.iri;


public class RDFtoCSV {
    private static final String STATIC_DELIMITER_FOR_CSVS_IN_ONE_STRING = """
                
            -----------ANOTHER CSV TABLE-----------
                
            """;
    public final String DEFAULT_OUTPUT_FILE_NAME = "tabularDataOutput";
    public final String DEFAULT_METHOD = "splitQuery";
    public final String DEFAULT_READ_METHOD = "rdf4j";
    Repository db;
    MethodService methodService;
    RepositoryConnection rc;
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
    public RDFtoCSV(String fileName) {
        this.fileName = isUrl(fileName) ? fileName : "../" + fileName;
        System.out.println("this.filename in RDFtoCSV(String fileName)" + this.fileName);
        this.metadataFilename = this.fileName + ".csv-metadata.json";
        this.filePathForOutput = this.fileName;// + "TestOutput";
        ConfigurationManager.processConfigMap(null);
    }
    public RDFtoCSV(String fileName, Map<String, String> configMap) {

        this.fileName = isUrl(fileName) ? fileName : "../" + fileName;
        System.out.println("this.filename in RDFtoCSV(String fileName, Map<String, String> configMap]" + this.fileName);
        this.metadataFilename = this.fileName + ".csv-metadata.json";
        this.filePathForOutput = this.fileName;// + "TestOutput";
        ConfigurationManager.processConfigMap(configMap);
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    /**
     * Default conversion method, returns zipped file
     */
    public FinalizedOutput<byte[]> convertToZip() throws IOException {
        this.configure();

        parseInput();

        PrefinishedOutput<RowsAndKeys> po = convertData();

        Metadata metadata = createMetadata(po);

        // Enrich metadata with online reachable data - disabled if offline
        // TODO

        // Write data to CSV by the metadata prepared

        writeToCSV(po, metadata);


        return finalizeOutput(po);
        // Finalize the output to .zip

    }

    public String getCSVTableAsString() throws IOException {
        this.configure();

        parseInput();

        PrefinishedOutput<RowsAndKeys> po = convertData();

        Metadata metadata = createMetadata(po);

        // Enrich metadata with online reachable data - disabled if offline
        // TODO

        // Write data to CSV by the metadata prepared

        return writeToString(po, metadata);
    }

    public String getMetadataAsString() throws IOException {
        Metadata metadata = getMetadata();

        return JsonUtil.serializeAndReturnPrettyString(metadata);
    }

    public Metadata getMetadata() throws IOException {
        this.configure();

        parseInput();

        PrefinishedOutput<RowsAndKeys> po = convertData();

        Metadata metadata = createMetadata(po);

        return metadata;
    }

    public FinalizedOutput<byte[]> getCSVTableAsFile() throws IOException {
        String outputString = getCSVTableAsString();
        String fileNameSafe = isUrl(fileName) ? (iri(fileName).getLocalName()) : "../" + fileName;
        File f = FileWrite.makeFileByNameAndExtension(fileNameSafe, "csv");
        FileWrite.writeToTheFile(f, outputString, true);
        // Read the file into a byte array
        assert f != null;
        byte[] fileBytes = Files.readAllBytes(f.toPath());
        return new FinalizedOutput<>(fileBytes);
    }

    public FinalizedOutput<byte[]> getMetadataAsFile() throws IOException {
        Metadata metadata = getMetadata();

        JsonUtil.serializeAndWriteToFile(metadata);
        File f = new File(ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME));
        // Read the file into a byte array
        byte[] fileBytes = Files.readAllBytes(f.toPath());
        return new FinalizedOutput<>(fileBytes);
    }


    private String writeToString(PrefinishedOutput<?> po, Metadata metadata) {
        if (po == null) {
            if (ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD).equalsIgnoreCase("streaming")) {
                return "";
            }
            return processStreaming(metadata);
        }
        String allFiles = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES);
        String[] files = allFiles.split(",");
        StringBuilder sb = new StringBuilder();
        try {
            RowsAndKeys rnk = (RowsAndKeys) po.getPrefinishedOutput();
            int i = 0;

            for (RowAndKey rowAndKey : rnk.getRowsAndKeys()) {
                if (i > 0) {
                    sb.append(STATIC_DELIMITER_FOR_CSVS_IN_ONE_STRING);
                }
                System.out.println("FileWrite for i= " + i + " rowAndKey = " + rowAndKey.getKeys() + " getRows= " + rowAndKey.getRows());
                String newFileName = files[i];
                sb.append(FileWrite.saveCSVFileFromRows(newFileName, rowAndKey.getRows(), metadata));
                i++;
            }
        } catch (ClassCastException ex) {
            RowAndKey rnk = (RowAndKey) po.getPrefinishedOutput();

            String newFileName = files[0];
            System.out.println("ClassCastException FileWrite for newfilename= " + newFileName + " rowAndKey = ");
            FileWrite.saveCSVFileFromRows(newFileName, rnk.getRows(), metadata);
        }
        db.shutDown();

        return sb.toString();
    }

    private String processStreaming(Metadata metadata) {
        processStreamingEntities(metadata);
        processStreamingWrite(metadata, fileName);
        /*

                FileWrite.writeToTheFile(f, result, true);

         */

        return "CSV written to the file by stream, the file is available here: " + fileName + ".csv";

    }

    private void processStreamingWrite(Metadata metadata, String fileName) {
        StreamingNTriplesWrite streamingWrite = new StreamingNTriplesWrite(metadata, fileName);
        streamingWrite.writeToFileByMetadata();
    }

    private void processStreamingEntities(Metadata metadata) {
    }

    private void writeToCSV(PrefinishedOutput<?> po, Metadata metadata) {
        if (po == null) {
            processStreaming(metadata);
        }
        String allFiles = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES);
        String[] files = allFiles.split(",");
        try {
            RowsAndKeys rnk = (RowsAndKeys) po.getPrefinishedOutput();
            int i = 0;

            for (RowAndKey rowAndKey : rnk.getRowsAndKeys()) {
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
        } catch (ClassCastException ex) {
            RowAndKey rnk = (RowAndKey) po.getPrefinishedOutput();


            String newFileName = files[0];
            System.out.println("ClassCastException FileWrite for newfilename= " + newFileName + " rowAndKey = ");
            FileWrite.saveCSVFileFromRows(newFileName, rnk.getRows(), metadata);
        }
        //System.out.println("rnk size " + rnk.getRowsAndKeys().size());


        db.shutDown();
    }

    private FinalizedOutput<byte[]> finalizeOutput(PrefinishedOutput<?> po) {
        ZipOutputProcessor zop = new ZipOutputProcessor();
        return zop.processCSVToOutput(po);
    }

    public Metadata createMetadata(PrefinishedOutput<RowsAndKeys> po) {
        // Convert intermediate data into basic metadata
        MetadataService ms = new MetadataService();
        return ms.createMetadata(po);
    }

    private PrefinishedOutput<RowsAndKeys> convertData() {
        // Convert the table to intermediate data for processing into metadata
        ConversionService cs = new ConversionService();
        return cs.convertByQuery(rc, db);
    }

    public PrefinishedOutput<?> convertData(RepositoryConnection repositoryConnection, Repository repository) {
        // Convert the table to intermediate data for processing into metadata
        ConversionService cs = new ConversionService();
        return cs.convertByQuery(repositoryConnection, repository);
    }

    public void parseInput() throws IOException {
        // Parse input
        // Create a new Repository.
        db = new SailRepository(new MemoryStore());
        MethodService methodService = new MethodService();
        System.out.println("read method: " + readMethod);
        rc = methodService.processInput(fileName, readMethod, db);

    }

    public RepositoryConnection createRepositoryConnection(Repository repository, String filename, String readMethod) throws IOException {
        // Parse input
        // Create a new Repository.
        MethodService methodService = new MethodService();
        RepositoryConnection repositoryConnection = methodService.processInput(filename, readMethod, repository);
        assert (repositoryConnection != null);
        return repositoryConnection;
    }

    public void configure() {
        BasicConfigurator.configure();

        String m = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.CONVERSION_METHOD);
        method = (m != null) ? m : DEFAULT_METHOD;
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_METHOD, method);
        readMethod = ConfigurationManager.getVariableFromConfigFile(ConfigurationManager.READ_METHOD);
        readMethod = (readMethod != null) ? readMethod : DEFAULT_READ_METHOD;
        System.out.println("readMethod is " + readMethod);
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.READ_METHOD, readMethod);

        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_METADATA_FILE_NAME, metadataFilename);

        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_FILENAME, filePathForOutput);
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.INTERMEDIATE_FILE_NAMES, "");
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_BLANK_NODES, "false");
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.CONVERSION_HAS_RDF_TYPES, "true");
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.METADATA_ROWNUMS, "false");
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_FILE_PATH, "");
        ConfigurationManager.saveVariableToConfigFile(ConfigurationManager.OUTPUT_ZIPFILE_NAME, "zippedCSVW.zip");
    }
}
