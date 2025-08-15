package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.support.ConfigurationManager;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LabelFormatter {

    public static final Logger logger = Logger.getLogger(LabelFormatter.class.getName());
    public static final String CAMEL_CASE_CONFIG_STRING = "camelCase";
    public static final String PASCAL_CASE_CONFIG_STRING = "PascalCase";
    public static final String SNAKE_CASE_CONFIG_STRING = "snake_case";
    public static final String SCREAMING_SNAKE_CASE_CONFIG_STRING = "SCREAMING_SNAKE_CASE";
    public static final String KEBAB_CASE_CONFIG_STRING = "kebab-case";
    public static final String TITLE_CASE_CONFIG_STRING = "Title Case";
    public static final String DOT_NOTATION_CASE_CONFIG_STRING = "dot.notation";

    public static String changeLabelToTheConfiguredFormat(String originalLabel){
        String formattedLabel = null;
        String formatting = ConfigurationManager.loadConfig("app.columnNamingConvention");

        logger.info("Configuration for app.columnNamingConvention = " + formatting);

        switch(formatting){
            case CAMEL_CASE_CONFIG_STRING: formattedLabel = toCamelCase(originalLabel);
                break;
            case PASCAL_CASE_CONFIG_STRING: formattedLabel = toPascalCase(originalLabel);
                break;
            case SNAKE_CASE_CONFIG_STRING: formattedLabel = toSnakeCase(originalLabel);
                break;
            case SCREAMING_SNAKE_CASE_CONFIG_STRING: formattedLabel = toScreamingSnakeCase(originalLabel);
                break;
            case KEBAB_CASE_CONFIG_STRING: formattedLabel = toKebabCase(originalLabel);
                break;
            case TITLE_CASE_CONFIG_STRING: formattedLabel = toTitleCase(originalLabel);
                break;
            case DOT_NOTATION_CASE_CONFIG_STRING: formattedLabel = toDotNotation(originalLabel);
                break;
            default: formattedLabel = originalLabel;
        }

        return formattedLabel;
    }

    public static String toPascalCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Insert space before capital letters to handle toCamelCase
        // Replace all non-alphanumeric characters with spaces
        String spaced = input.replaceAll("([A-Z])", " $1")
                .replaceAll("[^a-zA-Z0-9]", " ")
                .trim();

        String[] words = spaced.split(" +");

        if (words.length == 0) {
            return "";
        }

        return Arrays.stream(words)
                .map(word -> {
                    if (word.isEmpty()) {
                        return "";
                    }
                    // Handle acronyms (all uppercase)
                    if (word.equals(word.toUpperCase())) {
                        return word.charAt(0) + word.substring(1).toLowerCase();
                    }
                    return word.substring(0, 1).toUpperCase() +
                            word.substring(1).toLowerCase();
                })
                .collect(Collectors.joining());
    }

    private static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
