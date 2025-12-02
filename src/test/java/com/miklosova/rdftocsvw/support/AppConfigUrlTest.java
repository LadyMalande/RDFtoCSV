package com.miklosova.rdftocsvw.support;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for AppConfig URL support.
 * Verifies that URLs can be used as valid file input.
 */
class AppConfigUrlTest {

    @Test
    void testBuilderAcceptsHttpUrl() {
        // Test that HTTP URLs are accepted as valid file input
        AppConfig config = new AppConfig.Builder("http://example.org/data.ttl").build();
        
        assertNotNull(config);
        assertEquals("http://example.org/data.ttl", config.getFile());
    }

    @Test
    void testBuilderAcceptsHttpsUrl() {
        // Test that HTTPS URLs are accepted as valid file input
        AppConfig config = new AppConfig.Builder("https://example.org/data.rdf").build();
        
        assertNotNull(config);
        assertEquals("https://example.org/data.rdf", config.getFile());
    }

    @Test
    void testBuilderAcceptsUrlWithQueryParameters() {
        // Test that URLs with query parameters are accepted
        String urlWithParams = "https://example.org/data.ttl?format=turtle&encoding=utf8";
        AppConfig config = new AppConfig.Builder(urlWithParams).build();
        
        assertNotNull(config);
        assertEquals(urlWithParams, config.getFile());
    }

    @Test
    void testBuilderAcceptsUrlWithFragment() {
        // Test that URLs with fragments are accepted
        String urlWithFragment = "https://example.org/data.ttl#section1";
        AppConfig config = new AppConfig.Builder(urlWithFragment).build();
        
        assertNotNull(config);
        assertEquals(urlWithFragment, config.getFile());
    }

    @Test
    void testBuilderAcceptsLocalFilePath() {
        // Test that local file paths still work
        AppConfig config = new AppConfig.Builder("test.ttl").build();
        
        assertNotNull(config);
        assertEquals("test.ttl", config.getFile());
    }

    @Test
    void testBuilderAcceptsAbsoluteFilePath() {
        // Test that absolute file paths still work
        AppConfig config = new AppConfig.Builder("C:\\Users\\test\\data.ttl").build();
        
        assertNotNull(config);
        assertEquals("C:\\Users\\test\\data.ttl", config.getFile());
    }

    @Test
    void testUrlConfigWithMultipleParameters() {
        // Test URL input with other configuration parameters
        AppConfig config = new AppConfig.Builder("https://example.org/data.ttl")
                .parsing("rdf4j")
                .multipleTables(true)
                .output("output.csv")
                .preferredLanguages("en,cs")
                .build();
        
        assertNotNull(config);
        assertEquals("https://example.org/data.ttl", config.getFile());
        assertEquals("rdf4j", config.getParsing());
        assertTrue(config.getMultipleTables());
        assertEquals("output.csv", config.getOutput());
        assertEquals("en,cs", config.getPreferredLanguages());
    }

    @Test
    void testUrlConfigWithStreamingMode() {
        // Test URL input with streaming mode
        // Note: Streaming typically requires .nt files, but URLs may download different formats
        AppConfig config = new AppConfig.Builder("https://example.org/data.nt")
                .parsing("streaming")
                .streaming(true)
                .build();
        
        assertNotNull(config);
        assertEquals("https://example.org/data.nt", config.getFile());
        assertEquals("streaming", config.getParsing());
        assertTrue(config.getStreaming());
    }
}
