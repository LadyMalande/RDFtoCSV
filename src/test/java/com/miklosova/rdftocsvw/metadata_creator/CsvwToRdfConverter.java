package com.miklosova.rdftocsvw.metadata_creator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A CSVW-compliant CSV-to-RDF converter that properly handles:
 * - separator property (multiple values per cell)
 * - datatype specifications
 * - empty values (no triples generated)
 * - valueUrl templates
 * - language tags
 * - aboutUrl templates
 * 
 * This is a reference implementation for testing purposes.
 */
public class CsvwToRdfConverter {
    
    private final ValueFactory vf = SimpleValueFactory.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Convert CSV + metadata to RDF
     * 
     * @param metadataPath Path to the CSVW metadata JSON file
     * @param outputPath Path where the RDF output will be written
     * @throws Exception if conversion fails
     */
    public void convert(String metadataPath, String outputPath) throws Exception {
        // Parse metadata
        JsonNode metadata = mapper.readTree(new File(metadataPath));
        
        // Get table information
        JsonNode tables = metadata.get("tables");
        if (tables == null || !tables.isArray() || tables.size() == 0) {
            throw new IllegalArgumentException("No tables found in metadata");
        }
        
        List<Statement> statements = new ArrayList<>();
        
        // Process each table
        for (JsonNode table : tables) {
            String csvUrl = table.get("url").asText();
            File csvFile = new File(new File(metadataPath).getParent(), csvUrl);
            
            JsonNode tableSchema = table.get("tableSchema");
            JsonNode columns = tableSchema.get("columns");
            
            // Read CSV
            List<Map<String, String>> rows = readCsv(csvFile);
            
            // Convert each row
            for (Map<String, String> row : rows) {
                statements.addAll(convertRow(row, columns));
            }
        }
        
        // Write RDF output
        try (FileOutputStream out = new FileOutputStream(outputPath)) {
            Rio.write(statements, out, RDFFormat.TURTLE);
        }
        
        System.out.println("Generated " + statements.size() + " triples");
    }
    
    /**
     * Read CSV file into list of maps
     */
    private List<Map<String, String>> readCsv(File csvFile) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {
            
            // Read header
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return rows;
            }
            
            List<String> headers = parseCsvLine(headerLine);
            
            // Read rows
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                List<String> values = parseCsvLine(line);
                Map<String, String> row = new HashMap<>();
                
                for (int i = 0; i < headers.size() && i < values.size(); i++) {
                    row.put(headers.get(i), values.get(i));
                }
                
