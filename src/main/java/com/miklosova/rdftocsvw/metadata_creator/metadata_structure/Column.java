package com.miklosova.rdftocsvw.metadata_creator.metadata_structure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.miklosova.rdftocsvw.converter.data_structure.Row;
import com.miklosova.rdftocsvw.converter.data_structure.TypeIdAndValues;
import com.miklosova.rdftocsvw.metadata_creator.Dereferencer;
import com.miklosova.rdftocsvw.support.BuiltInDatatypes;
import com.miklosova.rdftocsvw.support.ConnectionChecker;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.jsoup.helper.ValidationException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The Column from CSVW Metadata specificaion.
 */
@SuppressWarnings("SpellCheckingInspection")
@JsonldType("Column")
public class Column {
    @JsonIgnore
    private boolean isNamespaceTheSame;
    /**
     * Title for the column
     */
    private String titles;
    /**
     * Column name for the foreign key feature to work. Also used as the property key name when transforming to JSON/RD
     */
    private String name;
    /**
     * Name to map when transforming back to JSON/RDF - can be the same at multiple columns (will differ by language for example)
     */
    private String propertyUrl;
    /**
     * The value of the cell is combined by the template in this value and a substitute given in the template from given name column
     * http://example.org/country/{country}
     */
    private String valueUrl;
    /**
     * datatype from the literal value. Helps with transforming the data back to JSON/RDF.
     * By documentation: <a href="https://www.w3.org/TR/2015/REC-tabular-metadata-20151217/#built-in-datatypes">Built in datatypes</a>
     * Datatypes prefixed "with <a href="http://www.w3.org/2001/XMLSchema#">XMLSchema</a>".
     * Only Built-in Datatypes are supported: anyAtomicType and its children, number, binary, datetime,
     * any, xml, html, json.
     */
    private String datatype;
    /**
     * A URI template property that MAY be used to indicate what a cell contains information about
     * (subject of a triple of RDF). MAY be also defined on a tableSchema level to define aboutUrl
     * for all the columns.
     */
    private String aboutUrl;
    /**
     * Denotes in which language the given column has the values. Helps with transforming the data back to JSON/RDF.
     */
    private String lang;
    /**
     * Record for column that is not displayed in the .csv but has significance for the meaning of data.
     * Namely, useful for denoting the rdf:type of all the rows in one file.
     * {
     * "virtual": true,
     * "propertyUrl": "rdf:type",
     * "valueUrl": "schema:Country"
     * }
     */
    private Boolean virtual;
    /**
     * True if given column is not for to RDF output writing. Usually true is for the columns which are also tied to virtual column subject.
     * False is default if there is not suppressOutput.
     */
    private Boolean suppressOutput;
    /**
     * Separator that is used in case the column contains multiple values enclosed in ""
     * When converting csvw back to rdf it gets split to separate objects
     */
    private String separator;
    private Map.Entry<Value, TypeIdAndValues> column;
    private Value originalColumnKey;

    /**
     * Instantiates a new Column.
     */
    public Column() {

    }

    /**
     * Instantiates a new Column.
     *
     * @param column             the column to be created
     * @param namespaceIsTheSame True if the namespace is the same forall rows ids
     */
    public Column(Map.Entry<Value, TypeIdAndValues> column, boolean namespaceIsTheSame) {
        //assert column != null;
        this.column = column;
        this.isNamespaceTheSame = namespaceIsTheSame;
        if (column != null) {
            this.originalColumnKey = column.getKey();
        }

    }

    /**
     * Gets name from IRI and the object if its literal.
     *
     * @param predicate the predicate
     * @param object    the object
     * @return the name from iri, it has this form if the object is language literal: localName_(languageTag)
     */
    public static String getNameFromIRI(IRI predicate, Value object) {
        // Create name without - and nonascii characters
        String safeName = createSafeName(predicate.getLocalName());
        String langOfObject = null;
        if (object.isLiteral()) {
            Literal literal = (Literal) object;
            Optional<String> languageTag = literal.getLanguage();
            if (languageTag.isPresent()) {
                langOfObject = languageTag.get();
            }
        }
        if (langOfObject != null) {
            return safeName + "_" + langOfObject;
        } else {
            return safeName;
        }
    }

    /**
     * Create safe name string.
     *
     * @param localName the local name of IRI
     * @return the string without any harmful characters
     */
    public static String createSafeName(String localName) {
        // Replace all non-ASCII characters and hyphens
        return localName.replaceAll("[^\\x00-\\x7F-]", "").replace("-", "");
    }

