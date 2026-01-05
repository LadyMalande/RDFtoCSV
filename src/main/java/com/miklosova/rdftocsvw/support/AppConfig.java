package com.miklosova.rdftocsvw.support;

import java.util.logging.Logger;

/**
 * Configuration class for RDF to CSVW conversion.
 * This class holds all parameters needed for the conversion process.
 *
 * User-provided parameters:
 * - file (required): Input RDF file path or URL
 * - parsing (optional): Parsing method (default: "rdf4j")
 * - multipleTables (optional): Enable multiple tables (default: false)
 * - streaming (optional): Enable streaming mode (default: false)
 * - firstNormalForm (optional): Enable first normal form (default: true)
 * - output (optional): Output file path (default: based on input file)
 * - preferredLanguages (optional): Comma-separated language codes (default: "cs,en,pl")
 * - columnNamingConvention (optional): Column naming style (default: "Title Case")
 * - logLevel (optional): Logging level (default: "INFO")
 *
 * Runtime parameters (set during execution):
 * - intermediateFileNames: Files created during processing
 * - conversionHasBlankNodes: Whether conversion contains blank nodes
 * - conversionHasRdfTypes: Whether conversion has RDF types
 * - outputZipFileName: Name of the output ZIP file
 * - readMethod: Method used for reading (derived from parsing)
 * - metadataRowNums: Whether to include row numbers in metadata
 * - outputFilePath: Path for output files
 * - streamingContinuous: Whether streaming is continuous
 * - simpleBasicQuery: Whether to use simple basic query
 * - outputMetadataFileName: Name of the output metadata file
 * - outputFileName: Name of the output file (can be dynamically set)
 * - inputFileName: Name of the input file (defaults to file parameter)
 */
