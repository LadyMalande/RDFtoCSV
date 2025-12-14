# Testing with Newer csv2rdf Versions

## Current Status

The test currently uses **Swirrl csv2rdf v0.4.7**, which has known bugs:
- ❌ Ignores `separator` property (doesn't split multi-value cells)
- ❌ Converts custom datatypes to string (e.g., `xsd:duration` → `xsd:string`)
- ❌ Creates invalid `file://` URIs for empty values with `valueUrl`
- ⚠️  Minor decimal precision differences

**Your RDF→CSV conversion is correct.** These are bugs in the csv2rdf tool.

## How to Test with Newer Versions

### Option 1: Try Swirrl csv2rdf v0.4.8+

1. **Check for newer releases**:
   - Visit: https://github.com/Swirrl/csv2rdf/releases
   - Look for versions newer than v0.4.7

2. **Download a newer version**:
   ```powershell
   # Example for v0.4.8 (check if it exists)
   cd src/test/resources/tools
   curl -L -o csv2rdf-0.4.8-standalone.jar https://github.com/Swirrl/csv2rdf/releases/download/v0.4.8/csv2rdf-0.4.8-standalone.jar
   ```

3. **Update the test**:
   - Open [ReverseConversionTest.java](src/test/java/com/miklosova/rdftocsvw/metadata_creator/ReverseConversionTest.java)
   - Find line ~63: `String csv2rdfPath = "./src/test/resources/tools/csv2rdf-0.4.7-standalone.jar";`
   - Change to: `String csv2rdfPath = "./src/test/resources/tools/csv2rdf-0.4.8-standalone.jar";`

4. **Run the test**:
   ```powershell
   mvn test -Dtest=ReverseConversionTest
   ```

5. **Compare results**:
   - Check if separator now works (allergens split into multiple triples)
   - Check if duration datatype is preserved
   - Check if empty values are handled correctly

### Option 2: Try W3C Reference Implementation

The W3C also provides a reference implementation in JavaScript:

1. **Repository**: https://github.com/w3c/csvw

2. **Installation** (requires Node.js):
   ```powershell
   npm install -g csvw
   ```

3. **Usage**:
   ```powershell
   csvw restaurantTest.csv-metadata.json -o restaurantTest-reconverted.ttl
   ```

4. **Modify test to use it**:
   - Update `convertCsvToRdfUsingCsv2rdf()` method to call `csvw` command instead
   - Or create a new method `convertCsvToRdfUsingW3CTool()`

### Option 3: Use Custom Converter

The repository now includes `CsvwToRdfConverter.java` which correctly implements:
- ✅ Separator property
- ✅ Custom datatypes
- ✅ Empty value handling

**To enable**:

1. In [ReverseConversionTest.java](src/test/java/com/miklosova/rdftocsvw/metadata_creator/ReverseConversionTest.java), find line ~103
2. Comment out the csv2rdf section:
   ```java
   /* 
   System.out.println("Converting CSV+Metadata back to RDF using csv2rdf...");
   convertCsvToRdfUsingCsv2rdf(...);
   */
   ```
3. Uncomment the custom converter section:
   ```java
   System.out.println("Converting CSV+Metadata back to RDF using custom CSVW-compliant converter...");
   convertCsvToRdfUsingCustomConverter(...);
   ```
4. Uncomment the method at line ~149:
   ```java
   private void convertCsvToRdfUsingCustomConverter(String metadataPath, String outputPath) 
           throws Exception {
       // ... implementation
   }
   ```

## Checking for Bug Fixes

When testing a newer csv2rdf version, check for these specific fixes:

### 1. Separator Bug
**Test**: Look at `restaurantTest-reconverted.ttl` for dish2:

```turtle
# CORRECT (bug fixed):
<dish2> <containsAllergen> <http://example.org/allergen/dairy> .
<dish2> <containsAllergen> <http://example.org/allergen/gluten> .

# INCORRECT (bug still present):
<dish2> <containsAllergen> "http://example.org/allergen/dairy,http://example.org/allergen/gluten" .
```

### 2. Datatype Bug
**Test**: Look for prepTime in the output:

```turtle
# CORRECT (bug fixed):
<dish3> <prepTime> "PT25M"^^<http://www.w3.org/2001/XMLSchema#duration> .

# INCORRECT (bug still present):
<dish3> <prepTime> "PT25M"^^<http://www.w3.org/2001/XMLSchema#string> .
```

### 3. Empty Value Bug
**Test**: dish1 has no allergens - should have NO containsAllergen triple:

```turtle
# CORRECT (bug fixed):
<dish1> <category> <http://example.org/category/pizza> .
# (no containsAllergen line)

# INCORRECT (bug still present):
<dish1> <containsAllergen> <file:/C:/Users/.../restaurantTest.nt_merged.csv> .
```

## Results Tracking

| Version | Separator | Datatype | Empty Values | Notes |
|---------|-----------|----------|--------------|-------|
| v0.4.7  | ❌ | ❌ | ❌ | Current version with known bugs |
| v0.4.8+ | ❓ | ❓ | ❓ | Test and update this table |
| W3C ref | ❓ | ❓ | ❓ | Test and update this table |
| Custom  | ✅ | ✅ | ✅ | CsvwToRdfConverter.java |

## Reporting Issues

If you test a newer version and bugs persist:

1. **Document your findings** in this file
2. **Report to Swirrl**: https://github.com/Swirrl/csv2rdf/issues
3. Include:
   - Version tested
   - Your metadata file (restaurantTest.csv-metadata.json)
   - Your CSV file (restaurantTest.nt_merged.csv)
   - Expected vs actual output
   - Reference to CSVW spec sections

## Questions?

- For CSVW specification: https://www.w3.org/TR/csv2rdf/
- For test issues: Check ROUND_TRIP_TEST_FINDINGS.md
- For tool bugs: Check CSV2RDF_TOOL_ISSUES.md
