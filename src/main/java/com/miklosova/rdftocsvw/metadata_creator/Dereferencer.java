package com.miklosova.rdftocsvw.metadata_creator;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDFS;
import org.eclipse.rdf4j.model.IRI;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.eclipse.rdf4j.model.util.Values.iri;

/**
 * The Dereferencer class. Contains custom parsers for finding the label/name property of given IRI. If the pretty label exists, it will be used as titles attribute in the metadata which will be used as header of column in the CSV file
 */
public class Dereferencer {
    private static final Logger logger = Logger.getLogger(Dereferencer.class.getName());

    //private final String CC_PREFIX = "http://web.resource.org/cc/"; - the web does not publish lables of given addresses such as https://web.resource.org/cc/Distribution

    private String url;

    /**
     * Instantiates a new Dereferencer.
     *
     * @param url the IRI to parse into pretty label
     */
    public Dereferencer(String url) {
        this.url = url;
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
     * Sets url.
     *
     * @param url the url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets title (human readable nice name/label).
     *
     * @return the title
     */
    public String getTitle() {
        try {
            String SKOS_PREFIX = "http://www.w3.org/2004/02/skos/core#";
            String WOT_PREFIX = "http://xmlns.com/wot/0.1/";
            String VS_PREFIX = "http://www.w3.org/2003/06/sw-vocab-status/ns#";
            String VANN_PREFIX = "http://purl.org/vocab/vann/";
            String DCTERMS_PREFIX = "http://purl.org/dc/terms/";
            String DC_PREFIX = "http://purl.org/dc/elements/1.1/";
            String FOAF_PREFIX = "http://xmlns.com/foaf/0.1/";
            if (this.url.startsWith(FOAF_PREFIX)) {
                return foafDereference();
            } else if (this.url.startsWith(DC_PREFIX) || this.url.startsWith(DCTERMS_PREFIX)) {
                return dcDereference();
            } else if (this.url.startsWith(VANN_PREFIX)) {
                return vannDereference();
            } else if (this.url.startsWith(VS_PREFIX)) {
                return vsDereference();
            } else if (this.url.startsWith(WOT_PREFIX)) {
                return wotDereference();
            } else if (this.url.startsWith(SKOS_PREFIX)) {
                return skosDereference();
            } else {
                throw new NullPointerException();
            }
        } catch (NullPointerException | IOException ex) {
            return null;
        }
    }

    private String vannDereference() {
        try {
            // dereference for skos
            // Fetch the HTML page

            IRI iri = iri(this.url);
            Document doc = Jsoup.connect(iri.getNamespace()).get();

            String cssQuery = "h3[id=\"" + iri.getLocalName() + "\"]";
            // Find the <h3> element with id=<iriLocalName>
            Element broaderTr = doc.select(cssQuery).first();
            if (broaderTr != null) {
                return broaderTr.text();
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "The dereferencer for vann namespace was unable to get a prettier label.");
        }
        throw new NullPointerException();
    }

    private String wotDereference() {
        try {
            // dereference for foaf prefix
            // Fetch the HTML page
            Document doc = Jsoup.connect(this.url).get();
            IRI iri = iri(this.url);

            String cssQuery = "div[id=\"term_" + iri.getLocalName() + "\"]";
            // Find the <tr> element with id="broader"
            Element foundElement = doc.select(cssQuery).first();
            if (foundElement != null) {
                // Find the next <tr> element relative to the <tr> with id="broader"
                Element elementEm = foundElement.selectFirst("em");


                assert elementEm != null;

                return elementEm.text();
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "The dereferencer for wot namespace was unable to get a prettier label.");        }
        throw new NullPointerException();
    }

    private String vsDereference() {
        try {
            // dereference for vs
            // Fetch the HTML page
            Document doc = Jsoup.connect(this.url).get();
            IRI iri = iri(this.url);
            String xpath = "//rdf:Property[@rdf:about='#" + iri.getLocalName() + "']";
            // Find the <h3> element with id=<iriLocalName>
            Element broaderTr = doc.selectXpath(xpath).first();
            if (broaderTr != null) {
                Element label = broaderTr.select("rdfs:label").first();
                assert label != null;
                return label.text();
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "The dereferencer for vs namespace was unable to get a prettier label.");        }
        throw new NullPointerException();
    }

    private String skosDereference() throws IOException {
        // dereference for skos
        // Fetch the HTML page
        Document doc = Jsoup.connect(this.url).get();
        IRI iri = iri(this.url);

        String cssQuery = "#" + iri.getLocalName();
        // Find the <tr> element with id="broader"
        Element broaderTr = doc.select(cssQuery).first().parent().parent();
        if (broaderTr != null) {
            // Find the next <tr> element relative to the <tr> with id="broader"
            Element nextTr = broaderTr.nextElementSibling();

            while (nextTr != null) {
                // Find <td> elements within the sibling <tr>
                Elements tds = nextTr.select("td");
                for (Element td : tds) {
                    // Check if any <td> contains the text "Label:"
                    if (td.text().contains("Label:")) {
                        Element titleElement = td.nextElementSibling();
                        assert titleElement != null;
                        return titleElement.text();
                    }
                }
                nextTr = nextTr.nextElementSibling();
            }
        }
        logger.log(Level.INFO, "The dereferencer for skos namespace was unable to get a prettier label.");
        throw new NullPointerException();
    }

    private String dcDereference() throws IOException {
        // dereference for dcterms for example http://purl.org/dc/terms/title
        // Fetch the HTML page
        Document doc = Jsoup.connect(this.url).get();
        IRI iri = iri(this.url);

        String xpath = "//td[text()='" + iri + "']";
        // Find the <tr> element with id="broader"

        Element tr = doc.selectXpath(xpath).first().parent();
        if (tr != null) {
            // Find the next <tr> element relative to the <tr> with id="broader"
            Element nextTr = tr.nextElementSibling();

            while (nextTr != null) {
                // Find <td> elements within the sibling <tr>
                Elements tds = nextTr.select("td");
                for (Element td : tds) {
                    // Check if any <td> contains the text "Label:"
                    if (td.text().contains("Label")) {
                        Element titleElement = td.nextElementSibling();

                        assert titleElement != null;
                        return titleElement.text();
                    }
                }
                nextTr = nextTr.nextElementSibling();
            }
        }
        logger.log(Level.INFO, "The dereferencer for dc namespace was unable to get a prettier label.");
        throw new NullPointerException();
    }

    private String foafDereference() {
        try {
            // dereference for foaf prefix
            // Fetch the HTML page
            Document doc = Jsoup.connect(this.url).get();
            IRI iri = iri(this.url);

            String cssQuery = "div[about=\"" + iri + "\"]";
            // Find the <tr> element with id="broader"
            Element foundElement = doc.select(cssQuery).first();

            if (foundElement != null) {
                // Find the next <tr> element relative to the <tr> with id="broader"
                Element elementEm = foundElement.selectFirst("em");

                assert elementEm != null;

                return elementEm.text();
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "The dereferencer for foaf namespace was unable to get a prettier label.");        }
        throw new NullPointerException();
    }

    public static String fetchLabel(String iri) throws IOException {
        // Create HTTP client
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(iri);

            // Set Accept header for RDF formats
            httpGet.addHeader("Accept",
                    "application/rdf+xml, " +
                            "text/turtle, " +
                            "application/ld+json, " +
                            "application/n-triples, " +
                            "application/n-quads, " +
                            "application/trig");

            // Execute request
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    throw new IOException("HTTP request failed with status code: " + statusCode);
                }

                // Get content type to determine RDF format
                String contentType = response.getEntity().getContentType().getValue();
                String rdfFormat = determineRDFFormat(contentType);
/*
                if (rdfFormat == null) {
                    throw new IOException("Unsupported RDF format in response: " + contentType);
                }
*/
                // Read response content
                String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                logger.log(Level.INFO, "rdfFormat="+rdfFormat);
                logger.log(Level.INFO, content);

                // Parse RDF
                Model model = ModelFactory.createDefaultModel();

                model.read(iri);

/*                model.read(
                        new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                        iri,
                        rdfFormat
                );*/



                String label = findLabelForIRI(model, iri);

                logger.log(Level.INFO, "label found? " + label);

                return label;
            }
        }
    }

    private static String determineRDFFormat(String contentType) {
        if (contentType == null) {
            return null;
        }

        contentType = contentType.toLowerCase();

        if (contentType.contains("rdf+xml")) {
            return "RDF/XML";
        } else if (contentType.contains("turtle") || contentType.contains("text/turtle")) {
            return "TTL";  // Changed from "TURTLE" to "TTL"
        } else if (contentType.contains("ld+json")) {
            return "JSON-LD";
        } else if (contentType.contains("n-triples")) {
            return "N-TRIPLES";
        } else if (contentType.contains("n-quads")) {
            return "N-QUADS";
        } else if (contentType.contains("trig")) {
            return "TRIG";
        } else if (contentType.contains("trix")) {
            return "TRIX";
        }
        return null;
    }

    /**
     * Finds the label for a given IRI in an RDF model.
     *
     * @param model The RDF model to search in
     * @param iri The subject IRI to find labels for
     * @return The label string if found, or null if no label exists
     */
    public static String findLabelForIRI(Model model, String iri) {
        // Get the resource for the IRI
        Resource resource = model.getResource(iri);

        // Try standard label predicates in order of preference
        String[] labelPredicates = {
                RDFS.label.getURI(),        // rdfs:label
                "http://www.w3.org/2000/01/rdf-schema#label",  // alternative form
                "http://www.w3.org/2004/02/skos/core#prefLabel",  // skos:prefLabel
                "http://purl.org/dc/elements/1.1/title",       // dc:title
                "http://purl.org/dc/terms/title",              // dcterms:title
                "http://www.w3.org/2000/01/rdf-schema#comment" // rdfs:comment (fallback)
        };

        // Check each predicate in order
        for (String predicate : labelPredicates) {
            Statement labelStmt = resource.getProperty(model.createProperty(predicate));
            if (labelStmt != null) {
                RDFNode object = labelStmt.getObject();
                if (object.isLiteral()) {
                    return object.asLiteral().getString();
                }
            }
        }

        // If no label was found with standard predicates, try any property ending with "label"
        for (Statement stmt : resource.listProperties().toList()) {
            String predicateURI = stmt.getPredicate().getURI();
            if (predicateURI.toLowerCase().contains("label")) {
                RDFNode object = stmt.getObject();
                if (object.isLiteral()) {
                    return object.asLiteral().getString();
                }
            }
        }

        return null; // No label found
    }

}