    /**
     * Gets about url.
     *
     * @return the about url
     */
    public String getAboutUrl() {
        return aboutUrl;
    }

    /**
     * Sets about url.
     *
     * @param aboutUrl the about url
     */
    public void setAboutUrl(String aboutUrl) {
        this.aboutUrl = aboutUrl;
    }

    /**
     * Gets separator.
     *
     * @return the separator
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * Sets separator.
     *
     * @param separator the separator
     */
    public void setSeparator(String separator) {
        this.separator = separator;
    }

    /**
     * Gets is namespace the same.
     *
     * @return the is namespace the same
     */
    @JsonIgnore
    public boolean getIsNamespaceTheSame() {
        return isNamespaceTheSame;
    }

    /**
     * Gets column.
     *
     * @return the column
     */
    @JsonIgnore
    public Map.Entry<Value, TypeIdAndValues> getColumn() {
        return column;
    }

    /**
     * Sets column.
     *
     * @param column the column
     */
    public void setColumn(Map.Entry<Value, TypeIdAndValues> column) {
        this.column = column;
    }

    /**
     * Gets original column key.
     *
     * @return the original column key
     */
    @JsonIgnore
    public Value getOriginalColumnKey() {
        return originalColumnKey;
    }

    /**
     * Gets lang.
     *
     * @return the lang
     */
    public String getLang() {
        return lang;
    }

    /**
     * Create column.
     *
     * @param row              the row
     * @param isSubjectTheSame the is subject the same
     * @param rows             the rows
     */
    public void createColumn(Row row, boolean isSubjectTheSame, List<Row> rows) {
        if (this.column != null) {

            createLang();
            createName();
            createPropertyUrl();
            this.titles = createTitles(this.column.getKey(), this.column.getValue().values.get(0));
            createValueUrl();
            createDatatype();
            createAboutUrl(row, isSubjectTheSame, rows);
            createSuppressOutput();
        }

    }

    private void createSuppressOutput() {

    }

    private void createAboutUrl(Row row, boolean isSubjectTheSame, List<Row> rows) {
        boolean isRdfType = row.isRdfType;
        Value id = column.getValue().id;
        boolean isTypetheSame = TableSchema.isTypeTheSameForAllPrimary(rows);

        IRI typeIri = null;
        if(isRdfType){
            typeIri = (IRI) rows.get(0).type;
        }

        if (this.name.contains("_MULTILEVEL_")) {
            // Regex pattern
            String regex = id.stringValue();

            for (Map.Entry<Value, TypeIdAndValues> entry : row.columns.entrySet()) {
                for (Value valueFromMap : entry.getValue().values) {
                    if (valueFromMap.stringValue().matches(regex)) {
                        // Optionally break out of the loop if you only want the first match
                        break;
                    }
                }
            }
        }


        if (id.isBNode()) {
            this.aboutUrl = null;
        } else {
            IRI idIRI = (IRI) column.getValue().id;
            if (idIRI.stringValue().startsWith("https://blank_Nodes_IRI")) {
                // Subject is a blank node, put no aboutUrl
                this.aboutUrl = null;
            } else {
                if (isSubjectTheSame) {
                    if (isRdfType && isTypetheSame) {
                        // We don't know how aboutUrl is supposed to look like because we don't know semantic ties to the iris
                        assert typeIri != null;
                        this.aboutUrl = idIRI.getNamespace() + "{+" + typeIri.getLocalName() + "}";
                    } else {
                        // We don't know how aboutUrl is supposed to look like because we don't know semantic ties to the iris
                        this.aboutUrl = idIRI.getNamespace() + "{+" + "Subject" + "}";
                    }
                } else {
                    if (isRdfType && isTypetheSame) {
                        // We don't know how aboutUrl is supposed to look like because we don't know semantic ties to the iris
                        this.aboutUrl = "{+" + typeIri.getLocalName() + "}";
                    } else {
                        // We don't know how aboutUrl is supposed to look like because we don't know semantic ties to the iris
                        this.aboutUrl = "{+" + "Subject" + "}";
                    }
                }
            }
        }
    }

    private void createLang() {
        Value valueFromThisColumn = this.column.getValue().values.get(0);
        if (valueFromThisColumn.isLiteral()) {
            Literal literal = (Literal) valueFromThisColumn;
            Optional<String> languageTag = literal.getLanguage();
            languageTag.ifPresent(s -> this.lang = s);
        }
    }

