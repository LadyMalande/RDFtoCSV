package com.miklosova.rdftocsvw.metadata;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import static java.util.Map.entry;

/**
 * This prefix storage has been based on the list: https://www.w3.org/2011/rdfa-context/rdfa-1.1
 */
public class RecognizedPrefixes {
    public static Map<String, String> predefinedPrefixes = Map.ofEntries(
            entry(PredefinedPrefixes.AS.getUrl(), "https://www.w3.org/ns/activitystreams#"),
            entry(PredefinedPrefixes.CSVW.getUrl(), "http://www.w3.org/ns/csvw#"),
        entry(PredefinedPrefixes.DCAT.getUrl(), "http://www.w3.org/ns/dcat#"),
        entry(PredefinedPrefixes.DQV.getUrl(), "http://www.w3.org/ns/dqv#"),
        entry(PredefinedPrefixes.DUV.getUrl(), "https://www.w3.org/ns/duv#"),
        entry(PredefinedPrefixes.GRDDL.getUrl(), "http://www.w3.org/2003/g/data-view#"),
        entry(PredefinedPrefixes.JSONLD.getUrl(), "http://www.w3.org/ns/json-ld#"),
        entry(PredefinedPrefixes.LDP.getUrl(), "http://www.w3.org/ns/ldp#"),
        entry(PredefinedPrefixes.MA.getUrl(), "http://www.w3.org/ns/ma-ont#"),
        entry(PredefinedPrefixes.OA.getUrl(), "http://www.w3.org/ns/oa#"),
        entry(PredefinedPrefixes.ODRL.getUrl(), "http://www.w3.org/ns/odrl/2/"),
        entry(PredefinedPrefixes.ORG.getUrl(), "http://www.w3.org/ns/org#"),
        entry(PredefinedPrefixes.OWL.getUrl(), "http://www.w3.org/2002/07/owl#"),
        entry(PredefinedPrefixes.PROV.getUrl(), "http://www.w3.org/ns/prov#"),
        entry(PredefinedPrefixes.QB.getUrl(), "http://purl.org/linked-data/cube#"),
        entry(PredefinedPrefixes.RDF.getUrl(), "http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
        entry(PredefinedPrefixes.RDFA.getUrl(), "http://www.w3.org/ns/rdfa#"),
        entry(PredefinedPrefixes.RDFS.getUrl(), "http://www.w3.org/2000/01/rdf-schema#"),
        entry(PredefinedPrefixes.RIF.getUrl(), "http://www.w3.org/2007/rif#"),
        entry(PredefinedPrefixes.RR.getUrl(), "http://www.w3.org/ns/r2rml#"),
        entry(PredefinedPrefixes.SD.getUrl(), "http://www.w3.org/ns/sparql-service-description#"),
        entry(PredefinedPrefixes.SKOS.getUrl(), "http://www.w3.org/2004/02/skos/core#"),
        entry(PredefinedPrefixes.SKOSXL.getUrl(), "http://www.w3.org/2008/05/skos-xl#"),
        entry(PredefinedPrefixes.SSN.getUrl(), "http://www.w3.org/ns/ssn/"),
        entry(PredefinedPrefixes.SOSA.getUrl(), "http://www.w3.org/ns/sosa/"),
        entry(PredefinedPrefixes.TIME.getUrl(), "http://www.w3.org/2006/time#"),
        entry(PredefinedPrefixes.VOID.getUrl(), "http://rdfs.org/ns/void#"),
        entry(PredefinedPrefixes.WDR.getUrl(), "http://www.w3.org/2007/05/powder#"),
        entry(PredefinedPrefixes.WDRS.getUrl(), "http://www.w3.org/2007/05/powder-s#"),
        entry(PredefinedPrefixes.XHV.getUrl(), "http://www.w3.org/1999/xhtml/vocab#"),
        entry(PredefinedPrefixes.XML.getUrl(), "http://www.w3.org/XML/1998/namespace"),
        entry(PredefinedPrefixes.XSD.getUrl(), "http://www.w3.org/2001/XMLSchema#"),
        entry(PredefinedPrefixes.CC.getUrl(), "http://creativecommons.org/ns#"),
        entry(PredefinedPrefixes.CTAG.getUrl(), "http://commontag.org/ns#"),
        entry(PredefinedPrefixes.DC.getUrl(), "http://purl.org/dc/terms/"),
        entry(PredefinedPrefixes.DCTERMS.getUrl(), "http://purl.org/dc/terms/"),
        entry(PredefinedPrefixes.DC11.getUrl(), "http://purl.org/dc/elements/1.1/"),
        entry(PredefinedPrefixes.FOAF.getUrl(), "http://xmlns.com/foaf/0.1/"),
        entry(PredefinedPrefixes.GR.getUrl(), "http://purl.org/goodrelations/v1#"),
        entry(PredefinedPrefixes.ICAL.getUrl(), "http://www.w3.org/2002/12/cal/icaltzd#"),
        entry(PredefinedPrefixes.OG.getUrl(), "http://ogp.me/ns#"),
        entry(PredefinedPrefixes.REV.getUrl(), "http://purl.org/stuff/rev#"),
        entry(PredefinedPrefixes.SIOC.getUrl(), "http://rdfs.org/sioc/ns#"),
        entry(PredefinedPrefixes.V.getUrl(), "http://rdf.data-vocabulary.org/#"),
        entry(PredefinedPrefixes.VCARD.getUrl(), "http://www.w3.org/2006/vcard/ns#"),
        entry(PredefinedPrefixes.SCHEMA.getUrl(), "http://schema.org/"));


}
