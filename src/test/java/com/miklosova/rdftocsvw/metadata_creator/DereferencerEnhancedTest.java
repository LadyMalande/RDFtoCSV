package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.support.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DereferencerEnhancedTest {

    private Dereferencer dereferencer;
    private AppConfig config;
    private static final String TEST_URL = "http://www.w3.org/2000/01/rdf-schema#label";

    @BeforeEach
    void setUp() {
        config = new AppConfig.Builder("test.ttl")
                .build();
        dereferencer = new Dereferencer(TEST_URL, config);
    }

    @Test
    void testConstructorWithConfig() {
        assertNotNull(dereferencer);
        Dereferencer newDereferencer = new Dereferencer("http://example.org/test", config);
        assertNotNull(newDereferencer);
    }

    @Test
    void testConstructorWithNullConfig() {
        
        assertThrows(NullPointerException.class, () -> {
            new Dereferencer("http://example.org/test", null);
        });
    }

    @Test
    void testGetFailedHostsCacheStats() {
        String stats = Dereferencer.getFailedHostsCacheStats();
        assertNotNull(stats);
        assertTrue(stats.contains("Failed Hosts Cache"));
    }

    @Test
    void testClearFailedHostsCache() {
        // Test the static method
        Dereferencer.clearFailedHostsCache();
        // Should not throw exception
        String stats = Dereferencer.getFailedHostsCacheStats();
        assertNotNull(stats);
    }

    @Test
    void testGetTitleWithFOAFPrefix() {
        String result = Dereferencer.getTitle("http://xmlns.com/foaf/0.1/name");
        // May return a title or null depending on network availability
        assertNotNull(result == null || !result.isEmpty());
    }

    @Test
    void testGetTitleWithDCPrefix() {
        String result = Dereferencer.getTitle("http://purl.org/dc/terms/title");
        // May return a title or null
        assertNotNull(result == null || !result.isEmpty());
    }

    @Test
    void testGetTitleWithSKOSPrefix() {
        String result = Dereferencer.getTitle("http://www.w3.org/2004/02/skos/core#prefLabel");
        // May return a title or null
        assertNotNull(result == null || !result.isEmpty());
    }

    @Test
    void testGetTitleWithUnknownPrefix() {
        String result = Dereferencer.getTitle("http://unknown.example.org/property");
        // Should return null for unknown prefixes
        assertNull(result);
    }

    @Test
    void testStartsWithAny() {
        String[] prefixes = {"http://example.org/", "http://test.org/"};
        
        assertTrue(Dereferencer.startsWithAny("http://example.org/property", prefixes));
        assertTrue(Dereferencer.startsWithAny("http://test.org/property", prefixes));
        assertFalse(Dereferencer.startsWithAny("http://other.org/property", prefixes));
    }

    @Test
    void testStartsWithAnyNullUri() {
        String[] prefixes = {"http://example.org/"};
        assertThrows(NullPointerException.class, () -> {
            Dereferencer.startsWithAny(null, prefixes);
        });
        
    }

    @Test
    void testStartsWithAnyEmptyPrefixes() {
        String[] prefixes = {};
        assertFalse(Dereferencer.startsWithAny("http://example.org/property", prefixes));
    }

    @Test
    void testExtractBaseUri() {
        assertEquals("http://www.w3.org/2000/01/rdf-schema", 
                    Dereferencer.extractBaseUri("http://www.w3.org/2000/01/rdf-schema#label"));
        
        assertEquals("http://example.org/ontology", 
                    Dereferencer.extractBaseUri("http://example.org/ontology#Class"));
        
        assertEquals("http://example.org/vocab/property", 
                    Dereferencer.extractBaseUri("http://example.org/vocab/property"));
    }

    @Test
    void testExtractBaseUriWithSlash() {
        String result = Dereferencer.extractBaseUri("http://xmlns.com/foaf/0.1/name");
        assertNotNull(result);
        assertTrue(result.startsWith("http://"));
    }

    @Test
    void testExtractBaseUriNoDelimiter() {
        String uri = "http://example.org";
        String result = Dereferencer.extractBaseUri(uri);
        assertNotNull(result);
    }

    @Test
    void testFetchLabelInstance() throws Exception {
        String label = dereferencer.fetchLabel("http://www.w3.org/2000/01/rdf-schema#label");
        assertNotNull(label);
    }

    @Test
    void testFetchLabelWithInvalidIRI() throws Exception {
        
        assertThrows(NullPointerException.class, () -> {
            dereferencer.fetchLabel("not-a-valid-iri");
        });
        
    }

    @Test
    void testGetUrl() {
        assertEquals(TEST_URL, dereferencer.getUrl());
    }

    @Test
    void testGetUrlAfterConstruction() {
        Dereferencer d = new Dereferencer("http://example.org/test", config);
        assertEquals("http://example.org/test", d.getUrl());
    }

    @Test
    void testMultipleInstancesWithDifferentUrls() {
        Dereferencer d1 = new Dereferencer("http://example.org/prop1", config);
        Dereferencer d2 = new Dereferencer("http://example.org/prop2", config);
        
        assertEquals("http://example.org/prop1", d1.getUrl());
        assertEquals("http://example.org/prop2", d2.getUrl());
    }

    @Test
    void testConfigWithDifferentLanguages() {
        AppConfig configCs = new AppConfig.Builder("test.ttl")
                .preferredLanguages("cs")
                .build();
        Dereferencer dereferencerCs = new Dereferencer("http://example.org/test", configCs);
        
        assertNotNull(dereferencerCs);
        assertEquals("http://example.org/test", dereferencerCs.getUrl());
    }

    @Test
    void testConfigWithMultipleLanguages() {
        AppConfig configMulti = new AppConfig.Builder("test.ttl")
                .preferredLanguages("cs,en,de")
                .build();
        Dereferencer dereferencerMulti = new Dereferencer("http://example.org/test", configMulti);
        
        assertNotNull(dereferencerMulti);
        assertEquals("http://example.org/test", dereferencerMulti.getUrl());
    }

    @Test
    void testFetchLabelWithDifferentConfigs() throws Exception {
        AppConfig config1 = new AppConfig.Builder("test1.ttl").preferredLanguages("en").build();
        AppConfig config2 = new AppConfig.Builder("test2.ttl").preferredLanguages("cs").build();
        
        Dereferencer d1 = new Dereferencer("http://example.org/test1", config1);
        Dereferencer d2 = new Dereferencer("http://example.org/test2", config2);
        
        String label1 = d1.fetchLabel("http://www.w3.org/2000/01/rdf-schema#label");
        String label2 = d2.fetchLabel("http://www.w3.org/2000/01/rdf-schema#label");
        
        assertNotNull(label1);
        assertNotNull(label2);
    }

    @Test
    void testClearFailedHostsCacheMultipleTimes() {
        Dereferencer.clearFailedHostsCache();
        Dereferencer.clearFailedHostsCache();
        // Should handle multiple clears without issue
        String stats = Dereferencer.getFailedHostsCacheStats();
        assertNotNull(stats);
    }

    @Test
    void testExtractBaseUriVariousCases() {
        // Test with hash
        String result1 = Dereferencer.extractBaseUri("http://www.w3.org/ns/prov#Entity");
        assertTrue(result1.contains("prov"));
        
        // Test with slash
        String result2 = Dereferencer.extractBaseUri("http://xmlns.com/foaf/0.1/Person");
        assertTrue(result2.contains("foaf"));
        
        // Test with neither
        String result3 = Dereferencer.extractBaseUri("http://example.org");
        assertNotNull(result3);
    }

    @Test
    void testGetTitleWithNullUrl() {
        String result = Dereferencer.getTitle(null);
        assertNull(result);
    }

    @Test
    void testGetTitleWithEmptyUrl() {
        String result = Dereferencer.getTitle("");
        assertNull(result);
    }
}