public class AppConfig {
    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());

    // Valid column naming conventions (must match LabelFormatter constants)
    public static final String COLUMN_NAMING_CAMEL_CASE = "camelCase";
    public static final String COLUMN_NAMING_PASCAL_CASE = "PascalCase";
    public static final String COLUMN_NAMING_SNAKE_CASE = "snake_case";
    public static final String COLUMN_NAMING_SCREAMING_SNAKE_CASE = "SCREAMING_SNAKE_CASE";
    public static final String COLUMN_NAMING_KEBAB_CASE = "kebab-case";
    public static final String COLUMN_NAMING_TITLE_CASE = "Title Case";
    public static final String COLUMN_NAMING_DOT_NOTATION = "dot.notation";

    public static final String ORIGINAL_NAMING_NOTATION = "original";

    // Default values for optional parameters
    private static final String DEFAULT_PARSING = "rdf4j";
    private static final String DEFAULT_CONVERSION_METHOD = "basicQuery";
    private static final String DEFAULT_READ_METHOD = "rdf4j";
    private static final String DEFAULT_OUTPUT = "RDFtoCSVOutput.csv";
    private static final Boolean DEFAULT_MULTIPLE_TABLES = false;
    private static final Boolean DEFAULT_STREAMING = false;
    private static final Boolean DEFAULT_CONVERSION_HAS_BLANK_NODES = false;
    private static final String DEFAULT_LOG_LEVEL = "INFO";
    private static final Boolean DEFAULT_METADATA_ROWNUMS = false;
    private static final Boolean DEFAULT_STREAMING_CONTINUOUS = false;
    private static final Boolean DEFAULT_SIMPLE_BASIC_QUERY = false;
    private static final Boolean DEFAULT_FIRST_NORMAL_FORM = true;
    private static final Boolean DEFAULT_CONVERSION_HAS_RDF_TYPES = true;
    private static final String DEFAULT_COLUMN_NAMING_CONVENTION = ORIGINAL_NAMING_NOTATION;
    private static final String DEFAULT_PREFERRED_LANGUAGES = "en,cs";
    private static final Boolean DEFAULT_SKIP_DEREFERENCING = false;
    private static final String DEFAULT_PERFORMANCE_LOG_PATH = "performance_log.txt";

    // User-provided parameters (required)
    private final String file;

    // User-provided parameters (optional)
    private final String logLevel;
    private final Boolean multipleTables;
    private final String parsing;
    private final Boolean streaming;
    private final Boolean firstNormalForm;
    private final String output;
    private final String outputMetadata;
    private String columnNamingConvention;
    private final String preferredLanguages;
    private final Boolean skipDereferencing;
    private final String performanceLogPath;

    // Runtime/derived parameters (set during execution or in constructor)
    private String conversionMethod;
    private String intermediateFileNames;
    private Boolean conversionHasBlankNodes;
    private Boolean conversionHasRdfTypes;
    private String outputZipFileName;
    private String readMethod;
    private Boolean metadataRowNums;
    private String outputFilePath;

    private String outputMetadataFileName;
    private String outputFileName;
    private Boolean streamingContinuous;
    private Boolean simpleBasicQuery;
    private String inputFileName;


    // Getters for user-provided parameters
    public String getFile() {
        return file;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public Boolean getMultipleTables() {
        return multipleTables;
    }

    public String getParsing() {
        return parsing;
    }

    public Boolean getStreaming() {
        return streaming;
    }

    public Boolean getFirstNormalForm() {
        return firstNormalForm;
    }

    public String getOutput() {
        return output;
    }

    public String getColumnNamingConvention() {
        return columnNamingConvention;
    }

    public String getPreferredLanguages() {
        return preferredLanguages;
    }

    public Boolean getSkipDereferencing() {
        return skipDereferencing;
    }

    public String getPerformanceLogPath() {
        return performanceLogPath;
    }

    // Getters and setters for runtime parameters
    public String getConversionMethod() {
        return conversionMethod;
    }

    public void setConversionMethod(String conversionMethod) {
        this.conversionMethod = conversionMethod;
    }

    public String getIntermediateFileNames() {
        return intermediateFileNames;
    }

    public void setIntermediateFileNames(String intermediateFileNames) {
        this.intermediateFileNames = intermediateFileNames;
    }

    public Boolean getConversionHasBlankNodes() {
        return conversionHasBlankNodes;
    }

    public void setConversionHasBlankNodes(Boolean conversionHasBlankNodes) {
        this.conversionHasBlankNodes = conversionHasBlankNodes;
    }

    public Boolean getConversionHasRdfTypes() {
        return conversionHasRdfTypes;
    }

    public void setConversionHasRdfTypes(Boolean conversionHasRdfTypes) {
        this.conversionHasRdfTypes = conversionHasRdfTypes;
    }

    public String getOutputZipFileName() {
        return outputZipFileName;
    }

    public void setOutputZipFileName(String outputZipFileName) {
        this.outputZipFileName = outputZipFileName;
    }

    public String getReadMethod() {
        return readMethod;
    }

    public void setReadMethod(String readMethod) {
        this.readMethod = readMethod;
    }

    public Boolean getMetadataRowNums() {
        return metadataRowNums;
    }

    public void setMetadataRowNums(Boolean metadataRowNums) {
        this.metadataRowNums = metadataRowNums;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    public Boolean getStreamingContinuous() {
        return streamingContinuous;
    }

    public void setStreamingContinuous(Boolean streamingContinuous) {
        this.streamingContinuous = streamingContinuous;
    }

    public Boolean getSimpleBasicQuery() {
        return simpleBasicQuery;
    }

    public void setSimpleBasicQuery(Boolean simpleBasicQuery) {
        this.simpleBasicQuery = simpleBasicQuery;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public String getOutputMetadataFileName() {
        return outputMetadataFileName;
    }

    public void setOutputMetadataFileName(String outputMetadataFileName) {
        this.outputMetadataFileName = outputMetadataFileName;
    }

    // Private constructor - use Builder to create instances
    private AppConfig(Builder builder) {
        // Required parameter
        this.file = builder.file;

        // Optional parameters - use builder values or defaults if null
        this.logLevel = builder.logLevel != null ? builder.logLevel : DEFAULT_LOG_LEVEL;
        this.multipleTables = builder.multipleTables != null ? builder.multipleTables : DEFAULT_MULTIPLE_TABLES;
        this.parsing = builder.parsing != null ? builder.parsing : DEFAULT_PARSING;
        this.streaming = builder.streaming != null ? builder.streaming : DEFAULT_STREAMING;
        this.firstNormalForm = builder.firstNormalForm != null ? builder.firstNormalForm : DEFAULT_FIRST_NORMAL_FORM;
        this.output = builder.output; // Can be null - handled in initializeRuntimeParameters
        this.outputMetadata = builder.outputMetadata;
        this.columnNamingConvention = builder.columnNamingConvention != null ? builder.columnNamingConvention : DEFAULT_COLUMN_NAMING_CONVENTION;
        this.preferredLanguages = builder.preferredLanguages != null ? builder.preferredLanguages : DEFAULT_PREFERRED_LANGUAGES;
        this.skipDereferencing = builder.skipDereferencing != null ? builder.skipDereferencing : DEFAULT_SKIP_DEREFERENCING;
        this.performanceLogPath = builder.performanceLogPath != null ? builder.performanceLogPath : DEFAULT_PERFORMANCE_LOG_PATH;

        // Initialize runtime parameters with defaults
        initializeRuntimeParameters();

    }

    /**
     * Initialize runtime parameters with default values.
     * This method is called during construction and sets up derived/runtime values.
     */
    private void initializeRuntimeParameters() {
        //this.conversionMethod = multipleTables ? "splitQuery" : DEFAULT_CONVERSION_METHOD;
        this.conversionMethod = (parsing.equalsIgnoreCase("rdf4j") && multipleTables) ? "splitQuery" : (parsing.equalsIgnoreCase("rdf4j") && !multipleTables) ? "basicQuery": (parsing.equalsIgnoreCase("streaming")) ? "streaming" : "bigfilestreaming";
        this.intermediateFileNames = "";
        this.conversionHasBlankNodes = DEFAULT_CONVERSION_HAS_BLANK_NODES;
        this.conversionHasRdfTypes = DEFAULT_CONVERSION_HAS_RDF_TYPES;
        this.readMethod = parsing; // Read method is typically the same as parsing method
        this.metadataRowNums = DEFAULT_METADATA_ROWNUMS;
        this.streamingContinuous = streaming;
        this.simpleBasicQuery = DEFAULT_SIMPLE_BASIC_QUERY;

        // Calculate output file path and zip file name
        String baseFileName = getBaseFileName(file, output);
        
        // For both URLs and local files without explicit output, prepend ../ to place output alongside input file
        if (output != null) {
            this.outputFilePath = output;
        } else {
            // Use just the base filename (no ../ prefix) so output is created in current directory
            this.outputFilePath = baseFileName;
        }
        
        // Use full output path for ZIP file, not just base filename
        this.outputZipFileName = this.outputFilePath + "_CSVW.zip";
        this.outputFileName = baseFileName; // Initialize to base file name
        this.inputFileName = file; // Initialize to input file name
        this.outputMetadataFileName = this.outputFilePath + ".csv-metadata.json"; // Initialize to base file name + metadata extension
        /* 
        LOGGER.info("+++this.baseFileName = getBaseFileName(file, output); = " + baseFileName);
        LOGGER.info("+++this.outputFilePath = output != null ? output : baseFileName; = " + this.outputFilePath);
        LOGGER.info("+++this.outputMetadataFileName = this.outputFilePath + .csv-metadata.json; = " + this.outputMetadataFileName);
        LOGGER.info("this.outputZipFileName = baseFileName + _CSVW.zip; = " + outputZipFileName);
        LOGGER.info("+++this.inputFileName = file; = " + this.inputFileName);
        */
    }

    /**
     * Builder class for creating AppConfig instances.
     * Example usage:
     * <pre>
     * AppConfig config = new AppConfig.Builder("input.ttl")
     *     .parsing("rdf4j")
     *     .multipleTables(true)
     *     .firstNormalForm(true)
     *     .build();
     * </pre>
     */
    public static class Builder {
        // Required parameter
        private final String file;

        // Optional parameters with defaults
        private String logLevel = DEFAULT_LOG_LEVEL;
        private Boolean multipleTables = DEFAULT_MULTIPLE_TABLES;
        private String parsing = DEFAULT_PARSING;
        private Boolean streaming = DEFAULT_STREAMING;
        private Boolean firstNormalForm = DEFAULT_FIRST_NORMAL_FORM;
        private String output = null;
        private String outputMetadata = null;
        private String columnNamingConvention;
        private String preferredLanguages = DEFAULT_PREFERRED_LANGUAGES;
        private Boolean skipDereferencing = DEFAULT_SKIP_DEREFERENCING;
        private String performanceLogPath = DEFAULT_PERFORMANCE_LOG_PATH;

        /**
         * Create a new builder with the required file parameter.
         * @param file The input RDF file path or URL (required)
         */
        public Builder(String file) {
            if (file == null || file.trim().isEmpty()) {
                throw new IllegalArgumentException("File parameter is required and cannot be null or empty");
            }
            this.file = file;
        }

        /**
         * Set the parsing method.
         * @param parsing Parsing method (e.g., "rdf4j", "streaming", "bigfilestreaming")
         * @return this Builder instance
         */
        public Builder parsing(String parsing) {
            this.parsing = parsing != null ? parsing : DEFAULT_PARSING;
            return this;
        }

        /**
         * Set whether to create multiple tables.
         * @param multipleTables true to create multiple tables, false for single table
         * @return this Builder instance
         */
        public Builder multipleTables(Boolean multipleTables) {
            this.multipleTables = multipleTables != null ? multipleTables : DEFAULT_MULTIPLE_TABLES;
            return this;
        }

        /**
         * Set whether to use streaming mode.
         * @param streaming true to enable streaming
         * @return this Builder instance
         */
        public Builder streaming(Boolean streaming) {
            this.streaming = streaming != null ? streaming : DEFAULT_STREAMING;
            return this;
        }

        /**
         * Set whether to enforce first normal form.
         * @param firstNormalForm true to enforce first normal form
         * @return this Builder instance
         */
        public Builder firstNormalForm(Boolean firstNormalForm) {
            this.firstNormalForm = firstNormalForm != null ? firstNormalForm : DEFAULT_FIRST_NORMAL_FORM;
            return this;
        }

        /**
         * Set the output file path.
         * @param output Output file path
         * @return this Builder instance
         */
        public Builder output(String output) {
            this.output = output;
            return this;
        }

        /**
         * Sets the output metadata file path.
         * @param outputMetadata The output metadata file path.
         * @return The builder instance.
         */
        public Builder outputMetadata(String outputMetadata) {
            this.outputMetadata = outputMetadata;
            return this;
        }

        /**
         * Set preferred languages for output.
         * @param preferredLanguages Comma-separated language codes (e.g., "cs,en,pl")
         * @return this Builder instance
         */
        public Builder preferredLanguages(String preferredLanguages) {
            //LOGGER.info("preferred languages that got into AppConfig Builder: '" + preferredLanguages + "'");
            this.preferredLanguages = preferredLanguages != null ? preferredLanguages : DEFAULT_PREFERRED_LANGUAGES;
            //LOGGER.info("preferred languages after handling their setup in AppConfig Builder: '" + this.preferredLanguages + "'");
            return this;
        }

        /**
         * Set whether to skip dereferencing and use local names instead.
         * When enabled, vocabulary lookups are skipped and IRI local names are used for column titles.
         * This significantly improves performance by avoiding network requests.
         * @param skipDereferencing true to skip vocabulary lookups and use IRI local names
         * @return this Builder instance
         */
        public Builder skipDereferencing(Boolean skipDereferencing) {
            this.skipDereferencing = skipDereferencing != null ? skipDereferencing : DEFAULT_SKIP_DEREFERENCING;
            return this;
        }

        /**
         * Set the performance log file path.
         * @param performanceLogPath Path to the performance log file
         * @return this Builder instance
         */
        public Builder performanceLogPath(String performanceLogPath) {
            this.performanceLogPath = performanceLogPath != null ? performanceLogPath : DEFAULT_PERFORMANCE_LOG_PATH;
            return this;
        }

        /**
         * Set the column naming convention.
         * Valid values are defined as constants in AppConfig:
         * - COLUMN_NAMING_CAMEL_CASE ("camelCase")
         * - COLUMN_NAMING_PASCAL_CASE ("PascalCase")
         * - COLUMN_NAMING_SNAKE_CASE ("snake_case")
         * - COLUMN_NAMING_SCREAMING_SNAKE_CASE ("SCREAMING_SNAKE_CASE")
         * - COLUMN_NAMING_KEBAB_CASE ("kebab-case")
         * - COLUMN_NAMING_TITLE_CASE ("Title Case")
         * - COLUMN_NAMING_DOT_NOTATION ("dot.notation")
         *
         * @param columnNamingConvention Column naming convention (case-sensitive)
         * @return this Builder instance
         * @throws IllegalArgumentException if the convention is not one of the valid values
         */
        public Builder columnNamingConvention(String columnNamingConvention) {
            this.columnNamingConvention = columnNamingConvention != null ? columnNamingConvention : DEFAULT_COLUMN_NAMING_CONVENTION;
            return this;
        }

        /**
         * Set the log level.
         * @param logLevel Logging level (e.g., "INFO", "DEBUG", "WARNING")
         * @return this Builder instance
         */
        public Builder logLevel(String logLevel) {
            this.logLevel = logLevel != null ? logLevel : DEFAULT_LOG_LEVEL;
            return this;
        }

        /**
         * Build and return the AppConfig instance.
         * Validates parameter combinations before building.
         * @return A new AppConfig instance
         * @throws IllegalArgumentException if parameter validation fails
         */
        public AppConfig build() {
            validate();
            return new AppConfig(this);
        }

        /**
         * Validate parameter combinations.
         * @throws IllegalArgumentException if validation fails
         */
        private void validate() {
            // Validate streaming mode requirements
            if (streaming && (parsing.equalsIgnoreCase("streaming") || parsing.equalsIgnoreCase("bigfilestreaming"))) {
                // Streaming modes typically require .nt files
                if (file != null && !file.toLowerCase().endsWith(".nt") && !file.startsWith("http")) {
                    // Only warn, don't fail - the actual check happens during processing
                    System.err.println("Warning: Streaming mode typically requires N-Triples (.nt) files. File: " + file);
                }
            }

            // Validate parsing method
            if (parsing != null) {
                String parsingLower = parsing.toLowerCase();
                if (!parsingLower.equals("rdf4j") &&
                    !parsingLower.equals("streaming") &&
                    !parsingLower.equals("bigfilestreaming")) {
                    throw new IllegalArgumentException("Invalid parsing method: " + parsing +
                        ". Valid values are: rdf4j, streaming, bigfilestreaming");
                }
            }

            // Validate log level
            if (logLevel != null) {
                String levelUpper = logLevel.toUpperCase();
                if (!levelUpper.equals("SEVERE") &&
                    !levelUpper.equals("WARNING") &&
                    !levelUpper.equals("INFO") &&
                    !levelUpper.equals("CONFIG") &&
                    !levelUpper.equals("FINE") &&
                    !levelUpper.equals("FINER") &&
                    !levelUpper.equals("FINEST") &&
                    !levelUpper.equals("ALL") &&
                    !levelUpper.equals("OFF")) {
                    throw new IllegalArgumentException("Invalid log level: " + logLevel +
                        ". Valid values are: SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL, OFF");
                }
            }

            // Validate column naming convention
            if (columnNamingConvention != null) {
                if (columnNamingConvention.isEmpty()) {
                    throw new IllegalArgumentException("Column naming convention cannot be empty");
                }

                // Valid naming conventions based on LabelFormatter
                boolean isValid = columnNamingConvention.equals(COLUMN_NAMING_CAMEL_CASE) ||
                                  columnNamingConvention.equals(COLUMN_NAMING_PASCAL_CASE) ||
                                  columnNamingConvention.equals(COLUMN_NAMING_SNAKE_CASE) ||
                                  columnNamingConvention.equals(COLUMN_NAMING_SCREAMING_SNAKE_CASE) ||
                                  columnNamingConvention.equals(COLUMN_NAMING_KEBAB_CASE) ||
                                  columnNamingConvention.equals(COLUMN_NAMING_TITLE_CASE) ||
                                  columnNamingConvention.equals(COLUMN_NAMING_DOT_NOTATION) ||
                                  columnNamingConvention.equals(ORIGINAL_NAMING_NOTATION);

                if (!isValid) {
                    throw new IllegalArgumentException(
                        "Invalid column naming convention: '" + columnNamingConvention + "'. " +
                        "Valid values are: '" + COLUMN_NAMING_CAMEL_CASE + "', '" +
                        COLUMN_NAMING_PASCAL_CASE + "', '" + COLUMN_NAMING_SNAKE_CASE + "', '" +
                        COLUMN_NAMING_SCREAMING_SNAKE_CASE + "', '" + COLUMN_NAMING_KEBAB_CASE + "', '" +
                        COLUMN_NAMING_TITLE_CASE + "', '" + COLUMN_NAMING_DOT_NOTATION + "', '" +
                        ORIGINAL_NAMING_NOTATION + "'"
                    );
                }
            }

            // Validate preferred languages format (should be comma-separated)
            if (preferredLanguages != null) {
                // Check for valid format - comma-separated language codes
                // Use split with -1 to preserve trailing empty strings
                String[] langs = preferredLanguages.split(",");
                for (String lang : langs) {
                    //LOGGER.info("lang while parsed from the string array: '" + lang + "'");
                    if (lang.trim().isEmpty()) {
                        throw new IllegalArgumentException("Preferred languages contains empty value. Format: 'en,cs,pl'");
                    }
                }
            }

            // Validate file extension compatibility with multiple tables
            if (multipleTables && file != null) {
                // Multiple tables work with any RDF format, but give info
                if (file.toLowerCase().endsWith(".nt")) {
                    // N-Triples with multiple tables is valid but might be large
                    //System.out.println("Info: Using multiple tables mode with N-Triples format");
                }
            }
        }
    }

    /**
     * Legacy constructor for backward compatibility.
     * @deprecated Use Builder pattern instead: new AppConfig.Builder(file).build()
     */
    @Deprecated
    public AppConfig(String logLevel) {
        this.file = "";
        this.logLevel = logLevel != null ? logLevel : DEFAULT_LOG_LEVEL;
        this.multipleTables = DEFAULT_MULTIPLE_TABLES;
        this.parsing = DEFAULT_PARSING;
        this.streaming = DEFAULT_STREAMING;
        this.firstNormalForm = DEFAULT_FIRST_NORMAL_FORM;
        this.output = DEFAULT_OUTPUT;
        this.outputMetadata = null;
        this.preferredLanguages = DEFAULT_PREFERRED_LANGUAGES;
        this.skipDereferencing = DEFAULT_SKIP_DEREFERENCING;
        this.performanceLogPath = DEFAULT_PERFORMANCE_LOG_PATH;
        initializeRuntimeParameters();
    }

    /**
     * Legacy default constructor for backward compatibility.
     * @deprecated Use Builder pattern instead: new AppConfig.Builder(file).build()
     */
    @Deprecated
    public AppConfig() {
        this.file = "";
        this.logLevel = DEFAULT_LOG_LEVEL;
        this.multipleTables = DEFAULT_MULTIPLE_TABLES;
        this.parsing = DEFAULT_PARSING;
        this.streaming = DEFAULT_STREAMING;
        this.firstNormalForm = DEFAULT_FIRST_NORMAL_FORM;
        this.output = DEFAULT_OUTPUT;
        this.outputMetadata = null;
        //this.columnNamingConvention = DEFAULT_COLUMN_NAMING_CONVENTION;
        this.preferredLanguages = DEFAULT_PREFERRED_LANGUAGES;
        this.skipDereferencing = DEFAULT_SKIP_DEREFERENCING;
        this.performanceLogPath = DEFAULT_PERFORMANCE_LOG_PATH;
        initializeRuntimeParameters();
    }

    // Getters for default values (for backward compatibility)
    public String getDEFAULT_PARSING() {
        return DEFAULT_PARSING;
    }

    public String getDEFAULT_CONVERSION_METHOD() {
        return DEFAULT_CONVERSION_METHOD;
    }

    public String getDEFAULT_READ_METHOD() {
        return DEFAULT_READ_METHOD;
    }

    public String getDEFAULT_OUTPUT() {
        return DEFAULT_OUTPUT;
    }

    public Boolean getDEFAULT_MULTIPLE_TABLES() {
        return DEFAULT_MULTIPLE_TABLES;
    }

    public Boolean getDEFAULT_STREAMING() {
        return DEFAULT_STREAMING;
    }

    public Boolean getDEFAULT_CONVERSION_HAS_BLANK_NODES() {
        return DEFAULT_CONVERSION_HAS_BLANK_NODES;
    }

    public String getDEFAULT_LOG_LEVEL() {
        return DEFAULT_LOG_LEVEL;
    }

    public Boolean getDEFAULT_METADATA_ROWNUMS() {
        return DEFAULT_METADATA_ROWNUMS;
    }

    public Boolean getDEFAULT_STREAMING_CONTINUOUS() {
        return DEFAULT_STREAMING_CONTINUOUS;
    }

    public Boolean getDEFAULT_SIMPLE_BASIC_QUERY() {
        return DEFAULT_SIMPLE_BASIC_QUERY;
    }

    public Boolean getDEFAULT_FIRST_NORMAL_FORM() {
        return DEFAULT_FIRST_NORMAL_FORM;
    }

    public Boolean getDEFAULT_CONVERSION_HAS_RDF_TYPES() {
        return DEFAULT_CONVERSION_HAS_RDF_TYPES;
    }

    /**
     * Returns the base file name (without extension) for the given input file and output file.
     * If output is provided, uses its base name; otherwise, uses the input file's base name.
     * @param inputFile The input file path or URL
     * @param outputFile The output file path (optional)
     * @return The base file name without extension
     */
    public static String getBaseFileName(String inputFile, String outputFile) {
        String fileName = (outputFile != null && !outputFile.isEmpty()) ? outputFile : inputFile;
        //LOGGER.info("String fileName = (outputFile != null && !outputFile.isEmpty()) ? outputFile : inputFile;");
        //LOGGER.info("+++getBaseFileName --> fileName: " + fileName + ", outputFile = " + outputFile + ", inputFile = " + inputFile);
        
        // If it's a URL, extract just the filename from the URL path
        if (ConnectionChecker.isUrl(fileName)) {
            try {
                java.net.URL url = new java.net.URL(fileName);
                String path = url.getPath();
                // Get the last segment of the path (the filename)
                int lastSlash = path.lastIndexOf('/');
                if (lastSlash >= 0) {
                    fileName = path.substring(lastSlash + 1);
                } else {
                    fileName = path;
                }
                //LOGGER.info("+++Extracted filename from URL: " + fileName);
            } catch (java.net.MalformedURLException e) {
                LOGGER.warning("Failed to parse URL: " + fileName);
            }
        } else {
            // Remove any path for local files
            int lastSlash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
            if (lastSlash >= 0) {
                fileName = fileName.substring(lastSlash + 1);
            }
        }
        
        // Remove extension
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            fileName = fileName.substring(0, lastDot);
        }
        //LOGGER.info("+++Final base filename without extension: " + fileName);
        return fileName;
    }
}