    private void createDatatype() {
        Value valueFromThisColumn = this.column.getValue().values.get(0);
        if (valueFromThisColumn.isLiteral()) {
            Literal literal = (Literal) valueFromThisColumn;
            IRI datatype = literal.getDatatype();
            String localname = datatype.getLocalName();
            localname = (localname.equalsIgnoreCase("langString")) ? "string" : localname;
            this.datatype = (localname.equalsIgnoreCase("string")) ? null : localname;
        }
    }

    /**
     * Create datatype from value.
     *
     * @param object the object
     */
    public void createDatatypeFromValue(Value object) {
        if (object.isLiteral()) {
            Literal literal = (Literal) object;
            IRI datatype = literal.getDatatype();
            String localname = datatype.getLocalName();
            localname = (localname.equalsIgnoreCase("langString")) ? "string" : localname;
            this.datatype = (localname.equalsIgnoreCase("string")) ? null : (BuiltInDatatypes.isBuiltInDatatype(datatype)) ? localname : datatype.stringValue();
        }
    }

    private void createValueUrl() {
        Value valueFromThisColumn = this.column.getValue().values.get(0);
        IRI iriOfColumnKey = (IRI) this.column.getKey();
        String safeNameOfTheColumn = createSafeName(iriOfColumnKey.getLocalName());
        if (valueFromThisColumn.isIRI()) {
            IRI iri = (IRI) valueFromThisColumn;
            String firstPart = iri.getNamespace();
            if (isNamespaceTheSame) {
                this.valueUrl = firstPart + "{+" + safeNameOfTheColumn + "}";
            } else {
                this.valueUrl = "{+" + safeNameOfTheColumn + "}";
            }

        } else if (valueFromThisColumn.isBNode()) {
            this.valueUrl = "_:" + "{+" + safeNameOfTheColumn + "}";
        } else {
            this.valueUrl = "{+" + this.getName() + "}";
        }

    }

