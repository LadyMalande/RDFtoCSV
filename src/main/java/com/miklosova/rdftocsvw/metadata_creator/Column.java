package com.miklosova.rdftocsvw.metadata_creator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

import java.util.List;
import java.util.Map;
import java.util.Optional;
@JsonldType("Column")
public class Column {
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
     */
    private String datatype;
    /**
     * A URI template property that MAY be used to indicate what a cell contains information about.
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

    private Map.Entry<Value, List<Value>> column;

    @JsonIgnore
    public Map.Entry<Value, List<Value>> getColumn() {
        return column;
    }

    public void setColumn(Map.Entry<Value, List<Value>> column) {
        this.column = column;
    }

    public String getLang() {
        return lang;
    }

    public Column(Map.Entry<Value, List<Value>> column) {
        this.column = column;
    }

    public void createColumn() {
        if(this.column != null) {
            this.titles = createTitles();
            createLang();
            createName();
            createPropertyUrl();
            createValueUrl();
            createDatatype();
            createAboutUrl();
        }

    }

    private void createAboutUrl() {
        // TODO createAboutUrl
    }

    private void createLang() {
        Value valueFromThisColumn = this.column.getValue().get(0);
        if(valueFromThisColumn.isLiteral()){
            Literal literal = (Literal) valueFromThisColumn;
            Optional<String> languageTag = literal.getLanguage();
            languageTag.ifPresent(s -> this.lang = s);
        }
    }

    private void createDatatype() {
        Value valueFromThisColumn = this.column.getValue().get(0);
        if(valueFromThisColumn.isLiteral()){
            Literal literal = (Literal) valueFromThisColumn;
            IRI datatype = literal.getDatatype();
            String localname = datatype.getLocalName();
            this.datatype = (localname.equalsIgnoreCase("string")) ? null : localname;
        }
    }

    private void createValueUrl() {
        Value valueFromThisColumn = this.column.getValue().get(0);
        if(valueFromThisColumn.isIRI()){
            IRI iri = (IRI) valueFromThisColumn;
            String firstPart = iri.getNamespace();
            String lastPart = iri.getLocalName();
            this.valueUrl = firstPart + "{" + lastPart + "}";
        }

    }

    private void createPropertyUrl() {
        Value column = this.column.getKey();
        IRI columnKeyIRI = (IRI) column;
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
        this.name = columnKeyIRI.getLocalName();
    }

    private String createTitles() {
        Value valueFromThisColumn = this.column.getValue().get(0);
        System.out.println("valueFromThisColumn from createTitles() = " + valueFromThisColumn.toString());
        Value column = this.column.getKey();
        IRI columnKeyIRI = (IRI) column;


        if(valueFromThisColumn.isLiteral()){
            Literal literal = (Literal) valueFromThisColumn;
            Optional<String> languageTag = literal.getLanguage();
                languageTag.ifPresent(s -> this.titles = s);
                if(this.titles != null) {
                    System.out.println("valueFromThisColumn from createTitles() isLiteral = " + this.titles);
                    return new String(columnKeyIRI.getLocalName() + " (" + this.titles + ")");
                } else {
                    return columnKeyIRI.getLocalName();
                }

        } else {
            return columnKeyIRI.getLocalName();
        }


    }

    public void addFirstColumn(Value type, Value value) {
        IRI typeIri = (IRI) type;
        IRI valueIri = (IRI) value;
        this.titles = typeIri.getLocalName();
        this.name = typeIri.getLocalName();
        this.valueUrl = valueIri.getNamespace() +  "{" + this.name + "}";

    }

    public void addVirtualTypeColumn(Value type, Value value){
        this.virtual = true;
        this.propertyUrl = "rdf:type";
        this.valueUrl = type.toString();
    }
}