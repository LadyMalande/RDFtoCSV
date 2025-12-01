package com.miklosova.rdftocsvw.support;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for AppConfig and its Builder pattern.
 * Demonstrates both new AppConfig approach and validates functionality.
 */
class AppConfigTest {

    @Test
    void testBuilderWithRequiredParameterOnly() {
        AppConfig config = new AppConfig.Builder("test.ttl").build();
        
        assertEquals("test.ttl", config.getFile());
        assertEquals("rdf4j", config.getParsing());
        assertEquals(false, config.getMultipleTables());
        assertEquals(false, config.getStreaming());
        assertEquals(false, config.getFirstNormalForm());
        assertEquals("INFO", config.getLogLevel());
        assertEquals("original", config.getColumnNamingConvention());
        assertEquals("en,cs", config.getPreferredLanguages());
    }

    @Test
    void testBuilderWithAllParameters() {
        AppConfig config = new AppConfig.Builder("input.ttl")
                .parsing("rdf4j")
                .multipleTables(true)
                .streaming(false)
                .firstNormalForm(true)
                .output("output.csv")
                .preferredLanguages("en,de,fr")
                .columnNamingConvention("camelCase")
                .logLevel("INFO")
                .build();
        
        assertEquals("input.ttl", config.getFile());
        assertEquals("rdf4j", config.getParsing());
        assertTrue(config.getMultipleTables());
        assertFalse(config.getStreaming());
        assertTrue(config.getFirstNormalForm());
        assertEquals("output.csv", config.getOutput());
        assertEquals("en,de,fr", config.getPreferredLanguages());
        assertEquals("camelCase", config.getColumnNamingConvention());
        assertEquals("INFO", config.getLogLevel());
    }