/*
        if(input.equals(input.toUpperCase())){
            return input.toLowerCase();
        }

 */

        // Handle existing toCamelCase by inserting space before capitals
        String spaced = input.replaceAll("([A-Z])", " $1")
                .replaceAll("[^a-zA-Z0-9]", " ")
                .trim();

        String[] words = spaced.split(" +");

        if (words.length == 0) {
            return "";
        }

        // Process first word
        String firstWord = words[0].toLowerCase();

        // Process remaining words
        String camelCase = Arrays.stream(words, 1, words.length)
                .map(word -> {
                    if (word.length() == 0) return "";
                    // Check if word is all uppercase (acronym)
                    if (word.equals(word.toUpperCase())) {
                        return word.charAt(0) + word.substring(1).toLowerCase();
                    }
                    return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
                })
                .collect(Collectors.joining());

        return firstWord + camelCase;
    }

    public static String toSnakeCase(String input) {
        if (input == null) {
            return null;
        }
        if (input.isEmpty()) {
            return "";
        }

        // Step 1: Convert camelCase and acronyms to temporary format
        String step1 = input
                .replaceAll("([a-z])([A-Z])", "$1 $2")          // camelCase to space
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2");   // Handle acronyms

        // Step 2: Convert all non-alphanumeric to underscores
        String step2 = step1.replaceAll("[^a-zA-Z0-9]", "_");

        // Step 3: Clean up (lowercase, collapse underscores, trim)
        String result = step2.toLowerCase()
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        // Final cleanup for multiple word separators
        return result.replaceAll(" {2,}", " ")  // Collapse multiple spaces
                .replaceAll(" ", "_")      // Convert remaining spaces to underscores
                .replaceAll("_+", "_");    // Collapse underscores again
    }

    public static String toKebabCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Step 1: Convert camelCase and numbers to spaced format
        String step1 = input
                // Insert space before capital letters (camelCase)
                .replaceAll("([a-z0-9])([A-Z])", "$1 $2")
                // Insert space after numbers followed by letters
                .replaceAll("([0-9])([a-zA-Z])", "$1 $2")
                // Handle acronyms followed by lowercase
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2");

        // Step 2: Convert all non-alphanumeric to single hyphens
        String step2 = step1.replaceAll("[^a-zA-Z0-9]+", "-");

        // Step 3: Clean up (lowercase, remove leading/trailing hyphens)
        return step2.toLowerCase()
                .replaceAll("^-|-$", "");
    }

    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // First insert spaces between camelCase words but preserve number-letter combinations
        String spaced = input
                .replaceAll("([a-z])([A-Z])", "$1 $2")       // camelCase to space
                .replaceAll("([A-Z])([A-Z][a-z])", "$1 $2")   // Handle acronyms
                .replaceAll("([a-zA-Z])([0-9])([a-zA-Z])", "$1 $2 $3"); // Letters around numbers

        // Then split on any remaining non-alphanumeric characters except numbers
        return Arrays.stream(spaced.split("[^a-zA-Z0-9]+"))
                .filter(word -> !word.isEmpty())
                .map(word -> {
                    // Special handling for number-only "words"
                    if (word.matches("\\d+")) {
                        return word;
                    }
                    // Handle words with numbers at the end (like "Case123")
                    if (word.matches(".*\\d+$")) {
                        String letters = word.replaceAll("\\d+$", "");
                        String numbers = word.substring(letters.length());
                        return capitalize(letters) + numbers;
                    }
                    // Handle words with numbers at the start
                    if (word.matches("^\\d+.*")) {
                        String numbers = word.replaceAll("^(\\d+).*", "$1");
                        String letters = word.substring(numbers.length());
                        return numbers + capitalize(letters);
                    }
                    // Normal word capitalization
                    return capitalize(word);
                })
                .collect(Collectors.joining(" "));
    }

    private static String capitalize(String word) {
        if (word.isEmpty()) return word;
        return word.substring(0, 1).toUpperCase() +
                word.substring(1).toLowerCase();
    }

    public static String toScreamingSnakeCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Special case: already in SCREAMING_SNAKE_CASE
        if (input.equals(input.toUpperCase()) && input.contains("_")) {
            return input;
        }

        // Step 1: Convert camelCase to snake_case
        // Handle both standard camelCase and acronyms (like JSONData)
        String step1 = input
                // Insert underscore before capital letters not preceded by underscore
                .replaceAll("(?<=[^A-Z_])([A-Z])", "_$1")
                // Handle multiple capital letters (acronyms) followed by lowercase
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2");

        // Step 2: Convert any non-alphanumeric to underscore
        String step2 = step1.replaceAll("[^a-zA-Z0-9]", "_");

        // Step 3: Clean up (collapse underscores, trim, uppercase)
        return step2.replaceAll("_+", "_")
                .replaceAll("^_|_$", "")
                .toUpperCase();
    }

    public static String toDotNotation(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Step 1: Convert camelCase and acronyms to spaced format
        String step1 = input
                .replaceAll("([a-z])([A-Z])", "$1 $2")          // camelCase to space
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2");   // Handle acronyms

        // Step 2: Convert all non-alphanumeric to dots
        String step2 = step1.replaceAll("[^a-zA-Z0-9]", ".");

        // Step 3: Clean up (lowercase, collapse dots, trim)
        return step2.toLowerCase()
                .replaceAll("\\.+", ".")
                .replaceAll("^\\.|\\.$", "");
    }
}
