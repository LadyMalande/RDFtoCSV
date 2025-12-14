# Reverse Conversion Test Setup

This document explains how to set up and run the reverse conversion tests that validate the RDF → CSV → RDF round-trip.

## What the Test Does

The `ReverseConversionTest` class validates that your RDF data can survive a round-trip conversion:

1. **RDF → CSV+Metadata**: Uses this tool to convert RDF to CSV with CSVW metadata
2. **CSV+Metadata → RDF**: Uses an external tool (csv2rdf) to convert back to RDF
3. **Validation**: Compares the original and reconverted RDF to ensure all triples are preserved

The test handles blank nodes intelligently - they may have different identifiers after reconversion, but their relationships are validated to be correct.

## Setup Instructions

### Download csv2rdf Tool

The test requires the `csv2rdf` tool to convert CSVW back to RDF. You have two options:

#### Option 1: Download Pre-built JAR (Recommended)

```bash
# Create the tools directory
mkdir -p src/test/resources/tools

# Download csv2rdf
cd src/test/resources/tools
wget https://github.com/Swirrl/csv2rdf/releases/download/v0.4.7/csv2rdf-0.4.7-standalone.jar

# Or using curl
curl -L -o csv2rdf-0.4.7-standalone.jar https://github.com/Swirrl/csv2rdf/releases/download/v0.4.7/csv2rdf-0.4.7-standalone.jar
```

#### Option 2: Build from Source

```bash
# Clone the repository
git clone https://github.com/Swirrl/csv2rdf.git
cd csv2rdf

# Build (requires Leiningen)
lein uberjar

# Copy the JAR to your test resources
cp target/csv2rdf-*-standalone.jar ../RDFtoCSV/src/test/resources/tools/
```

### Verify Installation

```bash
# Test that csv2rdf works
java -jar src/test/resources/tools/csv2rdf-0.4.7-standalone.jar --help
```

## Running the Tests

Once csv2rdf is installed, run the tests:

```bash
# Run all reverse conversion tests
mvn test -Dtest=ReverseConversionTest

# Run specific test
mvn test -Dtest=ReverseConversionTest#testRoundTripConversion
```

### Expected Behavior

- **If csv2rdf is found**: Tests will run and validate the round-trip conversion
- **If csv2rdf is not found**: Tests will skip gracefully with a message explaining where to download it

## Test Files

The test uses these example RDF files:
- `restaurantTest.nt` - Small dataset about restaurant menu items (30 triples)
- `companyTest.nt` - Larger dataset about company employees and departments (185 triples)

You can add more test files by modifying the `testFileSets()` method in `ReverseConversionTest.java`.

## What the Test Validates

1. **Triple Count**: All original triples are present in the reconverted RDF
2. **Literal Values**: String values, numbers, booleans, dates are preserved
3. **Datatypes**: XSD datatypes are correctly maintained
4. **Language Tags**: Language-tagged strings (e.g., `"Pizza"@en`) are preserved
5. **Relationships**: Object properties and references between resources are maintained
6. **Blank Nodes**: While identifiers change, blank node structures are preserved

## Troubleshooting

### Test Skips with "csv2rdf jar not found"

The JAR file is not in the expected location. Make sure it's at:
```
src/test/resources/tools/csv2rdf-0.4.7-standalone.jar
```

### "csv2rdf conversion failed with exit code: 1"

Check the console output for error messages from csv2rdf. Common issues:
- Malformed CSV files
- Invalid metadata
- Missing CSV files referenced in metadata

### "Reconverted RDF is missing X triples"

This indicates a problem with the round-trip conversion. Check:
1. The generated CSV files are correct
2. The metadata properly describes all columns
3. csv2rdf is interpreting the metadata correctly

Enable verbose logging to see which triples are missing.

## Alternative CSVW-to-RDF Tools

If csv2rdf doesn't work for you, other options include:

1. **COW** (Converter for CSV on the Web): https://github.com/CLARIAH/COW
2. **rdflib**: Python library with CSVW support
3. **Apache Jena**: Has experimental CSVW support

To use a different tool, modify the `convertCsvToRdfUsingCsv2rdf()` method in `ReverseConversionTest.java`.

## References

- CSVW Specification: https://www.w3.org/TR/tabular-data-primer/
- csv2rdf GitHub: https://github.com/Swirrl/csv2rdf
- W3C CSVW Tests: https://w3c.github.io/csvw/tests/
