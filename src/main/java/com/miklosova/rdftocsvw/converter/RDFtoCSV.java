package com.miklosova.rdftocsvw.converter;

import com.miklosova.rdftocsvw.converter.data_structure.PrefinishedOutput;
import com.miklosova.rdftocsvw.converter.data_structure.RowAndKey;
import com.miklosova.rdftocsvw.converter.data_structure.RowsAndKeys;
import com.miklosova.rdftocsvw.input_processor.MethodService;
import com.miklosova.rdftocsvw.metadata_creator.MetadataService;
import com.miklosova.rdftocsvw.metadata_creator.metadata_structure.Metadata;
import com.miklosova.rdftocsvw.output_processor.FileWrite;
import com.miklosova.rdftocsvw.output_processor.FinalizedOutput;
import com.miklosova.rdftocsvw.output_processor.StreamingNTriplesWrite;
import com.miklosova.rdftocsvw.output_processor.ZipOutputProcessor;
import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.JsonUtil;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.logging.Logger;

import static com.miklosova.rdftocsvw.support.ConnectionChecker.isUrl;
import static org.eclipse.rdf4j.model.util.Values.iri;


/**
 * The main library class for requesting the RDF to CSVW conversion. The Web service operates on these methods.
 */
public class RDFtoCSV {
    private static final Logger logger = Logger.getLogger(RDFtoCSV.class.getName());
    
    /**
     * Delimiter for answering /csv/string and /csv if the conversion method is set to MORE.
     * The generated tables are written to one string after each other, and are delimited by this string to help the viewer
     * see the ends of tables FAST.
     * The proper experience and method to use for getting converted data is the convertToZip method, as it returns
     * all of the generated data in its proper format.
     */
    private static final String STATIC_DELIMITER_FOR_CSVS_IN_ONE_STRING = """
                
            -----------ANOTHER CSV TABLE-----------
                
            """;

    /**
     * The Repository to make RepositoryConnection Upon.
     */
    Repository db;
    /**
     * The RepositoryConnection to ask SPARQL queries on.
     */
    RepositoryConnection rc;
    
    /**
     * Configuration for the conversion process.
     */
    private final AppConfig config;
    
    /**
     * Mandatory, sets the original RDF file to convert.
     */
    private String fileName;
    private String filePathForOutput;

    /**
     * Gets file path for output.
     *
     * @return the file path for output
     */
    public String getFilePathForOutput() {
        return filePathForOutput;
    }

    /**
     * Gets metadata filename.
     *
     * @return the metadata filename
     */
    public String getMetadataFilename() {
        return metadataFilename;
    }

    private final String metadataFilename;

    /**
     * Gets the current configuration.
     *
     * @return the AppConfig instance
     */
    public AppConfig getConfig() {
        return config;
    }

    /**
     * Instantiates a new RDFtoCSV with AppConfig (recommended approach).
     *
     * @param config The application configuration containing all conversion parameters
     */
    public RDFtoCSV(AppConfig config) {
        this.config = config;
        this.fileName = isUrl(config.getFile()) ? config.getFile() : "../" + config.getFile();
        logger.info("Input file for conversion from RDFtoCSV: " + this.fileName);
        this.metadataFilename = this.fileName + ".csv-metadata.json";
        this.filePathForOutput = this.fileName;
        if (isUrl(config.getFile())) {
            this.filePathForOutput = iri(this.fileName).getLocalName();
        }
    }

    /**
     * Instantiates a new Rd fto csv.
     *
     * @param fileName the file name
     * @deprecated Use {@link #RDFtoCSV(AppConfig)} instead
     */
    @Deprecated
    public RDFtoCSV(String fileName) {
        // Create default config for backward compatibility
        this.config = new AppConfig.Builder(fileName).build();
        this.fileName = isUrl(fileName) ? fileName : "../" + fileName;
        this.metadataFilename = this.fileName + ".csv-metadata.json";
        this.filePathForOutput = this.fileName;
        if (isUrl(fileName)) {
            this.filePathForOutput = iri(this.fileName).getLocalName();
        }
    }

