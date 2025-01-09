package com.miklosova.rdftocsvw.support;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * The Connection checker class.
 */
public class ConnectionChecker {
    /**
     * Check if there is internet connection.
     *
     * @return True if the default URL is reachable.
     */
    public static boolean checkConnection() {
        try {
            URL url = new URL("http://www.google.com");
            URLConnection connection = url.openConnection();
            connection.connect();
            return true;
        } catch (IOException e) {
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
}
