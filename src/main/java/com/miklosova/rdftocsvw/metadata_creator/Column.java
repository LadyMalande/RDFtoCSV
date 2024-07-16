package com.miklosova.rdftocsvw.metadata_creator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.miklosova.rdftocsvw.convertor.Row;
import com.miklosova.rdftocsvw.convertor.TypeIdAndValues;
import com.miklosova.rdftocsvw.convertor.TypeOfValue;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import com.miklosova.rdftocsvw.support.ConnectionChecker;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

import java.util.Map;
import java.util.Optional;
@JsonldType("Column")
public class Column {
    @JsonIgnore
    public boolean getIsNamespaceTheSame() {
        return isNamespaceTheSame;
    }

    private final boolean isNamespaceTheSame;
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
     * Namely useful for denoting the rdf:type of all the rows in one file.
     * {
     *   "virtual": true,
     *   "propertyUrl": "rdf:type",
     *   "valueUrl": "schema:Country"
     * }
     */
    private Boolean virtual;
    /**
     * True if given column is not for to RDF output writing. Usually true is for the columns which are also tied to virtual column subject.
     * False is default if there is not suppressOutput.
     */
    private Boolean suppressOutput;

    private Map.Entry<Value, TypeIdAndValues> column;

    @JsonIgnore
    public Map.Entry<Value, TypeIdAndValues> getColumn() {
        return column;
    }

    public void setColumn(Map.Entry<Value, TypeIdAndValues> column) {
        this.column = column;
    }

    public String getLang() {
        return lang;
    }

    public Column(Map.Entry<Value, TypeIdAndValues> column, boolean namespaceIsTheSame) {
        //assert column != null;
        this.column = column;
        this.isNamespaceTheSame = namespaceIsTheSame;
    }

    public void createColumn(Row row, boolean isSubjectTheSame) {
        if(this.column != null) {

            createLang();
            createName();
            createPropertyUrl();
            this.titles = createTitles();
            createValueUrl();
            createDatatype();
            createAboutUrl(row, isSubjectTheSame);
            createSuppressOutput();
        }

    }

    private void createSuppressOutput() {

    }

    private void createAboutUrl(Row row, boolean isSubjectTheSame) {
        boolean isRdfType = row.isRdfType;
        TypeOfValue type = column.getValue().type;
        Value id = column.getValue().id;
        Value value = column.getValue().values.get(0);
        if(id.isBNode()){
            this.aboutUrl = null;
        } else {
            IRI idIRI = (IRI) column.getValue().id;
            if(idIRI.stringValue().startsWith("https://blank_Nodes_IRI")){
                // Subject is a blank node, put no aboutUrl
                this.aboutUrl = null;
            } else {
                if (isSubjectTheSame) {
                    if (isRdfType) {
                        // We dont know how aboutUrl is supposed to look like because we dont know semantic ties to the iris
                        this.aboutUrl = idIRI.getNamespace() + "{+" + ((IRI) row.type).getLocalName() + "}";
                    } else {
                        // We dont know how aboutUrl is supposed to look like because we dont know semantic ties to the iris
                        this.aboutUrl = idIRI.getNamespace() + "{+" + "Subjekt" + "}";
                    }
                } else {
                    if (isRdfType) {
                        // We dont know how aboutUrl is supposed to look like because we dont know semantic ties to the iris
                        this.aboutUrl = "{+" + ((IRI) row.type).getLocalName() + "}";
                    } else {
                        // We dont know how aboutUrl is supposed to look like because we dont know semantic ties to the iris
                        this.aboutUrl = "{+" + "Subjekt" + "}";
                    }
                }
            }
        }
    }

    private void createLang() {
        Value valueFromThisColumn = this.column.getValue().values.get(0);
        if(valueFromThisColumn.isLiteral()){
            Literal literal = (Literal) valueFromThisColumn;
            Optional<String> languageTag = literal.getLanguage();
            languageTag.ifPresent(s -> this.lang = s);
        }
    }

    private void createDatatype() {
        Value valueFromThisColumn = this.column.getValue().values.get(0);
        if(valueFromThisColumn.isLiteral()){
            Literal literal = (Literal) valueFromThisColumn;
            IRI datatype = literal.getDatatype();
            String localname = datatype.getLocalName();
            localname = (localname.equalsIgnoreCase("langString")) ? "string" : localname;
            this.datatype = (localname.equalsIgnoreCase("string")) ? null : localname;
        }
    }

