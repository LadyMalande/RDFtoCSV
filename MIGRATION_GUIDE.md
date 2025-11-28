# Migration Guide: ConfigurationManager to AppConfig

This guide helps you migrate from the file-based `ConfigurationManager` approach to the new `AppConfig` parameter class approach.

## Table of Contents
- [Overview](#overview)
- [Why Migrate?](#why-migrate)
- [Quick Start](#quick-start)
- [Detailed Migration Steps](#detailed-migration-steps)
- [API Reference](#api-reference)
- [Testing Guide](#testing-guide)
- [Backward Compatibility](#backward-compatibility)

## Overview

The RDFtoCSV library has been refactored to use a **parameter class pattern** (`AppConfig`) instead of file-based configuration (`ConfigurationManager`). This provides:

- ✅ **Type-safe configuration** with compile-time checks
- ✅ **Builder pattern** for clean, fluent API
- ✅ **Immutable user parameters** preventing accidental modifications
- ✅ **Better testability** without file system dependencies
- ✅ **Thread safety** with instance-based configuration
- ✅ **Full backward compatibility** with existing code

## Why Migrate?

### Old Approach (File-Based)
```java
// Problems:
// - File I/O on every config access
// - No type safety
// - Hard to test
// - Global state (static methods)
// - Configuration persisted to disk unnecessarily

ConfigurationManager.createConfigFile();
ConfigurationManager.saveVariableToConfigFile("tables", "more");
ConfigurationManager.saveVariableToConfigFile("readMethod", "rdf4j");
String method = ConfigurationManager.getVariableFromConfigFile("CONVERSION_METHOD");
```

### New Approach (Parameter Class)
```java
// Benefits:
// - In-memory configuration
// - Type-safe
// - Easy to test
// - Instance-based (thread-safe)
// - Fluent Builder API

AppConfig config = new AppConfig.Builder("input.ttl")
    .parsing("rdf4j")
    .multipleTables(true)
    .firstNormalForm(true)
    .build();

RDFtoCSV converter = new RDFtoCSV(config);
```

## Quick Start

### Basic Usage

**Before (Deprecated):**
```java
RDFtoCSV converter = new RDFtoCSV("input.ttl");
converter.convertToZip();
```

**After (Recommended):**
```java
AppConfig config = new AppConfig.Builder("input.ttl").build();
RDFtoCSV converter = new RDFtoCSV(config);
converter.convertToZip();
```

### With Configuration Options

**Before (Deprecated):**
```java
Map<String, String> configMap = new HashMap<>();
configMap.put("table", "more");
configMap.put("readMethod", "rdf4j");
configMap.put("firstNormalForm", "true");
RDFtoCSV converter = new RDFtoCSV("input.ttl", configMap);
```

**After (Recommended):**
```java
AppConfig config = new AppConfig.Builder("input.ttl")
    .parsing("rdf4j")
    .multipleTables(true)
    .firstNormalForm(true)
    .build();
RDFtoCSV converter = new RDFtoCSV(config);
```

## Detailed Migration Steps

### Step 1: Update Constructor Calls

#### Application Code
```java
// Old
RDFtoCSV converter = new RDFtoCSV(fileName);

// New
AppConfig config = new AppConfig.Builder(fileName).build();
RDFtoCSV converter = new RDFtoCSV(config);
```

#### With Config Map
```java
// Old
Map<String, String> configMap = new HashMap<>();
configMap.put("table", "more");
RDFtoCSV converter = new RDFtoCSV(fileName, configMap);

// New
AppConfig config = new AppConfig.Builder(fileName)
    .multipleTables(true)
    .build();
RDFtoCSV converter = new RDFtoCSV(config);
```

### Step 2: Update Parameter Mapping

| ConfigMap Key | AppConfig Builder Method |
|--------------|--------------------------|
| `table: "more"` | `.multipleTables(true)` |
| `table: "one"` | `.multipleTables(false)` |
| `readMethod` | `.parsing(value)` |
| `firstNormalForm` | `.firstNormalForm(Boolean)` |
| `output` | `.output(value)` |

### Step 3: Update Service Class Usage

If you're extending or using internal classes:

```java
// Old
ConversionService cs = new ConversionService();
MetadataService ms = new MetadataService();
ZipOutputProcessor zop = new ZipOutputProcessor();

// New
ConversionService cs = new ConversionService(config);
MetadataService ms = new MetadataService(config);
ZipOutputProcessor zop = new ZipOutputProcessor(config);
```

### Step 4: Update ConfigurationManager Calls

```java
// Old
String method = ConfigurationManager.getVariableFromConfigFile(
    ConfigurationManager.CONVERSION_METHOD);

// New
String method = config.getConversionMethod();
```

## API Reference

### AppConfig.Builder Methods

#### Required Parameters
```java
new AppConfig.Builder(String file)  // Input RDF file path or URL
```

#### Optional Parameters
```java
.parsing(String method)              // "rdf4j", "streaming", "bigfilestreaming"
.multipleTables(Boolean enable)      // true = multiple tables, false = single table
.streaming(Boolean enable)           // Enable streaming mode
.firstNormalForm(Boolean enable)     // Enforce first normal form
.output(String path)                 // Custom output file path
.preferredLanguages(String langs)    // Comma-separated: "en,cs,pl"
.columnNamingConvention(String conv) // Column naming style (see below)
.logLevel(String level)              // "INFO", "DEBUG", "WARNING", etc.
```

##### Valid Column Naming Conventions
The column naming convention parameter accepts the following **case-sensitive** values:
- `"camelCase"` - camelCase format (e.g., `firstName`)
- `"PascalCase"` - PascalCase format (e.g., `FirstName`)
- `"snake_case"` - snake_case format (e.g., `first_name`)
- `"SCREAMING_SNAKE_CASE"` - SCREAMING_SNAKE_CASE format (e.g., `FIRST_NAME`)
- `"kebab-case"` - kebab-case format (e.g., `first-name`)
- `"Title Case"` - Title Case format (e.g., `First Name`) - **default**
- `"dot.notation"` - dot.notation format (e.g., `first.name`)

You can also use the constants from `AppConfig`:
```java
AppConfig config = new AppConfig.Builder("input.ttl")
    .columnNamingConvention(AppConfig.COLUMN_NAMING_CAMEL_CASE)
    .build();
```

#### Build
```java
.build()  // Validates and creates AppConfig instance
```

### AppConfig Getters

#### User Parameters (Immutable)
```java
config.getFile()
config.getParsing()
config.getMultipleTables()
config.getStreaming()
config.getFirstNormalForm()
config.getOutput()
config.getPreferredLanguages()
config.getColumnNamingConvention()
config.getLogLevel()
```

#### Runtime Parameters (Mutable)
```java
config.getConversionMethod()
config.getIntermediateFileNames()
config.getConversionHasBlankNodes()
config.getConversionHasRdfTypes()
config.getOutputZipFileName()
config.getReadMethod()
config.getMetadataRowNums()
config.getOutputFilePath()
config.getStreamingContinuous()
config.getSimpleBasicQuery()

// Setters
config.setIntermediateFileNames(String)
config.setConversionHasBlankNodes(Boolean)
// ... etc
```

## Testing Guide

### Using Test Helpers

The library provides `AppConfigTestHelper` for common test scenarios:

```java
// Basic config
AppConfig config = AppConfigTestHelper.createBasicConfig("test.ttl");

// Multiple tables
AppConfig config = AppConfigTestHelper.createMultipleTablesConfig("test.ttl");

// Streaming
AppConfig config = AppConfigTestHelper.createStreamingConfig("test.nt");

// Big file streaming
AppConfig config = AppConfigTestHelper.createBigFileStreamingConfig("test.nt");

// First normal form
AppConfig config = AppConfigTestHelper.createFirstNormalFormConfig("test.ttl");

// Custom configuration
AppConfig config = AppConfigTestHelper.createCustomConfig(
    "test.ttl", "rdf4j", true, true, false);

// Full configuration
AppConfig config = AppConfigTestHelper.createFullConfig("test.ttl");

// From legacy map (for migration)
Map<String, String> legacyMap = ...;
AppConfig config = AppConfigTestHelper.createFromLegacyMap("test.ttl", legacyMap);
```

### Test Migration Example

**Before:**
```java
@Test
void testConversion() {
    ConfigurationManager.createConfigFile();
    ConfigurationManager.saveVariableToConfigFile("tables", "one");
    ConfigurationManager.saveVariableToConfigFile("readMethod", "rdf4j");
    
    RDFtoCSV converter = new RDFtoCSV("test.ttl");
    // ... test
}
```

**After:**
```java
@Test
void testConversion() {
    AppConfig config = new AppConfig.Builder("test.ttl")
        .parsing("rdf4j")
        .multipleTables(false)
        .build();
    
    RDFtoCSV converter = new RDFtoCSV(config);
    // ... test
}
```

**Or using helper:**
```java
@Test
void testConversion() {
    AppConfig config = AppConfigTestHelper.createBasicConfig("test.ttl");
    RDFtoCSV converter = new RDFtoCSV(config);
    // ... test
}
```

## Validation

The Builder validates parameters before creating AppConfig:

### File Validation
```java
// Throws IllegalArgumentException for null or empty file
new AppConfig.Builder(null).build();  // ❌ throws exception
new AppConfig.Builder("").build();     // ❌ throws exception
new AppConfig.Builder("input.ttl").build();  // ✅ valid
```

### Parsing Method Validation
```java
// Valid parsing methods: "rdf4j", "streaming", "bigfilestreaming"
new AppConfig.Builder("test.ttl")
    .parsing("invalid").build();  // ❌ throws exception

new AppConfig.Builder("test.ttl")
    .parsing("rdf4j").build();     // ✅ valid
```

### Log Level Validation
```java
// Valid log levels: SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL, OFF
new AppConfig.Builder("test.ttl")
    .logLevel("INVALID").build();  // ❌ throws exception

new AppConfig.Builder("test.ttl")
    .logLevel("INFO").build();     // ✅ valid
```

### Column Naming Convention Validation
```java
// Valid values (case-sensitive):
// "camelCase", "PascalCase", "snake_case", "SCREAMING_SNAKE_CASE", 
// "kebab-case", "Title Case", "dot.notation"

new AppConfig.Builder("test.ttl")
    .columnNamingConvention("invalidFormat").build();  // ❌ throws exception

new AppConfig.Builder("test.ttl")
    .columnNamingConvention("titlecase").build();      // ❌ throws exception (wrong case)

new AppConfig.Builder("test.ttl")
    .columnNamingConvention("Title Case").build();     // ✅ valid

// Using constants (recommended)
new AppConfig.Builder("test.ttl")
    .columnNamingConvention(AppConfig.COLUMN_NAMING_CAMEL_CASE).build();  // ✅ valid
```

### Preferred Languages Validation
```java
// Should be comma-separated language codes with no empty values
new AppConfig.Builder("test.ttl")
    .preferredLanguages("en,,cs").build();  // ❌ throws exception (empty value)

new AppConfig.Builder("test.ttl")
    .preferredLanguages("en,cs,pl").build(); // ✅ valid
```

### Complete Validation Example
```java
try {
    AppConfig config = new AppConfig.Builder("")
        .parsing("invalid")
        .columnNamingConvention("wrongFormat")
        .logLevel("INVALID_LEVEL")
        .build();
} catch (IllegalArgumentException e) {
    // Validation failed with detailed error message
    System.err.println(e.getMessage());
}
```

## Backward Compatibility

### Legacy Code Continues to Work

All old constructors and methods are marked `@Deprecated` but still function:

```java
// Still works (deprecated)
@SuppressWarnings("deprecation")
RDFtoCSV converter = new RDFtoCSV("input.ttl");

// Still works (deprecated)
@SuppressWarnings("deprecation")
Map<String, String> map = new HashMap<>();
RDFtoCSV converter = new RDFtoCSV("input.ttl", map);

// Still works (deprecated)
@SuppressWarnings("deprecation")
AppConfig config = new AppConfig();
```

### ConfigurationManager Still Available

For legacy code that hasn't migrated:
- ConfigurationManager methods still work
- File-based configuration still supported
- All service classes check for AppConfig first, fall back to ConfigurationManager

### Migration Timeline

1. **Current**: Both systems work side-by-side
2. **Phase 1**: Migrate new code to AppConfig
3. **Phase 2**: Gradually migrate existing code
4. **Phase 3**: Eventually deprecate ConfigurationManager (future version)

## Complete Example

### Command Line Application

```java
public static void main(String[] args) {
    // Parse command line args
    CommandLine cmd = parseArgs(args);
    
    // Build configuration
    AppConfig config = new AppConfig.Builder(cmd.getOptionValue("file"))
        .parsing(cmd.getOptionValue("parsing", "rdf4j"))
        .multipleTables(cmd.hasOption("multipleTables"))
        .streaming(cmd.hasOption("streaming"))
        .firstNormalForm(cmd.hasOption("firstNormalForm"))
        .output(cmd.getOptionValue("output"))
        .logLevel(cmd.getOptionValue("logLevel", "INFO"))
        .build();
    
    // Run conversion
    RDFtoCSV converter = new RDFtoCSV(config);
    converter.convertToZip();
}
```

### Library Usage

```java
public void convertRdfToCsv(String inputFile) {
    AppConfig config = new AppConfig.Builder(inputFile)
        .parsing("rdf4j")
        .firstNormalForm(true)
        .preferredLanguages("en,cs")
        .build();
    
    RDFtoCSV converter = new RDFtoCSV(config);
    
    try {
        FinalizedOutput<byte[]> output = converter.convertToZip();
        // Process output...
    } catch (IOException e) {
        // Handle error...
    }
}
```

### Web Service Integration

```java
@PostMapping("/convert")
public ResponseEntity<byte[]> convert(@RequestBody ConversionRequest request) {
    AppConfig config = new AppConfig.Builder(request.getFile())
        .parsing(request.getParsingMethod())
        .multipleTables(request.isMultipleTables())
        .firstNormalForm(request.isFirstNormalForm())
        .output(request.getOutputPath())
        .preferredLanguages(request.getLanguages())
        .build();
    
    RDFtoCSV converter = new RDFtoCSV(config);
    FinalizedOutput<byte[]> output = converter.convertToZip();
    
    return ResponseEntity.ok()
        .header("Content-Type", "application/zip")
        .body(output.getOutput());
}
```

## Common Migration Patterns

### Pattern 1: Simple Conversion
```java
// Before
RDFtoCSV converter = new RDFtoCSV("input.ttl");
converter.convertToZip();

// After
AppConfig config = new AppConfig.Builder("input.ttl").build();
RDFtoCSV converter = new RDFtoCSV(config);
converter.convertToZip();
```

### Pattern 2: With Options
```java
// Before
Map<String, String> opts = Map.of("table", "more", "readMethod", "rdf4j");
RDFtoCSV converter = new RDFtoCSV("input.ttl", opts);

// After
AppConfig config = new AppConfig.Builder("input.ttl")
    .multipleTables(true)
    .parsing("rdf4j")
    .build();
RDFtoCSV converter = new RDFtoCSV(config);
```

### Pattern 3: Testing
```java
// Before
@Test
void test() {
    ConfigurationManager.createConfigFile();
    RDFtoCSV conv = new RDFtoCSV("test.ttl");
}

// After
@Test
void test() {
    AppConfig config = AppConfigTestHelper.createBasicConfig("test.ttl");
    RDFtoCSV conv = new RDFtoCSV(config);
}
```

## Getting Help

- See `AppConfigTest.java` for comprehensive examples
- See `RDFtoCSVMigrationExampleTest.java` for side-by-side comparisons
- Use `AppConfigTestHelper` for common test scenarios
- Legacy code continues to work during migration

## Summary of Changes

| Component | Status | Notes |
|-----------|--------|-------|
| AppConfig | ✅ New | Builder pattern, validation, immutable user params |
| RDFtoCSV constructors | ✅ Updated | New `RDFtoCSV(AppConfig)` constructor |
| ConversionService | ✅ Updated | Accepts AppConfig, falls back to ConfigurationManager |
| MetadataService | ✅ Updated | Accepts AppConfig, falls back to ConfigurationManager |
| MethodService | ✅ Updated | Accepts AppConfig, falls back to ConfigurationManager |
| ZipOutputProcessor | ✅ Updated | Accepts AppConfig, falls back to ConfigurationManager |
| JsonUtil | ✅ Updated | Overloaded methods for AppConfig |
| Main.java | ✅ Updated | Uses AppConfig from command line args |
| ConfigurationManager | 📌 Deprecated | Still works for backward compatibility |
| Test helpers | ✅ New | AppConfigTestHelper with factory methods |
| Example tests | ✅ New | Migration examples and patterns |

Happy migrating! 🚀
