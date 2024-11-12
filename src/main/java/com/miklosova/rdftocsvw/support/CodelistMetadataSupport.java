package com.miklosova.rdftocsvw.support;

import java.util.HashMap;
import java.util.Map;

public class CodelistMetadataSupport {
    public static Map<String, Map<String, String>> articleMapOne;
    public static Map<String, String> listOfTitlesAndDescription_Column1;
    public static Map<String, String> listOfTitlesAndDescription_Column2;
    public static Map<String, String> listOfTitlesAndDescription_Column3;
    public static Map<String, String> listOfTitlesAndDescription_Column4;
    public static Map<String, String> listOfTitlesAndDescription_Column5;
    public static Map<String, String> listOfTitlesAndDescription_Column6;
    public static Map<String, String> listOfTitlesAndDescription_Column7;
    public static Map<String, String> listOfTitlesAndDescription_Column8;

    static {
        listOfTitlesAndDescription_Column1 = new HashMap<>();

        listOfTitlesAndDescription_Column1.put("titles", "číselník");
        listOfTitlesAndDescription_Column1.put("dc:description", "IRI číselníku");
        listOfTitlesAndDescription_Column1.put("name", "ciselnik");
        listOfTitlesAndDescription_Column1.put("aboutUrl", "{+ciselnik}");
        listOfTitlesAndDescription_Column1.put("propertyUrl", "rdf:type");
        listOfTitlesAndDescription_Column1.put("valueUrl", "skos:ConceptScheme");
        listOfTitlesAndDescription_Column1.put("required", "true");
        listOfTitlesAndDescription_Column1.put("datatype", "anyURI");

        listOfTitlesAndDescription_Column2 = new HashMap<>();

        listOfTitlesAndDescription_Column2.put("titles", "číselník_název_cs");
        listOfTitlesAndDescription_Column2.put("dc:description", "Název číselníku v češtině");
        listOfTitlesAndDescription_Column2.put("name", "ciselnik_nazev_cs");
        listOfTitlesAndDescription_Column2.put("aboutUrl", "{+ciselnik}");
        listOfTitlesAndDescription_Column2.put("propertyUrl", "skos:prefLabel");
        listOfTitlesAndDescription_Column2.put("valueUrl", "");
        listOfTitlesAndDescription_Column2.put("required", "true");
        listOfTitlesAndDescription_Column2.put("datatype", "string");
        listOfTitlesAndDescription_Column2.put("lang", "cs");

        listOfTitlesAndDescription_Column3 = new HashMap<>();

        listOfTitlesAndDescription_Column3.put("titles", "číselník_název_en");
        listOfTitlesAndDescription_Column3.put("dc:description", "Název číselníku v angličtině");
        listOfTitlesAndDescription_Column3.put("name", "ciselnik_nazev_en");
        listOfTitlesAndDescription_Column3.put("aboutUrl", "{+ciselnik}");
        listOfTitlesAndDescription_Column3.put("propertyUrl", "skos:prefLabel");
        listOfTitlesAndDescription_Column3.put("valueUrl", "");
        listOfTitlesAndDescription_Column3.put("required", "true");
        listOfTitlesAndDescription_Column3.put("datatype", "string");
        listOfTitlesAndDescription_Column3.put("lang", "en");

        listOfTitlesAndDescription_Column4 = new HashMap<>();

        listOfTitlesAndDescription_Column4.put("titles", "číselník_položka");
        listOfTitlesAndDescription_Column4.put("dc:description", "IRI položky");
        listOfTitlesAndDescription_Column4.put("name", "polozka");
        listOfTitlesAndDescription_Column4.put("aboutUrl", "{+polozka}");
        listOfTitlesAndDescription_Column4.put("propertyUrl", "rdf:type");
        listOfTitlesAndDescription_Column4.put("valueUrl", "skos:Concept");
        listOfTitlesAndDescription_Column4.put("required", "true");
        listOfTitlesAndDescription_Column4.put("datatype", "anyURI");

        listOfTitlesAndDescription_Column5 = new HashMap<>();

        listOfTitlesAndDescription_Column5.put("titles", "číselník_položka_kód");
        listOfTitlesAndDescription_Column5.put("dc:description", "Kód položky");
        listOfTitlesAndDescription_Column5.put("name", "polozka_kod");
        listOfTitlesAndDescription_Column5.put("aboutUrl", "{+polozka}");
        listOfTitlesAndDescription_Column5.put("propertyUrl", "skos:notation");
        listOfTitlesAndDescription_Column5.put("valueUrl", "");
        listOfTitlesAndDescription_Column5.put("required", "true");
        listOfTitlesAndDescription_Column5.put("datatype", "string");

        listOfTitlesAndDescription_Column6 = new HashMap<>();

        listOfTitlesAndDescription_Column6.put("titles", "číselník_položka_název_cs");
        listOfTitlesAndDescription_Column6.put("dc:description", "Název položky v češtině");
        listOfTitlesAndDescription_Column6.put("name", "polozka_nazev_cs");
        listOfTitlesAndDescription_Column6.put("aboutUrl", "{+polozka}");
        listOfTitlesAndDescription_Column6.put("propertyUrl", "skos:prefLabel");
        listOfTitlesAndDescription_Column6.put("valueUrl", "");
        listOfTitlesAndDescription_Column6.put("required", "true");
        listOfTitlesAndDescription_Column6.put("datatype", "string");
        listOfTitlesAndDescription_Column6.put("lang", "cs");

        listOfTitlesAndDescription_Column7 = new HashMap<>();

        listOfTitlesAndDescription_Column7.put("titles", "číselník_položka_název_en");
        listOfTitlesAndDescription_Column7.put("dc:description", "Název položky v angličtině");
        listOfTitlesAndDescription_Column7.put("name", "polozka_nazev_en");
        listOfTitlesAndDescription_Column7.put("aboutUrl", "{+polozka}");
        listOfTitlesAndDescription_Column7.put("propertyUrl", "skos:prefLabel");
        listOfTitlesAndDescription_Column7.put("valueUrl", "");
        listOfTitlesAndDescription_Column7.put("required", "true");
        listOfTitlesAndDescription_Column7.put("datatype", "string");
        listOfTitlesAndDescription_Column6.put("lang", "en");

        listOfTitlesAndDescription_Column8 = new HashMap<>();

        listOfTitlesAndDescription_Column8.put("titles", "");
        listOfTitlesAndDescription_Column8.put("dc:description", "");
        listOfTitlesAndDescription_Column8.put("name", "");
        listOfTitlesAndDescription_Column8.put("aboutUrl", "{+polozka}");
        listOfTitlesAndDescription_Column8.put("propertyUrl", "skos:inScheme");
        listOfTitlesAndDescription_Column8.put("valueUrl", "{+ciselnik}");
        listOfTitlesAndDescription_Column8.put("required", "");
        listOfTitlesAndDescription_Column8.put("datatype", "");
        listOfTitlesAndDescription_Column8.put("virtual", "true");


        articleMapOne = new HashMap<>();
        articleMapOne.put("skos:ConceptScheme", listOfTitlesAndDescription_Column1);
        articleMapOne.put("skos:ConceptScheme-skos:prefLabel@cs", listOfTitlesAndDescription_Column2);
        articleMapOne.put("skos:ConceptScheme-skos:prefLabel@en", listOfTitlesAndDescription_Column3);
        articleMapOne.put("skos:Concept", listOfTitlesAndDescription_Column4);
        articleMapOne.put("skos:Concept-skos:notation", listOfTitlesAndDescription_Column5);
        articleMapOne.put("skos:Concept-skos:prefLabel@cs", listOfTitlesAndDescription_Column6);
        articleMapOne.put("skos:Concept-skos:prefLabel@en", listOfTitlesAndDescription_Column7);
        articleMapOne.put("rdf:type", listOfTitlesAndDescription_Column8);

    }
}
