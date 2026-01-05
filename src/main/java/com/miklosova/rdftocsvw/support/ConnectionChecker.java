package com.miklosova.rdftocsvw.support;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * The Connection checker class.
 */
public class ConnectionChecker {
    private static Boolean cachedConnectionStatus = null;
    private static long lastCheckTime = 0;
    private static final long CACHE_DURATION_MS = 60000; // Cache for 60 seconds
    
    /**
     * Check if there is internet connection.
     * Result is cached for 60 seconds to avoid repeated network requests.
     *
     * @return True if the default URL is reachable.
     */
    public static boolean checkConnection() {
        long currentTime = System.currentTimeMillis();
        
        // Return cached result if still valid
        if (cachedConnectionStatus != null && (currentTime - lastCheckTime) < CACHE_DURATION_MS) {
            return cachedConnectionStatus;
        }
        
        // Perform actual connection check
        try {
            URL url = new URL("http://www.google.com");
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(2000); // 2 second timeout
            connection.connect();
            cachedConnectionStatus = true;
            lastCheckTime = currentTime;
            return true;
        } catch (IOException e) {
            cachedConnectionStatus = false;
            lastCheckTime = currentTime;
            return false;
        }
    }

    /**
     * Is given String a URL.
     *
     * @param fileName the file name to consider as a URL
     * @return True if fileName is URL and will be used for downloading the RDF file.
     */
    public static boolean isUrl(String fileName) {
        try {
            new URL(fileName);  // Try to create a URL object
            return true;         // If successful, the string is a valid URL
        } catch (MalformedURLException e) {
            return false;        // If an exception is thrown, the string is not a valid URL
        }
    }

    /**
     * Check if the given string is an absolute path.
     * Works for both Windows (C:\...) and Unix (/...) paths.
     *
     * @param path the path to check
     * @return true if the path is absolute
     */
    public static boolean isAbsolutePath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        // Check for Windows absolute path (e.g., C:\... or C:/...)
        if (path.length() >= 3 && Character.isLetter(path.charAt(0)) && 
            path.charAt(1) == ':' && (path.charAt(2) == '\\' || path.charAt(2) == '/')) {
            return true;
        }
        // Check for Unix absolute path (starts with /)
        return path.startsWith("/");
    }
}
