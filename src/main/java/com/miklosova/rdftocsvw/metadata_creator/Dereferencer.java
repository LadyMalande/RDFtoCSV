package com.miklosova.rdftocsvw.metadata_creator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.miklosova.rdftocsvw.support.AppConfig;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
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
    
    // Suppress Jena XML parser warnings about StAX properties
    static {
        Logger.getLogger("org.apache.jena.util.JenaXMLInput").setLevel(Level.OFF);
    }
    
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
    // Marker for failed dereferencing attempts - stored in cache to prevent retries
    private static final String FETCH_FAILED_MARKER = "__FETCH_FAILED__";
    // Timeout configuration for HTTP requests (in milliseconds)
    // These are intentionally conservative to handle external vocabulary servers
    private static final int CONNECTION_TIMEOUT = 3000; // 3 seconds to establish connection
    private static final int SOCKET_TIMEOUT = 7000; // 7 seconds to wait for data
    private static final int CONNECTION_REQUEST_TIMEOUT = 2000; // 2 seconds to get connection from pool
    // Define language preferences (order matters: first is most desired)
    private List<String> PREFERRED_LANGUAGES; // Initialized in constructor after config is set
    
    // Cache for failed hosts - prevents repeated attempts to unreachable domains
    // Key: hostname (e.g., "slovník.gov.cz")
    // Value: true (indicates this host failed with UnknownHostException or connection error)
    private static final LoadingCache<String, Boolean> failedHostsCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<String, Boolean>() {
                @Override
                public Boolean load(String host) {
                    return false; // Default: host not failed
                }
            });
    
    // Cache for vocabulary RDF models - shared across all Dereferencer instances
    // Key: base vocabulary URI (e.g., "http://www.w3.org/2004/02/skos/core")
    // Value: parsed Jena Model containing the entire vocabulary
    private static final LoadingCache<String, Model> vocabularyCache = CacheBuilder.newBuilder()
            .maximumSize(50)  // Cache up to 50 different vocabularies
            .expireAfterWrite(1, TimeUnit.HOURS)
            .recordStats()  // Enable statistics tracking
            .build(new CacheLoader<String, Model>() {
                @Override
                public Model load(String vocabularyUri) throws Exception {
                    return fetchVocabularyModel(vocabularyUri);
                }
            });
    
    private static final LoadingCache<String, String> labelCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .recordStats()  // Enable statistics tracking
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String iri) throws Exception {
                    try {
                        return fetchLabelUncached(iri, null);
                    } catch (java.net.SocketTimeoutException | java.net.ConnectException | org.apache.http.conn.ConnectTimeoutException e) {
                        // Connection failed or timed out - cache the failure to avoid retrying
                        logger.log(Level.WARNING, "Connection failed/timed out for IRI: " + iri + " - " + e.getMessage());
                        ValueFactory vf = SimpleValueFactory.getInstance();
                        IRI propertyUrlIRI = vf.createIRI(iri);
                        String localName = propertyUrlIRI.getLocalName();
                        // Store the local name in cache so we don't retry this URL
                        return localName;
                    } catch (IOException e) {
                        ValueFactory vf = SimpleValueFactory.getInstance();
                        IRI propertyUrlIRI = vf.createIRI(iri);
                        String localName = propertyUrlIRI.getLocalName();
                        logger.log(Level.WARNING, "IOException while fetching label for IRI: " + iri + " - " + e.getMessage() + ", using local name: " + localName);
                        return localName;
                    }
                }
            });
    private String url;

    private AppConfig config;

    /**
     * Get statistics about cached failed hosts for debugging.
     * @return Summary string of failed hosts cache
     */
    public static String getFailedHostsCacheStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("Failed Hosts Cache: ").append(failedHostsCache.size()).append(" entries\n");
        for (String host : failedHostsCache.asMap().keySet()) {
            sb.append("  - ").append(host).append("\n");
        }
        return sb.toString();
    }

    /**
     * Clear the failed hosts cache AND vocabulary cache AND label cache.
     * Should be called at the start of each conversion to avoid persisting stale failures
     * across multiple runs in the same JVM session (e.g., when running from IntelliJ).
     */
    public static void clearFailedHostsCache() {
        int hostsSize = failedHostsCache.asMap().size();
        int vocabSize = vocabularyCache.asMap().size();
        int labelSize = labelCache.asMap().size();
        
        if (hostsSize > 0 || vocabSize > 0 || labelSize > 0) {
            logger.info("Clearing ALL caches - Failed hosts: " + hostsSize + " entries " + 
                       (hostsSize > 0 ? "(" + String.join(", ", failedHostsCache.asMap().keySet()) + ")" : "") +
                       ", Vocabularies: " + vocabSize + " entries" +
                       ", Labels: " + labelSize + " entries");
            failedHostsCache.invalidateAll();
            vocabularyCache.invalidateAll();
            labelCache.invalidateAll();
        } else {
            logger.info("All three caches already empty (no stale entries to clear)");
        }
    }

    /**
     * Instantiates a new Dereferencer.
     *
     * @param url the IRI to parse into pretty label
     * @param config the application configuration
     */
    public Dereferencer(String url, AppConfig config) {
        this.url = url;
        this.config = config;
        // Initialize PREFERRED_LANGUAGES after config is set
        this.PREFERRED_LANGUAGES = loadPreferredLanguages(config);
    }

    // Package-private setter for testing
    void setPreferredLanguagesForTesting(List<String> languages) {
        PREFERRED_LANGUAGES = languages;
    }

    // Reset method for tests
    void resetPreferredLanguages() {
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

        Element firstElement = doc.selectXpath(xpath).first();
        if (firstElement == null) {
            throw new NullPointerException("Could not find element matching xpath: " + xpath);
        }
        Element tr = firstElement.parent();
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
        //logger.info("uri.getFragment(): " + uri.getFragment());
        if (Arrays.asList(standardKnownPrefixes).contains(fullUri)) {
            return fullUri;
        }
        if (uri.getFragment() != null) {
            // Strip fragment to get base vocabulary URI (e.g., http://...skos/core#prefLabel → http://...skos/core)
            String baseUri = uri.getScheme() + "://" + uri.getHost() + uri.getPath();
            logger.finest("Stripped fragment from " + fullUri + " → base: " + baseUri);
            return baseUri;
        }
        if (fullUri.startsWith(WOT_PREFIX)) {
            return WOT_RDF_FILE;
        }
        if (fullUri.startsWith(VANN_PREFIX)) {
            //logger.log(Level.INFO, "IRI starts with VANN_PREFIX");
            return VANN_RDF_FILE;
        }
        if (fullUri.startsWith(SCHEMA_PREFIX)) {
            //logger.log(Level.INFO, "IRI starts with SCHEMA_PREFIX");
            return SCHEMA_RDF_FILE;
        }
        if (startsWithAny(fullUri, standardKnownPrefixes)) {
            int lastSlash = path.lastIndexOf('/');
            String basePath = path.substring(0, lastSlash);
            String baseUri = uri.getScheme() + "://" + uri.getHost() + basePath;
            //logger.finest("baseUri starts with any STANDARDKNOWNPREFIXES: " + baseUri);
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

                //logger.log(Level.INFO, "rdfFormat="+rdfFormat);
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

                //logger.log(Level.INFO, "label found? " + label);

                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                logger.log(Level.WARNING, "Method execution time: " + duration + "ms");

                return label;
            }
        }
    }*/

    private static Lang determineLang(String contentType) {
        logger.finest("IN determineLang( " + contentType + " ) ");

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

    /**
     * Safely extract the host from a URI that may contain Unicode characters.
     * For URIs with Unicode in the hostname (like slovník.gov.cz), manually extracts the host.
     * 
     * @param uriString The URI string to extract host from
     * @return The host portion of the URI, or null if extraction fails
     */
    private static String extractHost(String uriString) {
        try {
            // First try java.net.URL which handles some Unicode cases better
            try {
                java.net.URL url = new java.net.URL(uriString);
                String host = url.getHost();
                if (host != null && !host.isEmpty()) {
                    return host;
                }
            } catch (java.net.MalformedURLException e) {
                // Fall through to manual extraction
            }
            
            // Try standard URI parsing (works for ASCII URIs)
            try {
                java.net.URI uri = new java.net.URI(uriString);
                String host = uri.getHost();
                if (host != null && !host.isEmpty()) {
                    return host;
                }
            } catch (java.net.URISyntaxException e) {
                // Fall through to manual extraction
            }
            
            // Manual extraction for URIs with Unicode in hostname
            // Extract hostname from "protocol://hostname/path" or "protocol://hostname:port/path"
            int protocolEnd = uriString.indexOf("://");
            if (protocolEnd > 0) {
                // Validate protocol part - should only contain alphanumeric characters, '+', '-', or '.'
                String protocol = uriString.substring(0, protocolEnd);
                if (!protocol.matches("[a-zA-Z][a-zA-Z0-9+.-]*")) {
                    logger.fine("Invalid protocol in URI: " + uriString);
                    return null;
                }
                
                String afterProtocol = uriString.substring(protocolEnd + 3);
                
                // Find the end of hostname (first /, :, ?, or #)
                int pathStart = afterProtocol.length();
                int slashPos = afterProtocol.indexOf('/');
                int colonPos = afterProtocol.indexOf(':');
                int queryPos = afterProtocol.indexOf('?');
                int fragmentPos = afterProtocol.indexOf('#');
                
                if (slashPos >= 0) pathStart = Math.min(pathStart, slashPos);
                if (colonPos >= 0) pathStart = Math.min(pathStart, colonPos);
                if (queryPos >= 0) pathStart = Math.min(pathStart, queryPos);
                if (fragmentPos >= 0) pathStart = Math.min(pathStart, fragmentPos);
                
                String host = afterProtocol.substring(0, pathStart);
                if (!host.isEmpty()) {
                    logger.fine("Manually extracted host: " + host + " from URI: " + uriString);
                    return host;
                }
            }
            
            return null;
        } catch (Exception e) {
            logger.fine("Could not extract host from URI: " + uriString + " - " + e.getMessage());
            return null;
        }
    }

    public String fetchLabel(String iri) throws IOException, ExecutionException {
        // Create cache key that includes language preferences
        String languages = (config != null && config.getPreferredLanguages() != null) 
            ? config.getPreferredLanguages() 
            : "en,cs";
        String cacheKey = iri + "|langs:" + languages;
        
        // Check if already cached with this language preference
        String label = labelCache.getIfPresent(cacheKey);
        if (label == null) {
            // Not in cache - fetch it with proper config
            label = fetchLabelUncached(iri, config);
            // Store in cache with language-specific key
            labelCache.put(cacheKey, label);
        }
        
        // Apply formatting based on this instance's config
        return LabelFormatter.changeLabelToTheConfiguredFormat(label, config);
    }

    public static String fetchLabelUncached(String iri, AppConfig config) throws IOException {
        long startTime = System.currentTimeMillis();
        
        // Extract the base vocabulary URI
        String vocabularyUri = extractBaseUri(iri);
        
        try {
            // EARLY CHECK: Skip if host previously failed (before checking vocabulary cache)
            String host = extractHost(vocabularyUri);
            
            // Check if this host is in the failed hosts cache AND has value true (actually failed)
            Boolean cachedFailure = failedHostsCache.getIfPresent(host);
            if (host != null && cachedFailure != null && cachedFailure) {
                //logger.warning("SKIPPING (host previously failed): " + host + " → IRI: " + iri + " → vocabularyUri: " + vocabularyUri);
                ValueFactory vf = SimpleValueFactory.getInstance();
                IRI propertyUrlIRI = vf.createIRI(iri);
                return propertyUrlIRI.getLocalName();
            } else if (host != null) {
                //logger.info("HOST NOT IN CACHE (will attempt fetch): " + host + " → vocabularyUri: " + vocabularyUri);
            }
            
            // Check if vocabulary is already in cache
            Model cachedModel = vocabularyCache.getIfPresent(vocabularyUri);
            boolean isCached = cachedModel != null;
            
            // Check if this vocabulary was previously marked as failed (empty model)
            if (isCached && cachedModel.isEmpty()) {
                // Vocabulary fetch failed before - return local name immediately
                //logger.fine("CACHED (failed vocab): " + vocabularyUri + " → using local name for: " + iri);
                ValueFactory vf = SimpleValueFactory.getInstance();
                IRI propertyUrlIRI = vf.createIRI(iri);
                return propertyUrlIRI.getLocalName();
            }
            
            if (isCached) {
                //logger.fine("CACHED vocab: " + vocabularyUri + " → IRI: " + iri);
            } else {
                //logger.warning("NEW vocab fetch needed: " + vocabularyUri + " → IRI: " + iri);
            }
            
            // Get or fetch the vocabulary model from cache
            Model model = vocabularyCache.get(vocabularyUri);
            
            // Check again if the newly fetched model is empty (fetch failed)
            if (model.isEmpty()) {
                // Fetch failed - return local name
                // logger.fine("Vocabulary fetch resulted in empty model: " + vocabularyUri);
                ValueFactory vf = SimpleValueFactory.getInstance();
                IRI propertyUrlIRI = vf.createIRI(iri);
                return propertyUrlIRI.getLocalName();
            }
            
            // PERFORMANCE: Skip label lookup for very large vocabularies (e.g., QUDT with 3.8MB)
            // Searching through thousands of statements for a label that probably doesn't exist is extremely slow
            /* 
            long modelSize = model.size();
            if (modelSize > 5000) {  // If vocabulary has more than 5000 statements
                logger.info("Skipping label lookup for large vocabulary (" + modelSize + " statements): " + vocabularyUri);
                ValueFactory vf = SimpleValueFactory.getInstance();
                IRI propertyUrlIRI = vf.createIRI(iri);
                return propertyUrlIRI.getLocalName();
            }
            */
            // Find the label in the model
            String label = findLabelForIRI(model, iri, config);
            
            // long duration = System.currentTimeMillis() - startTime;
            //logger.info( "Label '" + label + "' retrieved  (cached vocab: " + isCached + ")");
            
            // Log cache statistics periodically
            // if (vocabularyCache.size() > 0) {
            //     logger.fine(" Vocab cache: Size=" + vocabularyCache.size() + 
            //                ", Hits=" + vocabularyCache.stats().hitCount() + 
            //                ", Misses=" + vocabularyCache.stats().missCount() +
            //                ", HitRate=" + String.format("%.1f%%", vocabularyCache.stats().hitRate() * 100));
            // }
            
            // Note: Label formatting now happens in fetchLabel() per-instance
            return label;
            
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            String host = extractHost(vocabularyUri);
            
            // DETAILED EXCEPTION LOGGING
            logger.warning("=== VOCABULARY FETCH FAILED ===");
            logger.warning("  IRI: " + iri);
            logger.warning("  Vocabulary URI: " + vocabularyUri);
            logger.warning("  Host: " + host);
            logger.warning("  Exception Type: " + cause.getClass().getName());
            logger.warning("  Exception Message: " + cause.getMessage());
            if (cause.getCause() != null) {
                logger.warning("  Root Cause: " + cause.getCause().getClass().getName() + ": " + cause.getCause().getMessage());
            }
            logger.warning("  Stack trace: " + cause.toString());
            logger.warning("===============================");

            // Check if it's a network-related failure (host unreachable, DNS failure, etc.)
            if (cause instanceof java.net.UnknownHostException /* || 
                 cause instanceof java.net.SocketTimeoutException ||
                cause instanceof java.net.ConnectException ||
                cause instanceof org.apache.http.conn.ConnectTimeoutException ||
                cause instanceof org.apache.http.conn.HttpHostConnectException */) {
                
                // Mark this host as failed to prevent future attempts
                if (host != null) {
                    failedHostsCache.put(host, true);
                    logger.warning("✓ CACHED failed host '" + host + "' due to " + cause.getClass().getSimpleName() + 
                                 " for vocabularyUri: " + vocabularyUri);
                } else {
                    logger.warning("✗ CANNOT CACHE failed host (host is null) for vocabularyUri: " + vocabularyUri);
                }
            } else {
                // Log non-network failures for debugging - these should NOT cache the host
                logger.warning("Vocabulary fetch failed but NOT caching host (" + cause.getClass().getSimpleName() + 
                          ") for vocabularyUri: " + vocabularyUri);
            }
            
            // Cache an empty model to prevent retrying this vocabulary
            vocabularyCache.put(vocabularyUri, ModelFactory.createDefaultModel());
            logger.warning("Cached empty model for failed vocabulary: " + vocabularyUri);
            
            // Fall back to local name
            ValueFactory vf = SimpleValueFactory.getInstance();
            IRI propertyUrlIRI = vf.createIRI(iri);
            return propertyUrlIRI.getLocalName();
        }
    }
    
    /**
     * Fetch and parse an RDF vocabulary from its base URI.
     * This method is called by the vocabulary cache when a vocabulary hasn't been loaded yet.
     * 
     * @param vocabularyUri The base URI of the vocabulary to fetch
     * @return Parsed Jena Model containing the vocabulary
     * @throws IOException if fetching or parsing fails
     */
    private static Model fetchVocabularyModel(String vocabularyUri) throws IOException {
        long startTime = System.currentTimeMillis();
        //logger.warning("========================================");
        //logger.warning("FETCHING NEW VOCABULARY: " + vocabularyUri);
        //logger.warning("========================================");
        
        HttpGet httpGet = new HttpGet(vocabularyUri);
        
        // Configure request timeouts
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .build();
        httpGet.setConfig(requestConfig);
        
        // Set Accept header for RDF formats
        httpGet.addHeader("Accept",
                "application/rdf+xml, " +
                "text/turtle, " +
                "application/ld+json, " +
                "application/n-triples, " +
                "application/n-quads, " +
                "application/trig");

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                logger.warning("HTTP request failed for vocabulary " + vocabularyUri + ": " + response.getStatusLine());
                logger.fine("Returning empty model to cache this failed vocabulary fetch");
                // Return empty model to cache the failure - prevents retrying for all IRIs from this vocabulary
                return ModelFactory.createDefaultModel();
            }
            
            // Get content as bytes
            Header contentLengthHeader = response.getFirstHeader("Content-Length");
            if (contentLengthHeader != null) {
                long size = Long.parseLong(contentLengthHeader.getValue());
                //logger.info("Vocabulary size: " + size + " bytes for " + vocabularyUri);
            }
            
            byte[] content = EntityUtils.toByteArray(response.getEntity());
            String contentType = response.getEntity().getContentType().getValue();
            Lang lang = determineLang(contentType);
            
            if (lang == null) {
                throw new IOException("Unsupported RDF format: " + contentType);
            }
            
            // Parse into a temporary full model
            Model fullModel = ModelFactory.createDefaultModel();
            
            try (InputStream in = new ByteArrayInputStream(content)) {
                // Using RDFDataMgr for parsing
                try {
                    if (vocabularyUri.startsWith(VANN_PREFIX)) {
                        RDFDataMgr.read(fullModel, in, VANN_RDF_FILE, lang);
                    } else if (vocabularyUri.startsWith(SCHEMA_PREFIX)) {
                        RDFDataMgr.read(fullModel, in, SCHEMA_RDF_FILE, lang);
                    } else {
                        RDFDataMgr.read(fullModel, in, lang);
                    }
                } catch (org.apache.jena.riot.RiotException e) {
                    logger.log(Level.WARNING, "RiotException while parsing vocabulary " + vocabularyUri + ": " + e.getMessage());
                    throw new IOException("Failed to parse vocabulary RDF", e);
                }
            }
            
            long parseTime = System.currentTimeMillis() - startTime;
            logger.log(Level.INFO, "Parsed vocabulary " + vocabularyUri + " in " + parseTime + "ms");
            
            // Filter: keep ONLY label-related triples to reduce memory and search time
            Model filteredModel = ModelFactory.createDefaultModel();
            String[] labelPredicates = {
                RDFS.label.getURI(),
                "http://www.w3.org/2000/01/rdf-schema#label",
                "http://www.w3.org/2004/02/skos/core#prefLabel",
                "http://purl.org/dc/elements/1.1/title",
                "http://purl.org/dc/terms/title",
                "http://www.w3.org/2000/01/rdf-schema#comment"
            };
            
            long filterStart = System.currentTimeMillis();
            for (String labelPredicate : labelPredicates) {
                Property predicate = fullModel.createProperty(labelPredicate);
                StmtIterator iter = fullModel.listStatements(null, predicate, (RDFNode) null);
                while (iter.hasNext()) {
                    filteredModel.add(iter.nextStatement());
                }
                iter.close();
            }
            
            long originalSize = fullModel.size();
            long filteredSize = filteredModel.size();
            long filterTime = System.currentTimeMillis() - filterStart;
            long totalTime = System.currentTimeMillis() - startTime;
            
            //logger.log(Level.INFO, "Filtered vocabulary from " + originalSize + " to " + filteredSize + 
            //          " triples (filtered in " + filterTime + "ms, total " + totalTime + "ms)");
            
            return filteredModel;
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
    private static String findLabelForIRI(Model model, String iri, AppConfig config) {
        // Fast path: If model is empty (failed vocabulary), return local name immediately
        if (model.isEmpty()) {
            return SimpleValueFactory.getInstance().createIRI(iri).getLocalName();
        }

        // Get the resource for the IRI
        Resource resource = model.getResource(iri);
        
        // Load preferred languages from config (or use defaults)
        List<String> preferredLanguages = (config != null) ? loadPreferredLanguagesStatic(config) : Arrays.asList("en", "cs");
        
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
            
            // Fast check: if no statements at all, skip to next predicate
            if (!labelStmts.hasNext()) {
                labelStmts.close();
                continue;
            }

            // Variables to store the best candidate found for this predicate
            Literal bestLiteralForPredicate = null;
            int bestLanguageScore = -1; // Score to compare language preference

            while (labelStmts.hasNext()) {
                Statement stmt = labelStmts.nextStatement();
                RDFNode object = stmt.getObject();
                // logger.finest("object of <" + stmt.getString() + ">: " + object.toString());
                if (object.isLiteral()) {
                    Literal literal = object.asLiteral();
                    String lang = literal.getLanguage(); // Get language tag (can be empty string "")
                    String lexicalForm = literal.getLexicalForm();

                    // logger.finest("Found literal for predicate " + predicateURI + ": '" + lexicalForm + "'@" + lang);

                    // Score this literal's language
                    int currentScore = scoreLanguage(lang, preferredLanguages);

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
                // logger.finest("Selected label: '" + bestLiteralForPredicate.getLexicalForm() + "'@" + bestLiteralForPredicate.getLanguage() + " for predicate " + predicateURI);
                return bestLiteralForPredicate.getLexicalForm();
            }
        }
        // If no label was found in any predicate, you could return the local name or null.
        // logger.fine("No suitable label found for resource: " + resource.getURI());
        return resource.getLocalName(); // Fallback to the URI's local part
    }

    /**
     * Scores a language tag based on the preferred languages list.
     * Higher score is better.
     *
     * @param lang The language tag from the literal (e.g., "en", "de", "").
     * @return A score representing the preference for this language.
     */
    private static int scoreLanguage(String lang, List<String> preferredLanguages) {
        // 1. Highest priority: Check if it's a preferred language.
        //    The index in the list determines priority (earlier = higher score).
        int preferredIndex = preferredLanguages.indexOf(lang.toLowerCase());
        // logger.finest("PREFERRED_LANGUAGES = [" + String.join(", ", PREFERRED_LANGUAGES) + "]");
        // logger.finest("PREFERRED_LANGUAGES.indexOf(lang) = " + preferredIndex);
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

        logger.log(Level.FINEST, "---------------------------findLabelForIRI for " + iri );


        logger.finest("resource in findLabelForIRI: " + resource.getURI() + " localName: " + resource.getLocalName() );

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

    /**
     * Load preferred languages from configuration.
     * @return list of preferred language codes
     * @deprecated Use {@link #loadPreferredLanguages(AppConfig)} instead
     */
    @Deprecated
    private List<String> loadPreferredLanguages() {
        return loadPreferredLanguages(this.config);
    }

    /**
     * Load preferred languages from AppConfig or configuration (static version).
     * @param config the application configuration
     * @return list of preferred language codes
     */
    private static List<String> loadPreferredLanguagesStatic(AppConfig config) {
        //logger.info("loadPreferredLanguages config: " + config.getPreferredLanguages());
        String configValue = config.getPreferredLanguages();
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
     * Load preferred languages from AppConfig or configuration.
     * @param config the application configuration
     * @return list of preferred language codes
     */
    private List<String> loadPreferredLanguages(AppConfig config) {
        //logger.info("loadPreferredLanguages config: " + config.getPreferredLanguages());
        String configValue = config.getPreferredLanguages();
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