                rows.add(row);
            }
        }
        
        return rows;
    }
    
    /**
     * Parse CSV line handling quoted values
     */
    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        values.add(current.toString());
        return values;
    }
    
    /**
     * Convert a single CSV row to RDF statements
     */
    private List<Statement> convertRow(Map<String, String> row, JsonNode columns) {
        List<Statement> statements = new ArrayList<>();
        
        // Determine subject URI
        IRI subject = null;
        for (JsonNode column : columns) {
            String columnName = getColumnName(column);
            String cellValue = row.get(columnName);
            
            if (cellValue == null || cellValue.trim().isEmpty()) {
                continue;
            }
            
            // Check if this is the subject column
            Boolean suppressOutput = column.has("suppressOutput") ? 
                column.get("suppressOutput").asBoolean() : false;
            
            if (suppressOutput && column.has("valueUrl")) {
                String valueUrl = column.get("valueUrl").asText();
                String expandedUrl = expandTemplate(valueUrl, row);
                subject = vf.createIRI(expandedUrl);
                break;
            }
        }
        
        if (subject == null) {
            return statements; // Skip rows without subject
        }
        
        // Process each column
        for (JsonNode column : columns) {
            Boolean suppressOutput = column.has("suppressOutput") ? 
                column.get("suppressOutput").asBoolean() : false;
            
            if (suppressOutput) {
                continue; // Don't generate triples for this column
            }
            
            statements.addAll(convertColumn(subject, row, column));
        }
        
        return statements;
    }
    
    /**
     * Convert a single column value to RDF statements
     */
    private List<Statement> convertColumn(IRI subject, Map<String, String> row, JsonNode column) {
        List<Statement> statements = new ArrayList<>();
        
        String columnName = getColumnName(column);
        String cellValue = row.get(columnName);
        
        if (cellValue == null || cellValue.trim().isEmpty()) {
            return statements; // Don't generate triples for empty values
        }
        
        // Get property URI
        String propertyUrl = column.has("propertyUrl") ? 
            column.get("propertyUrl").asText() : null;
        
        if (propertyUrl == null) {
            return statements; // No property defined
        }
        
        IRI predicate = vf.createIRI(propertyUrl);
        
        // Determine the subject for this triple (might use aboutUrl)
        IRI tripleSubject = subject;
        if (column.has("aboutUrl")) {
            String aboutUrl = column.get("aboutUrl").asText();
            String expandedUrl = expandTemplate(aboutUrl, row);
            tripleSubject = vf.createIRI(expandedUrl);
        }
        
        // Check for separator (multiple values)
        String separator = column.has("separator") ? column.get("separator").asText() : null;
        
        List<String> values = new ArrayList<>();
        if (separator != null && !separator.isEmpty()) {
            // Split by separator
            String[] parts = cellValue.split(Pattern.quote(separator));
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    values.add(trimmed);
                }
            }
        } else {
            values.add(cellValue);
        }
        
        // Create triples for each value
        for (String value : values) {
            Value object = createValue(value, row, column);
            if (object != null) {
                statements.add(vf.createStatement(tripleSubject, predicate, object));
            }
        }
        
        return statements;
    }
    
    /**
     * Create RDF value from cell value according to column specification
     */
    private Value createValue(String value, Map<String, String> row, JsonNode column) {
        // Check for valueUrl (IRI reference)
        if (column.has("valueUrl")) {
            String valueUrl = column.get("valueUrl").asText();
            
            // Create a row with the value for template expansion
            Map<String, String> valueRow = new HashMap<>(row);
            String columnName = getColumnName(column);
            valueRow.put(columnName, value);
            
            String expandedUrl = expandTemplate(valueUrl, valueRow);
            return vf.createIRI(expandedUrl);
        }
        
        // Otherwise, create a literal
        String lang = column.has("lang") ? column.get("lang").asText() : null;
        String datatype = column.has("datatype") ? column.get("datatype").asText() : null;
        
        if (lang != null && !lang.isEmpty()) {
            return vf.createLiteral(value, lang);
        } else if (datatype != null && !datatype.isEmpty()) {
            // Expand short datatype names to full XSD URIs
            String expandedDatatype = expandDatatype(datatype);
            IRI datatypeIri = vf.createIRI(expandedDatatype);
            return vf.createLiteral(value, datatypeIri);
        } else {
            return vf.createLiteral(value);
        }
    }
    
    /**
     * Expand short datatype names to full URIs
     */
    private String expandDatatype(String datatype) {
        // If already a full URI, return as is
        if (datatype.startsWith("http://") || datatype.startsWith("https://")) {
            return datatype;
        }
        
        // Common XSD datatypes
        Map<String, String> xsdTypes = Map.ofEntries(
            Map.entry("string", XSD.STRING.stringValue()),
            Map.entry("boolean", XSD.BOOLEAN.stringValue()),
            Map.entry("decimal", XSD.DECIMAL.stringValue()),
            Map.entry("integer", XSD.INTEGER.stringValue()),
            Map.entry("double", XSD.DOUBLE.stringValue()),
            Map.entry("float", XSD.FLOAT.stringValue()),
            Map.entry("date", XSD.DATE.stringValue()),
            Map.entry("time", XSD.TIME.stringValue()),
            Map.entry("dateTime", XSD.DATETIME.stringValue()),
            Map.entry("duration", XSD.DURATION.stringValue()),
            Map.entry("anyURI", XSD.ANYURI.stringValue()),
            Map.entry("QName", XSD.QNAME.stringValue())
        );
        
        return xsdTypes.getOrDefault(datatype, XSD.NAMESPACE + datatype);
    }
    
    /**
     * Get the column name from column definition
     */
    private String getColumnName(JsonNode column) {
        if (column.has("name")) {
            return column.get("name").asText();
        }
        if (column.has("titles")) {
            JsonNode titles = column.get("titles");
            if (titles.isTextual()) {
                return titles.asText();
            }
        }
        return "";
    }
    
    /**
     * Expand URL template with values from row
     * Supports {columnName} and {+columnName} syntax
     */
    private String expandTemplate(String template, Map<String, String> row) {
        // Pattern to match {name} or {+name}
        Pattern pattern = Pattern.compile("\\{\\+?([^}]+)\\}");
        Matcher matcher = pattern.matcher(template);
        
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        
        while (matcher.find()) {
            result.append(template, lastEnd, matcher.start());
            
            String columnName = matcher.group(1);
            String value = row.get(columnName);
            
            if (value != null && !value.isEmpty()) {
                result.append(value);
            }
            
            lastEnd = matcher.end();
        }
        
        result.append(template.substring(lastEnd));
        return result.toString();
    }
    
    /**
     * Main method for standalone testing
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java CsvwToRdfConverter <metadata.json> <output.ttl>");
            System.exit(1);
        }
        
        CsvwToRdfConverter converter = new CsvwToRdfConverter();
        converter.convert(args[0], args[1]);
        System.out.println("Conversion complete: " + args[1]);
    }
}
