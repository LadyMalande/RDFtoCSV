package com.miklosova.rdftocsvw.metadata_creator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.miklosova.rdftocsvw.convertor.Row;
import com.miklosova.rdftocsvw.convertor.TypeIdAndValues;
import com.miklosova.rdftocsvw.convertor.TypeOfValue;
import com.miklosova.rdftocsvw.support.BuiltInDatatypes;
import com.miklosova.rdftocsvw.support.ConnectionChecker;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("SpellCheckingInspection")
@JsonldType("Column")
public class Column {
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
     * By documentation: https://www.w3.org/TR/2015/REC-tabular-metadata-20151217/#built-in-datatypes
     * Datatypes prefixed "with http://www.w3.org/2001/XMLSchema#".
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

    public Column() {

    }

    public Column(Map.Entry<Value, TypeIdAndValues> column, boolean namespaceIsTheSame) {
        //assert column != null;
        this.column = column;
        this.isNamespaceTheSame = namespaceIsTheSame;
        if (column != null) {
            this.originalColumnKey = column.getKey();

            System.out.println("original column key = " + originalColumnKey);
        }

    }

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

    public static String createSafeName(String localName) {
        // Replace all non-ASCII characters and hyphens
        String safeName = localName.replaceAll("[^\\x00-\\x7F-]", "").replace("-", "");
        //System.out.println("transformed localName to safe name: " + localName + " > " + safeName);
        return safeName;
    }

    @JsonIgnore
    public boolean getIsNamespaceTheSame() {
        return isNamespaceTheSame;
    }

    public String getAboutUrl() {
        return aboutUrl;
    }

