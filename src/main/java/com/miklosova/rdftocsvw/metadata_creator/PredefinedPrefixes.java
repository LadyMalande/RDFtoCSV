package com.miklosova.rdftocsvw.metadata_creator;

public enum PredefinedPrefixes {
    AS("as"),
    CSVW("csvw"),
    DCAT("dcat"),
    DQV("dqv"),
    DUV("duv"),
    GRDDL("grddl"),
    JSONLD("jsonld"),
    LDP("ldp"),
    MA("ma"),
    OA("oa"),
    ODRL("odrl"),
    ORG("org"),
    OWL("owl"),
    PROV("prov"),
    QB("qb"),
    RDF("rdf"),
    RDFA("rdfa"),
    RDFS("rdfs"),
    RIF("rif"),
            RR("rr"),
            SD("sd"),
            SKOS("skos"),
            SKOSXL("skosxl"),
            SSN("ssn"),
            SOSA("sosa"),
            TIME("time"),
            VOID("void"),
            WDR("wdr"),
            WDRS("wdrs"),
            XHV("xhv"),
            XML("xml"),
    XSD("xsd"),
    // Widely used Vocabulary Prefixes based on the vocabulary usage on the Semantic Web
                    CC("cc"),
                    CTAG("ctag"),
                    DC("dc"),
                    DCTERMS("dcterms"),
                    DC11("dc11"),
                    FOAF("foaf"),
                    GR("gr"),
                    ICAL("ical"),
                    OG("og"),
                    REV("rev"),
                    SIOC("sioc"),
                    V("v"),
                    VCARD("vcard"),
                    SCHEMA("schema");


    private String prefix;

    PredefinedPrefixes(String envUrl) {
        this.prefix = envUrl;
    }

    public String getUrl() {
        return prefix;
    }

}
