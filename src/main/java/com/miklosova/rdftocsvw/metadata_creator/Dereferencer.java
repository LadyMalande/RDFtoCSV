package com.miklosova.rdftocsvw.metadata_creator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.miklosova.rdftocsvw.support.ConfigurationManager;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDFS;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.eclipse.rdf4j.model.util.Values.iri;

/**
 * The Dereferencer class. Contains custom parsers for finding the label/name property of given IRI. If the pretty label exists, it will be used as titles attribute in the metadata which will be used as header of column in the CSV file
 */
public class Dereferencer {
    private static final Logger logger = Logger.getLogger(Dereferencer.class.getName());
    private static final String WOT_RDF_FILE = "http://xmlns.com/wot/0.1/index.rdf";
    private static final String VANN_RDF_FILE = "http://purl.org/vocab/vann/vann-vocab-20100607.rdf";


    private static final String SCHEMA_PREFIX = "http://schema.org";

    private static final String SCHEMA_RDF_FILE = "https://schema.org/version/latest/schemaorg-all-https.ttl";
    private static final String DEFAULT_LANGUAGE = "en"; // Your default language code
    // Reuse HTTP client (add at class level)
    private static final CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionTimeToLive(30, TimeUnit.SECONDS)
            .setMaxConnTotal(100)
            .setMaxConnPerRoute(10)
            .build();
    static String SKOS_PREFIX = "http://www.w3.org/2004/02/skos/core#";
    static String WOT_PREFIX = "http://xmlns.com/wot/0.1/";
    static String VS_PREFIX = "http://www.w3.org/2003/06/sw-vocab-status/ns#";
    static String VANN_PREFIX = "http://purl.org/vocab/vann/";
    static String DCTERMS_PREFIX = "http://purl.org/dc/terms/";
    static String DC_PREFIX = "http://purl.org/dc/elements/1.1/";
    static String FOAF_PREFIX = "http://xmlns.com/foaf/0.1/";
    static String RDFSchema_PREFIX = "http://www.w3.org/2000/01/rdf-schema";
    static String OWL_PREFIX = "http://www.w3.org/2002/07/owl";
    //private final String CC_PREFIX = "http://web.resource.org/cc/"; - the web does not publish lables of given addresses such as https://web.resource.org/cc/Distribution
    static String SKOS_REFERENCE = "http://www.w3.org/2009/08/skos-reference/skos.rdf";
    static String[] standardKnownPrefixes = {SKOS_PREFIX, WOT_PREFIX, VS_PREFIX, VANN_PREFIX, DCTERMS_PREFIX, DC_PREFIX, FOAF_PREFIX, RDFSchema_PREFIX, OWL_PREFIX, SKOS_REFERENCE};
    // Define language preferences (order matters: first is most desired)
    private static List<String> PREFERRED_LANGUAGES = loadPreferredLanguages(); // Example: Prefer English, then German, then French
    private static final LoadingCache<String, String> labelCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String iri) throws Exception {
                    try {

                        return fetchLabelUncached(iri);
                    } catch (IOException e) {
                        ValueFactory vf = SimpleValueFactory.getInstance();
                        IRI propertyUrlIRI = vf.createIRI(iri);
                        logger.info("--------After IOException is caught in fetchLabelUncached, trying to get LocalName...");
                        return propertyUrlIRI.getLocalName();
                        //throw new RuntimeException("Failed to fetch label for IRI: " + iri, e);
                    }
                }
            });
    private String url;

    /**
     * Instantiates a new Dereferencer.
     *
     * @param url the IRI to parse into pretty label
     */
    public Dereferencer(String url) {
        this.url = url;
    }

    // Package-private setter for testing
    static void setPreferredLanguagesForTesting(List<String> languages) {
        PREFERRED_LANGUAGES = languages;
    }

    // Reset method for tests
    static void resetPreferredLanguages() {
        PREFERRED_LANGUAGES = loadPreferredLanguages();
    }

    /**
     * Gets title (human readable nice name/label).
     *
     * @return the title
     */
    public static String getTitle(String url) {
        try {

            if (url.startsWith(FOAF_PREFIX)) {
                return foafDereference(url);
            } else if (url.startsWith(DC_PREFIX) || url.startsWith(DCTERMS_PREFIX)) {
                return dcDereference(url);
            } else if (url.startsWith(VANN_PREFIX)) {
                return vannDereference(url);
            } else if (url.startsWith(VS_PREFIX)) {
                return vsDereference(url);
            } else if (url.startsWith(WOT_PREFIX)) {
                return wotDereference(url);
            } else if (url.startsWith(SKOS_PREFIX)) {
                return skosDereference(url);
            } else {
                throw new NullPointerException();
            }
        } catch (NullPointerException | IOException ex) {
            return null;
        }
    }

    private static String vannDereference(String url) {
        try {
            // dereference for skos
            // Fetch the HTML page

            IRI iri = iri(url);
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

    private static String wotDereference(String url) {
        try {
            // dereference for foaf prefix
            // Fetch the HTML page
            Document doc = Jsoup.connect(url).get();
            IRI iri = iri(url);

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
            logger.log(Level.INFO, "The dereferencer for wot namespace was unable to get a prettier label.");
        }
        throw new NullPointerException();
    }

    private static String vsDereference(String url) {
        try {
            // dereference for vs
            // Fetch the HTML page
            Document doc = Jsoup.connect(url).get();
            IRI iri = iri(url);
            String xpath = "//rdf:Property[@rdf:about='#" + iri.getLocalName() + "']";
            // Find the <h3> element with id=<iriLocalName>
            Element broaderTr = doc.selectXpath(xpath).first();
            if (broaderTr != null) {
                Element label = broaderTr.select("rdfs:label").first();
                assert label != null;
                return label.text();
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "The dereferencer for vs namespace was unable to get a prettier label.");
        }
        throw new NullPointerException();
    }

    private static String skosDereference(String url) throws IOException {
        // dereference for skos
        // Fetch the HTML page
        Document doc = Jsoup.connect(url).get();
        IRI iri = iri(url);

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

    private static String dcDereference(String url) throws IOException {
        // dereference for dcterms for example http://purl.org/dc/terms/title
        // Fetch the HTML page
        Document doc = Jsoup.connect(url).get();
        IRI iri = iri(url);

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

    private static String foafDereference(String url) {
        try {
            // dereference for foaf prefix
            // Fetch the HTML page
            Document doc = Jsoup.connect(url).get();
            IRI iri = iri(url);

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
            logger.log(Level.INFO, "The dereferencer for foaf namespace was unable to get a prettier label.");
        }
        throw new NullPointerException();
    }

    public static boolean startsWithAny(String uri, String[] prefixes) {
        for (String prefix : prefixes) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public static String extractBaseUri(String fullUri) {

        URI uri = URI.create(fullUri);
        String path = uri.getPath();
        logger.info("uri.getFragment(): " + uri.getFragment());
        if (Arrays.asList(standardKnownPrefixes).contains(fullUri)) {
            return fullUri;
        }
        if (uri.getFragment() != null) {
            // The uri is ready to be fetched as is
            return fullUri;
        }
        if (fullUri.startsWith(WOT_PREFIX)) {
            return WOT_RDF_FILE;
        }
        if (fullUri.startsWith(VANN_PREFIX)) {
            logger.log(Level.INFO, "IRI starts with VANN_PREFIX");
            return VANN_RDF_FILE;
        }
        if (fullUri.startsWith(SCHEMA_PREFIX)) {
            logger.log(Level.INFO, "IRI starts with SCHEMA_PREFIX");
            return SCHEMA_RDF_FILE;
        }
        if (startsWithAny(fullUri, standardKnownPrefixes)) {
            int lastSlash = path.lastIndexOf('/');
            String basePath = path.substring(0, lastSlash);
            String baseUri = uri.getScheme() + "://" + uri.getHost() + basePath;
            logger.info("baseUri: " + baseUri);
            return baseUri;
        } else {
            return fullUri;
        }

    }

/*    public static String fetchLabel(String iri) throws IOException {
        long startTime = System.currentTimeMillis();
        // Create HTTP client
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpGet httpGet = new HttpGet(extractBaseUri(iri));

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
*//*
                if (rdfFormat == null) {
                    throw new IOException("Unsupported RDF format in response: " + contentType);
                }
*//*
                // Read response content
                String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                logger.log(Level.INFO, "rdfFormat="+rdfFormat);
                if(rdfFormat == null){
                    throw new IOException("No rdf format content was fetched for IRI "+iri+", create a column title from local name in the IRI.");
                }
                //logger.log(Level.INFO, content);
                Model model = null;
                try {
                // Parse RDF
                 model = ModelFactory.createDefaultModel();


                    model.read(extractBaseUri(iri));
                } catch (JenaException timedOut){
                    try {
                        Thread.sleep(500);
                        model = ModelFactory.createDefaultModel();

                        model.read(extractBaseUri(iri));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

*//*                model.read(
                        new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                        iri,
                        rdfFormat
                );*//*



                String label = findLabelForIRI(model, iri);

                logger.log(Level.INFO, "label found? " + label);

                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                logger.log(Level.WARNING, "Method execution time: " + duration + "ms");

                return label;
            }
        }
    }*/

    private static Lang determineLang(String contentType) {
        logger.info("IN determineLang( " + contentType + " ) ");

        if (contentType == null) return null;
        contentType = contentType.toLowerCase();

        if (contentType.contains("rdf+xml")) return Lang.RDFXML;
        if (contentType.contains("text/xml")) return Lang.RDFXML;
        if (contentType.contains("text/html")) return Lang.RDFXML;
        if (contentType.contains("turtle")) return Lang.TTL;
        if (contentType.contains("ld+json")) return Lang.JSONLD;
        if (contentType.contains("n-triples")) return Lang.NTRIPLES;
        if (contentType.contains("n-quads")) return Lang.NQUADS;
        if (contentType.contains("trig")) return Lang.TRIG;
        return null;
    }

    public static String fetchLabel(String iri) throws IOException, ExecutionException {
        return labelCache.getUnchecked(iri);
    }

    static String fetchLabelUncached(String iri) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("--------Before new HttpGet(extractBaseUri(" + iri + "));");

        HttpGet httpGet = new HttpGet(extractBaseUri(iri));
        long startTime1 = System.currentTimeMillis();
        // More focused Accept header
        //httpGet.addHeader("Accept", "text/turtle, application/rdf+xml, application/ld+json");
        // Set Accept header for RDF formats
        httpGet.addHeader("Accept",
                "application/rdf+xml, " +
                        "text/turtle, " +
                        "application/ld+json, " +
                        "application/n-triples, " +
                        "application/n-quads, " +
                        "application/trig");
        long startTime2 = System.currentTimeMillis();
        Arrays.stream(httpGet.getAllHeaders()).toList().forEach(header -> logger.info("request header -- " + header.getName() + ": " + header.getValue()));

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            long startTime3 = System.currentTimeMillis();
            logger.info("--------STATUS CODE = " + response.getStatusLine().getStatusCode());

            if (response.getStatusLine().getStatusCode() != 200) {
                Arrays.stream(response.getAllHeaders()).toList().forEach(header -> logger.info(header.getName() + ": " + header.getValue()));
                logger.warning(response.getEntity().getContent().toString());
                throw new IOException("HTTP request failed: " + response.getStatusLine());
            }
            long startTime4 = System.currentTimeMillis();
            // Get content as bytes to avoid double conversion
            Header contentLengthHeader = response.getFirstHeader("Content-Length");
            if (contentLengthHeader != null) {
                long size = Long.parseLong(contentLengthHeader.getValue());
                logger.info("Content size from header: " + size + " bytes for iri " + extractBaseUri(iri));
            } else {
                logger.warning("Content-Length header not available");
                Arrays.stream(response.getAllHeaders()).toList().forEach(header -> logger.info(header.getName() + ": " + header.getValue()));
            }
            logger.info("Before setting the response to content in byte[]");
            byte[] content = EntityUtils.toByteArray(response.getEntity());
            long startTime5 = System.currentTimeMillis();
            logger.info("Before response.getEntity().getContentType().getValue();");
            String contentType = response.getEntity().getContentType().getValue();
            logger.info("After response.getEntity().getContentType().getValue();");
            long startTime6 = System.currentTimeMillis();
            Lang lang = determineLang(contentType);
            long startTime7 = System.currentTimeMillis();
            logger.info("lang = " + lang);
            if (lang == null) {
                throw new IOException("Unsupported RDF format: " + contentType);
            }
            long startTime8 = System.currentTimeMillis();
            Model model = ModelFactory.createDefaultModel();
            long startTime9 = System.currentTimeMillis();
            long startTime10 = 0;
            logger.info("iri -----------------------------------*-*-*-*-*-*" + iri);
            try (InputStream in = new ByteArrayInputStream(content)) {
                startTime10 = System.currentTimeMillis();
                // Using RDFDataMgr for faster parsing
                if (iri.startsWith(VANN_PREFIX)) {
                    RDFDataMgr.read(model, in, VANN_RDF_FILE, lang);
                } else if (iri.startsWith(SCHEMA_PREFIX)) {
                    logger.info("Logged in with SCHEMA_PREFIX -----------------------------------*-*-*-*-*-*");
                    RDFDataMgr.read(model, in, SCHEMA_RDF_FILE, lang);
                } else {
                    RDFDataMgr.read(model, in, lang);
                }
            }
            long startTime11 = System.currentTimeMillis();
            String label = findLabelForIRI(model, iri);
            long startTime12 = System.currentTimeMillis();

            logger.log(Level.INFO, "Fetched label for " + iri + " in " + (System.currentTimeMillis() - startTime) + "ms");
            logger.log(Level.INFO, "HttpGet httpGet = new HttpGet(extractBaseUri(iri)); in " + (startTime1 - startTime) + "ms");
            logger.log(Level.INFO, "Fetched label for " + iri + " in " + (startTime2 - startTime1) + "ms");
            logger.log(Level.INFO, "----------------------" + iri + "---------------------------------------");
            logger.log(Level.INFO, "try (CloseableHttpResponse response = httpClient.execute(httpGet)) { in " + (startTime3 - startTime2) + "ms");
            logger.log(Level.INFO, "----------------------" + iri + "---------------------------------");
            logger.log(Level.INFO, "Fetched label for " + iri + " in " + (startTime4 - startTime3) + "ms");
            logger.log(Level.INFO, "byte[] content = EntityUtils.toByteArray(response.getEntity()); in " + (startTime5 - startTime4) + "ms");
            logger.log(Level.INFO, "String contentType = response.getEntity().getContentType().getValue(); in " + (startTime6 - startTime5) + "ms");
            logger.log(Level.INFO, "Lang lang = determineLang(contentType); in " + (startTime7 - startTime6) + "ms");
            logger.log(Level.INFO, "Fetched label for " + iri + " in " + (startTime8 - startTime7) + "ms");
            logger.log(Level.INFO, "Model model = ModelFactory.createDefaultModel(); in " + (startTime9 - startTime8) + "ms");
            logger.log(Level.INFO, "try (InputStream in = new ByteArrayInputStream(content)) { in " + (startTime10 - startTime9) + "ms");
            logger.log(Level.INFO, "RDFDataMgr.read(model, in, lang); in " + (startTime11 - startTime10) + "ms");
            logger.log(Level.INFO, "String label = findLabelForIRI(model, iri); in " + (startTime12 - startTime11) + "ms");

            logger.log(Level.INFO, "LABEL = " + label);

            label = LabelFormatter.changeLabelToTheConfiguredFormat(label);

            return label;
        }
/*        logger.log(Level.INFO, "Beginning fetchLabelUncached");
        long startTime = System.currentTimeMillis();
        HttpGet httpGet = new HttpGet(extractBaseUri(iri));
        logger.log(Level.INFO, "HttpGet got");
        // Create HTTP client




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
                logger.log(Level.INFO, "Executed CloseableHttpResponse response = httpClient.execute(httpGet)");
                int statusCode = response.getStatusLine().getStatusCode();
                logger.log(Level.INFO, "Got status code " + statusCode);
                if (statusCode != 200) {
                    throw new IOException("HTTP request failed with status code: " + statusCode);
                }

                // Get content type to determine RDF format
                String contentType = response.getEntity().getContentType().getValue();
                String rdfFormat = determineRDFFormat(contentType);
                logger.log(Level.INFO, "Determined RDFFormat " + rdfFormat);
*//*
                if (rdfFormat == null) {
                    throw new IOException("Unsupported RDF format in response: " + contentType);
                }
*//*

                logger.log(Level.INFO, "rdfFormat="+rdfFormat);
                if(rdfFormat == null){
                    throw new IOException("No rdf format content was fetched for IRI "+iri+", create a column title from local name in the IRI.");
                }
                //logger.log(Level.INFO, content);
                Model model = null;
                try {
                    // Parse RDF
                    model = ModelFactory.createDefaultModel();

                    logger.log(Level.INFO, "Before model.read(extractBaseUri(iri)); level 1");
                    model.read(extractBaseUri(iri));
                    logger.log(Level.INFO, "After model.read(extractBaseUri(iri)); level 1");
                } catch (JenaException timedOut){
                    try {

                        model = ModelFactory.createDefaultModel();

                        model.read(extractBaseUri(iri));
                    } catch (JenaException e) {
                        e.printStackTrace();
                    }
                }

*//*                model.read(
                        new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                        iri,
                        rdfFormat
                );*//*



                String label = findLabelForIRI(model, iri);

                logger.log(Level.INFO, "label found? " + label);

                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                logger.log(Level.WARNING, "Method execution time: " + duration + "ms");

                return label;

        }*/
    }

    private static String determineRDFFormat(String contentType) {
        if (contentType == null) {
            return null;
        }

        contentType = contentType.toLowerCase();

        if (contentType.contains("rdf+xml")) {
            return "RDF/XML";
        } else if (contentType.contains("turtle") || contentType.contains("text/turtle")) {
            return "TURTLE";  // Changed from "TURTLE" to "TTL"
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
     * Finds a label for a resource, respecting language preferences.
     * Strategy:
     * 1. Prefer a literal in the most desired language from PREFERRED_LANGUAGES.
     * 2. If none found, prefer a literal with no language tag (plain literal).
     * 3. If none found, return the first available literal from any language.
     * 4. If no literal is found, return null or the local name.
     *
     * @param model The model containing the resource.
     * @param iri   The IRI of the resource to get the label for.
     * @return The selected label string, or null if no label was found.
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
        // Check each predicate in order (e.g., rdfs:label, skos:prefLabel, etc.)
        for (String predicateURI : labelPredicates) {
            Property predicateProperty = model.createProperty(predicateURI);

            // Get all statements for this predicate
            StmtIterator labelStmts = resource.listProperties(predicateProperty);

            // Variables to store the best candidate found for this predicate
            Literal bestLiteralForPredicate = null;
            int bestLanguageScore = -1; // Score to compare language preference

            while (labelStmts.hasNext()) {
                Statement stmt = labelStmts.nextStatement();
                RDFNode object = stmt.getObject();
                logger.info("object of <" + stmt.getString() + ">: " + object.toString());
                if (object.isLiteral()) {
                    Literal literal = object.asLiteral();
                    String lang = literal.getLanguage(); // Get language tag (can be empty string "")
                    String lexicalForm = literal.getLexicalForm();

                    logger.info("Found literal for predicate " + predicateURI + ": '" + lexicalForm + "'@" + lang);

                    // Score this literal's language
                    int currentScore = scoreLanguage(lang);

                    // Check if this literal is better than the current best for this predicate
                    if (currentScore > bestLanguageScore) {
                        bestLiteralForPredicate = literal;
                        bestLanguageScore = currentScore;
                    }
                    // If it's a tie (same score), the first one found remains.
                    // You could add more tie-breaking logic here if needed.
                }
            }
            labelStmts.close(); // Important: Jena iterators must be closed

            // If we found a "good enough" label for this predicate, return it immediately.
            // "Good enough" means we found at least one literal for this predicate.
            // Our scoring system ensures the best one was chosen.
            if (bestLiteralForPredicate != null) {
                logger.info("Selected label: '" + bestLiteralForPredicate.getLexicalForm() + "'@" + bestLiteralForPredicate.getLanguage() + " for predicate " + predicateURI);
                return bestLiteralForPredicate.getLexicalForm();
            }
        }
        // If no label was found in any predicate, you could return the local name or null.
        logger.info("No suitable label found for resource: " + resource.getURI());
        return resource.getLocalName(); // Fallback to the URI's local part
    }

    /**
     * Scores a language tag based on the preferred languages list.
     * Higher score is better.
     *
     * @param lang The language tag from the literal (e.g., "en", "de", "").
     * @return A score representing the preference for this language.
     */
    private static int scoreLanguage(String lang) {
        // 1. Highest priority: Check if it's a preferred language.
        //    The index in the list determines priority (earlier = higher score).
        int preferredIndex = PREFERRED_LANGUAGES.indexOf(lang.toLowerCase());
        logger.info("PREFERRED_LANGUAGES = [" + String.join(", ", PREFERRED_LANGUAGES) + "]");
        logger.info("PREFERRED_LANGUAGES.indexOf(lang) = " + preferredIndex);
        if (preferredIndex != -1) {
            // Return a high score, inversely proportional to its position in the list.
            // "en" (index 0) -> 1000, "de" (index 1) -> 999, "fr" (index 2) -> 998
            return 1000 - preferredIndex;
        }

        // 2. Medium priority: Literals with no language tag (lang is empty string "")
        if (lang.isEmpty()) {
            return 500;
        }

        // 3. Lowest priority: Literals in any other, non-preferred language.
        // This ensures we prefer a no-language tag over an unknown language.
        return 0;
    }

    /**
     * Finds the label for a given IRI in an RDF model.
     *
     * @param model The RDF model to search in
     * @param iri The subject IRI to find labels for
     * @return The label string if found, or null if no label exists
     */
/*    public static String findLabelForIRI(Model model, String iri) {
        // Get the resource for the IRI
        Resource resource = model.getResource(iri);

        logger.log(Level.INFO, "---------------------------findLabelForIRI for " + iri );


        logger.info("resource in findLabelForIRI: " + resource.getURI() + " localName: " + resource.getLocalName() );

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
                    logger.info("object.isLiteral() in findLabelForIRI: " + resource.getURI() + " localName: " + resource.getLocalName() );
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
                    logger.info("Found alternative object for alternative label predicate findLabelForIRI:"+ object.asLiteral().getString());
                    return object.asLiteral().getString();
                }
            }
        }
        logger.info("No object found that seems like a label in findLabelForIRI. " );

        return null; // No label found
    }*/

    private static List<String> loadPreferredLanguages() {
        String configValue = ConfigurationManager.loadConfig("app.preferredLanguages");
        if (configValue == null || configValue.trim().isEmpty()) {
            return Arrays.asList("en", "cs"); // default fallback
        }

        List<String> languages = Arrays.stream(configValue.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // Additional safety check: if processing results in empty list, return default
        if (languages.isEmpty()) {
            return Arrays.asList("en", "cs");
        }

        return languages;
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

}