    public void setAboutUrl(String aboutUrl) {
        this.aboutUrl = aboutUrl;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    @JsonIgnore
    public Map.Entry<Value, TypeIdAndValues> getColumn() {
        return column;
    }

    public void setColumn(Map.Entry<Value, TypeIdAndValues> column) {
        this.column = column;
    }

    @JsonIgnore
    public Value getOriginalColumnKey() {
        return originalColumnKey;
    }

    public String getLang() {
        return lang;
    }

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
        TypeOfValue type = column.getValue().type;
        Value id = column.getValue().id;
        Value value = column.getValue().values.get(0);
        boolean isTypetheSame = TableSchema.isTypeTheSameForAllPrimary(rows);
        IRI typeIri = (IRI) rows.get(0).type;
        Value valueForAboutUrlPattern = row.type;

        if (this.name.contains("_MULTILEVEL_")) {
            System.out.println("making different aboutUrl for _MULTILEVEL_ column ");
            // Regex pattern
            String regex = id.stringValue();
            System.out.println("regex = " + regex);

            for (Map.Entry<Value, TypeIdAndValues> entry : row.columns.entrySet()) {
                for (Value valueFromMap : entry.getValue().values) {
                    System.out.println("Matching value = " + valueFromMap + " with regex ");
                    if (valueFromMap.stringValue().matches(regex)) {
                        System.out.println("Found matching value: " + valueFromMap.stringValue());
                        valueForAboutUrlPattern = entry.getKey();
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
                        //this.aboutUrl = idIRI.getNamespace() + "{+" + createSafeName(((IRI) valueForAboutUrlPattern).getLocalName()) + "}";
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
        System.out.println("Column being made in createLang " + this.column.getKey().stringValue() + " size of values " + this.column.getValue().values.size());
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
            String lastPart = iri.getLocalName();
            if (isNamespaceTheSame) {
                this.valueUrl = firstPart + "{+" + safeNameOfTheColumn + "}";
            } else {
                this.valueUrl = "{+" + safeNameOfTheColumn + "}";
            }

        } else if (valueFromThisColumn.isBNode()) {
            this.valueUrl = "_:" + "{+" + safeNameOfTheColumn + "}";
        }

    }

    private void createPropertyUrl() {
        Value column = this.column.getKey();
        IRI columnKeyIRI = (IRI) column;
        String iri = columnKeyIRI.toString();
        String delimiter = "_MULTILEVEL_";
        int delimiterIndex = iri.indexOf(delimiter);
        String fixedPropertyUrl = (delimiterIndex != -1) ? iri.substring(0, delimiterIndex) : iri;
        //System.out.println("CreatedColumn.PropertyUrl=" + columnKeyIRI.toString());
        this.propertyUrl = fixedPropertyUrl;
    }

    public String getTitles() {
        return titles;
    }

    public void setTitles(String titles) {
        this.titles = titles;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPropertyUrl() {
        return propertyUrl;
    }

    public void setPropertyUrl(String propertyUrl) {
        this.propertyUrl = propertyUrl;
    }

    public String getValueUrl() {
        return valueUrl;
    }

    public void setValueUrl(String valueUrl) {
        this.valueUrl = valueUrl;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public Boolean getVirtual() {
        return virtual;
    }

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

    public void createNameFromIRI(IRI predicate) {
        // Create name without - and nonascii characters
        String safeName = createSafeName(predicate.getLocalName());
        if (this.lang != null) {
            this.name = safeName + "_" + this.lang;
        } else {
            this.name = safeName;
        }
    }

    public String createTitles(Value predicate, Value object) {
        //System.out.println("valueFromThisColumn from createTitles() = " + valueFromThisColumn.toString());
        IRI columnKeyIRI = (IRI) predicate;
        ValueFactory vf = SimpleValueFactory.getInstance();
        IRI propertyUrlIRI = vf.createIRI(this.getPropertyUrl());

        this.titles = null;

        String delimiter = "_MULTILEVEL_";
        int delimiterIndex = columnKeyIRI.stringValue().indexOf(delimiter);
        //System.out.println("The delimiterIndex is " + delimiterIndex + " for iri " + columnKeyIRI.stringValue());
        String prependix = (delimiterIndex != -1) ? columnKeyIRI.stringValue().substring(delimiterIndex + delimiter.length()) + "_" : "";
        //System.out.println("prependix==" + prependix);

        if (ConnectionChecker.checkConnection()) {
            Dereferencer dereferencer = new Dereferencer(this.getPropertyUrl());
            try {
                this.titles = dereferencer.getTitle();
            } catch (NullPointerException noElement) {
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
                //System.out.println("valueFromThisColumn from createTitles() isLiteral = " + this.titles);
                return (langTag == null) ? prependix + this.titles : prependix + this.titles + " (" + langTag + ")";
            } else {
                return (langTag == null) ? prependix + propertyUrlIRI.getLocalName() : prependix + propertyUrlIRI.getLocalName() + " (" + langTag + ")";
            }

        } else {
            return (this.titles == null) ? prependix + propertyUrlIRI.getLocalName() : prependix + this.titles;
        }


    }

    public Boolean getSuppressOutput() {
        return suppressOutput;
    }

    public void setSuppressOutput(Boolean suppressOutput) {
        this.suppressOutput = suppressOutput;
    }

    public void addFirstColumn(Value type, Value value, boolean isRdfType, boolean isNamespaceTheSame, boolean typeIsTheSame) {
        //System.out.println("addFirstColumn is typeIri string value " + typeIri.stringValue());
        if (isRdfType && typeIsTheSame) {
            IRI typeIri = (IRI) type;

            //System.out.println("CONVERSION_HAS_RDF_TYPES is " + true);
            Dereferencer d = new Dereferencer(typeIri.toString());
            this.titles = d.getTitle();
            this.name = typeIri.getLocalName();

        } else {
            //System.out.println("CONVERSION_HAS_RDF_TYPES is " + false);
            this.titles = "Subject";
            this.name = "Subject";
        }
        if (value.isBNode()) {

        } else {
            if (isNamespaceTheSame) {
                IRI valueIri = (IRI) value;
                //System.out.println("isNamespaceTheSame is " + isNamespaceTheSame);
                this.valueUrl = valueIri.getNamespace() + "{+" + this.name + "}";
            } else {
                this.valueUrl = "{+" + this.name + "}";
            }

        }
        this.suppressOutput = true;


    }

    public void addVirtualTypeColumn(Value type, Value value, Value id, boolean isTypeTheSame) {
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

    public void createLangFromLiteral(Value object) {
        if (object.isLiteral()) {
            Literal literal = (Literal) object;
            Optional<String> languageTag = literal.getLanguage();
            languageTag.ifPresent(s -> this.lang = s);
        }
    }
}