    @Test
    void testBuilderValidationNullFile() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AppConfig.Builder(null).build();
        });
    }

    @Test
    void testBuilderValidationEmptyFile() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AppConfig.Builder("").build();
        });
    }

    @Test
    void testBuilderValidationInvalidParsingMethod() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AppConfig.Builder("test.ttl")
                    .parsing("invalid_method")
                    .build();
        });
    }

    @Test
    void testBuilderValidationInvalidLogLevel() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AppConfig.Builder("test.ttl")
                    .logLevel("INVALID")
                    .build();
        });
    }

    @Test
    void testBuilderValidationEmptyPreferredLanguages() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AppConfig.Builder("test.ttl")
                    .preferredLanguages("en,,cs")
                    .build();
        });
    }

    @Test
    void testBuilderValidationInvalidColumnNamingConvention() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AppConfig.Builder("test.ttl")
                    .columnNamingConvention("invalidFormat")
                    .build();
        });
    }

    @Test
    void testBuilderValidationEmptyColumnNamingConvention() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AppConfig.Builder("test.ttl")
                    .columnNamingConvention("")
                    .build();
        });
    }

    @Test
    void testBuilderValidationAllValidColumnNamingConventions() {
        // Test all valid naming conventions from LabelFormatter
        String[] validConventions = {
            AppConfig.COLUMN_NAMING_CAMEL_CASE,          // "camelCase"
            AppConfig.COLUMN_NAMING_PASCAL_CASE,         // "PascalCase"
            AppConfig.COLUMN_NAMING_SNAKE_CASE,          // "snake_case"
            AppConfig.COLUMN_NAMING_SCREAMING_SNAKE_CASE,// "SCREAMING_SNAKE_CASE"
            AppConfig.COLUMN_NAMING_KEBAB_CASE,          // "kebab-case"
            AppConfig.COLUMN_NAMING_TITLE_CASE,          // "Title Case"
            AppConfig.COLUMN_NAMING_DOT_NOTATION         // "dot.notation"
        };

        for (String convention : validConventions) {
            AppConfig config = new AppConfig.Builder("test.ttl")
                    .columnNamingConvention(convention)
                    .build();
            assertEquals(convention, config.getColumnNamingConvention(),
                "Failed for convention: " + convention);
        }
    }

    @Test
    void testBuilderValidationCaseSensitiveColumnNaming() {
        // Column naming convention is case-sensitive
        // "titlecase" is NOT valid, must be "Title Case"
        assertThrows(IllegalArgumentException.class, () -> {
            new AppConfig.Builder("test.ttl")
                    .columnNamingConvention("titlecase")
                    .build();
        });

        // "CamelCase" is NOT valid, must be "camelCase"
        assertThrows(IllegalArgumentException.class, () -> {
            new AppConfig.Builder("test.ttl")
                    .columnNamingConvention("CamelCase")
                    .build();
        });
    }

    @Test
    void testConversionMethodDerivedFromMultipleTables() {
        AppConfig singleTable = new AppConfig.Builder("test.ttl")
                .multipleTables(false)
                .build();
        assertEquals("basicQuery", singleTable.getConversionMethod());

        AppConfig multipleTables = new AppConfig.Builder("test.ttl")
                .multipleTables(true)
                .build();
        assertEquals("splitQuery", multipleTables.getConversionMethod());
    }

    @Test
    void testRuntimeParametersInitialization() {
        AppConfig config = new AppConfig.Builder("test.ttl").build();
        
        assertNotNull(config.getConversionMethod());
        assertEquals("", config.getIntermediateFileNames());
        assertFalse(config.getConversionHasBlankNodes());
        assertTrue(config.getConversionHasRdfTypes());
        assertNotNull(config.getOutputZipFileName());
        assertTrue(config.getOutputZipFileName().endsWith("_CSVW.zip"));
    }

    @Test
    void testRuntimeParametersCanBeModified() {
        AppConfig config = new AppConfig.Builder("test.ttl").build();
        
        config.setIntermediateFileNames("file1.csv,file2.csv");
        assertEquals("file1.csv,file2.csv", config.getIntermediateFileNames());
        
        config.setConversionHasBlankNodes(true);
        assertTrue(config.getConversionHasBlankNodes());
        
        config.setMetadataRowNums(true);
        assertTrue(config.getMetadataRowNums());
    }

    @Test
    void testStreamingModeConfiguration() {
        AppConfig config = new AppConfig.Builder("test.nt")
                .parsing("streaming")
                .streaming(true)
                .build();
        
        assertTrue(config.getStreaming());
        assertEquals("streaming", config.getParsing());
        assertTrue(config.getStreamingContinuous());
    }

    @Test
    void testOutputFilePathCalculation() {
        AppConfig configWithOutput = new AppConfig.Builder("input.ttl")
                .output("custom_output.csv")
                .build();
        assertEquals("custom_output.csv", configWithOutput.getOutput());
        
        AppConfig configWithoutOutput = new AppConfig.Builder("input.ttl").build();
        assertNotNull(configWithoutOutput.getOutputFilePath());
    }

    @Test
    void testHelperMethods() {
        // Test using helper for basic config
        AppConfig basic = AppConfigTestHelper.createBasicConfig("test.ttl");
        assertNotNull(basic);
        assertEquals("test.ttl", basic.getFile());
        
        // Test using helper for multiple tables
        AppConfig multiTable = AppConfigTestHelper.createMultipleTablesConfig("test.ttl");
        assertTrue(multiTable.getMultipleTables());
        
        // Test using helper for streaming
        AppConfig streaming = AppConfigTestHelper.createStreamingConfig("test.nt");
        assertTrue(streaming.getStreaming());
        
        // Test using helper for first normal form
        AppConfig fnf = AppConfigTestHelper.createFirstNormalFormConfig("test.ttl");
        assertTrue(fnf.getFirstNormalForm());
    }

    @Test
    void testLegacyConstructorBackwardCompatibility() {
        // Test deprecated constructor still works
        @SuppressWarnings("deprecation")
        AppConfig config = new AppConfig("DEBUG");
        assertEquals("DEBUG", config.getLogLevel());
        
        @SuppressWarnings("deprecation")
        AppConfig defaultConfig = new AppConfig();
        assertEquals("INFO", defaultConfig.getLogLevel());
    }
}
