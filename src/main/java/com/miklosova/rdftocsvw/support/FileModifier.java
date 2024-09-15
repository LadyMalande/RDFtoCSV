package com.miklosova.rdftocsvw.support;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileModifier {

    public static void addColonsToIRIsInFile(File file) {
        String inputFilePath = "input.txt";
        String outputFilePath = "output.txt";

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
            e.printStackTrace();
        }

        // Replace all occurrences of the regex pattern
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        String modifiedContent = matcher.replaceAll(replacement);

        // Write modified content back to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(modifiedContent);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
