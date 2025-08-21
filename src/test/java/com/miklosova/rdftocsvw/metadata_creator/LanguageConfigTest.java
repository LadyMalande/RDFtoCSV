package com.miklosova.rdftocsvw.metadata_creator;

import com.miklosova.rdftocsvw.support.ConfigurationManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LanguageConfigTest {
    @ParameterizedTest
    @MethodSource("languageTestCases")
    void loadPreferredLanguagesTest(String mockedConfig, String expectedConfig) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        try (MockedStatic<ConfigurationManager> mocked = Mockito.mockStatic(ConfigurationManager.class)) {
            // Mock the config loader to return camel case setting
            mocked.when(() -> ConfigurationManager.loadConfig("app.preferredLanguages"))
                    .thenReturn(mockedConfig);
            // Invoke the method
            Dereferencer instance = new Dereferencer("https://publications.europa.eu/resource/authority/eurovoc/2294");
            // Get the private method
            Method method = Dereferencer.class.getDeclaredMethod("loadPreferredLanguages");

            // Make it accessible
            method.setAccessible(true);

            // Invoke the method
            Object result = method.invoke(instance);
            for(String oneExpectedLanguage : expectedConfig.split(",")){
                assertTrue(((List<String>)result).contains(oneExpectedLanguage));
            }
        }
    }

    @Test
    void languageScoring_WithEmptyConfig_UsesDefaultLanguages() throws Exception {
        try (MockedStatic<ConfigurationManager> mocked = Mockito.mockStatic(ConfigurationManager.class)) {
            mocked.when(() -> ConfigurationManager.loadConfig("app.preferredLanguages"))
                    .thenReturn("");

            Dereferencer instance = new Dereferencer("https://publications.europa.eu/resource/authority/eurovoc/2294");
            Method method = Dereferencer.class.getDeclaredMethod("scoreLanguage", String.class);
            method.setAccessible(true);

            // Should use default languages ["en", "cs"]
            assertEquals(1000, method.invoke(instance, "en"));  // first default
            assertEquals(999, method.invoke(instance, "cs"));   // second default
            assertEquals(0, method.invoke(instance, "fr"));     // non-default
            assertEquals(500, method.invoke(instance, ""));     // empty language
        }
    }
    @ParameterizedTest
    @MethodSource("languageScoringTestCases")
    void languageScoringTest(String language, String preferredLanguageConfig, int expectedScore) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        try (MockedStatic<ConfigurationManager> mocked = Mockito.mockStatic(ConfigurationManager.class)) {
            // Mock the config loader to return camel case setting
            mocked.when(() -> ConfigurationManager.loadConfig("app.preferredLanguages"))
                    .thenReturn(preferredLanguageConfig);
            // Invoke the method
            Dereferencer instance = new Dereferencer("https://publications.europa.eu/resource/authority/eurovoc/2294");
            // Get the private method
            Method method = Dereferencer.class.getDeclaredMethod("scoreLanguage", String.class);
            Method method2 = Dereferencer.class.getDeclaredMethod("loadPreferredLanguages");
            method2.setAccessible(true);
            instance.setPreferredLanguagesForTesting((List<String>) method2.invoke(instance));
            // Make it accessible
            method.setAccessible(true);

            // Invoke the method
            Object result = method.invoke(instance, language);

            assertEquals(expectedScore, result);

        }
    }

    @ParameterizedTest
    @MethodSource("fetchLabelLangTagTestCases")
    void fetchLabelLangTagTest(String iri, String preferredLanguageConfig, String expectedLabel) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, IOException, ExecutionException {
        try (MockedStatic<ConfigurationManager> mocked = Mockito.mockStatic(ConfigurationManager.class)) {
            // Mock the config loader to return camel case setting
            mocked.when(() -> ConfigurationManager.loadConfig("app.preferredLanguages"))
                    .thenReturn(preferredLanguageConfig);
            mocked.when(() -> ConfigurationManager.loadConfig("app.columnNamingConvention"))
                    .thenReturn("none");
            // Invoke the method
            Dereferencer instance = new Dereferencer(iri);
            // Get the private method
            Method method2 = Dereferencer.class.getDeclaredMethod("loadPreferredLanguages");
            method2.setAccessible(true);
            instance.setPreferredLanguagesForTesting((List<String>) method2.invoke(instance));
            // Make it accessible
            if(iri.contains("nonexistent-property") || iri.contains("does-not-exist")){
                assertEquals(expectedLabel, Dereferencer.fetchLabel(iri));
            } else {
                assertEquals(expectedLabel, Dereferencer.fetchLabelUncached(iri));
            }

        }
    }

    @Test
    void loadPreferredLanguages_WhenConfigIsNull_ShouldReturnDefault() throws Exception {
        try (MockedStatic<ConfigurationManager> mocked = Mockito.mockStatic(ConfigurationManager.class)) {
            // Mock returning null
            mocked.when(() -> ConfigurationManager.loadConfig("app.preferredLanguages"))
                    .thenReturn(null);

            Dereferencer instance = new Dereferencer("https://publications.europa.eu/resource/authority/eurovoc/2294");
            Method method = Dereferencer.class.getDeclaredMethod("loadPreferredLanguages");
            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            List<String> result = (List<String>) method.invoke(instance);

            assertEquals(Arrays.asList("en", "cs"), result);
        }
    }

    private static Stream<Arguments> fetchLabelLangTagTestCases() {
        return Stream.of(
                // Dublin Core Terms - well known multilingual support
                Arguments.of("http://purl.org/dc/terms/title", "en,de,fr", "Title"),
                Arguments.of("http://purl.org/dc/terms/title", "de,en,fr", "Title"),
                Arguments.of("http://purl.org/dc/terms/title", "fr,en,de", "Title"),
                Arguments.of("http://purl.org/dc/terms/title", "es,it,en", "Title"),

                // FOAF (Friend of a Friend) properties
                Arguments.of("http://xmlns.com/foaf/0.1/name", "en,de,fr", "name"),
                Arguments.of("http://xmlns.com/foaf/0.1/name", "de,en,fr", "name"),
                Arguments.of("http://xmlns.com/foaf/0.1/name", "fr,en,de", "name"),

                // SKOS (Simple Knowledge Organization System)
                Arguments.of("http://www.w3.org/2004/02/skos/core#prefLabel", "en,de,fr", "preferred label"),
                Arguments.of("http://www.w3.org/2004/02/skos/core#prefLabel", "de,en,fr", "preferred label"),
                Arguments.of("http://www.w3.org/2004/02/skos/core#prefLabel", "fr,en,de", "preferred label"),

                // RDF Schema properties
                Arguments.of("http://www.w3.org/2000/01/rdf-schema#label", "en,de,fr", "label"),
                Arguments.of("http://www.w3.org/2000/01/rdf-schema#label", "de,en,fr", "label"),
                Arguments.of("http://www.w3.org/2000/01/rdf-schema#label", "fr,en,de", "label"),

                // OWL (Web Ontology Language)
                Arguments.of("http://www.w3.org/2002/07/owl#Class", "en,de,fr", "Class"),
                Arguments.of("http://www.w3.org/2002/07/owl#Class", "de,en,fr", "Class"),
                Arguments.of("http://www.w3.org/2002/07/owl#Class", "fr,en,de", "Class"),

                // Schema.org properties (good multilingual support)
                Arguments.of("http://schema.org/name", "en,de,fr", "name"),
                Arguments.of("http://schema.org/name", "de,en,fr", "name"),
                Arguments.of("http://schema.org/name", "fr,en,de", "name"),
                Arguments.of("http://schema.org/description", "en,de,fr", "description"),
                Arguments.of("http://schema.org/description", "de,en,fr", "description"),
                Arguments.of("http://schema.org/description", "fr,en,de", "description"),

                // DBpedia Ontology
                Arguments.of("http://dbpedia.org/ontology/abstract", "en,de,fr", "has abstract"),
                Arguments.of("http://dbpedia.org/ontology/abstract", "ur,de,fr", "خلاصہ"),
                Arguments.of("http://dbpedia.org/ontology/abstract", "el,de,fr", "έχει περίληψη"),
                Arguments.of("http://dbpedia.org/ontology/abstract", "sr,de,fr", "апстракт"),
                Arguments.of("http://dbpedia.org/ontology/abstract", "de,en,fr", "abstract"),
                Arguments.of("http://dbpedia.org/ontology/abstract", "fr,en,de", "has abstract"),

                // EUROVOC (multilingual thesaurus)
                Arguments.of("http://eurovoc.europa.eu/100142", "en,de,fr", "04 POLITICS"),
                Arguments.of("http://eurovoc.europa.eu/100142", "de,en,fr", "04 POLITISCHES LEBEN"),
                Arguments.of("http://eurovoc.europa.eu/100142", "fr,en,de", "04 VIE POLITIQUE"),

                // Geonames ontology
                Arguments.of("http://www.geonames.org/ontology#name", "en,de,fr", "name"),
                Arguments.of("http://www.geonames.org/ontology#name", "de,en,fr", "name"),
                Arguments.of("http://www.geonames.org/ontology#name", "fr,en,de", "name"),

                // Test cases with fallback to English (many ontologies have only English labels)
                Arguments.of("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "de,fr,es", "type"),
                Arguments.of("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "it,pt,ru", "type"),

                // Test cases with non-existent IRIs
                Arguments.of("http://example.org/nonexistent-property", "en,de,fr", "nonexistent-property"),
                Arguments.of("http://invalid.iri/does-not-exist", "en,de,fr", "does-not-exist"),

                // Test cases with empty config (should use default languages)
                Arguments.of("http://purl.org/dc/terms/title", "", "Title"),
                Arguments.of("http://purl.org/dc/terms/title", "   ", "Title"),

                // Test cases with single language preference
                Arguments.of("http://purl.org/dc/terms/title", "de", "Title"),
                Arguments.of("http://purl.org/dc/terms/title", "fr", "Title"),
                Arguments.of("http://purl.org/dc/terms/title", "es", "Title"),

                // Test cases with complex language preferences
                Arguments.of("http://purl.org/dc/terms/title", "en,de,fr,es,it,pt", "Title"),
                Arguments.of("http://purl.org/dc/terms/title", "it,pt,es,fr,de,en", "Title"),

                // Test cases with language variants
                Arguments.of("http://purl.org/dc/terms/title", "en-US,en-GB,de", "Title"),
                Arguments.of("http://purl.org/dc/terms/title", "en-GB,en-US,de", "Title")
        );
    }

    private static Stream<Arguments> languageScoringTestCases() {
        return Stream.of(
                // Normal cases - exact matches in preferred languages
                Arguments.of("cs", "cs", 1000),
                Arguments.of("en", "en,es", 1000),
                Arguments.of("es", "fr,en,es", 998),
                Arguments.of("fr", "fr,en,es", 1000), // first position
                Arguments.of("es", "en,es,fr", 999),  // second position
                Arguments.of("fr", "en,es,fr", 998),  // third position
                // Case sensitivity tests
                Arguments.of("EN", "en,es,fr", 1000), // uppercase match
                Arguments.of("en", "EN,es,fr", 1000), // uppercase in config
                Arguments.of("En", "en,ES,fr", 1000), // mixed case

                // Whitespace handling
                Arguments.of("en", " en , es , fr ", 1000), // spaces around config
                Arguments.of("en", "  en  ,  es  ,  fr  ", 1000), // multiple spaces

                // Empty language tag cases
                Arguments.of("", "en,es,fr", 500),    // empty language with preferred langs
                Arguments.of("", "", 500),            // empty language with empty config
                Arguments.of("", "   ", 500),         // empty language with whitespace config

                // Non-preferred languages
                Arguments.of("de", "en,es,fr", 0),    // language not in preferred list
                Arguments.of("zh", "en,es,fr", 0),    // completely different language
                Arguments.of("unknown", "en,es,fr", 0), // made up language code

                // Edge cases with malformed config
                Arguments.of("en", ",,en,,", 1000),   // malformed config with empty elements
                Arguments.of("en", "en,,es", 1000),   // empty middle element
                Arguments.of("en", ",en,es", 1000),   // empty first element
                Arguments.of("es", "en,es,", 999),    // empty last element

                // Single element cases
                Arguments.of("en", "en", 1000),       // single preferred language
                Arguments.of("fr", "en", 0),          // non-matching single config
                Arguments.of("", "en", 500),          // empty language with single config

                // Long list scenarios
                Arguments.of("it", "en,de,fr,es,it,cs,pl,ru", 996), // 5th position (index 4)
                Arguments.of("ru", "en,de,fr,es,it,cs,pl,ru", 993), // 8th position (index 7)

                // Special language codes
                Arguments.of("en-US", "en-US,en-GB,es", 1000), // locale codes
                Arguments.of("en-GB", "en-US,en-GB,es", 999),
                Arguments.of("es-ES", "en-US,en-GB,es-ES", 998),

                // Config with only whitespace/empty (should use default)
                Arguments.of("en", "", 1000),         // empty config should use default
                Arguments.of("cs", "   ", 999),      // whitespace config should use default
                Arguments.of("", "", 500),            // empty language with empty config
                Arguments.of("de", "", 0),            // non-preferred with empty config

                // Null safety (though your method should handle this)
                Arguments.of("en", null, 1000)        // null config should use default

        );
    }

    private static Stream<Arguments> languageTestCases() {
        return Stream.of(
                // Normal cases
                Arguments.of("cs", "cs"),
                Arguments.of("en,es", "en,es"),
                Arguments.of("fr,en,es", "fr,en,es"),

                // Cases with whitespace
                Arguments.of(" en , cs ", "en,cs"), // leading/trailing spaces
                Arguments.of("en, cs, fr", "en,cs,fr"), // spaces after commas
                Arguments.of("  en  ,  cs  ,  fr  ", "en,cs,fr"), // multiple spaces

                // Edge cases with empty/malformed values
                Arguments.of("", "en,cs"), // empty string should return default
                Arguments.of("   ", "en,cs"), // whitespace only should return default
                Arguments.of("en,,cs", "en,cs"), // empty middle element
                Arguments.of(",en,cs", "en,cs"), // empty first element
                Arguments.of("en,cs,", "en,cs"), // empty last element
                Arguments.of(",,,", "en,cs"), // only commas/empty

                // Single element edge cases
                Arguments.of("en", "en"),
                Arguments.of(" en ", "en"), // single with spaces

                // Mixed case scenarios
                Arguments.of("EN,cs,FR", "EN,cs,FR"), // mixed case languages
                Arguments.of("en-US,cs-CZ", "en-US,cs-CZ"), // locale formats

                // Long list
                Arguments.of("en,de,fr,es,it,cs,pl,ru", "en,de,fr,es,it,cs,pl,ru")
        );
    }
}
