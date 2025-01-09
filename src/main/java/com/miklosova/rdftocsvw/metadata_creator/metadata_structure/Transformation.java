package com.miklosova.rdftocsvw.metadata_creator.metadata_structure;

import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;

/**
 * A transformation is a definition of how tabular data can be transformed into another format using a script or template.
 */
@JsonldType("Template")
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class Transformation {
    /**
     * Required Property
     */
    private final String url;
    /**
     * Required Property
     */
    private final String scriptFormat;
    /**
     * Required Property
     */
    private final String targetFormat;
    /**
     * Optional Property
     */
    private final String source;
    /**
     * Optional Property
     */
    private final String titles;

    /**
     * Instantiates a new Transformation.
     *
     * @param url          the url
     * @param scriptFormat the script format
     * @param targetFormat the target format
     * @param source       the source
     * @param titles       the titles
     */
    public Transformation(String url, String scriptFormat, String targetFormat, String source, String titles) {
        this.url = url;
        this.scriptFormat = scriptFormat;
        this.targetFormat = targetFormat;
        this.source = source;
        this.titles = titles;
    }

    /**
     * Instantiates a new Transformation.
     */
    public Transformation() {
        this.url = "https://raw.githubusercontent.com/LadyMalande/RDFtoCSVNotes/main/scripts/transformationForBlankNodesStreamed.js";
        this.scriptFormat = "http://www.iana.org/assignments/media-types/application/javascript";
        this.targetFormat = "http://www.iana.org/assignments/media-types/turtle";
        this.source = "rdf";
        this.titles = "RDF format used as the output format in the transformation from CSV to RDF";
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets source.
     *
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * Gets titles.
     *
     * @return the titles
     */
    public String getTitles() {
        return titles;
    }

    /**
     * Gets script format.
     *
     * @return the script format
     */
    public String getScriptFormat() {
        return scriptFormat;
    }

    /**
     * Gets target format.
     *
     * @return the target format
     */
    public String getTargetFormat() {
        return targetFormat;
    }
}
