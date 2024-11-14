package com.miklosova.rdftocsvw.support;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ConnectionChecker {
    public static boolean checkConnection() {
        try {
            URL url = new URL("http://www.google.com");
            URLConnection connection = url.openConnection();
            connection.connect();
            //System.out.println("Internet is connected");
            return true;
        } catch (MalformedURLException e) {
            //System.out.println("Internet is not connected");
            return false;
        } catch (IOException e) {
            //System.out.println("Internet is not connected");
            return false;
        }
    }

    public static boolean isUrl(String fileName) {
        try {
            new URL(fileName);  // Try to create a URL object
            return true;         // If successful, the string is a valid URL
        } catch (MalformedURLException e) {
            return false;        // If an exception is thrown, the string is not a valid URL
        }
    }
}
