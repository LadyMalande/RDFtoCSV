package com.miklosova.rdftocsvw.support;

public class AppConfig {
    private final String DEFAULT_PARSING = "rdf4j";
    private final String DEFAULT_CONVERSION_METHOD = "simple";
    private final String DEFAULT_READ_METHOD = "rdf4j";
    private final String DEFAULT_OUTPUT = "RDFtoCSVOutput.csv";
    private final Boolean DEFAULT_MULTIPLE_TABLES = false;
    private final Boolean DEFAULT_STREAMING = false;
    private final Boolean DEFAULT_CONVERSION_HAS_BLANK_NODES = false;
    private final String DEFAULT_LOG_LEVEL = "INFO";
    private final Boolean DEFAULT_METADATA_ROWNUMS = false;
    private final Boolean DEFAULT_STREAMING_CONTINUOUS = false;
    private final Boolean DEFAULT_SIMPLE_BASIC_QUERY = false;
    private final Boolean DEFAULT_FIRST_NORMAL_FORM = true;
    private final Boolean DEFAULT_CONVERSION_HAS_RDF_TYPES = true;
    private String logLevel;
    private Boolean multipleTables;
    private String parsing;
    private String file;
    private Boolean streaming;
    private Boolean firstNormalForm;
    private String output;
    private String conversionMethod;
    private String INTERMEDIATE_FILE_NAMES;
    private Boolean CONVERSION_HAS_BLANK_NODES;
    private Boolean CONVERSION_HAS_RDF_TYPES;
    private String OUTPUT_ZIPFILE_NAME;
    private String READ_METHOD;
    private Boolean METADATA_ROWNUMS;
    private String columnNamingConvention;
    private String preferredLanguages;

    public String getColumnNamingConvention() {
        return columnNamingConvention;
    }

    public void setColumnNamingConvention(String columnNamingConvention) {
        this.columnNamingConvention = columnNamingConvention;
    }

    public String getPreferredLanguages() {
        return preferredLanguages;
    }

    public void setPreferredLanguages(String preferredLanguages) {
        this.preferredLanguages = preferredLanguages;
    }

    private String OUTPUT_FILE_PATH;
    private Boolean STREAMING_CONTINUOUS;
    private Boolean simpleBasicQuery;

    public void setParsing(String parsing) {
        this.parsing = parsing;
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

    public String getLogLevel() {
        return logLevel;
    }

    public Boolean getMultipleTables() {
        return multipleTables;
    }

    public String getParsing() {
        return parsing;
    }

    public String getFile() {
        return file;
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

    public String getConversionMethod() {
        return conversionMethod;
    }

    public String getINTERMEDIATE_FILE_NAMES() {
        return INTERMEDIATE_FILE_NAMES;
    }

    public Boolean getCONVERSION_HAS_BLANK_NODES() {
        return CONVERSION_HAS_BLANK_NODES;
    }

    public Boolean getCONVERSION_HAS_RDF_TYPES() {
        return CONVERSION_HAS_RDF_TYPES;
    }

    public String getOUTPUT_ZIPFILE_NAME() {
        return OUTPUT_ZIPFILE_NAME;
    }

    public String getREAD_METHOD() {
        return READ_METHOD;
    }

    public Boolean getMETADATA_ROWNUMS() {
        return METADATA_ROWNUMS;
    }

    public String getOUTPUT_FILE_PATH() {
        return OUTPUT_FILE_PATH;
    }

    public Boolean getSTREAMING_CONTINUOUS() {
        return STREAMING_CONTINUOUS;
    }

    public Boolean getSimpleBasicQuery() {
        return simpleBasicQuery;
    }

    public AppConfig(String logLevel) {
        this.logLevel = logLevel;
    }

    public AppConfig() {
        this.logLevel = DEFAULT_LOG_LEVEL;
        this.multipleTables = DEFAULT_MULTIPLE_TABLES;
        this.parsing = DEFAULT_PARSING;
        this.file = "";
        this.streaming = DEFAULT_STREAMING;
        this.firstNormalForm = DEFAULT_FIRST_NORMAL_FORM;
        this.output = DEFAULT_OUTPUT;
        this.conversionMethod = DEFAULT_CONVERSION_METHOD;
        this.INTERMEDIATE_FILE_NAMES = "";
        this.CONVERSION_HAS_BLANK_NODES = DEFAULT_CONVERSION_HAS_BLANK_NODES;
        this.CONVERSION_HAS_RDF_TYPES = DEFAULT_CONVERSION_HAS_RDF_TYPES;
        this.OUTPUT_ZIPFILE_NAME = ConfigurationManager.getBaseFileName(file, output) + "_CSVW.zip";
        this.READ_METHOD = DEFAULT_READ_METHOD;
        this.METADATA_ROWNUMS = DEFAULT_METADATA_ROWNUMS;
        this.OUTPUT_FILE_PATH = DEFAULT_OUTPUT;
        this.STREAMING_CONTINUOUS = DEFAULT_STREAMING_CONTINUOUS;
        this.simpleBasicQuery = DEFAULT_SIMPLE_BASIC_QUERY;
        this.columnNamingConvention = "Title case";
        this.preferredLanguages =  "cs,en,pl";
    }
}
