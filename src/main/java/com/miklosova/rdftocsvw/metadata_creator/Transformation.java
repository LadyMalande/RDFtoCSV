package com.miklosova.rdftocsvw.metadata_creator;

import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;

/**
 * A transformation is a definition of how tabular data can be transformed into another format using a script or template.
 */
@JsonldType("Template")
public class Transformation {
    /**
     * Required Property
     */
    private String url;
    /**
     * Required Property
     */
    private String scriptFormat;
    /**
     * Required Property
     */
    private String targetFormat;
    /**
     * Optional Property
     */
    private String source;
    /**
     * Optional Property
     */
    private String titles;

    public Transformation(String url, String scriptFormat, String targetFormat, String source, String titles) {
        this.url = url;
        this.scriptFormat = scriptFormat;
        this.targetFormat = targetFormat;
        this.source = source;
        this.titles = titles;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getScriptFormat() {
        return scriptFormat;
    }

    public void setScriptFormat(String scriptFormat) {
        this.scriptFormat = scriptFormat;
    }

    public String getTargetFormat() {
        return targetFormat;
    }

    public void setTargetFormat(String targetFormat) {
        this.targetFormat = targetFormat;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTitles() {
        return titles;
    }

    public void setTitles(String titles) {
        this.titles = titles;
    }
}
