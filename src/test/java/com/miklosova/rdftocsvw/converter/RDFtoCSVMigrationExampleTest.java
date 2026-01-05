package com.miklosova.rdftocsvw.converter;

import com.miklosova.rdftocsvw.support.AppConfig;
import com.miklosova.rdftocsvw.support.AppConfigTestHelper;
// ...existing code...
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Example test class showing migration from ConfigurationManager to AppConfig.
 * This demonstrates both approaches working side-by-side.
 */
class RDFtoCSVMigrationExampleTest {

    @TempDir
    Path tempDir;

    // ============================================================================
    // LEGACY APPROACH (Still works, but deprecated)
    // ============================================================================

    @Test
    @SuppressWarnings("deprecation")
    void testLegacyApproach_WithFileName() throws IOException {
        // Old way - direct constructor with filename
        String fileName = "src/test/resources/test-001.rdf";
        
        RDFtoCSV converter = new RDFtoCSV(fileName);
        
        assertNotNull(converter);
        assertNotNull(converter.getFilePathForOutput());
    }

    // Removed legacy test that depended on ConfigurationManager

    // ============================================================================
    // NEW APPROACH (Recommended)
    // ============================================================================

    @Test
    void testNewApproach_WithBasicConfig() throws IOException {
        // New way - using AppConfig Builder
        String fileName = "src/test/resources/test-001.rdf";
        
        AppConfig config = new AppConfig.Builder(fileName)
                .parsing("rdf4j")
                .build();
        
        RDFtoCSV converter = new RDFtoCSV(config);
        
        assertNotNull(converter);
        assertNotNull(converter.getConfig());
        assertEquals(fileName, converter.getConfig().getFile());
        assertEquals("rdf4j", converter.getConfig().getParsing());
    }

    @Test
    void testNewApproach_WithFullConfig() throws IOException {
        // New way - with all parameters
        String fileName = "src/test/resources/test-001.rdf";
        
        AppConfig config = new AppConfig.Builder(fileName)
                .parsing("rdf4j")
                .multipleTables(false)
                .firstNormalForm(true)
                .streaming(false)
                .output("test_output.csv")
                .preferredLanguages("en,cs")
                .columnNamingConvention("Title Case")
                .logLevel("INFO")
                .build();
        
        RDFtoCSV converter = new RDFtoCSV(config);
        
        assertNotNull(converter);
        AppConfig resultConfig = converter.getConfig();
        assertEquals("rdf4j", resultConfig.getParsing());
        assertFalse(resultConfig.getMultipleTables());
        assertTrue(resultConfig.getFirstNormalForm());
        assertEquals("test_output.csv", resultConfig.getOutput());
    }

    @Test
    void testNewApproach_WithHelperMethods() throws IOException {
        // New way - using helper methods for common scenarios
        String fileName = "src/test/resources/test-001.rdf";
        
        // Basic config
        AppConfig basicConfig = AppConfigTestHelper.createBasicConfig(fileName);
        RDFtoCSV converter1 = new RDFtoCSV(basicConfig);
        assertNotNull(converter1);
        
        // Multiple tables config
        AppConfig multiTableConfig = AppConfigTestHelper.createMultipleTablesConfig(fileName);
        RDFtoCSV converter2 = new RDFtoCSV(multiTableConfig);
        assertTrue(converter2.getConfig().getMultipleTables());
        
        // First normal form config
        AppConfig fnfConfig = AppConfigTestHelper.createFirstNormalFormConfig(fileName);
        RDFtoCSV converter3 = new RDFtoCSV(fnfConfig);
        assertTrue(converter3.getConfig().getFirstNormalForm());
    }

    // ============================================================================
    // MIGRATION HELPER - Converting legacy tests
    // ============================================================================

    @Test
    void testMigrationHelper_FromLegacyMap() throws IOException {
        // Converting old test that used config map
        String fileName = "src/test/resources/test-001.rdf";
        Map<String, String> legacyConfigMap = new HashMap<>();
        legacyConfigMap.put("table", "more");
        legacyConfigMap.put("readMethod", "rdf4j");
        legacyConfigMap.put("firstNormalForm", "true");
        
        // Convert to new AppConfig using helper
        AppConfig config = AppConfigTestHelper.createFromLegacyMap(fileName, legacyConfigMap);
        
        RDFtoCSV converter = new RDFtoCSV(config);
        
        assertNotNull(converter);
        assertTrue(converter.getConfig().getMultipleTables()); // "more" -> multipleTables
        assertEquals("rdf4j", converter.getConfig().getParsing());
        assertTrue(converter.getConfig().getFirstNormalForm());
    }

    // ============================================================================
    // SIDE-BY-SIDE COMPARISON
    // ============================================================================

    @Test
    @SuppressWarnings("deprecation")
    void testComparison_LegacyVsNew() throws IOException {
        String fileName = "src/test/resources/test-001.rdf";
        
        // Legacy approach
        Map<String, String> configMap = new HashMap<>();
        configMap.put("table", "one");
        configMap.put("readMethod", "rdf4j");
        RDFtoCSV legacyConverter = new RDFtoCSV(fileName, configMap);
        
        // New approach - equivalent configuration
        AppConfig config = new AppConfig.Builder(fileName)
                .parsing("rdf4j")
                .multipleTables(false)
                .build();
        RDFtoCSV newConverter = new RDFtoCSV(config);
        
        // Both should work and produce similar results
        assertNotNull(legacyConverter);
        assertNotNull(newConverter);
        
        // New approach gives us direct access to config
        assertNotNull(newConverter.getConfig());
        assertEquals("rdf4j", newConverter.getConfig().getParsing());
    }

    // ============================================================================
    // TESTING CONFIG MODIFICATIONS
    // ============================================================================

    @Test
    void testRuntimeConfigModification() throws IOException {
        String fileName = "src/test/resources/test-001.rdf";
        
        AppConfig config = new AppConfig.Builder(fileName).build();
        RDFtoCSV converter = new RDFtoCSV(config);
        
        // Runtime parameters can be modified during conversion
        config.setIntermediateFileNames("file1.csv,file2.csv");
        config.setConversionHasBlankNodes(true);
        
        assertEquals("file1.csv,file2.csv", converter.getConfig().getIntermediateFileNames());
        assertTrue(converter.getConfig().getConversionHasBlankNodes());
    }

    // ============================================================================
    // TESTING VALIDATION
    // ============================================================================

    @Test
    void testConfigValidation_InvalidParsing() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AppConfig.Builder("test.ttl")
                    .parsing("invalid_method")
                    .build();
        });
    }

    @Test
    void testConfigValidation_InvalidLogLevel() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AppConfig.Builder("test.ttl")
                    .logLevel("INVALID_LEVEL")
                    .build();
        });
    }

    @Test
    void testConfigValidation_EmptyFile() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AppConfig.Builder("").build();
        });
    }
}