    /**
     * Instantiates a new RDFtoCSV from library perspective, the config map is necessary to establish the parameters instead of the args[] that would come from Command Line.
     *
     * @param fileName  the file name
     * @param configMap the config map
     * @deprecated Use {@link #RDFtoCSV(AppConfig)} instead
     */
    @Deprecated
    public RDFtoCSV(String fileName, Map<String, String> configMap) {
        // Build AppConfig from configMap for backward compatibility
        AppConfig.Builder builder = new AppConfig.Builder(fileName);
        
        if (configMap != null) {
            if (configMap.containsKey("table")) {
                boolean multipleTables = "splitQuery".equalsIgnoreCase(configMap.get("table")) 
                    || "MORE".equalsIgnoreCase(configMap.get("table")) 
                    || "more".equalsIgnoreCase(configMap.get("table"));
                builder.multipleTables(multipleTables);
            }
            if (configMap.containsKey("readMethod")) {
                builder.parsing(configMap.get("readMethod"));
            }
            if (configMap.containsKey("firstNormalForm")) {
                builder.firstNormalForm(Boolean.parseBoolean(configMap.get("firstNormalForm")));
            }
        }
        
        this.config = builder.build();
        this.fileName = fileName;
        logger.info("Input file for conversion from RDFtoCSV: " + this.fileName);

        this.metadataFilename = this.fileName + ".csv-metadata.json";
        this.filePathForOutput = this.fileName;
        logger.info("this.metadataFilename = this.fileName + .csv-metadata.json;" + this.metadataFilename);

        if (isUrl(fileName)) {
            this.filePathForOutput = iri(this.fileName).getLocalName();
        }
    }

    /**
     * Default conversion method, returns zipped file
     *
     * @return the finalized output
     * @throws IOException the io exception
     */
    @SuppressWarnings("unused")
    public FinalizedOutput<byte[]> convertToZip() throws IOException {

        parseInput();

        PrefinishedOutput<RowsAndKeys> po = convertData();


        Metadata metadata = createMetadata(po);


        // Write data to CSV by the metadata prepared

        writeToCSV(po, metadata);

        return finalizeOutput(po);
        // Finalize the output to .zip

    }

    /**
     * Runs the trivial basic algorithm for grouping data into Map by predicates and then writes them out by the
     * sorted predicates.
     *
     * @return The complete CSV as String
     * @throws IOException if the file or URL cannot be read
     */
    public String getTrivialCSVTableAsString() throws IOException {
        // Configuration already done
        parseInput();

        PrefinishedOutput<RowsAndKeys> po = convertData();

        return writeToStringTrivial(po);
    }

    private String writeToStringTrivial(PrefinishedOutput<?> po) {
        StringBuilder sb = new StringBuilder();

        RowAndKey rnk;
        if (po.getPrefinishedOutput() instanceof RowsAndKeys) {
            rnk = ((RowsAndKeys) po.getPrefinishedOutput()).getRowsAndKeys().get(0);
        } else if (po.getPrefinishedOutput() instanceof RowAndKey) {
            rnk = (RowAndKey) po.getPrefinishedOutput();
        } else {
            throw new IllegalArgumentException("The passed argument for writeToStringTrivial can only be of " +
                    "generic PrefinishedOutput<RowsAndKeys> or PrefinishedOutput<RowAndKey>");
        }

        sb.append(FileWrite.writeToString(rnk.getKeys(), rnk.getRows()));


        db.shutDown();

        return sb.toString();
    }

    /**
     * Gets csv table as string.
     *
     * @return the csv table as string
     * @throws IOException the io exception
     */
    public String getCSVTableAsString() throws IOException {

        if (config.getConversionMethod().equalsIgnoreCase("trivial")) {
            return getTrivialCSVTableAsString();
        }

        parseInput();

        PrefinishedOutput<RowsAndKeys> po = convertData();

        Metadata metadata = createMetadata(po);

        // Write data to CSV by the metadata prepared
        return writeToString(po, metadata);
    }