    private void createValueUrl() {
        Value valueFromThisColumn = this.column.getValue().values.get(0);
        IRI iriOfColumnKey = (IRI) this.column.getKey();
        if(valueFromThisColumn.isIRI()){
            IRI iri = (IRI) valueFromThisColumn;
            String firstPart = iri.getNamespace();
            String lastPart = iri.getLocalName();
            if(isNamespaceTheSame){
                this.valueUrl = firstPart + "{+" + iriOfColumnKey.getLocalName() + "}";
            } else {
                this.valueUrl = "{+" + iriOfColumnKey.getLocalName() + "}";
            }

        } else if(valueFromThisColumn.isBNode()){
            this.valueUrl = "_:" + "{+" +  iriOfColumnKey.getLocalName() + "}";
        }

    }

    private void createPropertyUrl() {
        Value column = this.column.getKey();
        IRI columnKeyIRI = (IRI) column;
        //System.out.println("CreatedColumn.PropertyUrl=" + columnKeyIRI.toString());
        this.propertyUrl = columnKeyIRI.toString();
    }

    public String getTitles() {
        return titles;
    }

    public String getName() {
        return name;
    }

    public String getPropertyUrl() {
        return propertyUrl;
    }

    public String getValueUrl() {
        return valueUrl;
    }

    public String getDatatype() {
        return datatype;
    }



    public Boolean getVirtual() {
        return virtual;
    }

    private void createName() {
        Value column = this.column.getKey();
        IRI columnKeyIRI = (IRI) column;
        if(this.lang != null){
            this.name = columnKeyIRI.getLocalName() + "_" + this.lang;
        } else{
            this.name = columnKeyIRI.getLocalName();
        }

    }

    private String createTitles() {
        Value valueFromThisColumn = this.column.getValue().values.get(0);
        //System.out.println("valueFromThisColumn from createTitles() = " + valueFromThisColumn.toString());
        Value column = this.column.getKey();
        IRI columnKeyIRI = (IRI) column;

        this.titles = null;
        if(ConnectionChecker.checkConnection()){
            Dereferencer dereferencer = new Dereferencer(this.getPropertyUrl());
            try {
                this.titles = dereferencer.getTitle();
            } catch (NullPointerException noElement){

            }
        }
        if(valueFromThisColumn.isLiteral()){
            Literal literal = (Literal) valueFromThisColumn;
            Optional<String> languageTag = literal.getLanguage();
            String langTag = null;
            if(languageTag.isPresent()){
                langTag = languageTag.get();
            }
                if(this.titles != null) {
                    //System.out.println("valueFromThisColumn from createTitles() isLiteral = " + this.titles);
                    return (langTag == null) ? this.titles : this.titles + " (" + langTag + ")";
                } else {
                    return (langTag == null) ? columnKeyIRI.getLocalName() : columnKeyIRI.getLocalName() + " (" + this.titles + ")";
                }

        } else {
            return (this.titles == null) ? columnKeyIRI.getLocalName() : this.titles;
        }


    }

    public Boolean getSuppressOutput() {
        return suppressOutput;
    }

    public void addFirstColumn(Value type, Value value, boolean isRdfType, boolean isNamespaceTheSame) {
        IRI typeIri = (IRI) type;
        //System.out.println("addFirstColumn is typeIri string value " + typeIri.stringValue());
        if(isRdfType){
            //System.out.println("CONVERSION_HAS_RDF_TYPES is " + true);
            this.titles = typeIri.getLocalName();
            this.name = typeIri.getLocalName();

        } else{
            //System.out.println("CONVERSION_HAS_RDF_TYPES is " + false);
            this.titles = "Subjekt";
            this.name = "Subjekt";
        }
        if(value.isBNode()){

        } else{
            if(isNamespaceTheSame){
                IRI valueIri = (IRI) value;
                //System.out.println("isNamespaceTheSame is " + isNamespaceTheSame);
                this.valueUrl = valueIri.getNamespace() +  "{+" + this.name + "}";
            } else {
                this.valueUrl = "{+" + this.name + "}";
            }

        }
        this.suppressOutput = true;



    }

    public void addVirtualTypeColumn(Value type, Value value, Value id){
        this.virtual = true;
        this.propertyUrl = "rdf:type";
        if(isNamespaceTheSame){
            this.aboutUrl = ((IRI)id).getNamespace() + "{+" + ((IRI)type).getLocalName() + "}";
        } else{
            this.aboutUrl = "{+" + ((IRI)type).getLocalName() + "}";
        }

        this.valueUrl = type.toString();
    }
}
