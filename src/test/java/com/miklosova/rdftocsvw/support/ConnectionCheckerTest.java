package com.miklosova.rdftocsvw.support;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
class ConnectionCheckerTest {

    @Test
    void testCheckConnection_SuccessfulConnection() throws IOException {
        // Test that the connection succeeds
        assertTrue(ConnectionChecker.checkConnection());
    }

    @Test
    void testIsUrl_ValidUrl() {
        // Test with valid URLs
        assertTrue(ConnectionChecker.isUrl("http://www.google.com"));
        assertTrue(ConnectionChecker.isUrl("https://www.example.com/path?query=123"));
    }

    @Test
    void testIsUrl_InvalidUrl() {
        // Test with invalid URLs
        assertFalse(ConnectionChecker.isUrl("invalid-url"));
    }

    @Test
    void testIsUrl_EmptyOrNull() {
        // Test with empty and null values
        assertFalse(ConnectionChecker.isUrl(""));
        assertFalse(ConnectionChecker.isUrl(null));
    }
}