    /**
     * Gets metadata as string.
     *
     * @return the metadata as string
     * @throws IOException the io exception
     */
    @SuppressWarnings("unused")
    public String getMetadataAsString() throws IOException {
        Metadata metadata = getMetadata();

        return JsonUtil.serializeAndReturnPrettyString(metadata);
    }

    /**
     * Gets metadata as object.
     *
     * @return the metadata as object
     * @throws IOException the io exception
     */
    public Metadata getMetadata() throws IOException {

        parseInput();

        PrefinishedOutput<RowsAndKeys> po = convertData();

        return createMetadata(po);
    }

    /**
     * Gets csv table as file.
     *
     * @return the csv table as file
     * @throws IOException the io exception
     */
    @SuppressWarnings("unused")
    public FinalizedOutput<byte[]> getCSVTableAsFile() throws IOException {
        String outputString = getCSVTableAsString();
        if (fileName.startsWith("../")) {
            fileName = fileName.substring(2);
        }
        //String fileNameSafe = isUrl(fileName) ? (iri(fileName).getLocalName()) : "../" + fileName;
        String fileNameSafe = isUrl(fileName) ? (iri(fileName).getLocalName()) : fileName;
        System.out.println("fileNameSafe for final .csv file in getCSVTableAsFile = " + fileNameSafe);
        File f = FileWrite.makeFileByNameAndExtension(fileNameSafe, "csv");
        assert f != null;
        FileWrite.writeToTheFile(f, outputString, true);
        // Read the file into a byte array
        byte[] fileBytes = Files.readAllBytes(f.toPath());
        return new FinalizedOutput<>(fileBytes);
    }

    /**
     * Gets metadata as file.
     *
     * @return the metadata as file
     * @throws IOException the io exception
     */
    @SuppressWarnings("unused")
    public FinalizedOutput<byte[]> getMetadataAsFile() throws IOException {
        Metadata metadata = getMetadata();

        JsonUtil.serializeAndWriteToFile(metadata, config);
        File f = new File(metadataFilename);
        // Read the file into a byte array
        byte[] fileBytes = Files.readAllBytes(f.toPath());
        return new FinalizedOutput<>(fileBytes);
    }


    private String writeToString(PrefinishedOutput<?> po, Metadata metadata) {
        if (po == null) {
            if (config.getConversionMethod().equalsIgnoreCase("streaming")) {
                return "";
            }
            return processStreaming(metadata);
        }
        String allFiles = config.getIntermediateFileNames();
        String[] files = allFiles.split(",");
        StringBuilder sb = new StringBuilder();
        try {
            RowsAndKeys rnk = (RowsAndKeys) po.getPrefinishedOutput();
            int i = 0;

            for (RowAndKey rowAndKey : rnk.getRowsAndKeys()) {
                if (i > 0) {
                    sb.append(STATIC_DELIMITER_FOR_CSVS_IN_ONE_STRING);
                }

                String newFileName = files[i];
                //System.out.println("newFileName before saveCSVFileFromRows = " + newFileName + " allFileNames = " + allFiles);
                sb.append(FileWrite.saveCSVFileFromRows(newFileName, rowAndKey.getRows(), metadata, config));
                i++;
            }
        } catch (ClassCastException ex) {
            RowAndKey rnk = (RowAndKey) po.getPrefinishedOutput();

            String newFileName = files[0];
            //System.out.println("newFileName before saveCSVFileFromRows = " + newFileName + " allFileNames = " + allFiles);
            FileWrite.saveCSVFileFromRows(newFileName, rnk.getRows(), metadata, config);
        }
        db.shutDown();

        return sb.toString();
    }

    private String processStreaming(Metadata metadata) {
        if (config.getConversionMethod().equalsIgnoreCase("bigFileStreaming")) {
            processStreamingWrite(metadata, fileName);
        }
        return "CSV written to the file by stream, the file is available here: " + fileName + ".csv";
    }