    private void createPropertyUrl() {
        Value column = this.column.getKey();
        IRI columnKeyIRI = (IRI) column;
        String iri = columnKeyIRI.toString();
        String delimiter = "_MULTILEVEL_";
        int delimiterIndex = iri.indexOf(delimiter);
        this.propertyUrl = (delimiterIndex != -1) ? iri.substring(0, delimiterIndex) : iri;
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
     * Sets titles.
     *
     * @param titles the titles
     */
    public void setTitles(String titles) {
        this.titles = titles;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets property url.
     *
     * @return the property url
     */
    public String getPropertyUrl() {
        return propertyUrl;
    }

    /**
     * Sets property url.
     *
     * @param propertyUrl the property url
     */
    public void setPropertyUrl(String propertyUrl) {
        this.propertyUrl = propertyUrl;
    }

    /**
     * Gets value url.
     *
     * @return the value url
     */
    public String getValueUrl() {
        return valueUrl;
    }

    /**
     * Sets value url.
     *
     * @param valueUrl the value url
     */
    public void setValueUrl(String valueUrl) {
        this.valueUrl = valueUrl;
    }

    /**
     * Gets datatype.
     *
     * @return the datatype
     */
    public String getDatatype() {
        return datatype;
    }

    /**
     * Sets datatype.
     *
     * @param datatype the datatype
     */
    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    /**
     * Gets virtual.
     *
     * @return the virtual
     */
    public Boolean getVirtual() {
        return virtual;
    }

    /**
     * Sets virtual.
     *
     * @param virtual the virtual
     */
    public void setVirtual(Boolean virtual) {
        this.virtual = virtual;
    }

    private void createName() {
        Value column = this.column.getKey();
        IRI columnKeyIRI = (IRI) column;
        // Create name without - and nonascii characters
        String safeName = createSafeName(columnKeyIRI.getLocalName());
        if (this.lang != null) {
            this.name = safeName + "_" + this.lang;
        } else {
            this.name = safeName;
        }
    }

    /**
     * Create name from iri.
     *
     * @param predicate the predicate
     */
    public void createNameFromIRI(IRI predicate) {
        // Create name without - and nonascii characters
        String safeName = createSafeName(predicate.getLocalName());
        if (this.lang != null) {
            this.name = safeName + "_" + this.lang;
        } else {
            this.name = safeName;
        }
    }

    /**
     * Create titles string.
     *
     * @param predicate the predicate
     * @param object    the object
     * @return the string
     */
    public String createTitles(Value predicate, Value object) {
        IRI columnKeyIRI = (IRI) predicate;
        ValueFactory vf = SimpleValueFactory.getInstance();
        IRI propertyUrlIRI = vf.createIRI(this.getPropertyUrl());

        this.titles = null;

        String delimiter = "_MULTILEVEL_";
        int delimiterIndex = columnKeyIRI.stringValue().indexOf(delimiter);
        String prependix = (delimiterIndex != -1) ? columnKeyIRI.stringValue().substring(delimiterIndex + delimiter.length()) + "_" : "";

        if (ConnectionChecker.checkConnection()) {
            Dereferencer dereferencer = new Dereferencer(this.getPropertyUrl());
            try {
                //this.titles = dereferencer.getTitle();
                this.titles = Dereferencer.fetchLabel(this.getPropertyUrl());
            } catch (IOException noElement) {
                this.titles = propertyUrlIRI.getLocalName();
            }
        }
        if (object.isLiteral()) {
            Literal literal = (Literal) object;
            Optional<String> languageTag = literal.getLanguage();
            String langTag = null;
            if (languageTag.isPresent()) {
                langTag = languageTag.get();
            }
            if (this.titles != null) {
                return (langTag == null) ? prependix + this.titles : prependix + this.titles + " (" + langTag + ")";
            } else {
                return (langTag == null) ? prependix + propertyUrlIRI.getLocalName() : prependix + propertyUrlIRI.getLocalName() + " (" + langTag + ")";
            }

        } else {
            return (this.titles == null) ? prependix + propertyUrlIRI.getLocalName() : prependix + this.titles;
        }


    }

    /**
     * Gets suppress output.
     *
     * @return the suppress output
     */
    public Boolean getSuppressOutput() {
        return suppressOutput;
    }

    /**
     * Sets suppress output.
     *
     * @param suppressOutput the suppress output
     */
    public void setSuppressOutput(Boolean suppressOutput) {
        this.suppressOutput = suppressOutput;
    }

    /**
     * Add first column.
     *
     * @param type               the type
     * @param value              the value
     * @param isRdfType          the is rdf type
     * @param isNamespaceTheSame the is namespace the same
     * @param typeIsTheSame      the type is the same
     */
    public void addFirstColumn(Value type, Value value, boolean isRdfType, boolean isNamespaceTheSame, boolean typeIsTheSame) {
        if (isRdfType && typeIsTheSame) {
            IRI typeIri = (IRI) type;

            Dereferencer d = new Dereferencer(typeIri.toString());

            try {
                //this.titles = d.getTitle();
                this.titles = Dereferencer.fetchLabel(typeIri.toString());
                if(this.titles == null){
                    this.titles = "Subject";
                }
                this.name = typeIri.getLocalName();

            } catch(NullPointerException | ValidationException | IOException noElement){
                this.name = typeIri.getLocalName();
                this.titles = this.name;
            }


        } else {
            this.titles = "Subject";
            this.name = "Subject";
        }
        if (!value.isBNode()) {
            if (isNamespaceTheSame) {
                IRI valueIri = (IRI) value;
                this.valueUrl = valueIri.getNamespace() + "{+" + this.name + "}";
            } else {
                this.valueUrl = "{+" + this.name + "}";
            }

        }
        this.suppressOutput = true;


    }

    /**
     * Add virtual type column.
     *
     * @param type          the type
     * @param id            the id
     * @param isTypeTheSame the is type the same
     */
    public void addVirtualTypeColumn(Value type, Value id, boolean isTypeTheSame) {
        this.virtual = true;
        this.propertyUrl = "rdf:type";
        if (isNamespaceTheSame) {
            this.aboutUrl = ((IRI) id).getNamespace() + "{+" + ((IRI) type).getLocalName() + "}";
        } else {
            this.aboutUrl = "{+" + ((IRI) type).getLocalName() + "}";
        }
        if (isTypeTheSame) {
            this.valueUrl = type.stringValue();
        } else {
            this.valueUrl = "{+type}";
        }
    }

    /**
     * Create lang from literal.
     *
     * @param object the object
     */
    public void createLangFromLiteral(Value object) {
        if (object.isLiteral()) {
            Literal literal = (Literal) object;
            Optional<String> languageTag = literal.getLanguage();
            languageTag.ifPresent(s -> this.lang = s);
        }
    }

    /**
     * Sets lang.
     *
     * @param lang the lang
     */
    public void setLang(String lang) {
        this.lang = lang;
    }
}
