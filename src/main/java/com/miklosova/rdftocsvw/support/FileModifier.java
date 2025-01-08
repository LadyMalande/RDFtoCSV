package com.miklosova.rdftocsvw.support;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The file modifier. Used to correct the bad type of IRI that looks like IRI but instead begins with file:// and the RDF4J
 * parser is not able to parse that.
 */
public class FileModifier {
    private static final Logger logger = Logger.getLogger(FileModifier.class.getName());
    /**
     * Add colons to IRIs in file.
     *
     * @param file the file
     */
    public static void addColonsToIRIsInFile(File file) {
        // Define the regex pattern and replacement string
        String regex = "<([^:>]+)>";
        String replacement = "<file://$1>";

        // Read file content
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getCause() + " " + e.getLocalizedMessage());
        }

        // Replace all occurrences of the regex pattern
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        String modifiedContent = matcher.replaceAll(replacement);

        // Write modified content back to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(modifiedContent);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getCause() + " " + e.getLocalizedMessage());
        }
    }
}