    private void processStreamingWrite(Metadata metadata, String fileName) {
        StreamingNTriplesWrite streamingWrite = new StreamingNTriplesWrite(metadata, fileName, config);
        streamingWrite.writeToFileByMetadata();
    }


    private void writeToCSV(PrefinishedOutput<?> po, Metadata metadata) {
        if (po == null) {
            processStreaming(metadata);
        } else {
            System.out.println("writeToCSV config.getIntermediateFileNames() = " + config.getIntermediateFileNames());

            String allFiles = config.getIntermediateFileNames();
            String[] files = allFiles.split(",");
            try {
                RowsAndKeys rnk = (RowsAndKeys) po.getPrefinishedOutput();
                int i = 0;

                for (RowAndKey rowAndKey : rnk.getRowsAndKeys()) {
                    String newFileName = files[i];
                    //System.out.println("newFileName before saveCSVFileFromRows = " + newFileName + " allFileNames = " + allFiles);
                    FileWrite.saveCSVFileFromRows(newFileName, rowAndKey.getRows(), metadata, config);
                    i++;
                }
            } catch (ClassCastException ex) {
                RowAndKey rnk = (RowAndKey) po.getPrefinishedOutput();

                String newFileName = files[0];
                //System.out.println("newFileName before saveCSVFileFromRows = " + newFileName + " allFileNames = " + allFiles);
                FileWrite.saveCSVFileFromRows(newFileName, rnk.getRows(), metadata, config);
            } catch (NullPointerException ex2) {
                // The po is null because the methods for processing didn't create po
                return;
            }
        }
        db.shutDown();
    }

    /**
     * Make zip from the files created during the conversion
     *
     * @param po prefinished output inner representation
     * @return the .ZIP of CSV(s) and JSON metadata
     */
    private FinalizedOutput<byte[]> finalizeOutput(PrefinishedOutput<?> po) {
        ZipOutputProcessor zop = new ZipOutputProcessor(config);
        return zop.processCSVToOutput(po);
    }

    /**
     * Create metadata metadata.
     *
     * @param po the prefinished output
     * @return the metadata created from the prefinished output
     */
    public Metadata createMetadata(PrefinishedOutput<RowsAndKeys> po) {
        // Convert intermediate data into basic metadata
        MetadataService ms = new MetadataService(config);
        return ms.createMetadata(po);
    }

    /**
     * Convert data prefinished output.
     *
     * @return the prefinished output
     */
    public PrefinishedOutput<RowsAndKeys> convertData() {
        // Convert the table to intermediate data for processing into metadata
        ConversionService cs = new ConversionService(config);
        return cs.convertByQuery(rc, db);
    }

    /**
     * Convert data prefinished output.
     *
     * @param repositoryConnection the repository connection
     * @param repository           the repository
     * @return the prefinished output
     */
    public PrefinishedOutput<?> convertData(RepositoryConnection repositoryConnection, Repository repository) {
        // Convert the table to intermediate data for processing into metadata
        ConversionService cs = new ConversionService(config);
        return cs.convertByQuery(repositoryConnection, repository);
    }

    /**
     * Parse input. Create Repository and set reading methods.
     *
     * @throws IOException the io exception
     */
    public void parseInput() throws IOException {
        // Parse input
        // Create a new Repository.
        db = new SailRepository(new MemoryStore());
        MethodService methodService = new MethodService(config);

        String readMethod = config.getReadMethod();
        rc = methodService.processInput(fileName, readMethod, db);

    }

    /**
     * Create repository connection.
     *
     * @param repository the repository
     * @param filename   the filename
     * @param readMethod the read method
     * @return the repository connection
     * @throws IOException the io exception
     */
    public RepositoryConnection createRepositoryConnection(Repository repository, String filename, String readMethod) throws IOException {
        // Parse input
        // Create a new Repository.
        MethodService methodService = new MethodService(config);
        return methodService.processInput(filename, readMethod, repository);
    }

}
